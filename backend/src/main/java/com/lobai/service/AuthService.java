package com.lobai.service;

import com.lobai.dto.request.LoginRequest;
import com.lobai.dto.request.RefreshTokenRequest;
import com.lobai.dto.request.RegisterRequest;
import com.lobai.dto.response.AuthResponse;
import com.lobai.dto.response.UserResponse;
import com.lobai.entity.RefreshToken;
import com.lobai.entity.User;
import com.lobai.repository.RefreshTokenRepository;
import com.lobai.repository.UserRepository;
import com.lobai.security.JwtTokenProvider;
import com.lobai.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * AuthService
 *
 * 사용자 인증 관련 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     *
     * @param request 회원가입 요청
     * @return 인증 응답 (JWT 토큰 포함)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        // 2. 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .currentHunger(80)
                .currentEnergy(90)
                .currentHappiness(70)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getEmail());

        // 3. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole().name()
        );
        String refreshTokenString = jwtTokenProvider.createRefreshToken(savedUser.getId());

        // 4. Refresh Token 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .user(savedUser)
                .token(refreshTokenString)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiry() / 1000))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        // 5. 응답 생성
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .expiresIn(jwtTokenProvider.getAccessTokenExpiry())
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .build();
    }

    /**
     * 로그인
     *
     * @param request 로그인 요청
     * @return 인증 응답 (JWT 토큰 포함)
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // 1. Spring Security 인증
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 2. 사용자 조회
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

            // 3. 마지막 로그인 시간 업데이트
            user.updateLastLogin();
            userRepository.save(user);

            // 4. JWT 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
            );
            String refreshTokenString = jwtTokenProvider.createRefreshToken(user.getId());

            // 5. Refresh Token 저장
            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(refreshTokenString)
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiry() / 1000))
                    .isRevoked(false)
                    .build();

            refreshTokenRepository.save(refreshToken);

            log.info("User logged in: {}", user.getEmail());

            // 6. 응답 생성
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenString)
                    .expiresIn(jwtTokenProvider.getAccessTokenExpiry())
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getEmail());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
    }

    /**
     * Access Token 갱신
     *
     * @param request Refresh Token 요청
     * @return 새로운 인증 응답
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        // 1. Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token입니다"));

        // 2. Refresh Token 유효성 검증
        if (!refreshToken.isValid()) {
            throw new IllegalArgumentException("만료되었거나 폐기된 Refresh Token입니다");
        }

        // 3. 사용자 조회
        User user = refreshToken.getUser();

        // 4. 새로운 Access Token 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(
            user.getId(),
            user.getEmail(),
            user.getRole().name()
        );

        log.info("Access token refreshed for user: {}", user.getEmail());

        // 5. 응답 생성 (Refresh Token은 재사용)
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .expiresIn(jwtTokenProvider.getAccessTokenExpiry())
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

    /**
     * 로그아웃
     *
     * @param refreshTokenString Refresh Token
     */
    @Transactional
    public void logout(String refreshTokenString) {
        // Refresh Token 조회 및 폐기
        refreshTokenRepository.findByToken(refreshTokenString)
                .ifPresent(refreshToken -> {
                    refreshToken.revoke();
                    refreshTokenRepository.save(refreshToken);
                    log.info("User logged out: {}", refreshToken.getUser().getEmail());
                });
    }

    /**
     * 사용자의 모든 Refresh Token 폐기
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All refresh tokens revoked for user ID: {}", userId);
    }

    /**
     * 현재 인증된 사용자의 정보 조회
     *
     * @return 사용자 정보
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .currentHunger(user.getCurrentHunger())
                .currentEnergy(user.getCurrentEnergy())
                .currentHappiness(user.getCurrentHappiness())
                .currentPersonaId(user.getCurrentPersona() != null ? user.getCurrentPersona().getId() : null)
                .trustLevel(user.getTrustLevel() != null ? user.getTrustLevel() : 1)
                .experiencePoints(user.getExperiencePoints() != null ? user.getExperiencePoints() : 0)
                .build();
    }
}
