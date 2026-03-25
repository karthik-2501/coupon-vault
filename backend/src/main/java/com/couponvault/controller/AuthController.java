package com.couponvault.controller;

import com.couponvault.dto.AuthResponse;
import com.couponvault.dto.LoginRequest;
import com.couponvault.dto.RegisterRequest;
import com.couponvault.security.AuthUser;
import com.couponvault.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public Object me(@AuthenticationPrincipal AuthUser user) {
        if (user == null) return java.util.Map.of("authenticated", false);
        return java.util.Map.of("authenticated", true, "id", user.id(), "name", user.name(), "email", user.email());
    }
}
