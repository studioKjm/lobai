package com.lobai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UpdateUserRequest
 *
 * 사용자 정보 수정 요청 DTO (관리자용)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Size(min = 1, max = 100, message = "사용자 이름은 1자 이상 100자 이하여야 합니다")
    private String username;
}
