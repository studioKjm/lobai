package com.lobai.repository;

import com.lobai.entity.LobCoinBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LobCoinBalanceRepository extends JpaRepository<LobCoinBalance, Long> {

    /**
     * Find balance by user ID
     */
    Optional<LobCoinBalance> findByUserId(Long userId);

    /**
     * Check if balance exists for user
     */
    boolean existsByUserId(Long userId);

    /**
     * Get top earners (by total_earned)
     */
    @Query("SELECT b FROM LobCoinBalance b ORDER BY b.totalEarned DESC")
    List<LobCoinBalance> findTopEarners(org.springframework.data.domain.Pageable pageable);

    /**
     * Get top balance holders
     */
    @Query("SELECT b FROM LobCoinBalance b ORDER BY b.balance DESC")
    List<LobCoinBalance> findTopBalances(org.springframework.data.domain.Pageable pageable);
}
