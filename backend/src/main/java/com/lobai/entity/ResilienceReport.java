package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ResilienceReport Entity
 * AI 적응력 리포트
 */
@Entity
@Table(name = "resilience_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResilienceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 리포트 유형 (basic, premium)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 20)
    @Builder.Default
    private ReportType reportType = ReportType.basic;

    /**
     * 리포트 버전
     */
    @Column(name = "report_version", nullable = false, length = 10)
    @Builder.Default
    private String reportVersion = "v1.0";

    /**
     * AI 준비도 점수 (0-100)
     */
    @Column(name = "readiness_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal readinessScore;

    /**
     * AI 준비도 레벨 (1-5)
     */
    @Column(name = "readiness_level", nullable = false)
    private Integer readinessLevel;

    /**
     * AI 친화 커뮤니케이션 지수 (0-100)
     */
    @Column(name = "communication_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal communicationScore;

    /**
     * 자동화 위험도 (0-100, 낮을수록 안전)
     */
    @Column(name = "automation_risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal automationRiskScore;

    /**
     * AI 협업 적합도 (0-100)
     */
    @Column(name = "collaboration_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal collaborationScore;

    /**
     * AI 오해/오용 가능성 (0-100, 낮을수록 안전)
     */
    @Column(name = "misuse_risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal misuseRiskScore;

    /**
     * 강점 목록 (JSON array)
     */
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;

    /**
     * 취약점 목록 (JSON array)
     */
    @Column(name = "weaknesses", columnDefinition = "TEXT")
    private String weaknesses;

    /**
     * AI 시대 포지션 시뮬레이션 (JSON)
     */
    @Column(name = "simulation_result", columnDefinition = "TEXT")
    private String simulationResult;

    /**
     * 상세 피드백 (JSON)
     */
    @Column(name = "detailed_feedback", columnDefinition = "TEXT")
    private String detailedFeedback;

    // 친밀도 연동 필드 (Phase 2)
    @Column(name = "avg_engagement_depth", precision = 5, scale = 2)
    private BigDecimal avgEngagementDepth;

    @Column(name = "avg_self_disclosure", precision = 5, scale = 2)
    private BigDecimal avgSelfDisclosure;

    @Column(name = "avg_reciprocity", precision = 5, scale = 2)
    private BigDecimal avgReciprocity;

    @Column(name = "affinity_score_at_report", precision = 5, scale = 2)
    private BigDecimal affinityScoreAtReport;

    @Column(name = "affinity_level_at_report")
    private Integer affinityLevelAtReport;

    @Column(name = "recommendations", columnDefinition = "JSON")
    private String recommendations;

    /**
     * 분석된 메시지 수
     */
    @Column(name = "analyzed_message_count", nullable = false)
    private Integer analyzedMessageCount;

    /**
     * 분석 기간 (일)
     */
    @Column(name = "analyzed_period_days", nullable = false)
    private Integer analyzedPeriodDays;

    /**
     * 분석 기간 시작
     */
    @Column(name = "analysis_start_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime analysisStartDate;

    /**
     * 분석 기간 종료
     */
    @Column(name = "analysis_end_date", columnDefinition = "TIMESTAMP")
    private LocalDateTime analysisEndDate;

    /**
     * 리포트 잠금 해제 여부
     */
    @Column(name = "is_unlocked")
    @Builder.Default
    private Boolean isUnlocked = false;

    /**
     * 잠금 해제 시각
     */
    @Column(name = "unlocked_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime unlockedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 리포트 유형 Enum
     */
    public enum ReportType {
        basic,
        premium
    }

    /**
     * AI 준비도 레벨에 따른 레벨명 반환
     */
    public String getReadinessLevelName() {
        return switch (this.readinessLevel) {
            case 1 -> "초보자";
            case 2 -> "학습자";
            case 3 -> "활용자";
            case 4 -> "전문가";
            case 5 -> "마스터";
            default -> "알 수 없음";
        };
    }

    /**
     * 리포트 잠금 해제
     */
    public void unlock() {
        this.isUnlocked = true;
        this.unlockedAt = LocalDateTime.now();
    }
}
