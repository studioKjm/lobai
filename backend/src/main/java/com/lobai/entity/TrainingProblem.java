package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_problems", indexes = {
    @Index(name = "idx_type_difficulty", columnList = "training_type, difficulty_level"),
    @Index(name = "idx_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type", nullable = false, length = 50)
    private TrainingSession.TrainingType trainingType;

    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel;

    @Column(name = "problem_text", columnDefinition = "TEXT", nullable = false)
    private String problemText;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "evaluation_criteria", columnDefinition = "TEXT")
    private String evaluationCriteria;

    @Column(name = "hints", columnDefinition = "TEXT")
    private String hints;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "avg_score", precision = 5, scale = 2)
    private BigDecimal avgScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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

    /**
     * 사용 횟수 증가
     */
    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    /**
     * 평균 점수 업데이트
     */
    public void updateAvgScore(int newScore) {
        if (avgScore == null) {
            avgScore = BigDecimal.valueOf(newScore);
        } else {
            // 이동 평균 계산
            BigDecimal currentTotal = avgScore.multiply(BigDecimal.valueOf(usageCount - 1));
            BigDecimal newTotal = currentTotal.add(BigDecimal.valueOf(newScore));
            avgScore = newTotal.divide(BigDecimal.valueOf(usageCount), 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
