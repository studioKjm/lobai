package com.lobai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ChangeStatusRequest
 *
 * 사용자 상태 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {

    @NotBlank(message = "상태는 필수입니다")
    @Pattern(regexp = "^(active|suspended|deleted)$",
             message = "상태는 active, suspended, deleted 중 하나여야 합니다")
    private String status;
}
