package com.couponvault.validation;

import com.couponvault.domain.Coupon;
import org.springframework.stereotype.Component;

@Component
public class MockStoreValidationStrategy implements StoreValidationStrategy {
    @Override
    public ValidationResult validate(Coupon coupon) {
        boolean valid = coupon.getCode().startsWith("VALID-");
        String msg = valid ? "Coupon accepted by mock strategy" : "Coupon rejected by mock strategy";
        String raw = "{\"store\":\"" + coupon.getStore().getWebsiteUrl() + "\",\"rule\":\"code starts with VALID-\"}";
        return new ValidationResult(valid, msg, raw);
    }
}
