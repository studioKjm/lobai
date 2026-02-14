package com.lobai.repository;

import com.lobai.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * Find all sessions for a user
     */
    List<ChatSession> findByUserIdOrderByStartedAtDesc(Long userId);

    /**
     * Find active session for a user
     */
    @Query("SELECT s FROM ChatSession s WHERE s.user.id = :userId AND s.isActive = true " +
           "ORDER BY s.startedAt DESC LIMIT 1")
    Optional<ChatSession> findActiveSession(@Param("userId") Long userId);

    /**
     * Find all active sessions for a user
     */
    List<ChatSession> findByUserIdAndIsActiveTrueOrderByStartedAtDesc(Long userId);

    /**
     * Find sessions by persona
     */
    List<ChatSession> findByUserIdAndPersonaIdOrderByStartedAtDesc(Long userId, Long personaId);

    /**
     * Find sessions in date range
     */
    List<ChatSession> findByUserIdAndStartedAtBetweenOrderByStartedAtDesc(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * Find expired active sessions
     */
    @Query("SELECT s FROM ChatSession s WHERE s.isActive = true " +
           "AND s.lastActivityAt < :cutoffTime")
    List<ChatSession> findExpiredActiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count total sessions for a user
     */
    Long countByUserId(Long userId);

    /**
     * Count active sessions
     */
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.user.id = :userId AND s.isActive = true")
    Long countActiveSessions(@Param("userId") Long userId);

    /**
     * Sum total messages for a user
     */
    @Query("SELECT COALESCE(SUM(s.messageCount), 0) FROM ChatSession s WHERE s.user.id = :userId")
    Integer sumTotalMessages(@Param("userId") Long userId);

    /**
     * Sum total chat time for a user
     */
    @Query("SELECT COALESCE(SUM(s.durationMinutes), 0) FROM ChatSession s WHERE s.user.id = :userId")
    Integer sumTotalChatTime(@Param("userId") Long userId);

    /**
     * Calculate average session duration
     */
    @Query("SELECT AVG(s.durationMinutes) FROM ChatSession s " +
           "WHERE s.user.id = :userId AND s.isActive = false")
    Double calculateAverageSessionDuration(@Param("userId") Long userId);

    /**
     * Find most recent N sessions
     */
    @Query("SELECT s FROM ChatSession s WHERE s.user.id = :userId " +
           "ORDER BY s.startedAt DESC LIMIT :limit")
    List<ChatSession> findRecentSessions(@Param("userId") Long userId, @Param("limit") int limit);
}
