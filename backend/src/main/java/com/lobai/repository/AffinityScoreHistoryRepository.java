package com.lobai.repository;

import com.lobai.entity.AffinityScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffinityScoreHistoryRepository extends JpaRepository<AffinityScoreHistory, Long> {

    /**
     * 사용자의 특정 날짜 스냅샷 조회
     */
    Optional<AffinityScoreHistory> findByUserIdAndSnapshotDate(Long userId, LocalDate snapshotDate);

    /**
     * 사용자의 최근 N일 스냅샷 조회 (트렌드)
     */
    @Query("SELECT h FROM AffinityScoreHistory h " +
           "WHERE h.userId = :userId AND h.snapshotDate >= :since " +
           "ORDER BY h.snapshotDate ASC")
    List<AffinityScoreHistory> findRecentHistory(@Param("userId") Long userId, @Param("since") LocalDate since);

    /**
     * 사용자의 가장 최근 스냅샷 조회
     */
    Optional<AffinityScoreHistory> findTopByUserIdOrderBySnapshotDateDesc(Long userId);
}
