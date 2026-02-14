package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_missions", indexes = {
    @Index(name = "idx_user_status", columnList = "user_id, status"),
    @Index(name = "idx_mission_status", columnList = "mission_id, status"),
    @Index(name = "idx_completed_at", columnList = "completed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MissionStatus status = MissionStatus.ASSIGNED;

    @Column(name = "progress_data", columnDefinition = "JSON")
    private String progressData;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reward_claimed", nullable = false)
    @Builder.Default
    private Boolean rewardClaimed = false;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

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
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Mark mission as completed
     */
    public void complete() {
        this.status = MissionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Claim reward
     */
    public void claimReward() {
        if (this.status == MissionStatus.COMPLETED && !this.rewardClaimed) {
            this.rewardClaimed = true;
            this.claimedAt = LocalDateTime.now();
        }
    }

    /**
     * Mark as failed
     */
    public void fail() {
        this.status = MissionStatus.FAILED;
    }

    /**
     * Mark as expired
     */
    public void expire() {
        this.status = MissionStatus.EXPIRED;
    }

    /**
     * Check if mission is in progress
     */
    public boolean isInProgress() {
        return status == MissionStatus.IN_PROGRESS || status == MissionStatus.ASSIGNED;
    }

    public enum MissionStatus {
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        EXPIRED
    }
}
