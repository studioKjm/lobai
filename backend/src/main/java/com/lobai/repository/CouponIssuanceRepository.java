package com.lobai.repository;

import com.lobai.entity.CouponIssuance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponIssuanceRepository extends JpaRepository<CouponIssuance, Long> {

    /**
     * Find issuances by user ID (ordered by issued_at DESC)
     */
    List<CouponIssuance> findByUserIdOrderByIssuedAtDesc(Long userId);

    /**
     * Find issuances by user ID and status
     */
    List<CouponIssuance> findByUserIdAndStatusOrderByIssuedAtDesc(
        Long userId,
        CouponIssuance.CouponStatus status
    );

    /**
     * Find issuance by coupon code
     */
    Optional<CouponIssuance> findByCouponCode(String couponCode);

    /**
     * Find expired coupons that haven't been marked as expired
     */
    @Query("SELECT c FROM CouponIssuance c WHERE c.status = 'ISSUED' " +
           "AND c.expiresAt IS NOT NULL AND c.expiresAt < :now")
    List<CouponIssuance> findExpiredCoupons(@Param("now") LocalDateTime now);

    /**
     * Count issued coupons by coupon ID
     */
    long countByCouponIdAndStatus(Long couponId, CouponIssuance.CouponStatus status);

    /**
     * Check if user has already received a specific coupon
     */
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
