package com.lobai.repository;

import com.lobai.entity.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    /**
     * Find all monthly reports for a user
     */
    List<MonthlyReport> findByUserIdOrderByReportMonthDesc(Long userId);

    /**
     * Find monthly report by user and month
     */
    Optional<MonthlyReport> findByUserIdAndReportMonth(Long userId, String reportMonth);

    /**
     * Find latest monthly report for a user
     */
    @Query("SELECT m FROM MonthlyReport m WHERE m.user.id = :userId ORDER BY m.reportMonth DESC LIMIT 1")
    Optional<MonthlyReport> findLatestByUserId(@Param("userId") Long userId);

    /**
     * Check if monthly report exists for a user and month
     */
    boolean existsByUserIdAndReportMonth(Long userId, String reportMonth);

    /**
     * Find reports in year
     */
    @Query("SELECT m FROM MonthlyReport m WHERE m.user.id = :userId " +
           "AND m.reportMonth LIKE :year% " +
           "ORDER BY m.reportMonth DESC")
    List<MonthlyReport> findByYear(@Param("userId") Long userId, @Param("year") String year);

    /**
     * Count total reports for a user
     */
    Long countByUserId(Long userId);

    /**
     * Find reports with positive performance
     */
    @Query("SELECT m FROM MonthlyReport m WHERE m.user.id = :userId " +
           "AND m.trustLevelEnd >= m.trustLevelStart " +
           "AND m.affinityScoreEnd >= m.affinityScoreStart " +
           "ORDER BY m.reportMonth DESC")
    List<MonthlyReport> findGoodMonths(@Param("userId") Long userId);

    /**
     * Calculate total LobCoin earned
     */
    @Query("SELECT COALESCE(SUM(m.lobcoinEarned), 0) FROM MonthlyReport m WHERE m.user.id = :userId")
    Integer calculateTotalLobcoinEarned(@Param("userId") Long userId);

    /**
     * Calculate total missions completed
     */
    @Query("SELECT COALESCE(SUM(m.missionsCompleted), 0) FROM MonthlyReport m WHERE m.user.id = :userId")
    Integer calculateTotalMissionsCompleted(@Param("userId") Long userId);
}
