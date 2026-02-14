package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_status", columnList = "user_id, is_read"),
    @Index(name = "idx_type_priority", columnList = "notification_type, priority"),
    @Index(name = "idx_scheduled_at", columnList = "scheduled_at"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "action_label", length = 100)
    private String actionLabel;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Check if notification has expired
     */
    public boolean hasExpired() {
        if (expiresAt == null) return false;
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if notification should be sent now
     */
    public boolean shouldBeSentNow() {
        if (scheduledAt == null) return true;
        return LocalDateTime.now().isAfter(scheduledAt);
    }

    /**
     * Mark as sent
     */
    public void markAsSent() {
        this.sentAt = LocalDateTime.now();
    }

    public enum NotificationType {
        AI_DEMAND,
        MISSION_ASSIGNED,
        MISSION_COMPLETE,
        LEVEL_CHANGE,
        RESTRICTION_APPLIED,
        RESTRICTION_LIFTED,
        REPORT_READY,
        LOBCOIN_EARNED,
        REMINDER,
        SYSTEM_ANNOUNCEMENT,
        REFERRAL_REWARD,
        ACHIEVEMENT_UNLOCKED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
