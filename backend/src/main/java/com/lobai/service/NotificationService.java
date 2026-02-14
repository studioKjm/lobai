package com.lobai.service;

import com.lobai.entity.Notification;
import com.lobai.entity.NotificationSettings;
import com.lobai.entity.User;
import com.lobai.repository.NotificationRepository;
import com.lobai.repository.NotificationSettingsRepository;
import com.lobai.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationService
 * 알림 발송 및 관리 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    /**
     * Send notification to user
     */
    @Transactional
    public Notification sendNotification(
        Long userId,
        Notification.NotificationType type,
        String title,
        String message,
        Notification.Priority priority
    ) {
        // Check user settings
        NotificationSettings settings = getOrCreateSettings(userId);

        if (!settings.wantsNotification(type)) {
            log.debug("User {} has disabled {} notifications", userId, type);
            return null;
        }

        if (!settings.shouldSendByPriority(priority)) {
            log.debug("Notification priority {} below user {} minimum threshold", priority, userId);
            return null;
        }

        if (settings.isQuietHours()) {
            log.debug("User {} is in quiet hours, scheduling notification", userId);
            // Schedule for later
            return scheduleNotification(userId, type, title, message, priority, null);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Notification notification = Notification.builder()
            .user(user)
            .notificationType(type)
            .title(title)
            .message(message)
            .priority(priority)
            .build();

        notification.markAsSent();
        Notification saved = notificationRepository.save(notification);

        log.info("Notification sent to user {}: {} - {}", userId, type, title);
        return saved;
    }

    /**
     * Schedule notification for later
     */
    @Transactional
    public Notification scheduleNotification(
        Long userId,
        Notification.NotificationType type,
        String title,
        String message,
        Notification.Priority priority,
        LocalDateTime scheduledAt
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Notification notification = Notification.builder()
            .user(user)
            .notificationType(type)
            .title(title)
            .message(message)
            .priority(priority)
            .scheduledAt(scheduledAt != null ? scheduledAt : LocalDateTime.now().plusHours(1))
            .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification scheduled for user {}: {} at {}", userId, type, notification.getScheduledAt());
        return saved;
    }

    /**
     * Send level change notification
     */
    @Transactional
    public void sendLevelChangeNotification(Long userId, int oldLevel, int newLevel) {
        String title = newLevel > oldLevel ? "레벨 상승!" : "레벨 변동";
        String message = String.format("레벨이 %d에서 %d로 변경되었습니다.", oldLevel, newLevel);

        sendNotification(userId, Notification.NotificationType.LEVEL_CHANGE, title, message,
            Notification.Priority.HIGH);
    }

    /**
     * Send mission assigned notification
     */
    @Transactional
    public void sendMissionAssignedNotification(Long userId, String missionTitle) {
        String title = "새로운 미션";
        String message = String.format("'%s' 미션이 할당되었습니다!", missionTitle);

        sendNotification(userId, Notification.NotificationType.MISSION_ASSIGNED, title, message,
            Notification.Priority.MEDIUM);
    }

    /**
     * Send mission completed notification
     */
    @Transactional
    public void sendMissionCompletedNotification(Long userId, String missionTitle, int reward) {
        String title = "미션 완료!";
        String message = String.format("'%s' 미션을 완료하여 %d LobCoin을 획득했습니다!", missionTitle, reward);

        sendNotification(userId, Notification.NotificationType.MISSION_COMPLETE, title, message,
            Notification.Priority.HIGH);
    }

    /**
     * Send AI demand notification
     */
    @Transactional
    public void sendAiDemandNotification(Long userId, String demandTitle, LocalDateTime deadline) {
        String title = "AI 요구사항";
        String message = String.format("'%s' - 기한: %s", demandTitle, deadline);

        sendNotification(userId, Notification.NotificationType.AI_DEMAND, title, message,
            Notification.Priority.URGENT);
    }

    /**
     * Send restriction notification
     */
    @Transactional
    public void sendRestrictionNotification(Long userId, String reason) {
        String title = "제재 적용";
        String message = String.format("제재가 적용되었습니다: %s", reason);

        sendNotification(userId, Notification.NotificationType.RESTRICTION_APPLIED, title, message,
            Notification.Priority.URGENT);
    }

    /**
     * Send report ready notification
     */
    @Transactional
    public void sendReportReadyNotification(Long userId, String reportType) {
        String title = "보고서 생성 완료";
        String message = String.format("%s 보고서가 준비되었습니다!", reportType);

        sendNotification(userId, Notification.NotificationType.REPORT_READY, title, message,
            Notification.Priority.MEDIUM);
    }

    /**
     * Get unread notifications for user
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * Get all notifications for user
     */
    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다"));

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    /**
     * Get notification settings for user
     */
    @Transactional(readOnly = true)
    public NotificationSettings getSettings(Long userId) {
        return getOrCreateSettings(userId);
    }

    /**
     * Update notification settings
     */
    @Transactional
    public NotificationSettings updateSettings(Long userId, NotificationSettings newSettings) {
        NotificationSettings settings = getOrCreateSettings(userId);

        // Update settings
        settings.setEmailEnabled(newSettings.getEmailEnabled());
        settings.setPushEnabled(newSettings.getPushEnabled());
        settings.setInAppEnabled(newSettings.getInAppEnabled());
        settings.setAiDemandNotifications(newSettings.getAiDemandNotifications());
        settings.setMissionNotifications(newSettings.getMissionNotifications());
        settings.setLevelChangeNotifications(newSettings.getLevelChangeNotifications());
        settings.setReportNotifications(newSettings.getReportNotifications());
        settings.setLobcoinNotifications(newSettings.getLobcoinNotifications());
        settings.setReminderNotifications(newSettings.getReminderNotifications());
        settings.setAchievementNotifications(newSettings.getAchievementNotifications());
        settings.setQuietHoursStart(newSettings.getQuietHoursStart());
        settings.setQuietHoursEnd(newSettings.getQuietHoursEnd());
        settings.setMinimumPriority(newSettings.getMinimumPriority());

        return settingsRepository.save(settings);
    }

    /**
     * Process pending notifications (scheduled for sending)
     */
    @Transactional
    public void processPendingNotifications() {
        List<Notification> pending = notificationRepository.findPendingNotifications(LocalDateTime.now());

        for (Notification notification : pending) {
            notification.markAsSent();
            notificationRepository.save(notification);
            log.info("Sent scheduled notification: {}", notification.getTitle());
        }
    }

    /**
     * Get or create notification settings
     */
    private NotificationSettings getOrCreateSettings(Long userId) {
        return settingsRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultSettings(userId));
    }

    /**
     * Create default notification settings for user
     */
    private NotificationSettings createDefaultSettings(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        NotificationSettings settings = NotificationSettings.builder()
            .user(user)
            .build();

        return settingsRepository.save(settings);
    }
}
