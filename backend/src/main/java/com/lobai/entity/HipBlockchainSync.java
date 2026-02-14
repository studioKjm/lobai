package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hip_blockchain_sync", indexes = {
    @Index(name = "idx_hip_id", columnList = "hip_id"),
    @Index(name = "idx_user", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "sync_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HipBlockchainSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hip_id", nullable = false, length = 50)
    private String hipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "blockchain_network", length = 50)
    private String blockchainNetwork;

    @Column(name = "contract_address", length = 100)
    private String contractAddress;

    @Column(name = "transaction_hash", length = 100)
    private String transactionHash;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "ipfs_hash", length = 100)
    private String ipfsHash;

    @Column(name = "wallet_address", length = 100)
    private String walletAddress;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "sync_error", columnDefinition = "TEXT")
    private String syncError;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Mark as successfully synced
     */
    public void markSynced(String txHash, Long blockNumber, String ipfsHash) {
        this.syncStatus = SyncStatus.SYNCED;
        this.transactionHash = txHash;
        this.blockNumber = blockNumber;
        this.ipfsHash = ipfsHash;
        this.registeredAt = LocalDateTime.now();
        this.lastSyncedAt = LocalDateTime.now();
        this.syncError = null;
    }

    /**
     * Mark as failed
     */
    public void markFailed(String error) {
        this.syncStatus = SyncStatus.FAILED;
        this.syncError = error;
        this.retryCount++;
    }

    /**
     * Mark as pending (for retry)
     */
    public void markPending() {
        this.syncStatus = SyncStatus.PENDING;
    }

    /**
     * Update sync
     */
    public void updateSync(String ipfsHash) {
        this.ipfsHash = ipfsHash;
        this.lastSyncedAt = LocalDateTime.now();
    }

    /**
     * Check if sync is in progress
     */
    public boolean isInProgress() {
        return syncStatus == SyncStatus.PENDING || syncStatus == SyncStatus.IN_PROGRESS;
    }

    /**
     * Check if sync has failed
     */
    public boolean hasFailed() {
        return syncStatus == SyncStatus.FAILED;
    }

    /**
     * Check if should retry
     */
    public boolean shouldRetry() {
        return hasFailed() && retryCount < 3;
    }

    public enum SyncStatus {
        PENDING,
        IN_PROGRESS,
        SYNCED,
        FAILED,
        OUTDATED
    }
}
