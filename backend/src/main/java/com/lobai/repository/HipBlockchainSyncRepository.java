package com.lobai.repository;

import com.lobai.entity.HipBlockchainSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HipBlockchainSyncRepository extends JpaRepository<HipBlockchainSync, Long> {

    /**
     * Find sync record by HIP ID
     */
    Optional<HipBlockchainSync> findByHipId(String hipId);

    /**
     * Find sync record by user ID
     */
    Optional<HipBlockchainSync> findByUserId(Long userId);

    /**
     * Find sync record by wallet address
     */
    Optional<HipBlockchainSync> findByWalletAddress(String walletAddress);

    /**
     * Find sync record by transaction hash
     */
    Optional<HipBlockchainSync> findByTransactionHash(String transactionHash);

    /**
     * Find all syncs by status
     */
    List<HipBlockchainSync> findBySyncStatusOrderByCreatedAtDesc(HipBlockchainSync.SyncStatus status);

    /**
     * Find failed syncs that should retry
     */
    @Query("SELECT h FROM HipBlockchainSync h WHERE h.syncStatus = 'FAILED' AND h.retryCount < 3")
    List<HipBlockchainSync> findFailedSyncsForRetry();

    /**
     * Find outdated syncs (need re-sync)
     */
    List<HipBlockchainSync> findBySyncStatusOrderByLastSyncedAtAsc(HipBlockchainSync.SyncStatus status);

    /**
     * Check if HIP ID is already registered
     */
    boolean existsByHipId(String hipId);

    /**
     * Check if wallet address is already used
     */
    boolean existsByWalletAddress(String walletAddress);

    /**
     * Count synced identities
     */
    @Query("SELECT COUNT(h) FROM HipBlockchainSync h WHERE h.syncStatus = 'SYNCED'")
    Long countSynced();

    /**
     * Count pending syncs
     */
    @Query("SELECT COUNT(h) FROM HipBlockchainSync h WHERE h.syncStatus = 'PENDING'")
    Long countPending();

    /**
     * Count failed syncs
     */
    @Query("SELECT COUNT(h) FROM HipBlockchainSync h WHERE h.syncStatus = 'FAILED'")
    Long countFailed();
}
