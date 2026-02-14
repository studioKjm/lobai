package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_issuances", indexes = {
    @Index(name = "idx_user_status", columnList = "user_id, status"),
    @Index(name = "idx_expires", columnList = "expires_at"),
    @Index(name = "idx_coupon_code", columnList = "coupon_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private PartnerCoupon coupon;

    @Column(name = "coupon_code", length = 50, nullable = false, unique = true)
    private String couponCode;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CouponStatus status = CouponStatus.ISSUED;

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
    }

    public enum CouponStatus {
        ISSUED,
        USED,
        EXPIRED,
        REVOKED
    }

    /**
     * Check if coupon is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Mark coupon as used
     */
    public void use() {
        if (status != CouponStatus.ISSUED) {
            throw new IllegalStateException("사용할 수 없는 쿠폰입니다");
        }
        if (isExpired()) {
            throw new IllegalStateException("만료된 쿠폰입니다");
        }
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }
}
