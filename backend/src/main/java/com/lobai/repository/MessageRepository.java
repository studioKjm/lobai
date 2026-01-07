package com.lobai.repository;

import com.lobai.entity.Message;
import com.lobai.entity.Message.MessageRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MessageRepository
 *
 * Message 엔티티에 대한 데이터 접근 계층
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 사용자의 메시지 목록 조회 (최신순, 페이징)
     */
    Page<Message> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자의 최근 메시지 조회 (제한된 개수)
     */
    List<Message> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자와 페르소나별 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND m.persona.id = :personaId ORDER BY m.createdAt DESC")
    List<Message> findByUserAndPersona(@Param("userId") Long userId, @Param("personaId") Long personaId);

    /**
     * 특정 기간 내 사용자 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND m.createdAt BETWEEN :startDate AND :endDate ORDER BY m.createdAt ASC")
    List<Message> findByUserIdAndDateRange(@Param("userId") Long userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 사용자의 메시지 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 사용자의 역할별 메시지 개수 조회
     */
    long countByUserIdAndRole(Long userId, MessageRole role);

    /**
     * 특정 페르소나의 메시지 개수 조회
     */
    long countByPersonaId(Long personaId);

    /**
     * 사용자의 가장 최근 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId ORDER BY m.createdAt DESC LIMIT 1")
    Message findLatestMessageByUserId(@Param("userId") Long userId);

    // ==================== Admin Statistics ====================

    /**
     * 특정 시간 이후 생성된 메시지 수
     */
    long countByCreatedAtAfter(LocalDateTime since);

    /**
     * 역할별 메시지 수
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.role = :role")
    long countByRole(@Param("role") MessageRole role);

    /**
     * 특정 역할의 특정 기간 메시지 수
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.role = :role AND m.createdAt BETWEEN :start AND :end")
    long countByRoleAndCreatedAtBetween(@Param("role") MessageRole role,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    /**
     * 가장 인기있는 페르소나 조회 (persona_id, name, message_count)
     * Object[] = {personaId, personaName, messageCount}
     */
    @Query(value = "SELECT m.persona_id, p.name, COUNT(*) as cnt " +
                   "FROM messages m " +
                   "LEFT JOIN personas p ON m.persona_id = p.id " +
                   "WHERE m.persona_id IS NOT NULL " +
                   "GROUP BY m.persona_id, p.name " +
                   "ORDER BY cnt DESC " +
                   "LIMIT 1",
           nativeQuery = true)
    Optional<Object[]> findMostPopularPersona();

    // ==================== Affinity Score Calculations ====================

    /**
     * 특정 기간 이후 사용자 메시지 수
     */
    long countByUserIdAndRoleAndCreatedAtAfter(Long userId, MessageRole role, LocalDateTime since);

    /**
     * 사용자가 사용한 고유 페르소나 수
     */
    @Query("SELECT COUNT(DISTINCT m.persona.id) FROM Message m WHERE m.user.id = :userId")
    long countUniquePersonasByUser(@Param("userId") Long userId);

    /**
     * 사용자 메시지의 평균 길이
     */
    @Query("SELECT AVG(LENGTH(m.content)) FROM Message m WHERE m.user.id = :userId AND m.role = :role")
    Double getAverageMessageLengthByUser(@Param("userId") Long userId, @Param("role") MessageRole role);

    /**
     * 최근 N개의 사용자 메시지 조회 (점수 계산용)
     */
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND m.role = 'user' ORDER BY m.createdAt DESC")
    List<Message> findRecentUserMessages(@Param("userId") Long userId, Pageable pageable);

    /**
     * 분석되지 않은 사용자 메시지 조회
     */
    @Query("SELECT m FROM Message m WHERE m.user.id = :userId AND m.role = 'user' AND (m.isAnalyzed IS NULL OR m.isAnalyzed = FALSE) ORDER BY m.createdAt ASC")
    List<Message> findUnanalyzedUserMessages(@Param("userId") Long userId);

    /**
     * 사용자의 평균 sentiment 점수
     */
    @Query("SELECT AVG(m.sentimentScore) FROM Message m WHERE m.user.id = :userId AND m.role = 'user' AND m.sentimentScore IS NOT NULL")
    Double getAverageSentimentByUser(@Param("userId") Long userId);

    /**
     * 사용자의 평균 clarity 점수
     */
    @Query("SELECT AVG(m.clarityScore) FROM Message m WHERE m.user.id = :userId AND m.role = 'user' AND m.clarityScore IS NOT NULL")
    Double getAverageClarityByUser(@Param("userId") Long userId);

    /**
     * 사용자의 평균 context 점수
     */
    @Query("SELECT AVG(m.contextScore) FROM Message m WHERE m.user.id = :userId AND m.role = 'user' AND m.contextScore IS NOT NULL")
    Double getAverageContextByUser(@Param("userId") Long userId);

    /**
     * 사용자의 평균 usage 점수
     */
    @Query("SELECT AVG(m.usageScore) FROM Message m WHERE m.user.id = :userId AND m.role = 'user' AND m.usageScore IS NOT NULL")
    Double getAverageUsageByUser(@Param("userId") Long userId);

    /**
     * 사용자의 모든 메시지 삭제
     */
    void deleteByUserId(Long userId);
}
