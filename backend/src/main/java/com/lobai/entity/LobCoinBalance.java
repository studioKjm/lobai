package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lobcoin_balances", indexes = {
    @Index(name = "idx_balance", columnList = "balance"),
    @Index(name = "idx_total_earned", columnList = "total_earned")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LobCoinBalance {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Builder.Default
    private Integer balance = 0;

    @Column(name = "total_earned", nullable = false)
    @Builder.Default
    private Integer totalEarned = 0;

    @Column(name = "total_spent", nullable = false)
    @Builder.Default
    private Integer totalSpent = 0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Validate that balance is non-negative
     */
    public void validateBalance() {
        if (balance < 0) {
            throw new IllegalArgumentException("잔액은 음수가 될 수 없습니다");
        }
    }

    /**
     * Add LobCoins to balance
     */
    public void earn(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("획득량은 양수여야 합니다");
        }
        this.balance += amount;
        this.totalEarned += amount;
    }

    /**
     * Deduct LobCoins from balance
     */
    public void spend(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용량은 양수여야 합니다");
        }
        if (this.balance < amount) {
            throw new IllegalArgumentException("잔액이 부족합니다");
        }
        this.balance -= amount;
        this.totalSpent += amount;
    }
}
