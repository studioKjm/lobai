package com.lobai.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * SecurityUtil
 *
 * Spring Security Context에서 인증 정보를 추출하는 유틸리티
 */
public class SecurityUtil {

    /**
     * 현재 인증된 사용자의 ID를 반환
     *
     * @return 사용자 ID
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다");
        }

        Object principal = authentication.getPrincipal();

        // JwtAuthenticationFilter에서 principal을 String(userId)로 설정
        if (principal instanceof String) {
            return Long.parseLong((String) principal);
        }

        if (principal instanceof UserDetails) {
            // UserDetails의 username은 실제로는 email이므로 사용 불가
            // Authentication의 name을 사용 (JwtAuthenticationFilter에서 설정한 userId)
            String userIdString = authentication.getName();
            return Long.parseLong(userIdString);
        }

        throw new IllegalStateException("올바르지 않은 인증 정보입니다");
    }

    /**
     * 현재 인증된 사용자의 이메일을 반환
     *
     * @return 사용자 이메일
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        throw new IllegalStateException("올바르지 않은 인증 정보입니다");
    }
}
