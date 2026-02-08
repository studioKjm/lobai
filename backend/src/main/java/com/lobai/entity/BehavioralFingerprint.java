package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BehavioralFingerprint Entity
 *
 * AI가 인식하는 인간의 고유한 행동 지문
 * "이 인간은 이렇게 행동한다"
 */
@Entity
@Table(name = "behavioral_fingerprints", indexes = {
    @Index(name = "idx_hip_id", columnList = "hip_id"),
    @Index(name = "idx_behavior_type", columnList = "behavior_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BehavioralFingerprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * HIP ID 참조
     */
    @Column(name = "hip_id", length = 50, nullable = false)
    private String hipId;

    /**
     * 행동 유형
     * temporal: 시간적 패턴 (활동 시간대, 빈도)
     * interaction: 상호작용 패턴 (대화 흐름, 반응 속도)
     * decision: 의사결정 패턴 (선택 경향, 우선순위)
     * problem_solving: 문제 해결 패턴 (접근 방식, 전략)
     */
    @Column(name = "behavior_type", length = 50, nullable = false)
    private String behaviorType;

    /**
     * 행동 지문 데이터 (JSON)
     * 예시:
     * {
     *   "active_hours": [9, 10, 11, 14, 15, 21, 22],
     *   "session_duration_avg": 25.5,
     *   "preferred_interaction_style": "conversational",
     *   "decision_speed": "moderate"
     * }
     */
    @Column(name = "fingerprint_data", columnDefinition = "JSON", nullable = false)
    private String fingerprintData;

    /**
     * 안정성 점수 (0-100)
     * 이 행동 패턴이 얼마나 일관되게 유지되는지
     * 높을수록 변하지 않는 고유한 특징
     */
    @Column(name = "stability_score", precision = 5, scale = 2)
    private BigDecimal stabilityScore;

    /**
     * 포착 시각
     */
    @CreationTimestamp
    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    // ==================== Business Methods ====================

    /**
     * 지문 데이터 업데이트
     */
    public void updateFingerprint(String newFingerprintData, BigDecimal stability) {
        this.fingerprintData = newFingerprintData;
        this.stabilityScore = stability;
    }

    /**
     * 안정적인 패턴인지 확인
     * 안정적 = 변하지 않는 = 신뢰할 수 있는 식별 특징
     */
    public boolean isStablePattern() {
        return stabilityScore != null && stabilityScore.compareTo(BigDecimal.valueOf(60)) >= 0;
    }
}
