package com.lobai.repository;

import com.lobai.entity.UserStatsHistory;
import com.lobai.entity.UserStatsHistory.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserStatsHistoryRepository
 *
 * UserStatsHistory 엔티티에 대한 데이터 접근 계층
 */
@Repository
public interface UserStatsHistoryRepository extends JpaRepository<UserStatsHistory, Long> {

    /**
     * 사용자의 스탯 히스토리 조회 (최신순)
     */
    List<UserStatsHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 최근 스탯 히스토리 조회 (제한된 개수)
     */
    List<UserStatsHistory> findTop30ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 특정 행동 유형 히스토리 조회
     */
    List<UserStatsHistory> findByUserIdAndActionTypeOrderByCreatedAtDesc(Long userId, ActionType actionType);

    /**
     * 특정 기간 내 사용자 스탯 히스토리 조회
     */
    @Query("SELECT h FROM UserStatsHistory h WHERE h.user.id = :userId AND h.createdAt BETWEEN :startDate AND :endDate ORDER BY h.createdAt ASC")
    List<UserStatsHistory> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 행동 유형별 개수 조회
     */
    long countByUserIdAndActionType(Long userId, ActionType actionType);

    /**
     * 사용자의 가장 최근 스탯 조회
     */
    @Query("SELECT h FROM UserStatsHistory h WHERE h.user.id = :userId ORDER BY h.createdAt DESC LIMIT 1")
    UserStatsHistory findLatestByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 일일 행동 통계 조회
     */
    @Query("SELECT h.actionType, COUNT(h) FROM UserStatsHistory h WHERE h.user.id = :userId AND h.createdAt >= :since GROUP BY h.actionType")
    List<Object[]> countActionTypesSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
