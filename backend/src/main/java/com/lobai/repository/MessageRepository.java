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
}
