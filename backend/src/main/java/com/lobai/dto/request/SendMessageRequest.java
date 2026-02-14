package com.lobai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SendMessageRequest
 *
 * 메시지 전송 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 2000, message = "메시지는 2000자 이하여야 합니다")
    private String content;

    /**
     * 페르소나 ID (선택)
     * null인 경우 사용자의 현재 페르소나 사용
     */
    private Long personaId;
}
