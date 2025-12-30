package com.lobai.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter
 *
 * HTTP 요청에서 JWT 토큰을 추출하고 검증하여 인증 정보를 설정하는 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 2. 토큰이 있고 유효하면 인증 정보 설정
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 토큰에서 사용자 ID 추출
                Long userId = jwtTokenProvider.getUserIdFromToken(jwt);

                // 사용자 정보 조회
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                // Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user ID: {}", userId);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 JWT 토큰 추출
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 (Bearer 제외)
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거
        }

        return null;
    }
}
