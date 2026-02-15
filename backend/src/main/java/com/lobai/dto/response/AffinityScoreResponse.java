package com.lobai.dto.response;

import com.lobai.entity.AffinityScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * AffinityScoreResponse
 *
 * 친밀도 점수 응답 DTO (Phase 2 - 7차원 고도화)
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

    // 기존 4개 차원 점수
    private BigDecimal avgSentimentScore;
    private BigDecimal avgClarityScore;
    private BigDecimal avgContextScore;
    private BigDecimal avgUsageScore;

    // 새로운 3개 차원 점수
    private BigDecimal avgEngagementDepth;
    private BigDecimal avgSelfDisclosure;
    private BigDecimal avgReciprocity;

    // 관계 메타데이터
    private String relationshipStage;       // EARLY, DEVELOPING, MATURE
    private String relationshipStageName;   // 초기 관계, 발전 중, 성숙한 관계
    private String dataThresholdStatus;     // COLLECTING, INITIAL, FULL
    private String dataThresholdMessage;    // UI 표시용 메시지
    private BigDecimal noveltyDiscountFactor;
    private Boolean honorificTransitionDetected;
    private Integer totalSessions;

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
        String stageName = getRelationshipStageName(affinityScore.getRelationshipStage());
        String thresholdMessage = getDataThresholdMessage(affinityScore.getDataThresholdStatus());

        return AffinityScoreResponse.builder()
                .userId(affinityScore.getUserId())
                .overallScore(affinityScore.getOverallScore())
                .level(affinityScore.getLevel())
                .levelName(affinityScore.getLevelName())
                // 기존 4차원
                .avgSentimentScore(affinityScore.getAvgSentimentScore())
                .avgClarityScore(affinityScore.getAvgClarityScore())
                .avgContextScore(affinityScore.getAvgContextScore())
                .avgUsageScore(affinityScore.getAvgUsageScore())
                // 새 3차원
                .avgEngagementDepth(affinityScore.getAvgEngagementDepth())
                .avgSelfDisclosure(affinityScore.getAvgSelfDisclosure())
                .avgReciprocity(affinityScore.getAvgReciprocity())
                // 관계 메타데이터
                .relationshipStage(affinityScore.getRelationshipStage())
                .relationshipStageName(stageName)
                .dataThresholdStatus(affinityScore.getDataThresholdStatus())
                .dataThresholdMessage(thresholdMessage)
                .noveltyDiscountFactor(affinityScore.getNoveltyDiscountFactor())
                .honorificTransitionDetected(affinityScore.getHonorificTransitionDetected())
                .totalSessions(affinityScore.getTotalSessions())
                // 통계
                .totalMessages(affinityScore.getTotalMessages())
                .analyzedMessages(affinityScore.getAnalyzedMessages())
                .progress(AffinityProgressResponse.from(affinityScore))
                .updatedAt(affinityScore.getUpdatedAt())
                .build();
    }

    private static String getRelationshipStageName(String stage) {
        if (stage == null) return "초기 관계";
        return switch (stage) {
            case "EARLY" -> "초기 관계";
            case "DEVELOPING" -> "발전 중";
            case "MATURE" -> "성숙한 관계";
            default -> "초기 관계";
        };
    }

    private static String getDataThresholdMessage(String status) {
        if (status == null) return "데이터 수집 중...";
        return switch (status) {
            case "COLLECTING" -> "데이터 수집 중... (최소 10개 메시지 필요)";
            case "INITIAL" -> "초기 분석 단계 (50개 메시지 이상 시 정식 분석)";
            case "FULL" -> "정식 분석 완료";
            default -> "데이터 수집 중...";
        };
    }

    /**
     * 레벨 진행도 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AffinityProgressResponse {
        private Integer currentLevelMin;
        private Integer currentLevelMax;
        private BigDecimal progressPercentage;

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
                    .progressPercentage(BigDecimal.valueOf(progress).setScale(2, RoundingMode.HALF_UP))
                    .build();
        }
    }
}
