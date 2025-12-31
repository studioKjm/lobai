package com.lobai.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RegisterRequest
 *
 * 회원가입 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 1, max = 100, message = "비밀번호는 1자 이상 100자 이하여야 합니다")
    private String password;

    @NotBlank(message = "사용자 이름은 필수입니다")
    @Size(min = 1, max = 50, message = "사용자 이름은 1자 이상 50자 이하여야 합니다")
    private String username;
}
