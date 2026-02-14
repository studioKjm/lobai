package com.lobai.llm;

import reactor.core.publisher.Flux;

/**
 * LLM Provider 추상 인터페이스
 *
 * 모든 LLM Provider(Gemini, OpenAI 등)는 이 인터페이스를 구현한다.
 */
public interface LlmProvider {

    /**
     * Provider 이름 (예: "gemini", "openai")
     */
    String getProviderName();

    /**
     * 동기 응답 생성
     */
    LlmResponse generate(LlmRequest request);

    /**
     * SSE 스트리밍 응답 생성
     */
    Flux<String> generateStream(LlmRequest request);

    /**
     * 특정 기능 지원 여부
     */
    boolean supportsFeature(LlmFeature feature);
}
