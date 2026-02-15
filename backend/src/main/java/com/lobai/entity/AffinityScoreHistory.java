package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AffinityScoreHistory Entity
 *
 * 친밀도 점수 일별 스냅샷 (트렌드 분석용)
 */
@Entity
@Table(name = "affinity_score_history", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, snapshot_date")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_date", columnNames = {"user_id", "snapshot_date"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffinityScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "overall_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal overallScore;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "avg_sentiment_score", precision = 5, scale = 2)
    private BigDecimal avgSentimentScore;

    @Column(name = "avg_clarity_score", precision = 5, scale = 2)
    private BigDecimal avgClarityScore;

    @Column(name = "avg_context_score", precision = 5, scale = 2)
    private BigDecimal avgContextScore;

    @Column(name = "avg_usage_score", precision = 5, scale = 2)
    private BigDecimal avgUsageScore;

    @Column(name = "avg_engagement_depth", precision = 5, scale = 2)
    private BigDecimal avgEngagementDepth;

    @Column(name = "avg_self_disclosure", precision = 5, scale = 2)
    private BigDecimal avgSelfDisclosure;

    @Column(name = "avg_reciprocity", precision = 5, scale = 2)
    private BigDecimal avgReciprocity;

    @Column(name = "relationship_stage", length = 20)
    private String relationshipStage;

    @Column(name = "data_threshold_status", length = 20)
    private String dataThresholdStatus;

    @Column(name = "total_messages")
    @Builder.Default
    private Integer totalMessages = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * AffinityScore에서 스냅샷 생성
     */
    public static AffinityScoreHistory fromAffinityScore(AffinityScore score) {
        return AffinityScoreHistory.builder()
                .userId(score.getUserId())
                .snapshotDate(LocalDate.now())
                .overallScore(score.getOverallScore())
                .level(score.getLevel())
                .avgSentimentScore(score.getAvgSentimentScore())
                .avgClarityScore(score.getAvgClarityScore())
                .avgContextScore(score.getAvgContextScore())
                .avgUsageScore(score.getAvgUsageScore())
                .avgEngagementDepth(score.getAvgEngagementDepth())
                .avgSelfDisclosure(score.getAvgSelfDisclosure())
                .avgReciprocity(score.getAvgReciprocity())
                .relationshipStage(score.getRelationshipStage())
                .dataThresholdStatus(score.getDataThresholdStatus())
                .totalMessages(score.getTotalMessages())
                .build();
    }
}
