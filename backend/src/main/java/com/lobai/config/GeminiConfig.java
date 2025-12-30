package com.lobai.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * GeminiConfig
 *
 * Google Gemini AI API 설정
 */
@Configuration
@Getter
public class GeminiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash-exp}")
    private String model;

    @Value("${gemini.temperature:0.8}")
    private Double temperature;

    @Value("${gemini.api-url:https://generativelanguage.googleapis.com/v1beta/models}")
    private String apiUrl;

    /**
     * RestTemplate Bean (Gemini API 호출용)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Gemini API Endpoint URL 생성
     */
    public String getGenerateContentUrl() {
        return apiUrl + "/" + model + ":generateContent?key=" + apiKey;
    }
}
