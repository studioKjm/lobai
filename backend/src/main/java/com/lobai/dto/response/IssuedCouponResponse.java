package com.lobai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuedCouponResponse {

    private Long id;
    private String couponCode;
    private String couponTitle;
    private String partnerName;
    private String status;
    private String issuedAt;
    private String usedAt;
    private String expiresAt;
}
