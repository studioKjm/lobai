package com.lobai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * MessageAnalysisResult
 *
 * Gemini 감성 분석 응답 파싱용 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAnalysisResult {

    /** 감정 점수 (-1.0 ~ 1.0) */
    private BigDecimal sentimentScore;

    /** 주요 감정 (joy, sadness, anger, fear, surprise, disgust, trust, anticipation, neutral) */
    private String primaryEmotion;

    /** 자기 개방 깊이 (0.0 ~ 1.0) */
    private BigDecimal selfDisclosureDepth;

    /** 존댓말 레벨 (formal, informal, mixed) */
    private String honorificLevel;

    /** 질문 여부 */
    private boolean isQuestion;

    /** 주도성 여부 (새 주제 제시 등) */
    private boolean isInitiative;

    /**
     * 기본 fallback 결과 생성 (Gemini 실패 시)
     */
    public static MessageAnalysisResult defaultResult() {
        return MessageAnalysisResult.builder()
                .sentimentScore(BigDecimal.ZERO)
                .primaryEmotion("neutral")
                .selfDisclosureDepth(BigDecimal.ZERO)
                .honorificLevel("mixed")
                .isQuestion(false)
                .isInitiative(false)
                .build();
    }
}
