package com.lobai.dto.response;

import com.lobai.entity.AffinityScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AffinityScoreResponse
 *
 * 친밀도 점수 응답 DTO (Phase 1)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffinityScoreResponse {

    private Long userId;
    private BigDecimal overallScore;  // 0.00 ~ 100.00
    private Integer level;            // 1-5
    private String levelName;         // "낯선 사이", "알아가는 중", ...

    // 개별 점수
    private BigDecimal avgSentimentScore;
    private BigDecimal avgClarityScore;
    private BigDecimal avgContextScore;
    private BigDecimal avgUsageScore;

    // 통계
    private Integer totalMessages;
    private Integer analyzedMessages;

    // 레벨 진행도
    private AffinityProgressResponse progress;

    // 메타데이터
    private LocalDateTime updatedAt;

    /**
     * AffinityScore Entity to Response DTO
     */
    public static AffinityScoreResponse from(AffinityScore affinityScore) {
        return AffinityScoreResponse.builder()
                .userId(affinityScore.getUserId())
                .overallScore(affinityScore.getOverallScore())
                .level(affinityScore.getLevel())
                .levelName(affinityScore.getLevelName())
                .avgSentimentScore(affinityScore.getAvgSentimentScore())
                .avgClarityScore(affinityScore.getAvgClarityScore())
                .avgContextScore(affinityScore.getAvgContextScore())
                .avgUsageScore(affinityScore.getAvgUsageScore())
                .totalMessages(affinityScore.getTotalMessages())
                .analyzedMessages(affinityScore.getAnalyzedMessages())
                .progress(AffinityProgressResponse.from(affinityScore))
                .updatedAt(affinityScore.getUpdatedAt())
                .build();
    }

    /**
     * 레벨 진행도 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AffinityProgressResponse {
        private Integer currentLevelMin;     // 현재 레벨 최소 점수
        private Integer currentLevelMax;     // 현재 레벨 최대 점수
        private BigDecimal progressPercentage; // 레벨 내 진행도 (0-100)

        public static AffinityProgressResponse from(AffinityScore score) {
            int level = score.getLevel();
            int min = (level - 1) * 20;
            int max = level * 20;

            double currentScore = score.getOverallScore().doubleValue();
            double progress = ((currentScore - min) / (max - min)) * 100.0;
            progress = Math.max(0.0, Math.min(100.0, progress));

            return AffinityProgressResponse.builder()
                    .currentLevelMin(min)
                    .currentLevelMax(max)
                    .progressPercentage(BigDecimal.valueOf(progress).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .build();
        }
    }
}
