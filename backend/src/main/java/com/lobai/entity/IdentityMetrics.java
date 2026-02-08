package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IdentityMetrics Entity
 *
 * HIP의 상세 측정 지표
 * 시계열로 저장되어 변화 추적 가능
 */
@Entity
@Table(name = "identity_metrics", indexes = {
    @Index(name = "idx_hip_id", columnList = "hip_id"),
    @Index(name = "idx_measured_at", columnList = "measured_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * HIP ID 참조
     */
    @Column(name = "hip_id", length = 50, nullable = false)
    private String hipId;

    // ==================== Communication Patterns ====================

    /**
     * 평균 메시지 길이
     */
    @Column(name = "avg_message_length", precision = 7, scale = 2)
    private BigDecimal avgMessageLength;

    /**
     * 어휘 다양성 (0-100)
     * 사용하는 단어의 다양성
     */
    @Column(name = "vocabulary_diversity", precision = 5, scale = 2)
    private BigDecimal vocabularyDiversity;

    /**
     * 평균 응답 시간 (초)
     */
    @Column(name = "response_time_avg", precision = 10, scale = 2)
    private BigDecimal responseTimeAvg;

    /**
     * 질문 대 진술 비율 (0-1)
     * 0: 모두 진술, 1: 모두 질문
     */
    @Column(name = "question_to_statement_ratio", precision = 5, scale = 2)
    private BigDecimal questionToStatementRatio;

    // ==================== Behavioral Patterns ====================

    /**
     * 일관성 점수 (0-100)
     * 시간에 따른 행동 패턴의 일관성
     */
    @Column(name = "consistency_score", precision = 5, scale = 2)
    private BigDecimal consistencyScore;

    /**
     * 적응성 점수 (0-100)
     * 새로운 정보나 상황에 대한 적응 능력
     */
    @Column(name = "adaptability_score", precision = 5, scale = 2)
    private BigDecimal adaptabilityScore;

    /**
     * 능동성 점수 (0-100)
     * 스스로 주도적으로 대화를 이끄는 정도
     */
    @Column(name = "proactivity_score", precision = 5, scale = 2)
    private BigDecimal proactivityScore;

    // ==================== AI Interaction Quality ====================

    /**
     * 프롬프트 품질 점수 (0-100)
     * AI에게 요청하는 방식의 명확성과 효과성
     */
    @Column(name = "prompt_quality_score", precision = 5, scale = 2)
    private BigDecimal promptQualityScore;

    /**
     * 맥락 유지 점수 (0-100)
     * 대화 흐름에서 맥락을 유지하는 능력
     */
    @Column(name = "context_maintenance_score", precision = 5, scale = 2)
    private BigDecimal contextMaintenanceScore;

    /**
     * 오류 회복 점수 (0-100)
     * AI의 오류나 오해에 대한 대처 능력
     */
    @Column(name = "error_recovery_score", precision = 5, scale = 2)
    private BigDecimal errorRecoveryScore;

    // ==================== Learning Patterns ====================

    /**
     * 지식 유지 점수 (0-100)
     * 이전 대화에서 배운 내용을 기억하고 활용하는 정도
     */
    @Column(name = "knowledge_retention_score", precision = 5, scale = 2)
    private BigDecimal knowledgeRetentionScore;

    /**
     * 기술 진보 속도 (0-100)
     * AI 활용 능력의 향상 속도
     */
    @Column(name = "skill_progression_rate", precision = 5, scale = 2)
    private BigDecimal skillProgressionRate;

    // ==================== Collaboration Style ====================

    /**
     * 협력 지수 (0-100)
     * AI와 협력하려는 의지와 태도
     */
    @Column(name = "cooperation_index", precision = 5, scale = 2)
    private BigDecimal cooperationIndex;

    /**
     * 갈등 해결 점수 (0-100)
     * AI와의 의견 불일치 시 해결 능력
     */
    @Column(name = "conflict_resolution_score", precision = 5, scale = 2)
    private BigDecimal conflictResolutionScore;

    /**
     * 측정 시각
     */
    @CreationTimestamp
    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    // ==================== Business Methods ====================

    /**
     * Communication 지표 업데이트
     */
    public void updateCommunicationMetrics(
        BigDecimal avgLength,
        BigDecimal vocabulary,
        BigDecimal responseTime,
        BigDecimal questionRatio
    ) {
        this.avgMessageLength = avgLength;
        this.vocabularyDiversity = vocabulary;
        this.responseTimeAvg = responseTime;
        this.questionToStatementRatio = questionRatio;
    }

    /**
     * Behavioral 지표 업데이트
     */
    public void updateBehavioralMetrics(
        BigDecimal consistency,
        BigDecimal adaptability,
        BigDecimal proactivity
    ) {
        this.consistencyScore = consistency;
        this.adaptabilityScore = adaptability;
        this.proactivityScore = proactivity;
    }

    /**
     * AI Interaction 지표 업데이트
     */
    public void updateInteractionMetrics(
        BigDecimal promptQuality,
        BigDecimal contextMaintenance,
        BigDecimal errorRecovery
    ) {
        this.promptQualityScore = promptQuality;
        this.contextMaintenanceScore = contextMaintenance;
        this.errorRecoveryScore = errorRecovery;
    }

    /**
     * Learning 지표 업데이트
     */
    public void updateLearningMetrics(
        BigDecimal knowledgeRetention,
        BigDecimal skillProgression
    ) {
        this.knowledgeRetentionScore = knowledgeRetention;
        this.skillProgressionRate = skillProgression;
    }

    /**
     * Collaboration 지표 업데이트
     */
    public void updateCollaborationMetrics(
        BigDecimal cooperation,
        BigDecimal conflictResolution
    ) {
        this.cooperationIndex = cooperation;
        this.conflictResolutionScore = conflictResolution;
    }
}
