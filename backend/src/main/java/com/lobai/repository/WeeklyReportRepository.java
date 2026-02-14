package com.lobai.repository;

import com.lobai.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    /**
     * Find all weekly reports for a user
     */
    List<WeeklyReport> findByUserIdOrderByWeekStartDateDesc(Long userId);

    /**
     * Find weekly report by user and week start date
     */
    Optional<WeeklyReport> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);

    /**
     * Find latest weekly report for a user
     */
    @Query("SELECT w FROM WeeklyReport w WHERE w.user.id = :userId ORDER BY w.weekStartDate DESC LIMIT 1")
    Optional<WeeklyReport> findLatestByUserId(@Param("userId") Long userId);

    /**
     * Check if weekly report exists for a user and week
     */
    boolean existsByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);

    /**
     * Find reports in date range
     */
    List<WeeklyReport> findByUserIdAndWeekStartDateBetweenOrderByWeekStartDateDesc(
        Long userId,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Count total reports for a user
     */
    Long countByUserId(Long userId);

    /**
     * Find reports with positive trust change
     */
    @Query("SELECT w FROM WeeklyReport w WHERE w.user.id = :userId " +
           "AND w.trustLevelEnd > w.trustLevelStart " +
           "ORDER BY w.weekStartDate DESC")
    List<WeeklyReport> findGoodWeeks(@Param("userId") Long userId);

    /**
     * Calculate average engagement score
     */
    @Query("SELECT AVG(w.attendanceDays) FROM WeeklyReport w WHERE w.user.id = :userId")
    Double calculateAverageAttendance(@Param("userId") Long userId);
}
