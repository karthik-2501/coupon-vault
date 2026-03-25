package com.couponvault.controller;

import com.couponvault.domain.Transaction;
import com.couponvault.domain.TransactionStatus;
import com.couponvault.dto.PurchaseRequest;
import com.couponvault.security.AuthUser;
import com.couponvault.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    public Map<String, Object> buy(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody PurchaseRequest request) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Transaction tx = transactionService.purchase(user.id(), request.couponId());
        return Map.of("id", tx.getId(), "status", tx.getStatus(), "couponId", tx.getCoupon().getId(), "amount", tx.getAmount());
    }

    @GetMapping("/buyer/transactions")
    public List<Map<String, Object>> buyerTransactions(@AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return transactionService.buyerTransactions(user.id()).stream().map(tx -> Map.<String, Object>of(
                "id", tx.getId(),
                "status", tx.getStatus(),
                "amount", tx.getAmount(),
                "createdAt", tx.getCreatedAt(),
                "coupon", Map.of(
                        "id", tx.getCoupon().getId(),
                        "code", tx.getStatus() == TransactionStatus.COMPLETED ? tx.getCoupon().getCode() : "[hidden]",
                        "description", tx.getCoupon().getDescription(),
                        "storeName", tx.getCoupon().getStore().getName(),
                        "expiryDate", tx.getCoupon().getExpiryDate()
                )
        )).toList();
    }

    @GetMapping("/seller/transactions")
    public List<Map<String, Object>> sellerTransactions(@AuthenticationPrincipal AuthUser user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return transactionService.sellerTransactions(user.id()).stream().map(tx -> {
            java.util.Map<String, Object> row = new java.util.HashMap<>();
            row.put("id", tx.getId());
            row.put("status", tx.getStatus());
            row.put("amount", tx.getAmount());
            row.put("createdAt", tx.getCreatedAt());
            row.put("buyer", java.util.Map.of(
                    "id", tx.getBuyer().getId(),
                    "name", tx.getBuyer().getName()));
            row.put("coupon", java.util.Map.of(
                    "id", tx.getCoupon().getId(),
                    "code", tx.getCoupon().getCode(),
                    "description", tx.getCoupon().getDescription(),
                    "storeName", tx.getCoupon().getStore().getName(),
                    "expiryDate", tx.getCoupon().getExpiryDate(),
                    "price", tx.getCoupon().getPrice()));
            return row;
        }).toList();
    }
}
