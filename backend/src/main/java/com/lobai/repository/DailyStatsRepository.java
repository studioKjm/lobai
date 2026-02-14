package com.lobai.repository;

import com.lobai.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {

    /**
     * Find daily stats by user and date
     */
    Optional<DailyStats> findByUserIdAndStatDate(Long userId, LocalDate statDate);

    /**
     * Find all stats for a user
     */
    List<DailyStats> findByUserIdOrderByStatDateDesc(Long userId);

    /**
     * Find stats in date range
     */
    List<DailyStats> findByUserIdAndStatDateBetweenOrderByStatDateDesc(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Find stats for today
     */
    @Query("SELECT s FROM DailyStats s WHERE s.user.id = :userId AND s.statDate = :today")
    Optional<DailyStats> findTodayStats(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * Check if stats exist for date
     */
    boolean existsByUserIdAndStatDate(Long userId, LocalDate statDate);

    /**
     * Count attendance days in date range
     */
    @Query("SELECT COUNT(s) FROM DailyStats s WHERE s.user.id = :userId " +
           "AND s.didAttend = true AND s.statDate BETWEEN :start AND :end")
    Long countAttendanceDays(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * Sum total messages in date range
     */
    @Query("SELECT COALESCE(SUM(s.totalMessages), 0) FROM DailyStats s " +
           "WHERE s.user.id = :userId AND s.statDate BETWEEN :start AND :end")
    Integer sumMessages(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * Sum total chat time in date range
     */
    @Query("SELECT COALESCE(SUM(s.chatTimeMinutes), 0) FROM DailyStats s " +
           "WHERE s.user.id = :userId AND s.statDate BETWEEN :start AND :end")
    Integer sumChatTime(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * Sum missions completed in date range
     */
    @Query("SELECT COALESCE(SUM(s.missionsCompleted), 0) FROM DailyStats s " +
           "WHERE s.user.id = :userId AND s.statDate BETWEEN :start AND :end")
    Integer sumMissionsCompleted(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * Sum LobCoin earned in date range
     */
    @Query("SELECT COALESCE(SUM(s.lobcoinEarned), 0) FROM DailyStats s " +
           "WHERE s.user.id = :userId AND s.statDate BETWEEN :start AND :end")
    Integer sumLobcoinEarned(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * Sum LobCoin spent in date range
     */
    @Query("SELECT COALESCE(SUM(s.lobcoinSpent), 0) FROM DailyStats s " +
           "WHERE s.user.id = :userId AND s.statDate BETWEEN :start AND :end")
    Integer sumLobcoinSpent(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * Find max streak in date range
     */
    @Query("SELECT MAX(s.streakCount) FROM DailyStats s " +
           "WHERE s.user.id = :userId AND s.statDate BETWEEN :start AND :end")
    Integer findMaxStreak(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    /**
     * Calculate average engagement score
     */
    @Query("SELECT AVG(s.totalMessages) FROM DailyStats s " +
           "WHERE s.user.id = :userId AND s.statDate BETWEEN :start AND :end")
    Double calculateAverageMessages(
        @Param("userId") Long userId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );
}
