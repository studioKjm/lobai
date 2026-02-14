package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lobcoin_transactions", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_type_source", columnList = "type, source"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LobCoinTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LobCoinType type;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(length = 255)
    private String description;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum LobCoinType {
        EARN,
        SPEND
    }

    /**
     * LobCoin Source Types
     * EARN: CHECK_IN, MISSION_COMPLETE, PERFECT_REPORT, LEVEL_UP, REFERRAL, WELCOME_BONUS
     * SPEND: FAVOR_BOOST, TRUST_RECOVERY, MISSION_SKIP, SUBSCRIPTION_DISCOUNT, PARTNER_COUPON, CASHOUT
     */
}
