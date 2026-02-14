package com.lobai.llm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Multi-LLM 설정 클래스
 *
 * application.yml의 llm.* 프로퍼티를 바인딩한다.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmConfig {

    private Map<String, ProviderConfig> providers = new HashMap<>();
    private RoutingConfig routing = new RoutingConfig();

    @Getter
    @Setter
    public static class ProviderConfig {
        private String apiKey;
        private String model;
        private double temperature = 0.8;
        private int maxOutputTokens = 2048;
        private String apiUrl;
    }

    @Getter
    @Setter
    public static class RoutingConfig {
        private String defaultProvider = "gemini";
        private Map<String, String> taskRouting = new HashMap<>();
        private Map<String, String> fallbackChain = new HashMap<>();
    }

    /**
     * Provider 설정 조회 (없으면 null)
     */
    public ProviderConfig getProviderConfig(String providerName) {
        return providers.get(providerName);
    }

    /**
     * 특정 작업 타입에 대한 Provider 이름 조회
     */
    public String getProviderForTask(LlmTaskType taskType) {
        String provider = routing.getTaskRouting().get(taskType.name());
        return provider != null ? provider : routing.getDefaultProvider();
    }

    /**
     * Fallback Provider 이름 조회
     */
    public String getFallbackProvider(String providerName) {
        return routing.getFallbackChain().get(providerName);
    }
}
