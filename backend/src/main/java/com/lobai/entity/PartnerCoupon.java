package com.lobai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner_coupons", indexes = {
    @Index(name = "idx_partner", columnList = "partner_name"),
    @Index(name = "idx_active_cost", columnList = "is_active, cost_lobcoin"),
    @Index(name = "idx_display_order", columnList = "display_order, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_name", length = 100, nullable = false)
    private String partnerName;

    @Column(name = "coupon_type", length = 50, nullable = false)
    private String couponType;

    @Column(name = "cost_lobcoin", nullable = false)
    private Integer costLobcoin;

    @Column(name = "real_value_usd", precision = 10, scale = 2)
    private BigDecimal realValueUsd;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column
    private Integer stock;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

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
     * Check if coupon is available for purchase
     */
    public boolean isAvailable() {
        return isActive && (stock == null || stock > 0);
    }

    /**
     * Decrease stock by 1
     */
    public void decreaseStock() {
        if (stock != null) {
            if (stock <= 0) {
                throw new IllegalStateException("재고가 부족합니다");
            }
            stock--;
        }
    }
}
