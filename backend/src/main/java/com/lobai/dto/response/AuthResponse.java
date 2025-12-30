package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AuthResponse
 *
 * 인증 응답 DTO (로그인/회원가입/토큰 갱신)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    /**
     * Access Token (Bearer 토큰)
     */
    private String accessToken;

    /**
     * Refresh Token (토큰 갱신용)
     */
    private String refreshToken;

    /**
     * Access Token 만료 시간 (밀리초)
     */
    private Long expiresIn;

    /**
     * 토큰 타입 (항상 "Bearer")
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 사용자 이메일
     */
    private String email;

    /**
     * 사용자 이름
     */
    private String username;
}
