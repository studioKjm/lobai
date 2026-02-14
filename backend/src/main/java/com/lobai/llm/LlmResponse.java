package com.lobai.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Provider-agnostic LLM 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {

    private String content;
    private String providerName;
    private String modelUsed;
    private String finishReason;

    /** Function Call이 있는 경우 */
    private FunctionCall functionCall;

    /** 토큰 사용량 */
    private Usage usage;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionCall {
        private String name;
        private String argsJson;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @Builder.Default
        private int promptTokens = 0;
        @Builder.Default
        private int completionTokens = 0;
        @Builder.Default
        private int totalTokens = 0;
        @Builder.Default
        private BigDecimal estimatedCostUsd = BigDecimal.ZERO;
    }

    public boolean hasFunctionCall() {
        return functionCall != null && functionCall.getName() != null;
    }
}
