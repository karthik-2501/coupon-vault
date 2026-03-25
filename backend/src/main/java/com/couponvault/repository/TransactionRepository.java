package com.couponvault.repository;

import com.couponvault.domain.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByBuyerIdOrderByCreatedAtDesc(String buyerId);

    List<Transaction> findBySellerIdOrderByCreatedAtDesc(String sellerId);
    boolean existsByCouponId(String couponId);
    boolean existsByCouponIdAndBuyerIdAndStatus(String couponId, String buyerId, com.couponvault.domain.TransactionStatus status);
}
