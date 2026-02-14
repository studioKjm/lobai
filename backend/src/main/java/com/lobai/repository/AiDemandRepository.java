package com.lobai.repository;

import com.lobai.entity.AiDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiDemandRepository extends JpaRepository<AiDemand, Long> {

    /**
     * Find all pending demands for a user
     */
    List<AiDemand> findByUserIdAndStatusOrderByDeadlineAsc(
        Long userId,
        AiDemand.DemandStatus status
    );

    /**
     * Find all active demands for a user (pending or in-progress)
     */
    @Query("SELECT d FROM AiDemand d WHERE d.user.id = :userId " +
           "AND (d.status = 'PENDING' OR d.status = 'IN_PROGRESS') " +
           "ORDER BY d.deadline ASC")
    List<AiDemand> findActiveDemands(@Param("userId") Long userId);

    /**
     * Find demands by type
     */
    List<AiDemand> findByUserIdAndDemandTypeOrderByDeadlineAsc(
        Long userId,
        AiDemand.DemandType type
    );

    /**
     * Find demands by priority
     */
    List<AiDemand> findByUserIdAndPriorityOrderByDeadlineAsc(
        Long userId,
        AiDemand.Priority priority
    );

    /**
     * Find expired pending demands
     */
    @Query("SELECT d FROM AiDemand d WHERE d.status = 'PENDING' AND d.deadline < :now")
    List<AiDemand> findExpiredPendingDemands(@Param("now") LocalDateTime now);

    /**
     * Find demands that need reminder
     */
    @Query("SELECT d FROM AiDemand d WHERE d.status = 'PENDING' " +
           "AND d.deadline > :now " +
           "AND d.deadline < :reminderTime " +
           "AND (d.remindedAt IS NULL OR d.remindedAt < :lastReminderCutoff)")
    List<AiDemand> findDemandsNeedingReminder(
        @Param("now") LocalDateTime now,
        @Param("reminderTime") LocalDateTime reminderTime,
        @Param("lastReminderCutoff") LocalDateTime lastReminderCutoff
    );

    /**
     * Count active demands by user
     */
    @Query("SELECT COUNT(d) FROM AiDemand d WHERE d.user.id = :userId " +
           "AND (d.status = 'PENDING' OR d.status = 'IN_PROGRESS')")
    Long countActiveDemands(@Param("userId") Long userId);

    /**
     * Count completed demands in date range
     */
    @Query("SELECT COUNT(d) FROM AiDemand d WHERE d.user.id = :userId " +
           "AND d.status = 'COMPLETED' AND d.updatedAt BETWEEN :start AND :end")
    Long countCompletedInRange(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * Count failed demands in date range
     */
    @Query("SELECT COUNT(d) FROM AiDemand d WHERE d.user.id = :userId " +
           "AND d.status = 'FAILED' AND d.updatedAt BETWEEN :start AND :end")
    Long countFailedInRange(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
