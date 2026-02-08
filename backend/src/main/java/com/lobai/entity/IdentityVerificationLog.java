package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * IdentityVerificationLog Entity
 *
 * HIP 검증 이력 로그
 * "이 인간의 정체성이 언제, 어떻게 검증되었는가"
 */
@Entity
@Table(name = "identity_verification_logs", indexes = {
    @Index(name = "idx_hip_id", columnList = "hip_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_verified_at", columnList = "verified_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityVerificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * HIP ID 참조
     */
    @Column(name = "hip_id", length = 50, nullable = false)
    private String hipId;

    /**
     * 검증 유형
     * initial: 최초 HIP 생성 시
     * periodic: 정기 검증 (자동)
     * challenge: 이상 행동 탐지 시 챌린지 검증
     * manual: 수동 검증 (관리자)
     */
    @Column(name = "verification_type", length = 50, nullable = false)
    private String verificationType;

    /**
     * 검증 방법
     * behavioral_analysis: 행동 패턴 분석
     * cross_reference: 교차 참조 (다른 데이터와 비교)
     * consistency_check: 일관성 검사
     * ai_assessment: AI 평가
     */
    @Column(name = "verification_method", length = 50)
    private String verificationMethod;

    /**
     * 이전 점수
     */
    @Column(name = "previous_score", precision = 5, scale = 2)
    private BigDecimal previousScore;

    /**
     * 새로운 점수
     */
    @Column(name = "new_score", precision = 5, scale = 2)
    private BigDecimal newScore;

    /**
     * 검증 상태
     * verified: 검증 통과
     * flagged: 이상 탐지
     * failed: 검증 실패
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 비고
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 검증 시각
     */
    @CreationTimestamp
    @Column(name = "verified_at", nullable = false)
    private LocalDateTime verifiedAt;

    // ==================== Business Methods ====================

    /**
     * 점수 변화량 계산
     */
    public BigDecimal getScoreChange() {
        if (previousScore == null || newScore == null) {
            return BigDecimal.ZERO;
        }
        return newScore.subtract(previousScore);
    }

    /**
     * 검증 통과 여부
     */
    public boolean isPassed() {
        return "verified".equals(status);
    }

    /**
     * 이상 탐지 여부
     */
    public boolean isFlagged() {
        return "flagged".equals(status);
    }
}
