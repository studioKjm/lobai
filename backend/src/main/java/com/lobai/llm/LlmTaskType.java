package com.lobai.llm;

/**
 * LLM 호출 작업 유형 (라우팅 결정에 사용)
 */
public enum LlmTaskType {
    CHAT_CONVERSATION,
    HIP_ANALYSIS,
    CONVERSATION_SUMMARY,
    FUNCTION_CALLING,
    RESILIENCE_ANALYSIS,
    PROACTIVE_MESSAGE,
    AFFINITY_ANALYSIS
}
