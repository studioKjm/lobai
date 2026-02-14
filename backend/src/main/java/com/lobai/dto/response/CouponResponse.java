package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {

    private Long id;
    private String partnerName;
    private String couponType;
    private Integer costLobcoin;
    private BigDecimal realValueUsd;
    private String title;
    private String description;
    private String terms;
    private String imageUrl;
    private Boolean isActive;
    private Integer stock;
    private Boolean isAffordable;  // Can user afford this coupon?
    private Boolean isAvailable;   // Is coupon available for purchase?
}
