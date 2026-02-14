package com.lobai.repository;

import com.lobai.entity.Schedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * Find schedules by user ID and date range (not deleted)
     */
    List<Schedule> findByUserIdAndStartTimeBetweenAndIsDeletedFalse(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * Find schedules by user ID, type, and date range (not deleted)
     */
    List<Schedule> findByUserIdAndTypeAndStartTimeBetweenAndIsDeletedFalse(
        Long userId,
        Schedule.ScheduleType type,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * Find upcoming schedules (not deleted, ordered by start time)
     */
    @Query("SELECT s FROM Schedule s WHERE s.user.id = :userId AND s.startTime >= :now " +
           "AND s.isDeleted = false ORDER BY s.startTime ASC")
    List<Schedule> findUpcomingSchedules(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    /**
     * Count schedules by user ID and type (not deleted)
     */
    long countByUserIdAndTypeAndIsDeletedFalse(Long userId, Schedule.ScheduleType type);

    /**
     * Find all schedules by user ID (not deleted)
     */
    List<Schedule> findByUserIdAndIsDeletedFalse(Long userId);
}
