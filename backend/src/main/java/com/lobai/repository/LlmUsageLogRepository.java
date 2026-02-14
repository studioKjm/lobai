package com.lobai.repository;

import com.lobai.entity.LlmUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LlmUsageLogRepository extends JpaRepository<LlmUsageLog, Long> {

    /**
     * 사용자의 일일 토큰 사용량 합계
     */
    @Query("SELECT COALESCE(SUM(l.totalTokens), 0) FROM LlmUsageLog l " +
           "WHERE l.user.id = :userId AND l.createdAt >= :startOfDay")
    int sumDailyTokensByUserId(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay);

    /**
     * Provider별 사용량 합계
     */
    @Query("SELECT COALESCE(SUM(l.totalTokens), 0) FROM LlmUsageLog l " +
           "WHERE l.providerName = :provider AND l.createdAt >= :since")
    int sumTokensByProvider(
            @Param("provider") String provider,
            @Param("since") LocalDateTime since);
}
