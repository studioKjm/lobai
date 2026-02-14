package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "monthly_reports", indexes = {
    @Index(name = "idx_user_month", columnList = "user_id, report_month"),
    @Index(name = "idx_generated_at", columnList = "generated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_month", nullable = false, length = 7)
    private String reportMonth; // Format: "YYYY-MM"

    @Column(name = "total_messages")
    private Integer totalMessages;

    @Column(name = "total_chat_time_minutes")
    private Integer totalChatTimeMinutes;

    @Column(name = "attendance_days")
    private Integer attendanceDays;

    @Column(name = "max_streak_days")
    private Integer maxStreakDays;

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

    @Column(name = "trust_level_changes")
    private Integer trustLevelChanges;

    @Column(name = "affinity_score_start")
    private Integer affinityScoreStart;

    @Column(name = "affinity_score_end")
    private Integer affinityScoreEnd;

    @Column(name = "resilience_score")
    private Integer resilienceScore;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "achievements", columnDefinition = "JSON")
    private String achievements;

    @Column(name = "key_insights", columnDefinition = "JSON")
    private String keyInsights;

    @Column(name = "next_month_goals", columnDefinition = "JSON")
    private String nextMonthGoals;

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
     * Parse report month as YearMonth
     */
    public YearMonth getReportYearMonth() {
        return YearMonth.parse(reportMonth);
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
     * Calculate affinity score change
     */
    public Integer getAffinityScoreChange() {
        if (affinityScoreStart != null && affinityScoreEnd != null) {
            return affinityScoreEnd - affinityScoreStart;
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
     * Calculate overall performance score (0-100)
     */
    public int calculatePerformanceScore() {
        int score = 0;

        // Attendance (25 points)
        if (attendanceDays != null) {
            score += Math.min(attendanceDays, 25);
        }

        // Missions (25 points)
        if (missionsCompleted != null) {
            score += Math.min(missionsCompleted * 2, 25);
        }

        // Trust improvement (25 points)
        score += Math.min(getTrustLevelChange() * 5, 25);

        // Affinity improvement (25 points)
        score += Math.min(getAffinityScoreChange() / 2, 25);

        return Math.max(0, Math.min(score, 100));
    }

    /**
     * Check if user had a good month
     */
    public boolean isGoodMonth() {
        return getTrustLevelChange() >= 0 && getAffinityScoreChange() >= 0;
    }
}
