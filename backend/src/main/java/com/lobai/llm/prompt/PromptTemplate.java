package com.lobai.llm.prompt;

/**
 * 프롬프트 템플릿 인터페이스
 */
public interface PromptTemplate {
    String render(PromptContext context);
}
