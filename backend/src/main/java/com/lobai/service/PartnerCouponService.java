package com.lobai.service;

import com.lobai.dto.response.CouponResponse;
import com.lobai.dto.response.IssuedCouponResponse;
import com.lobai.entity.CouponIssuance;
import com.lobai.entity.PartnerCoupon;
import com.lobai.entity.LobCoinBalance;
import com.lobai.entity.User;
import com.lobai.repository.CouponIssuanceRepository;
import com.lobai.repository.LobCoinBalanceRepository;
import com.lobai.repository.PartnerCouponRepository;
import com.lobai.repository.UserRepository;
import com.lobai.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerCouponService {

    private final PartnerCouponRepository couponRepository;
    private final CouponIssuanceRepository issuanceRepository;
    private final LobCoinBalanceRepository balanceRepository;
    private final UserRepository userRepository;
    private final LobCoinService lobCoinService;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Get all available coupons
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getAvailableCoupons() {
        Long userId = SecurityUtil.getCurrentUserId();
        LobCoinBalance balance = balanceRepository.findByUserId(userId)
            .orElse(null);

        int userBalance = (balance != null) ? balance.getBalance() : 0;

        List<PartnerCoupon> coupons = couponRepository.findAvailableCoupons();

        return coupons.stream()
            .map(coupon -> toCouponResponse(coupon, userBalance))
            .collect(Collectors.toList());
    }

    /**
     * Purchase a coupon
     */
    @Transactional
    public IssuedCouponResponse purchaseCoupon(Long couponId) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // Get coupon
        PartnerCoupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        // Validate coupon availability
        if (!coupon.isAvailable()) {
            throw new IllegalStateException("해당 쿠폰은 현재 구매할 수 없습니다");
        }

        // Check if user has sufficient balance
        LobCoinBalance balance = balanceRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("잔액 정보를 찾을 수 없습니다"));

        if (balance.getBalance() < coupon.getCostLobcoin()) {
            throw new IllegalArgumentException(
                String.format("잔액이 부족합니다 (현재: %d, 필요: %d)",
                    balance.getBalance(), coupon.getCostLobcoin())
            );
        }

        // Spend LobCoins
        lobCoinService.spendLobCoin(
            userId,
            coupon.getCostLobcoin(),
            "PARTNER_COUPON",
            String.format("%s 쿠폰 구매", coupon.getTitle())
        );

        // Decrease stock
        if (coupon.getStock() != null) {
            coupon.decreaseStock();
            couponRepository.save(coupon);
        }

        // Generate coupon code
        String couponCode = generateCouponCode(coupon);

        // Create issuance
        CouponIssuance issuance = CouponIssuance.builder()
            .user(user)
            .coupon(coupon)
            .couponCode(couponCode)
            .expiresAt(LocalDateTime.now().plusMonths(3))  // 3 months validity
            .status(CouponIssuance.CouponStatus.ISSUED)
            .build();

        CouponIssuance saved = issuanceRepository.save(issuance);

        log.info("Coupon purchased: {} (id: {}) by user {}", coupon.getTitle(), couponId, userId);
        return toIssuedCouponResponse(saved);
    }

    /**
     * Get my purchased coupons
     */
    @Transactional(readOnly = true)
    public List<IssuedCouponResponse> getMyCoupons() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<CouponIssuance> issuances = issuanceRepository.findByUserIdOrderByIssuedAtDesc(userId);

        return issuances.stream()
            .map(this::toIssuedCouponResponse)
            .collect(Collectors.toList());
    }

    /**
     * Use a coupon
     */
    @Transactional
    public void useCoupon(String couponCode) {
        CouponIssuance issuance = issuanceRepository.findByCouponCode(couponCode)
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다"));

        Long userId = SecurityUtil.getCurrentUserId();
        if (!issuance.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 쿠폰만 사용할 수 있습니다");
        }

        issuance.use();
        issuanceRepository.save(issuance);

        log.info("Coupon used: {} by user {}", couponCode, userId);
    }

    /**
     * Generate unique coupon code
     */
    private String generateCouponCode(PartnerCoupon coupon) {
        String prefix = coupon.getPartnerName().substring(0, Math.min(3, coupon.getPartnerName().length())).toUpperCase();
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("%s-%s", prefix, randomPart);
    }

    /**
     * Convert PartnerCoupon to CouponResponse
     */
    private CouponResponse toCouponResponse(PartnerCoupon coupon, int userBalance) {
        return CouponResponse.builder()
            .id(coupon.getId())
            .partnerName(coupon.getPartnerName())
            .couponType(coupon.getCouponType())
            .costLobcoin(coupon.getCostLobcoin())
            .realValueUsd(coupon.getRealValueUsd())
            .title(coupon.getTitle())
            .description(coupon.getDescription())
            .terms(coupon.getTerms())
            .imageUrl(coupon.getImageUrl())
            .isActive(coupon.getIsActive())
            .stock(coupon.getStock())
            .isAffordable(userBalance >= coupon.getCostLobcoin())
            .isAvailable(coupon.isAvailable())
            .build();
    }

    /**
     * Convert CouponIssuance to IssuedCouponResponse
     */
    private IssuedCouponResponse toIssuedCouponResponse(CouponIssuance issuance) {
        return IssuedCouponResponse.builder()
            .id(issuance.getId())
            .couponCode(issuance.getCouponCode())
            .couponTitle(issuance.getCoupon().getTitle())
            .partnerName(issuance.getCoupon().getPartnerName())
            .status(issuance.getStatus().name())
            .issuedAt(issuance.getIssuedAt().format(ISO_FORMATTER))
            .usedAt(issuance.getUsedAt() != null ? issuance.getUsedAt().format(ISO_FORMATTER) : null)
            .expiresAt(issuance.getExpiresAt() != null ? issuance.getExpiresAt().format(ISO_FORMATTER) : null)
            .build();
    }
}
