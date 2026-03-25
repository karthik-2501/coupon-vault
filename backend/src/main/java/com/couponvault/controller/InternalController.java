package com.couponvault.controller;

import com.couponvault.service.CouponService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalController {
    private final CouponService couponService;

    public InternalController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate-coupon/{couponId}")
    public Object validate(@PathVariable String couponId) {
        couponService.validateCoupon(couponId);
        return java.util.Map.of("ok", true, "couponId", couponId);
    }
}
