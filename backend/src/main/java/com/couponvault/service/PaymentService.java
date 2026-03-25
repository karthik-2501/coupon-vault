package com.couponvault.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PaymentService {

    /**
     * Mock payment gateway. Returns false only if {@code couponId} contains
     * {@code FAILPAY} (case-insensitive) so demos stay predictable.
     */
    public boolean charge(String couponId) {
        if (couponId == null) return true;
        return !couponId.toUpperCase().contains("FAILPAY");
    }
}
