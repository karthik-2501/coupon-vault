package com.couponvault.repository;

import com.couponvault.domain.ValidationLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationLogRepository extends JpaRepository<ValidationLog, String> {
    Optional<ValidationLog> findTopByCouponIdOrderByCheckedAtDesc(String couponId);

    void deleteByCoupon_Id(String couponId);
}
