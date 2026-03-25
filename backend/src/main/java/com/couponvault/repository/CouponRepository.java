package com.couponvault.repository;

import com.couponvault.domain.Coupon;
import com.couponvault.domain.CouponStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, String> {
    List<Coupon> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    List<Coupon> findByStatusAndExpiryDateAfter(CouponStatus status, LocalDate date);
    List<Coupon> findByStatusAndExpiryDateBefore(CouponStatus status, LocalDate date);
}
