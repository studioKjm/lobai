package com.lobai.repository;

import com.lobai.entity.PartnerCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerCouponRepository extends JpaRepository<PartnerCoupon, Long> {

    /**
     * Find all active coupons ordered by display_order
     */
    List<PartnerCoupon> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Find coupons by partner name
     */
    List<PartnerCoupon> findByPartnerNameAndIsActiveTrueOrderByDisplayOrderAsc(String partnerName);

    /**
     * Find coupons by type
     */
    List<PartnerCoupon> findByCouponTypeAndIsActiveTrueOrderByDisplayOrderAsc(String couponType);

    /**
     * Find affordable coupons for a given balance
     */
    @Query("SELECT c FROM PartnerCoupon c WHERE c.isActive = true " +
           "AND c.costLobcoin <= :balance ORDER BY c.displayOrder ASC")
    List<PartnerCoupon> findAffordableCoupons(Integer balance);

    /**
     * Find available coupons (active and in stock)
     */
    @Query("SELECT c FROM PartnerCoupon c WHERE c.isActive = true " +
           "AND (c.stock IS NULL OR c.stock > 0) ORDER BY c.displayOrder ASC")
    List<PartnerCoupon> findAvailableCoupons();
}
