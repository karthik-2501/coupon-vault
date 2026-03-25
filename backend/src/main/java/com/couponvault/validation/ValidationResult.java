package com.couponvault.validation;

public record ValidationResult(boolean valid, String message, String rawResponse) {}
