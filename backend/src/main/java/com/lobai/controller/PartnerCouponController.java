package com.lobai.controller;

import com.lobai.dto.response.ApiResponse;
import com.lobai.dto.response.CouponResponse;
import com.lobai.dto.response.IssuedCouponResponse;
import com.lobai.service.PartnerCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PartnerCouponController
 *
 * 파트너 쿠폰 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class PartnerCouponController {

    private final PartnerCouponService couponService;

    /**
     * 사용 가능한 쿠폰 목록 조회
     *
     * GET /api/coupons
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAvailableCoupons() {
        log.info("Get available coupons request");

        List<CouponResponse> coupons = couponService.getAvailableCoupons();

        return ResponseEntity.ok(ApiResponse.success("쿠폰 목록 조회 성공", coupons));
    }

    /**
     * 쿠폰 구매
     *
     * POST /api/coupons/{id}/purchase
     */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<ApiResponse<IssuedCouponResponse>> purchaseCoupon(
            @PathVariable Long id) {
        log.info("Purchase coupon request: {}", id);

        IssuedCouponResponse issuedCoupon = couponService.purchaseCoupon(id);

        return ResponseEntity.ok(ApiResponse.success("쿠폰 구매 성공", issuedCoupon));
    }

    /**
     * 내 쿠폰 목록 조회
     *
     * GET /api/coupons/my
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<IssuedCouponResponse>>> getMyCoupons() {
        log.info("Get my coupons request");

        List<IssuedCouponResponse> coupons = couponService.getMyCoupons();

        return ResponseEntity.ok(ApiResponse.success("내 쿠폰 조회 성공", coupons));
    }

    /**
     * 쿠폰 사용
     *
     * POST /api/coupons/use/{code}
     */
    @PostMapping("/use/{code}")
    public ResponseEntity<ApiResponse<Void>> useCoupon(@PathVariable String code) {
        log.info("Use coupon request: {}", code);

        couponService.useCoupon(code);

        return ResponseEntity.ok(ApiResponse.success("쿠폰 사용 완료", null));
    }
}
