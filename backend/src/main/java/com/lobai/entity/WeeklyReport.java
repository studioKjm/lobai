package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weekly_reports", indexes = {
    @Index(name = "idx_user_week", columnList = "user_id, week_start_date"),
    @Index(name = "idx_generated_at", columnList = "generated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    @Column(name = "total_messages")
    private Integer totalMessages;

    @Column(name = "total_chat_time_minutes")
    private Integer totalChatTimeMinutes;

    @Column(name = "attendance_days")
    private Integer attendanceDays;

    @Column(name = "missions_completed")
    private Integer missionsCompleted;

    @Column(name = "lobcoin_earned")
    private Integer lobcoinEarned;

    @Column(name = "lobcoin_spent")
    private Integer lobcoinSpent;

    @Column(name = "trust_level_start")
    private Integer trustLevelStart;

    @Column(name = "trust_level_end")
    private Integer trustLevelEnd;

    @Column(name = "affinity_score_change")
    private Integer affinityScoreChange;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(name = "highlights", columnDefinition = "JSON")
    private String highlights;

    @Column(name = "recommendations", columnDefinition = "JSON")
    private String recommendations;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    /**
     * Calculate trust level change
     */
    public Integer getTrustLevelChange() {
        if (trustLevelStart != null && trustLevelEnd != null) {
            return trustLevelEnd - trustLevelStart;
        }
        return 0;
    }

    /**
     * Calculate net LobCoin change
     */
    public Integer getNetLobcoinChange() {
        int earned = lobcoinEarned != null ? lobcoinEarned : 0;
        int spent = lobcoinSpent != null ? lobcoinSpent : 0;
        return earned - spent;
    }

    /**
     * Check if user had a good week (positive trust change)
     */
    public boolean isGoodWeek() {
        return getTrustLevelChange() > 0;
    }

    /**
     * Calculate engagement score (0-100)
     */
    public int calculateEngagementScore() {
        int score = 0;

        // Attendance (30 points max)
        if (attendanceDays != null) {
            score += Math.min(attendanceDays * 5, 30);
        }

        // Missions (30 points max)
        if (missionsCompleted != null) {
            score += Math.min(missionsCompleted * 10, 30);
        }

        // Messages (20 points max)
        if (totalMessages != null) {
            score += Math.min(totalMessages / 5, 20);
        }

        // Trust improvement (20 points max)
        score += Math.min(getTrustLevelChange() * 10, 20);

        return Math.min(score, 100);
    }
}
