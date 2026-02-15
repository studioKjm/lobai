package com.lobai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lobai.dto.MessageAnalysisResult;
import com.lobai.llm.LlmRequest;
import com.lobai.llm.LlmResponse;
import com.lobai.llm.LlmRouter;
import com.lobai.llm.LlmTaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GeminiAffinityAnalyzer
 *
 * Gemini AI 기반 메시지 감성 분석 서비스
 * - 3번째 메시지마다 Gemini 호출 (비용 최적화)
 * - 나머지는 heuristic 분석
 * - 실패 시 키워드 매칭 fallback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAffinityAnalyzer {

    private final LlmRouter llmRouter;
    private final ObjectMapper objectMapper;

    /** 메시지 카운터 (3번째 메시지마다 Gemini 호출) */
    private final AtomicInteger messageCounter = new AtomicInteger(0);

    private static final int GEMINI_CALL_INTERVAL = 3;

    // 기존 키워드 매칭용 (fallback)
    private static final String[] POSITIVE_WORDS = {
        "감사", "고마워", "좋아", "멋져", "훌륭", "최고", "완벽",
        "도움", "친절", "사랑", "기쁘", "행복", "만족", "좋네",
        "대단", "놀라워", "멋지", "완전", "정말", "짱", "굿"
    };
    private static final String[] NEGATIVE_WORDS = {
        "싫어", "화나", "짜증", "멍청", "바보", "쓰레기", "최악",
        "못해", "안돼", "틀려", "나빠", "실망", "별로", "형편없"
    };

    /**
     * 메시지 분석 (Gemini 또는 heuristic)
     */
    public MessageAnalysisResult analyzeMessage(String content, List<String> recentContext) {
        int count = messageCounter.incrementAndGet();

        // 3번째 메시지마다 Gemini 호출
        if (count % GEMINI_CALL_INTERVAL == 0) {
            try {
                return analyzeWithGemini(content, recentContext);
            } catch (Exception e) {
                log.warn("Gemini 분석 실패, heuristic fallback: {}", e.getMessage());
                return analyzeWithHeuristic(content);
            }
        }

        return analyzeWithHeuristic(content);
    }

    /**
     * Gemini AI를 사용한 구조화 분석
     */
    private MessageAnalysisResult analyzeWithGemini(String content, List<String> recentContext) {
        String contextStr = recentContext != null && !recentContext.isEmpty()
                ? String.join("\n", recentContext.subList(0, Math.min(3, recentContext.size())))
                : "없음";

        String prompt = String.format("""
                사용자 메시지를 분석하여 아래 JSON 형식으로만 응답하세요. 다른 텍스트 없이 JSON만 반환하세요.

                [최근 대화 맥락]
                %s

                [분석할 메시지]
                %s

                {
                  "sentimentScore": 0.0,
                  "primaryEmotion": "neutral",
                  "selfDisclosureDepth": 0.0,
                  "honorificLevel": "mixed",
                  "isQuestion": false,
                  "isInitiative": false
                }

                필드 설명:
                - sentimentScore: -1.0(매우 부정) ~ 1.0(매우 긍정)
                - primaryEmotion: joy, sadness, anger, fear, surprise, disgust, trust, anticipation, neutral 중 하나
                - selfDisclosureDepth: 0.0(사실적/표면적) ~ 1.0(깊은 감정/개인 이야기 공유)
                - honorificLevel: formal(존댓말), informal(반말), mixed(혼합)
                - isQuestion: 질문인지 여부
                - isInitiative: 새 주제를 먼저 꺼내는 주도적 대화인지 여부
                """, contextStr, content);

        LlmRequest request = LlmRequest.builder()
                .userMessage(prompt)
                .temperature(0.2)
                .maxOutputTokens(512)
                .jsonMode(true)
                .taskType(LlmTaskType.AFFINITY_ANALYSIS)
                .build();

        LlmResponse response = llmRouter.executeWithFallback(LlmTaskType.AFFINITY_ANALYSIS, request);

        return parseGeminiResponse(response.getContent());
    }

    /**
     * Gemini 응답 JSON 파싱
     */
    private MessageAnalysisResult parseGeminiResponse(String responseContent) {
        try {
            JsonNode json = objectMapper.readTree(responseContent);

            return MessageAnalysisResult.builder()
                    .sentimentScore(parseBigDecimal(json, "sentimentScore", BigDecimal.ZERO))
                    .primaryEmotion(parseString(json, "primaryEmotion", "neutral"))
                    .selfDisclosureDepth(parseBigDecimal(json, "selfDisclosureDepth", BigDecimal.ZERO))
                    .honorificLevel(parseString(json, "honorificLevel", "mixed"))
                    .isQuestion(parseBoolean(json, "isQuestion", false))
                    .isInitiative(parseBoolean(json, "isInitiative", false))
                    .build();
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            return MessageAnalysisResult.defaultResult();
        }
    }

    /**
     * Heuristic 기반 분석 (Gemini 미호출 시)
     */
    private MessageAnalysisResult analyzeWithHeuristic(String content) {
        // Sentiment
        BigDecimal sentimentScore = calculateKeywordSentiment(content);

        // Primary emotion from sentiment
        String primaryEmotion = "neutral";
        double sentVal = sentimentScore.doubleValue();
        if (sentVal > 0.3) primaryEmotion = "joy";
        else if (sentVal < -0.3) primaryEmotion = "sadness";

        // Self-disclosure: long personal messages score higher
        double disclosureVal = 0.0;
        if (content.length() > 100) disclosureVal += 0.3;
        if (content.contains("나는") || content.contains("제가") || content.contains("내가")) disclosureVal += 0.3;
        if (content.contains("느낌") || content.contains("기분") || content.contains("생각")) disclosureVal += 0.2;
        disclosureVal = Math.min(1.0, disclosureVal);

        // Honorific
        String honorificLevel = detectHonorificLevel(content);

        // Question
        boolean isQuestion = content.contains("?") || content.contains("어떻게") ||
                content.contains("무엇") || content.contains("왜") || content.contains("뭐");

        // Initiative: starts conversation, not a response
        boolean isInitiative = !content.contains("그거") && !content.contains("아까") &&
                content.length() > 15 && !isQuestion;

        return MessageAnalysisResult.builder()
                .sentimentScore(sentimentScore)
                .primaryEmotion(primaryEmotion)
                .selfDisclosureDepth(BigDecimal.valueOf(disclosureVal).setScale(2, RoundingMode.HALF_UP))
                .honorificLevel(honorificLevel)
                .isQuestion(isQuestion)
                .isInitiative(isInitiative)
                .build();
    }

    /**
     * 키워드 기반 감정 점수 계산 (기존 로직)
     */
    private BigDecimal calculateKeywordSentiment(String content) {
        int positiveCount = 0;
        int negativeCount = 0;
        String lower = content.toLowerCase();

        for (String word : POSITIVE_WORDS) {
            if (lower.contains(word)) positiveCount++;
        }
        for (String word : NEGATIVE_WORDS) {
            if (lower.contains(word)) negativeCount++;
        }

        int total = positiveCount + negativeCount;
        if (total == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        double score = (double) (positiveCount - negativeCount) / total;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 존댓말 레벨 감지
     */
    private String detectHonorificLevel(String content) {
        boolean hasFormal = content.contains("습니다") || content.contains("요") ||
                content.contains("세요") || content.contains("주세요") || content.contains("합니다");
        boolean hasInformal = content.contains("해") || content.contains("야") ||
                content.contains("지?") || content.contains("냐") || content.contains("거든");

        if (hasFormal && hasInformal) return "mixed";
        if (hasFormal) return "formal";
        if (hasInformal) return "informal";
        return "mixed";
    }

    // JSON parsing helpers
    private BigDecimal parseBigDecimal(JsonNode json, String field, BigDecimal defaultVal) {
        JsonNode node = json.get(field);
        if (node != null && node.isNumber()) {
            return BigDecimal.valueOf(node.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        return defaultVal;
    }

    private String parseString(JsonNode json, String field, String defaultVal) {
        JsonNode node = json.get(field);
        if (node != null && node.isTextual()) return node.asText();
        return defaultVal;
    }

    private boolean parseBoolean(JsonNode json, String field, boolean defaultVal) {
        JsonNode node = json.get(field);
        if (node != null && node.isBoolean()) return node.asBoolean();
        return defaultVal;
    }
}
