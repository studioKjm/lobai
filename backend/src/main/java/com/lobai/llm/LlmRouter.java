package com.lobai.llm;

import com.lobai.llm.provider.GeminiLlmProvider;
import com.lobai.llm.provider.OpenAiLlmProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM Router
 *
 * 작업 타입 + 페르소나별로 최적 Provider를 선택하고,
 * 실패 시 Fallback Provider로 자동 전환한다.
 */
@Slf4j
@Service
public class LlmRouter {

    private final LlmConfig llmConfig;
    private final Map<String, LlmProvider> providers = new HashMap<>();

    public LlmRouter(LlmConfig llmConfig,
                     GeminiLlmProvider geminiProvider,
                     OpenAiLlmProvider openAiProvider) {
        this.llmConfig = llmConfig;
        this.providers.put("gemini", geminiProvider);
        this.providers.put("openai", openAiProvider);
    }

    /**
     * 작업 타입에 따른 최적 Provider 선택
     */
    public LlmProvider resolve(LlmTaskType taskType) {
        String providerName = llmConfig.getProviderForTask(taskType);
        LlmProvider provider = providers.get(providerName);

        if (provider != null && isProviderAvailable(providerName)) {
            log.debug("Resolved provider '{}' for task type '{}'", providerName, taskType);
            return provider;
        }

        // Fallback
        String fallbackName = llmConfig.getFallbackProvider(providerName);
        if (fallbackName != null) {
            LlmProvider fallback = providers.get(fallbackName);
            if (fallback != null) {
                log.warn("Primary provider '{}' unavailable, using fallback '{}'", providerName, fallbackName);
                return fallback;
            }
        }

        // 최후의 수단: gemini (기존 동작 보장)
        LlmProvider gemini = providers.get("gemini");
        if (gemini != null) return gemini;

        throw new IllegalStateException("No LLM provider available");
    }

    /**
     * 특정 Provider를 이름으로 직접 가져오기
     */
    public LlmProvider getProvider(String providerName) {
        LlmProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown LLM provider: " + providerName);
        }
        return provider;
    }

    /**
     * 요청 실행 (자동 폴백 포함)
     */
    public LlmResponse executeWithFallback(LlmTaskType taskType, LlmRequest request) {
        String primaryName = llmConfig.getProviderForTask(taskType);
        LlmProvider primary = providers.get(primaryName);

        // Primary 시도
        if (primary != null && isProviderAvailable(primaryName)) {
            try {
                LlmResponse response = primary.generate(request);
                log.info("LLM response from {} ({}): {} tokens",
                        primaryName, taskType, response.getUsage() != null ? response.getUsage().getTotalTokens() : "?");
                return response;
            } catch (Exception e) {
                log.warn("Primary provider '{}' failed for task '{}': {}", primaryName, taskType, e.getMessage());
            }
        }

        // Fallback 시도
        String fallbackName = llmConfig.getFallbackProvider(primaryName);
        if (fallbackName != null) {
            LlmProvider fallback = providers.get(fallbackName);
            if (fallback != null) {
                try {
                    log.info("Falling back to '{}' for task '{}'", fallbackName, taskType);
                    LlmResponse response = fallback.generate(request);
                    log.info("Fallback response from {} ({}): {} tokens",
                            fallbackName, taskType, response.getUsage() != null ? response.getUsage().getTotalTokens() : "?");
                    return response;
                } catch (Exception e) {
                    log.error("Fallback provider '{}' also failed for task '{}': {}", fallbackName, taskType, e.getMessage());
                    throw new RuntimeException("All LLM providers failed for task: " + taskType, e);
                }
            }
        }

        throw new RuntimeException("No available LLM provider for task: " + taskType);
    }

    /**
     * Provider가 설정되어 있고 사용 가능한지 확인
     */
    private boolean isProviderAvailable(String providerName) {
        LlmConfig.ProviderConfig config = llmConfig.getProviderConfig(providerName);
        if (config == null) return false;
        String apiKey = config.getApiKey();
        return apiKey != null && !apiKey.isEmpty()
                && !apiKey.startsWith("your-") && !apiKey.equals("${OPENAI_API_KEY:}");
    }
}
