package com.couponvault.service;

import com.couponvault.domain.*;
import com.couponvault.dto.CreateCouponRequest;
import com.couponvault.repository.*;
import com.couponvault.validation.StoreValidationStrategy;
import com.couponvault.validation.ValidationResult;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ValidationLogRepository validationLogRepository;
    private final StoreValidationStrategy validationStrategy;

    public CouponService(CouponRepository couponRepository, StoreRepository storeRepository, UserRepository userRepository,
                         ValidationLogRepository validationLogRepository, StoreValidationStrategy validationStrategy) {
        this.couponRepository = couponRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.validationLogRepository = validationLogRepository;
        this.validationStrategy = validationStrategy;
    }

    @Transactional
    public Coupon createCoupon(String sellerId, CreateCouponRequest req) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

        Store store = storeRepository.findByNameIgnoreCase(req.storeName())
                .orElseGet(() -> {
                    Store s = new Store();
                    s.setName(req.storeName());
                    s.setWebsiteUrl(req.storeWebsiteUrl());
                    s.setLogoUrl(req.storeLogoUrl());
                    return storeRepository.save(s);
                });

        Coupon coupon = new Coupon();
        coupon.setStore(store);
        coupon.setSeller(seller);
        coupon.setCode(req.code());
        coupon.setDescription(req.description());
        coupon.setDiscountType(req.discountType());
        coupon.setDiscountValue(req.discountValue());
        coupon.setMinOrderValue(req.minOrderValue());
        coupon.setExpiryDate(req.expiryDate());
        coupon.setPrice(req.askingPrice());
        coupon.setStatus(CouponStatus.PENDING_VALIDATION);
        couponRepository.save(coupon);

        validateCoupon(coupon.getId());
        return coupon;
    }

    @Transactional
    public void validateCoupon(String couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));

        ValidationResult result = validationStrategy.validate(coupon);
        ValidationLog log = new ValidationLog();
        log.setCoupon(coupon);
        log.setRawResponse(result.rawResponse());
        log.setMessage(result.message());

        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            coupon.setStatus(CouponStatus.EXPIRED);
            log.setStatus(ValidationStatus.INVALID);
            log.setMessage("Coupon already expired");
        } else if (result.valid()) {
            coupon.setStatus(CouponStatus.VALID);
            log.setStatus(ValidationStatus.VALID);
        } else {
            coupon.setStatus(CouponStatus.INVALID);
            log.setStatus(ValidationStatus.INVALID);
        }

        couponRepository.save(coupon);
        validationLogRepository.save(log);
    }

    public List<Coupon> marketplace(String store, String discountType, Double minOrderMin, Double minOrderMax, Double priceMin, Double priceMax) {
        expireCoupons();
        return couponRepository.findByStatusAndExpiryDateAfter(CouponStatus.VALID, LocalDate.now()).stream()
                .filter(c -> store == null || c.getStore().getName().toLowerCase().contains(store.toLowerCase()))
                .filter(c -> discountType == null || c.getDiscountType().name().equalsIgnoreCase(discountType))
                .filter(c -> minOrderMin == null || (c.getMinOrderValue() != null && c.getMinOrderValue().doubleValue() >= minOrderMin))
                .filter(c -> minOrderMax == null || (c.getMinOrderValue() != null && c.getMinOrderValue().doubleValue() <= minOrderMax))
                .filter(c -> priceMin == null || c.getPrice().doubleValue() >= priceMin)
                .filter(c -> priceMax == null || c.getPrice().doubleValue() <= priceMax)
                .toList();
    }

    public List<Coupon> sellerCoupons(String sellerId) {
        return couponRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    public Coupon getCoupon(String id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
    }

    @Transactional
    public void deleteSellerCoupon(String sellerId, String couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found"));
        if (!coupon.getSeller().getId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your listing");
        }
        if (coupon.getStatus() == CouponStatus.SOLD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete a sold coupon");
        }
        validationLogRepository.deleteByCoupon_Id(couponId);
        couponRepository.delete(coupon);
    }

    @Scheduled(fixedDelay = 60000)
    public void expireCoupons() {
        List<Coupon> expired = couponRepository.findByStatusAndExpiryDateBefore(CouponStatus.VALID, LocalDate.now());
        for (Coupon coupon : expired) {
            if (coupon.getStatus() == CouponStatus.VALID) {
                coupon.setStatus(CouponStatus.EXPIRED);
                couponRepository.save(coupon);
            }
        }
    }
}
