package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CommunicationSignature Entity
 *
 * AI가 인식하는 인간의 고유한 대화 패턴
 * "이 인간은 이렇게 말한다"
 */
@Entity
@Table(name = "communication_signatures", indexes = {
    @Index(name = "idx_hip_id", columnList = "hip_id"),
    @Index(name = "idx_signature_type", columnList = "signature_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunicationSignature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * HIP ID 참조
     */
    @Column(name = "hip_id", length = 50, nullable = false)
    private String hipId;

    /**
     * 서명 유형
     * linguistic: 언어적 패턴
     * emotional: 감정 표현 패턴
     * structural: 구조적 패턴
     * temporal: 시간적 패턴
     */
    @Column(name = "signature_type", length = 50, nullable = false)
    private String signatureType;

    /**
     * 패턴 데이터 (JSON)
     * 예시:
     * {
     *   "frequent_phrases": ["~인 것 같아요", "어떻게 생각하세요?"],
     *   "sentence_structure": "complex",
     *   "politeness_level": "high",
     *   "emoji_usage": "moderate"
     * }
     */
    @Column(name = "pattern_data", columnDefinition = "JSON", nullable = false)
    private String patternData;

    /**
     * 신뢰도 점수 (0-100)
     * 이 패턴이 얼마나 확실한지
     */
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    /**
     * 추출 시각
     */
    @CreationTimestamp
    @Column(name = "extracted_at", nullable = false)
    private LocalDateTime extractedAt;

    // ==================== Business Methods ====================

    /**
     * 패턴 데이터 업데이트
     */
    public void updatePattern(String newPatternData, BigDecimal confidence) {
        this.patternData = newPatternData;
        this.confidenceScore = confidence;
    }

    /**
     * 신뢰도가 높은지 확인
     */
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore.compareTo(BigDecimal.valueOf(70)) >= 0;
    }
}
