package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "training_sessions", indexes = {
    @Index(name = "idx_user_type", columnList = "user_id, training_type"),
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type", nullable = false, length = 50)
    private TrainingType trainingType;

    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel;

    @Column(name = "problem_id")
    private Long problemId;

    @Column(name = "problem_text", columnDefinition = "TEXT", nullable = false)
    private String problemText;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "score")
    private Integer score;

    @Column(name = "evaluation", columnDefinition = "TEXT")
    private String evaluation;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "hints_used")
    @Builder.Default
    private Integer hintsUsed = 0;

    @Column(name = "lobcoin_earned")
    @Builder.Default
    private Integer lobcoinEarned = 0;

    @Column(name = "lobcoin_spent")
    @Builder.Default
    private Integer lobcoinSpent = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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
     * 훈련 유형
     */
    public enum TrainingType {
        CRITICAL_THINKING,    // 비판적 사고
        PROBLEM_SOLVING,      // 문제 해결
        CREATIVE_THINKING,    // 창의적 사고
        SELF_REFLECTION,      // 자기 성찰
        REASONING,            // 추리문제
        MEMORY                // 암기력
    }

    /**
     * 세션 상태
     */
    public enum SessionStatus {
        IN_PROGRESS,  // 진행 중
        COMPLETED,    // 완료
        ABANDONED     // 중단
    }

    /**
     * 세션 완료 처리
     */
    public void complete() {
        this.status = SessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 세션 중단 처리
     */
    public void abandon() {
        this.status = SessionStatus.ABANDONED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 점수 계산 및 LobCoin 지급
     */
    public void calculateReward() {
        if (score == null) return;

        // 기본 보상
        int reward = 10;

        // 점수에 따른 보너스
        if (score >= 90) {
            reward += 30; // 90점 이상: 40 LobCoin
        } else if (score >= 80) {
            reward += 20; // 80점 이상: 30 LobCoin
        } else if (score >= 70) {
            reward += 10; // 70점 이상: 20 LobCoin
        }

        // 난이도 보너스
        reward += (difficultyLevel - 1) * 2;

        // 힌트 사용 페널티 제외 (이미 힌트 사용 시 LobCoin 차감됨)

        this.lobcoinEarned = reward;
    }
}
