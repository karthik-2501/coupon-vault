package com.couponvault.dto;

public record AuthResponse(String token, String userId, String name, String email) {}
