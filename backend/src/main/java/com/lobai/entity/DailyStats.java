package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_stats", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, stat_date"),
    @Index(name = "idx_date", columnList = "stat_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_messages")
    @Builder.Default
    private Integer totalMessages = 0;

    @Column(name = "chat_time_minutes")
    @Builder.Default
    private Integer chatTimeMinutes = 0;

    @Column(name = "did_attend")
    @Builder.Default
    private Boolean didAttend = false;

    @Column(name = "streak_count")
    @Builder.Default
    private Integer streakCount = 0;

    @Column(name = "missions_completed")
    @Builder.Default
    private Integer missionsCompleted = 0;

    @Column(name = "demands_completed")
    @Builder.Default
    private Integer demandsCompleted = 0;

    @Column(name = "demands_failed")
    @Builder.Default
    private Integer demandsFailed = 0;

    @Column(name = "lobcoin_earned")
    @Builder.Default
    private Integer lobcoinEarned = 0;

    @Column(name = "lobcoin_spent")
    @Builder.Default
    private Integer lobcoinSpent = 0;

    @Column(name = "trust_level")
    private Integer trustLevel;

    @Column(name = "affinity_score")
    private Integer affinityScore;

    @Column(name = "resilience_score")
    private Integer resilienceScore;

    @Column(columnDefinition = "JSON")
    private String metadata;

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
     * Increment message count
     */
    public void incrementMessages() {
        this.totalMessages++;
    }

    /**
     * Add chat time
     */
    public void addChatTime(int minutes) {
        this.chatTimeMinutes += minutes;
    }

    /**
     * Mark attendance
     */
    public void markAttendance(int streakCount) {
        this.didAttend = true;
        this.streakCount = streakCount;
    }

    /**
     * Record mission completion
     */
    public void recordMissionCompletion(int lobcoinReward) {
        this.missionsCompleted++;
        this.lobcoinEarned += lobcoinReward;
    }

    /**
     * Record demand completion
     */
    public void recordDemandCompletion(int lobcoinReward) {
        this.demandsCompleted++;
        this.lobcoinEarned += lobcoinReward;
    }

    /**
     * Record demand failure
     */
    public void recordDemandFailure(int lobcoinPenalty) {
        this.demandsFailed++;
        this.lobcoinSpent += lobcoinPenalty;
    }

    /**
     * Add LobCoin earned
     */
    public void addLobcoinEarned(int amount) {
        this.lobcoinEarned += amount;
    }

    /**
     * Add LobCoin spent
     */
    public void addLobcoinSpent(int amount) {
        this.lobcoinSpent += amount;
    }

    /**
     * Update scores
     */
    public void updateScores(Integer trustLevel, Integer affinityScore, Integer resilienceScore) {
        if (trustLevel != null) this.trustLevel = trustLevel;
        if (affinityScore != null) this.affinityScore = affinityScore;
        if (resilienceScore != null) this.resilienceScore = resilienceScore;
    }

    /**
     * Calculate net LobCoin change
     */
    public Integer getNetLobcoinChange() {
        return lobcoinEarned - lobcoinSpent;
    }

    /**
     * Calculate engagement score
     */
    public int calculateEngagementScore() {
        int score = 0;

        if (didAttend) score += 20;
        score += Math.min(totalMessages * 2, 30);
        score += Math.min(missionsCompleted * 15, 30);
        score += Math.min(demandsCompleted * 10, 20);

        return Math.min(score, 100);
    }
}
