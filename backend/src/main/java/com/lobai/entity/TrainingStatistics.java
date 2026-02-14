package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_statistics", indexes = {
    @Index(name = "idx_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type", nullable = false, length = 50)
    private TrainingSession.TrainingType trainingType;

    @Column(name = "total_sessions")
    @Builder.Default
    private Integer totalSessions = 0;

    @Column(name = "completed_sessions")
    @Builder.Default
    private Integer completedSessions = 0;

    @Column(name = "total_score")
    @Builder.Default
    private Integer totalScore = 0;

    @Column(name = "avg_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal avgScore = BigDecimal.ZERO;

    @Column(name = "best_score")
    @Builder.Default
    private Integer bestScore = 0;

    @Column(name = "total_time_seconds")
    @Builder.Default
    private Integer totalTimeSeconds = 0;

    @Column(name = "total_lobcoin_earned")
    @Builder.Default
    private Integer totalLobcoinEarned = 0;

    @Column(name = "total_lobcoin_spent")
    @Builder.Default
    private Integer totalLobcoinSpent = 0;

    @Column(name = "current_streak")
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "last_training_date")
    private LocalDate lastTrainingDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 세션 완료 시 통계 업데이트
     */
    public void updateWithSession(TrainingSession session) {
        // 총 세션 수 증가
        totalSessions++;

        if (session.getStatus() == TrainingSession.SessionStatus.COMPLETED) {
            completedSessions++;

            // 점수 업데이트
            if (session.getScore() != null) {
                totalScore += session.getScore();
                avgScore = BigDecimal.valueOf((double) totalScore / completedSessions)
                    .setScale(2, java.math.RoundingMode.HALF_UP);

                if (session.getScore() > bestScore) {
                    bestScore = session.getScore();
                }
            }

            // 소요 시간 업데이트
            if (session.getTimeTakenSeconds() != null) {
                totalTimeSeconds += session.getTimeTakenSeconds();
            }

            // LobCoin 업데이트
            totalLobcoinEarned += session.getLobcoinEarned();
            totalLobcoinSpent += session.getLobcoinSpent();

            // 연속 훈련 일수 업데이트
            updateStreak();
        }
    }

    /**
     * 연속 훈련 일수 업데이트
     */
    private void updateStreak() {
        LocalDate today = LocalDate.now();

        if (lastTrainingDate == null) {
            // 첫 훈련
            currentStreak = 1;
        } else if (lastTrainingDate.equals(today)) {
            // 같은 날 추가 훈련 (streak 유지)
            // Do nothing
        } else if (lastTrainingDate.equals(today.minusDays(1))) {
            // 연속 훈련
            currentStreak++;
        } else {
            // 연속 끊김
            currentStreak = 1;
        }

        lastTrainingDate = today;
    }
}
