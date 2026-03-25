package com.couponvault.controller;

import com.couponvault.domain.Coupon;
import com.couponvault.domain.CouponStatus;
import com.couponvault.dto.CreateCouponRequest;
import com.couponvault.repository.ValidationLogRepository;
import com.couponvault.security.AuthUser;
import com.couponvault.service.CouponService;
import com.couponvault.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class CouponController {
    private final CouponService couponService;
    private final ValidationLogRepository validationLogRepository;
    private final TransactionService transactionService;

    public CouponController(CouponService couponService, ValidationLogRepository validationLogRepository, TransactionService transactionService) {
        this.couponService = couponService;
        this.validationLogRepository = validationLogRepository;
        this.transactionService = transactionService;
    }

    @PostMapping("/coupons")
    public Object create(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody CreateCouponRequest request) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Coupon coupon = couponService.createCoupon(user.id(), request);
        return Map.of("id", coupon.getId(), "status", coupon.getStatus(), "message", "Validating...");
    }

    @GetMapping("/coupons")
    public List<Map<String, Object>> marketplace(
            @RequestParam(required = false) String store,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) Double minOrderMin,
            @RequestParam(required = false) Double minOrderMax,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax) {
        return couponService.marketplace(store, discountType, minOrderMin, minOrderMax, priceMin, priceMax)
                .stream().map(this::toPublicCoupon).toList();
    }

    @GetMapping("/coupons/{id}")
    public Map<String, Object> detail(@PathVariable String id, @AuthenticationPrincipal AuthUser user) {
        Coupon c = couponService.getCoupon(id);
        boolean reveal = user != null && user.id().equals(c.getSeller().getId());
        if (!reveal && user != null && c.getStatus() == CouponStatus.SOLD) {
            reveal = transactionService.hasPurchased(user.id(), c.getId());
        }
        return toCouponDetail(c, reveal);
    }

    @DeleteMapping("/seller/coupons/{id}")
    public Map<String, Object> deleteSellerCoupon(@AuthenticationPrincipal AuthUser user, @PathVariable String id) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        couponService.deleteSellerCoupon(user.id(), id);
        return Map.of("deleted", true, "id", id);
    }

    @GetMapping("/seller/coupons")
    public List<Map<String, Object>> sellerCoupons(@AuthenticationPrincipal AuthUser user) {
        return couponService.sellerCoupons(user.id()).stream().map(c -> {
            String msg = validationLogRepository.findTopByCouponIdOrderByCheckedAtDesc(c.getId()).map(v -> v.getMessage()).orElse("");
            Map<String, Object> base = toCouponDetail(c, true);
            base.put("latestValidationMessage", msg);
            return base;
        }).toList();
    }

    private Map<String, Object> toPublicCoupon(Coupon c) {
        java.util.Map<String, Object> row = new java.util.HashMap<>();
        row.put("id", c.getId());
        row.put("description", c.getDescription());
        row.put("discountType", c.getDiscountType());
        row.put("discountValue", c.getDiscountValue());
        row.put("minOrderValue", c.getMinOrderValue());
        row.put("expiryDate", c.getExpiryDate());
        row.put("price", c.getPrice());
        row.put("store", storeSummary(c.getStore()));
        row.put("seller", Map.of("id", c.getSeller().getId(), "name", c.getSeller().getName()));
        return row;
    }

    private java.util.Map<String, Object> storeSummary(com.couponvault.domain.Store s) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", s.getId());
        m.put("name", s.getName());
        m.put("logoUrl", s.getLogoUrl());
        return m;
    }

    private Map<String, Object> toCouponDetail(Coupon c, boolean revealCode) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", c.getId());
        m.put("code", revealCode ? c.getCode() : "[hidden]");
        m.put("description", c.getDescription());
        m.put("status", c.getStatus());
        m.put("discountType", c.getDiscountType());
        m.put("discountValue", c.getDiscountValue());
        m.put("minOrderValue", c.getMinOrderValue());
        m.put("expiryDate", c.getExpiryDate());
        m.put("price", c.getPrice());
        java.util.Map<String, Object> store = new java.util.HashMap<>();
        store.put("id", c.getStore().getId());
        store.put("name", c.getStore().getName());
        store.put("websiteUrl", c.getStore().getWebsiteUrl());
        store.put("logoUrl", c.getStore().getLogoUrl());
        m.put("store", store);
        m.put("seller", Map.of("id", c.getSeller().getId(), "name", c.getSeller().getName()));
        return m;
    }
}
