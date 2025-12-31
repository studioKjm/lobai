package com.lobai.controller;

import com.lobai.dto.request.LoginRequest;
import com.lobai.dto.request.RefreshTokenRequest;
import com.lobai.dto.request.RegisterRequest;
import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.AuthResponse;
import com.lobai.dto.response.UserResponse;
import com.lobai.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController
 *
 * 사용자 인증 관련 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     *
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}", request.getEmail());

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다", response));
    }

    /**
     * 로그인
     *
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        return ResponseEntity
                .ok(ApiResponse.success("로그인에 성공했습니다", response));
    }

    /**
     * Access Token 갱신
     *
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request received");

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity
                .ok(ApiResponse.success("토큰이 갱신되었습니다", response));
    }

    /**
     * 로그아웃
     *
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request received");

        authService.logout(request.getRefreshToken());

        return ResponseEntity
                .ok(ApiResponse.success("로그아웃되었습니다", null));
    }

    /**
     * 현재 인증된 사용자 정보 조회
     *
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("Get current user request received");

        UserResponse response = authService.getCurrentUser();

        return ResponseEntity
                .ok(ApiResponse.success(response));
    }
}
