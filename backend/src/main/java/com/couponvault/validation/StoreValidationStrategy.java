package com.couponvault.validation;

import com.couponvault.domain.Coupon;

public interface StoreValidationStrategy {
    ValidationResult validate(Coupon coupon);
}
