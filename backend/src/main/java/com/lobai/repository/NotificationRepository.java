package com.lobai.repository;

import com.lobai.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all unread notifications for a user
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Find all notifications for a user
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find notifications by type
     */
    List<Notification> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(
        Long userId,
        Notification.NotificationType type
    );

    /**
     * Find notifications by priority
     */
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(
        Long userId,
        Notification.Priority priority
    );

    /**
     * Find notifications that should be sent now
     */
    @Query("SELECT n FROM Notification n WHERE n.sentAt IS NULL " +
           "AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now) " +
           "AND (n.expiresAt IS NULL OR n.expiresAt > :now) " +
           "ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findPendingNotifications(@Param("now") LocalDateTime now);

    /**
     * Find expired notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

    /**
     * Count unread notifications
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnread(@Param("userId") Long userId);

    /**
     * Count unread by priority
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId " +
           "AND n.isRead = false AND n.priority = :priority")
    Long countUnreadByPriority(@Param("userId") Long userId, @Param("priority") Notification.Priority priority);

    /**
     * Mark all as read for a user
     */
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt " +
           "WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Delete old read notifications
     */
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}
