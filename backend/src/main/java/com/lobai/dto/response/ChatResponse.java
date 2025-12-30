package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ChatResponse
 *
 * 채팅 응답 DTO (사용자 메시지 + AI 응답 + Stats 업데이트 포함)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    /**
     * 사용자가 보낸 메시지
     */
    private MessageResponse userMessage;

    /**
     * AI 봇의 응답 메시지
     */
    private MessageResponse botMessage;

    /**
     * 업데이트된 Stats (대화 후 행복도 증가 등)
     */
    private StatsResponse statsUpdate;
}
