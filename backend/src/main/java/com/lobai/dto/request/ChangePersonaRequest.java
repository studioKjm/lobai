package com.lobai.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ChangePersonaRequest
 *
 * 페르소나 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePersonaRequest {

    @NotNull(message = "페르소나 ID는 필수입니다")
    private Long personaId;
}
