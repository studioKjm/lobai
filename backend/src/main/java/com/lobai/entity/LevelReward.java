package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "level_rewards", indexes = {
    @Index(name = "idx_user_level", columnList = "user_id, level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LevelReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "reward_type", length = 50, nullable = false)
    private String rewardType;

    @Column(name = "reward_data", columnDefinition = "JSON")
    private String rewardData;

    @Column(name = "claimed_at", nullable = false, updatable = false)
    private LocalDateTime claimedAt;

    @PrePersist
    protected void onCreate() {
        claimedAt = LocalDateTime.now();
    }

    /**
     * Reward Types:
     * LOBCOIN - LobCoin 보상
     * COUPON - 파트너 쿠폰
     * BADGE - 배지/업적
     * SUBSCRIPTION_DISCOUNT - 구독 할인
     */
}
