package com.lobai.dto.request;

import com.lobai.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ChangeRoleRequest
 *
 * 사용자 권한 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {

    @NotNull(message = "권한은 필수입니다")
    private Role role;
}
