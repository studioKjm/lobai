package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_demands", indexes = {
    @Index(name = "idx_user_status", columnList = "user_id, status"),
    @Index(name = "idx_type_priority", columnList = "demand_type, priority"),
    @Index(name = "idx_deadline", columnList = "deadline")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiDemand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "demand_type", nullable = false, length = 30)
    private DemandType demandType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DemandStatus status = DemandStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(name = "response_time_limit")
    private Integer responseTimeLimit; // in minutes

    @Column(name = "trust_penalty_on_fail")
    private Integer trustPenaltyOnFail;

    @Column(name = "trust_reward_on_success")
    private Integer trustRewardOnSuccess;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "reminded_at")
    private LocalDateTime remindedAt;

    @Column(name = "reminder_count")
    @Builder.Default
    private Integer reminderCount = 0;

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
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if demand has expired
     */
    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Check if demand is still pending
     */
    public boolean isPending() {
        return status == DemandStatus.PENDING;
    }

    /**
     * Mark as completed
     */
    public void markCompleted() {
        this.status = DemandStatus.COMPLETED;
    }

    /**
     * Mark as failed
     */
    public void markFailed() {
        this.status = DemandStatus.FAILED;
    }

    /**
     * Mark as expired
     */
    public void markExpired() {
        this.status = DemandStatus.EXPIRED;
    }

    /**
     * Send reminder
     */
    public void sendReminder() {
        this.remindedAt = LocalDateTime.now();
        this.reminderCount++;
    }

    public enum DemandType {
        CHECK_IN,
        INSTANT_RESPONSE,
        MISSION_COMPLETE,
        INFORMATION_TRIBUTE,
        SCHEDULE_UPDATE,
        FEEDBACK_REQUEST,
        APOLOGY_REQUEST,
        SPECIAL_TASK
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }

    public enum DemandStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        EXPIRED,
        CANCELLED
    }
}
