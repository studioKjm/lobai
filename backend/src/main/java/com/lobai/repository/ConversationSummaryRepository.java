package com.lobai.repository;

import com.lobai.entity.ConversationSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConversationSummaryRepository extends JpaRepository<ConversationSummary, Long> {

    /**
     * 최근 요약 N개 조회 (userId + personaId)
     */
    @Query("SELECT cs FROM ConversationSummary cs " +
           "WHERE cs.user.id = :userId AND cs.persona.id = :personaId " +
           "ORDER BY cs.createdAt DESC")
    List<ConversationSummary> findRecentByUserIdAndPersonaId(
            @Param("userId") Long userId,
            @Param("personaId") Long personaId,
            Pageable pageable);

    /**
     * 마지막 요약된 메시지 ID 조회
     */
    @Query("SELECT MAX(cs.messageEndId) FROM ConversationSummary cs " +
           "WHERE cs.user.id = :userId AND cs.persona.id = :personaId")
    Optional<Long> findLastMessageEndId(
            @Param("userId") Long userId,
            @Param("personaId") Long personaId);

    /**
     * 사용자의 전체 요약 수
     */
    long countByUserId(Long userId);

    // ==================== Daily Summary ====================

    /**
     * 중복 체크: 해당 날짜에 DAILY 요약이 이미 존재하는지
     */
    boolean existsByUserIdAndSummaryTypeAndSummaryDate(Long userId,
            ConversationSummary.SummaryType summaryType, LocalDate summaryDate);

    /**
     * 일일 요약 목록 조회 (날짜 DESC)
     */
    @Query("SELECT cs FROM ConversationSummary cs " +
           "WHERE cs.user.id = :userId AND cs.summaryType = 'DAILY' " +
           "ORDER BY cs.summaryDate DESC")
    List<ConversationSummary> findDailySummariesByUserId(
            @Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 날짜의 일일 요약 조회
     */
    @Query("SELECT cs FROM ConversationSummary cs " +
           "WHERE cs.user.id = :userId AND cs.summaryType = 'DAILY' " +
           "AND cs.summaryDate = :date")
    Optional<ConversationSummary> findDailySummaryByUserIdAndDate(
            @Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 기간 내 일일 요약 조회 (보고서용)
     */
    @Query("SELECT cs FROM ConversationSummary cs " +
           "WHERE cs.user.id = :userId AND cs.summaryType = 'DAILY' " +
           "AND cs.summaryDate BETWEEN :start AND :end " +
           "ORDER BY cs.summaryDate ASC")
    List<ConversationSummary> findDailySummariesByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    /**
     * 컨텍스트 조립용: DAILY 요약 우선, 없으면 기존 요약 폴백
     */
    @Query("SELECT cs FROM ConversationSummary cs " +
           "WHERE cs.user.id = :userId " +
           "ORDER BY CASE WHEN cs.summaryType = 'DAILY' THEN 0 ELSE 1 END, " +
           "cs.createdAt DESC")
    List<ConversationSummary> findRecentSummariesPrioritizingDaily(
            @Param("userId") Long userId, Pageable pageable);
}
