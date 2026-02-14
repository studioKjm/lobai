package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_settings", indexes = {
    @Index(name = "idx_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private Boolean pushEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default
    private Boolean inAppEnabled = true;

    @Column(name = "ai_demand_notifications", nullable = false)
    @Builder.Default
    private Boolean aiDemandNotifications = true;

    @Column(name = "mission_notifications", nullable = false)
    @Builder.Default
    private Boolean missionNotifications = true;

    @Column(name = "level_change_notifications", nullable = false)
    @Builder.Default
    private Boolean levelChangeNotifications = true;

    @Column(name = "report_notifications", nullable = false)
    @Builder.Default
    private Boolean reportNotifications = true;

    @Column(name = "lobcoin_notifications", nullable = false)
    @Builder.Default
    private Boolean lobcoinNotifications = true;

    @Column(name = "reminder_notifications", nullable = false)
    @Builder.Default
    private Boolean reminderNotifications = true;

    @Column(name = "achievement_notifications", nullable = false)
    @Builder.Default
    private Boolean achievementNotifications = true;

    @Column(name = "quiet_hours_start", length = 5)
    private String quietHoursStart; // Format: "HH:mm"

    @Column(name = "quiet_hours_end", length = 5)
    private String quietHoursEnd; // Format: "HH:mm"

    @Column(name = "minimum_priority", length = 20)
    @Builder.Default
    private String minimumPriority = "LOW";

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
     * Check if user wants notifications for a specific type
     */
    public boolean wantsNotification(Notification.NotificationType type) {
        return switch (type) {
            case AI_DEMAND, REMINDER -> aiDemandNotifications;
            case MISSION_ASSIGNED, MISSION_COMPLETE -> missionNotifications;
            case LEVEL_CHANGE, RESTRICTION_APPLIED, RESTRICTION_LIFTED -> levelChangeNotifications;
            case REPORT_READY -> reportNotifications;
            case LOBCOIN_EARNED, REFERRAL_REWARD -> lobcoinNotifications;
            case ACHIEVEMENT_UNLOCKED -> achievementNotifications;
            case SYSTEM_ANNOUNCEMENT -> true; // Always send system announcements
        };
    }

    /**
     * Check if notification should be sent based on priority
     */
    public boolean shouldSendByPriority(Notification.Priority priority) {
        int minPriorityLevel = getPriorityLevel(minimumPriority);
        int notificationPriorityLevel = getPriorityLevel(priority.name());
        return notificationPriorityLevel >= minPriorityLevel;
    }

    /**
     * Check if current time is within quiet hours
     */
    public boolean isQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        int currentHour = now.getHour();
        int currentMinute = now.getMinute();

        String[] startParts = quietHoursStart.split(":");
        int startHour = Integer.parseInt(startParts[0]);
        int startMinute = Integer.parseInt(startParts[1]);

        String[] endParts = quietHoursEnd.split(":");
        int endHour = Integer.parseInt(endParts[0]);
        int endMinute = Integer.parseInt(endParts[1]);

        int currentMinutes = currentHour * 60 + currentMinute;
        int startMinutes = startHour * 60 + startMinute;
        int endMinutes = endHour * 60 + endMinute;

        if (startMinutes < endMinutes) {
            return currentMinutes >= startMinutes && currentMinutes < endMinutes;
        } else {
            // Quiet hours span midnight
            return currentMinutes >= startMinutes || currentMinutes < endMinutes;
        }
    }

    private int getPriorityLevel(String priority) {
        return switch (priority.toUpperCase()) {
            case "LOW" -> 0;
            case "MEDIUM" -> 1;
            case "HIGH" -> 2;
            case "URGENT" -> 3;
            default -> 0;
        };
    }
}
