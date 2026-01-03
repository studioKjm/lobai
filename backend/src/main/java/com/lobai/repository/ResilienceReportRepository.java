package com.lobai.repository;

import com.lobai.entity.ResilienceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResilienceReportRepository extends JpaRepository<ResilienceReport, Long> {

    /**
     * 사용자의 모든 리포트 조회 (최신순)
     */
    List<ResilienceReport> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 가장 최근 리포트 조회
     */
    Optional<ResilienceReport> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 잠금 해제된 리포트만 조회
     */
    List<ResilienceReport> findByUserIdAndIsUnlockedTrueOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 총 리포트 수 조회
     */
    Long countByUserId(Long userId);

    /**
     * 사용자의 잠금 해제된 리포트 수 조회
     */
    @Query("SELECT COUNT(r) FROM ResilienceReport r WHERE r.userId = :userId AND r.isUnlocked = true")
    Long countUnlockedReportsByUserId(@Param("userId") Long userId);

    /**
     * 특정 유형의 리포트 조회
     */
    List<ResilienceReport> findByUserIdAndReportTypeOrderByCreatedAtDesc(
            Long userId,
            ResilienceReport.ReportType reportType
    );
}
