package com.lobai.service;

import com.lobai.entity.LlmUsageLog;
import com.lobai.entity.User;
import com.lobai.llm.LlmResponse;
import com.lobai.llm.LlmTaskType;
import com.lobai.repository.LlmUsageLogRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LLM 사용량 추적 서비스
 *
 * 모든 LLM 호출을 로깅하고, 사용자별 일일 토큰 제한을 관리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmUsageService {

    private final LlmUsageLogRepository llmUsageLogRepository;
    private final UserRepository userRepository;

    @Value("${llm.usage.free-daily-limit:50000}")
    private int freeDailyLimit;

    @Value("${llm.usage.basic-daily-limit:200000}")
    private int basicDailyLimit;

    @Value("${llm.usage.premium-daily-limit:0}")
    private int premiumDailyLimit; // 0 = unlimited

    /**
     * LLM 호출 로깅 (비동기)
     */
    @Async
    @Transactional
    public void logUsage(Long userId, LlmResponse response, LlmTaskType taskType,
                         int responseTimeMs, boolean isFallback) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;

            LlmResponse.Usage usage = response.getUsage();

            LlmUsageLog logEntry = LlmUsageLog.builder()
                    .user(user)
                    .providerName(response.getProviderName() != null ? response.getProviderName() : "unknown")
                    .modelName(response.getModelUsed() != null ? response.getModelUsed() : "unknown")
                    .taskType(taskType.name())
                    .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                    .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                    .totalTokens(usage != null ? usage.getTotalTokens() : 0)
                    .estimatedCostUsd(usage != null ? usage.getEstimatedCostUsd() : BigDecimal.ZERO)
                    .responseTimeMs(responseTimeMs)
                    .isFallback(isFallback)
                    .build();

            llmUsageLogRepository.save(logEntry);

            log.debug("LLM usage logged: user={}, provider={}, tokens={}, cost=${}",
                    userId, response.getProviderName(),
                    usage != null ? usage.getTotalTokens() : 0,
                    usage != null ? usage.getEstimatedCostUsd() : 0);

        } catch (Exception e) {
            log.error("Failed to log LLM usage for user {}", userId, e);
        }
    }

    /**
     * 에러 로깅
     */
    @Async
    @Transactional
    public void logError(Long userId, String providerName, String modelName,
                         LlmTaskType taskType, String errorMessage) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return;

            LlmUsageLog logEntry = LlmUsageLog.builder()
                    .user(user)
                    .providerName(providerName)
                    .modelName(modelName)
                    .taskType(taskType.name())
                    .errorMessage(errorMessage != null && errorMessage.length() > 500
                            ? errorMessage.substring(0, 500) : errorMessage)
                    .build();

            llmUsageLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to log LLM error for user {}", userId, e);
        }
    }

    /**
     * 사용자의 일일 토큰 사용량 확인
     */
    @Transactional(readOnly = true)
    public int getDailyTokenUsage(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return llmUsageLogRepository.sumDailyTokensByUserId(userId, startOfDay);
    }

    /**
     * 사용자가 일일 제한을 초과했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isOverDailyLimit(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return true;

        int limit = getDailyLimitForUser(user);
        if (limit == 0) return false; // unlimited

        int usage = getDailyTokenUsage(userId);
        return usage >= limit;
    }

    /**
     * 사용자 구독 티어별 일일 토큰 제한
     */
    private int getDailyLimitForUser(User user) {
        String tier = user.getSubscriptionTier();
        if (tier == null) tier = "free";

        return switch (tier.toLowerCase()) {
            case "premium" -> premiumDailyLimit;
            case "basic" -> basicDailyLimit;
            default -> freeDailyLimit;
        };
    }
}
