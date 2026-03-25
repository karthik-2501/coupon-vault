package com.couponvault.service;

import com.couponvault.domain.*;
import com.couponvault.repository.CouponRepository;
import com.couponvault.repository.TransactionRepository;
import com.couponvault.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    public TransactionService(TransactionRepository transactionRepository, CouponRepository couponRepository,
                              UserRepository userRepository, PaymentService paymentService) {
        this.transactionRepository = transactionRepository;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
        this.paymentService = paymentService;
    }

    @Transactional
    public Transaction purchase(String buyerId, String couponId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));

        if (transactionRepository.existsByCouponId(couponId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Coupon already sold");
        }
        if (coupon.getStatus() != CouponStatus.VALID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupon is not available");
        }
        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            coupon.setStatus(CouponStatus.EXPIRED);
            couponRepository.save(coupon);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupon expired");
        }
        if (coupon.getSeller().getId().equals(buyerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot buy own coupon");
        }

        Transaction transaction = new Transaction();
        transaction.setBuyer(buyer);
        transaction.setSeller(coupon.getSeller());
        transaction.setCoupon(coupon);
        transaction.setAmount(coupon.getPrice());

        boolean success = paymentService.charge(couponId);
        transaction.setStatus(success ? TransactionStatus.COMPLETED : TransactionStatus.FAILED);
        transactionRepository.save(transaction);

        if (success) {
            coupon.setStatus(CouponStatus.SOLD);
            couponRepository.save(coupon);
        }

        return transaction;
    }

    public List<Transaction> buyerTransactions(String buyerId) {
        return transactionRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }

    public List<Transaction> sellerTransactions(String sellerId) {
        return transactionRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    public boolean hasPurchased(String buyerId, String couponId) {
        return transactionRepository.existsByCouponIdAndBuyerIdAndStatus(couponId, buyerId, TransactionStatus.COMPLETED);
    }
}
