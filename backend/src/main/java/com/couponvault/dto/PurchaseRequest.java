package com.couponvault.dto;

import jakarta.validation.constraints.NotBlank;

public record PurchaseRequest(@NotBlank String couponId) {}
