package com.lobai.dto.response;

import com.lobai.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MessageResponse
 *
 * 메시지 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private String role;       // "user" or "bot"
    private String content;    // 메시지 내용
    private Long personaId;    // 페르소나 ID
    private String personaName; // 페르소나 이름
    private String messageType; // "NORMAL" or "PROACTIVE"
    private LocalDateTime createdAt;

    /**
     * Entity to DTO 변환
     */
    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .role(message.getRole().name())
                .content(message.getContent())
                .personaId(message.getPersona().getId())
                .personaName(message.getPersona().getDisplayName())
                .messageType(message.getMessageType() != null ? message.getMessageType() : "NORMAL")
                .createdAt(message.getCreatedAt())
                .build();
    }
}
