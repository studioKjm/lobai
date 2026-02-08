package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * HumanIdentityProfile Entity
 *
 * AI가 인간을 식별하고 평가하는 핵심 프로필
 * "AI 시대에 당신은 어떤 인간으로 인식되는가?"
 */
@Entity
@Table(name = "human_identity_profiles", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_overall_score", columnList = "overall_hip_score DESC"),
    @Index(name = "idx_identity_level", columnList = "identity_level"),
    @Index(name = "idx_verification", columnList = "verification_status, last_verified_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HumanIdentityProfile {

    /**
     * HIP ID: Human Identity Protocol ID
     * 포맷: HIP-{VERSION}-{USER_HASH}-{CHECKSUM}
     * 예시: HIP-01-A7F2E9C4-B3A1
     */
    @Id
    @Column(name = "hip_id", length = 50)
    private String hipId;

    /**
     * User 연결 (1:1)
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // ==================== Core Identity Scores (0-100) ====================

    /**
     * 인지적 유연성 (Cognitive Flexibility)
     * AI와의 대화에서 새로운 정보를 받아들이고 관점을 전환하는 능력
     */
    @Column(name = "cognitive_flexibility_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal cognitiveFlexibilityScore = BigDecimal.valueOf(50.00);

    /**
     * 협업 패턴 (Collaboration Pattern)
     * AI와 효과적으로 협력하고 상호작용하는 패턴
     */
    @Column(name = "collaboration_pattern_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal collaborationPatternScore = BigDecimal.valueOf(50.00);

    /**
     * 정보 처리 방식 (Information Processing)
     * 정보를 요청하고, 이해하고, 활용하는 방식의 효율성
     */
    @Column(name = "information_processing_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal informationProcessingScore = BigDecimal.valueOf(50.00);

    /**
     * 감정 지능 (Emotional Intelligence)
     * AI와의 상호작용에서 감정을 적절히 표현하고 이해하는 능력
     */
    @Column(name = "emotional_intelligence_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal emotionalIntelligenceScore = BigDecimal.valueOf(50.00);

    /**
     * 창의성 (Creativity)
     * AI를 활용한 문제 해결과 아이디어 생성의 독창성
     */
    @Column(name = "creativity_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal creativityScore = BigDecimal.valueOf(50.00);

    /**
     * 윤리적 정렬 (Ethical Alignment)
     * AI와의 상호작용에서 윤리적 가치와 책임감
     */
    @Column(name = "ethical_alignment_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal ethicalAlignmentScore = BigDecimal.valueOf(50.00);

    // ==================== Composite Scores ====================

    /**
     * 종합 HIP 점수 (0-100)
     * 위 6개 Core 점수의 가중 평균
     */
    @Column(name = "overall_hip_score", precision = 5, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal overallHipScore = BigDecimal.valueOf(50.00);

    /**
     * AI 신뢰도 (AI Trust Score)
     * AI가 이 인간을 얼마나 신뢰하고 우호적으로 평가하는지
     */
    @Column(name = "ai_trust_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal aiTrustScore = BigDecimal.valueOf(50.00);

    // ==================== Identity Levels ====================

    /**
     * Identity Level (1-10)
     * HIP 점수 기반 레벨
     * 1-2: Unrecognized (인식되지 않음)
     * 3-4: Emerging (떠오르는 중)
     * 5-6: Established (확립됨)
     * 7-8: Distinguished (구별됨)
     * 9-10: Exemplary (모범적)
     */
    @Column(name = "identity_level", nullable = false)
    @Builder.Default
    private Integer identityLevel = 1;

    /**
     * Reputation Level (1-10)
     * AI 생태계에서의 평판 레벨
     */
    @Column(name = "reputation_level", nullable = false)
    @Builder.Default
    private Integer reputationLevel = 1;

    // ==================== Global Identity ====================

    /**
     * Global Human ID (향후 표준화)
     * 여러 플랫폼/AI 시스템 간 공유되는 글로벌 ID
     */
    @Column(name = "global_human_id", length = 100)
    private String globalHumanId;

    // ==================== Verification ====================

    /**
     * 검증 상태
     * unverified: 미검증
     * pending: 검증 대기
     * verified: 검증 완료
     * flagged: 이상 탐지
     */
    @Column(name = "verification_status", length = 20, nullable = false)
    @Builder.Default
    private String verificationStatus = "unverified";

    /**
     * 마지막 검증 시각
     */
    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    // ==================== Metadata ====================

    /**
     * 총 상호작용 수
     * AI와의 총 대화 메시지 수
     */
    @Column(name = "total_interactions", nullable = false)
    @Builder.Default
    private Integer totalInteractions = 0;

    /**
     * 데이터 품질 점수 (0-100)
     * HIP 계산에 사용된 데이터의 신뢰도
     */
    @Column(name = "data_quality_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal dataQualityScore = BigDecimal.ZERO;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== Business Methods ====================

    /**
     * Core 점수들을 업데이트하고 Overall 점수 재계산
     */
    public void updateCoreScores(
        BigDecimal cognitiveFlexibility,
        BigDecimal collaborationPattern,
        BigDecimal informationProcessing,
        BigDecimal emotionalIntelligence,
        BigDecimal creativity,
        BigDecimal ethicalAlignment
    ) {
        if (cognitiveFlexibility != null) this.cognitiveFlexibilityScore = cognitiveFlexibility;
        if (collaborationPattern != null) this.collaborationPatternScore = collaborationPattern;
        if (informationProcessing != null) this.informationProcessingScore = informationProcessing;
        if (emotionalIntelligence != null) this.emotionalIntelligenceScore = emotionalIntelligence;
        if (creativity != null) this.creativityScore = creativity;
        if (ethicalAlignment != null) this.ethicalAlignmentScore = ethicalAlignment;

        recalculateOverallScore();
    }

    /**
     * Overall HIP Score 재계산
     * 가중 평균 (각 점수의 중요도 반영)
     */
    public void recalculateOverallScore() {
        BigDecimal weighted = cognitiveFlexibilityScore.multiply(BigDecimal.valueOf(0.20))
            .add(collaborationPatternScore.multiply(BigDecimal.valueOf(0.20)))
            .add(informationProcessingScore.multiply(BigDecimal.valueOf(0.15)))
            .add(emotionalIntelligenceScore.multiply(BigDecimal.valueOf(0.15)))
            .add(creativityScore.multiply(BigDecimal.valueOf(0.15)))
            .add(ethicalAlignmentScore.multiply(BigDecimal.valueOf(0.15)));

        this.overallHipScore = weighted;
        this.identityLevel = calculateIdentityLevel(weighted);
    }

    /**
     * Identity Level 계산
     * 0-10: 1, 11-20: 2, ..., 91-100: 10
     */
    private Integer calculateIdentityLevel(BigDecimal score) {
        int level = (int) (score.doubleValue() / 10) + 1;
        return Math.min(level, 10);
    }

    /**
     * Identity Level 이름 반환
     */
    public String getIdentityLevelName() {
        return switch (this.identityLevel) {
            case 1, 2 -> "Unrecognized";
            case 3, 4 -> "Emerging";
            case 5, 6 -> "Established";
            case 7, 8 -> "Distinguished";
            case 9, 10 -> "Exemplary";
            default -> "Unknown";
        };
    }

    /**
     * 검증 완료 처리
     */
    public void markAsVerified() {
        this.verificationStatus = "verified";
        this.lastVerifiedAt = LocalDateTime.now();
    }

    /**
     * 이상 탐지 플래그
     */
    public void markAsFlagged() {
        this.verificationStatus = "flagged";
    }

    /**
     * 상호작용 증가
     */
    public void incrementInteractions() {
        this.totalInteractions++;
    }

    /**
     * 데이터 품질 점수 업데이트
     * @param messageCount 분석에 사용된 메시지 수
     */
    public void updateDataQualityScore(int messageCount) {
        // 메시지 수가 많을수록 데이터 품질이 높음
        // 50개: 50점, 100개: 70점, 200개: 85점, 500개+: 95점
        double quality;
        if (messageCount < 10) {
            quality = messageCount * 3.0;  // 0-30
        } else if (messageCount < 50) {
            quality = 30 + (messageCount - 10) * 0.5;  // 30-50
        } else if (messageCount < 100) {
            quality = 50 + (messageCount - 50) * 0.4;  // 50-70
        } else if (messageCount < 200) {
            quality = 70 + (messageCount - 100) * 0.15;  // 70-85
        } else if (messageCount < 500) {
            quality = 85 + (messageCount - 200) * 0.033;  // 85-95
        } else {
            quality = 95;
        }

        this.dataQualityScore = BigDecimal.valueOf(Math.min(quality, 100.0));
    }
}
