package com.lobai.repository;

import com.lobai.entity.ConversationSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
