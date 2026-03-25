package com.couponvault.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "validation_logs")
public class ValidationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationStatus status;

    @Column(nullable = false)
    private Instant checkedAt = Instant.now();

    @Lob
    private String rawResponse;

    @Column(nullable = false)
    private String message;

    public String getId() { return id; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public ValidationStatus getStatus() { return status; }
    public void setStatus(ValidationStatus status) { this.status = status; }
    public Instant getCheckedAt() { return checkedAt; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
