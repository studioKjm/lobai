package com.lobai.repository;

import com.lobai.entity.LobCoinTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LobCoinTransactionRepository extends JpaRepository<LobCoinTransaction, Long> {

    /**
     * Find transactions by user ID (ordered by created_at DESC)
     */
    List<LobCoinTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find transactions by user ID with limit
     */
    @Query("SELECT t FROM LobCoinTransaction t WHERE t.user.id = :userId ORDER BY t.createdAt DESC")
    List<LobCoinTransaction> findRecentTransactions(
        @Param("userId") Long userId,
        Pageable pageable
    );

    /**
     * Find transactions by user ID and type
     */
    List<LobCoinTransaction> findByUserIdAndTypeOrderByCreatedAtDesc(
        Long userId,
        LobCoinTransaction.LobCoinType type
    );

    /**
     * Find transactions by user ID and source
     */
    List<LobCoinTransaction> findByUserIdAndSourceOrderByCreatedAtDesc(
        Long userId,
        String source
    );

    /**
     * Find transactions by user ID and date range
     */
    List<LobCoinTransaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId,
        LocalDateTime start,
        LocalDateTime end
    );

    /**
     * Count total earned by user
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM LobCoinTransaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EARN'")
    Integer sumEarnedByUserId(@Param("userId") Long userId);

    /**
     * Count total spent by user
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM LobCoinTransaction t " +
           "WHERE t.user.id = :userId AND t.type = 'SPEND'")
    Integer sumSpentByUserId(@Param("userId") Long userId);
}
