package com.lobai.repository;

import com.lobai.entity.IdentityMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IdentityMetricsRepository
 */
@Repository
public interface IdentityMetricsRepository extends JpaRepository<IdentityMetrics, Long> {

    /**
     * HIP ID로 모든 메트릭 조회 (최신순)
     */
    List<IdentityMetrics> findByHipIdOrderByMeasuredAtDesc(String hipId);

    /**
     * HIP ID로 최신 메트릭 조회
     */
    Optional<IdentityMetrics> findFirstByHipIdOrderByMeasuredAtDesc(String hipId);

    /**
     * HIP ID와 기간으로 메트릭 조회
     */
    @Query("SELECT m FROM IdentityMetrics m " +
           "WHERE m.hipId = :hipId " +
           "AND m.measuredAt BETWEEN :startDate AND :endDate " +
           "ORDER BY m.measuredAt DESC")
    List<IdentityMetrics> findByHipIdAndDateRange(
        @Param("hipId") String hipId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * HIP ID로 메트릭 수 카운트
     */
    long countByHipId(String hipId);
}
