package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AffinityScore Entity
 *
 * 사용자별 친밀도 점수 집계 엔티티 (Phase 1)
 */
@Entity
@Table(name = "affinity_scores", indexes = {
    @Index(name = "idx_overall_score", columnList = "overall_score DESC"),
    @Index(name = "idx_level", columnList = "level"),
    @Index(name = "idx_updated_at", columnList = "updated_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffinityScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // 종합 점수
    @Column(name = "overall_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal overallScore = BigDecimal.valueOf(50.00);  // 0.00 ~ 100.00

    @Column(name = "level", nullable = false)
    @Builder.Default
    private Integer level = 3;  // 1-5

    // 개별 점수 평균 (최근 N개 메시지 기준)
    @Column(name = "avg_sentiment_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgSentimentScore = BigDecimal.valueOf(0.00);  // -1.00 ~ 1.00

    @Column(name = "avg_clarity_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgClarityScore = BigDecimal.valueOf(0.50);  // 0.00 ~ 1.00

    @Column(name = "avg_context_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgContextScore = BigDecimal.valueOf(0.50);  // 0.00 ~ 1.00

    @Column(name = "avg_usage_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgUsageScore = BigDecimal.valueOf(0.50);  // 0.00 ~ 1.00

    // 새로운 차원 점수 (Phase 2)
    @Column(name = "avg_engagement_depth", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgEngagementDepth = BigDecimal.valueOf(0.50);  // 0.00 ~ 1.00

    @Column(name = "avg_self_disclosure", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgSelfDisclosure = BigDecimal.valueOf(0.00);  // 0.00 ~ 1.00

    @Column(name = "avg_reciprocity", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgReciprocity = BigDecimal.valueOf(0.50);  // 0.00 ~ 1.00

    // 관계 메타데이터 (Phase 2)
    @Column(name = "relationship_stage", length = 20)
    @Builder.Default
    private String relationshipStage = "EARLY";  // EARLY, DEVELOPING, MATURE

    @Column(name = "total_sessions")
    @Builder.Default
    private Integer totalSessions = 0;

    @Column(name = "novelty_discount_factor", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal noveltyDiscountFactor = BigDecimal.valueOf(0.60);

    @Column(name = "data_threshold_status", length = 20)
    @Builder.Default
    private String dataThresholdStatus = "COLLECTING";  // COLLECTING, INITIAL, FULL

    @Column(name = "honorific_transition_detected")
    @Builder.Default
    private Boolean honorificTransitionDetected = false;

    // 통계 정보
    @Column(name = "total_messages", nullable = false)
    @Builder.Default
    private Integer totalMessages = 0;

    @Column(name = "analyzed_messages", nullable = false)
    @Builder.Default
    private Integer analyzedMessages = 0;

    @Column(name = "last_analyzed_message_id")
    private Long lastAnalyzedMessageId;

    // 메타데이터
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void updateScores(BigDecimal sentiment, BigDecimal clarity, BigDecimal context, BigDecimal usage) {
        if (sentiment != null) this.avgSentimentScore = sentiment;
        if (clarity != null) this.avgClarityScore = clarity;
        if (context != null) this.avgContextScore = context;
        if (usage != null) this.avgUsageScore = usage;
    }

    public void updateAllScores(BigDecimal sentiment, BigDecimal clarity, BigDecimal context, BigDecimal usage,
                                 BigDecimal engagement, BigDecimal disclosure, BigDecimal reciprocity) {
        updateScores(sentiment, clarity, context, usage);
        if (engagement != null) this.avgEngagementDepth = engagement;
        if (disclosure != null) this.avgSelfDisclosure = disclosure;
        if (reciprocity != null) this.avgReciprocity = reciprocity;
    }

    public void setTotalMessages(int count) { this.totalMessages = count; }
    public void setTotalSessions(int count) { this.totalSessions = count; }
    public void setRelationshipStage(String stage) { this.relationshipStage = stage; }
    public void setNoveltyDiscountFactor(BigDecimal factor) { this.noveltyDiscountFactor = factor; }
    public void setDataThresholdStatus(String status) { this.dataThresholdStatus = status; }
    public void setHonorificTransitionDetected(Boolean detected) { this.honorificTransitionDetected = detected; }

    public void updateOverallScore(BigDecimal score) {
        this.overallScore = score;
        this.level = calculateLevel(score);
    }

    public void incrementAnalyzedMessages() {
        this.analyzedMessages++;
    }

    public void incrementTotalMessages() {
        this.totalMessages++;
    }

    /**
     * 점수를 기반으로 레벨 계산
     * 0-20: Lv1, 21-40: Lv2, 41-60: Lv3, 61-80: Lv4, 81-100: Lv5
     */
    private Integer calculateLevel(BigDecimal score) {
        double scoreValue = score.doubleValue();
        if (scoreValue <= 20) return 1;
        if (scoreValue <= 40) return 2;
        if (scoreValue <= 60) return 3;
        if (scoreValue <= 80) return 4;
        return 5;
    }

    /**
     * 레벨 이름 반환
     */
    public String getLevelName() {
        return switch (this.level) {
            case 1 -> "낯선 사이";
            case 2 -> "알아가는 중";
            case 3 -> "친근한 사이";
            case 4 -> "가까운 친구";
            case 5 -> "최고의 파트너";
            default -> "알 수 없음";
        };
    }
}
