package com.lobai.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Provider-agnostic LLM 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {

    private String systemInstruction;
    private List<LlmMessage> conversationHistory;
    private String userMessage;

    @Builder.Default
    private double temperature = 0.8;

    @Builder.Default
    private int maxOutputTokens = 2048;

    /** Function Calling 선언 (Gemini tools 형식) */
    private List<Map<String, Object>> tools;

    /** 이미지 등 첨부 */
    private List<LlmMessage.Attachment> attachments;

    /** JSON 모드 강제 여부 */
    @Builder.Default
    private boolean jsonMode = false;

    /** 특정 모델 오버라이드 (null이면 config 기본값 사용) */
    private String modelOverride;

    /** 작업 타입 (사용량 로깅용) */
    private LlmTaskType taskType;
}
