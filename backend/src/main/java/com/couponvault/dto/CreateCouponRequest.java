package com.couponvault.dto;

import com.couponvault.domain.DiscountType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCouponRequest(
        @NotBlank String storeName,
        @NotBlank String storeWebsiteUrl,
        String storeLogoUrl,
        @NotBlank String code,
        @NotBlank String description,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal discountValue,
        BigDecimal minOrderValue,
        @NotNull LocalDate expiryDate,
        @NotNull @DecimalMin("0.01") BigDecimal askingPrice
) {}
