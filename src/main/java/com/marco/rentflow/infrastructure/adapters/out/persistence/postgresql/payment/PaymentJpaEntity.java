package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity {

    @Id
    private UUID id;

    // Asociación Polimórfica Agnóstica (Sin @JoinColumn)
    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "payment_target", nullable = false, length = 50)
    private String paymentTarget; // RENT, SAAS

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING, PAID, OVERDUE

    // Fuente de la Verdad contra ataques F12
    @Column(name = "expected_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal expectedAmount;

    @Column(name = "amount_paid", precision = 19, scale = 4)
    private BigDecimal amountPaid;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "late_fee_applied", precision = 19, scale = 4)
    private BigDecimal lateFeeApplied;

    @Column(name = "payment_date")
    private ZonedDateTime paymentDate;

    // Escudo contra doble facturación (Condiciones de Carrera)
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    protected PaymentJpaEntity() {}

    public PaymentJpaEntity(UUID id, UUID referenceId, String paymentTarget, String status, BigDecimal expectedAmount, BigDecimal amountPaid, String currency, BigDecimal lateFeeApplied, ZonedDateTime paymentDate, String idempotencyKey) {
        this.id = id;
        this.referenceId = referenceId;
        this.paymentTarget = paymentTarget;
        this.status = status;
        this.expectedAmount = expectedAmount;
        this.amountPaid = amountPaid;
        this.currency = currency;
        this.lateFeeApplied = lateFeeApplied;
        this.paymentDate = paymentDate;
        this.idempotencyKey = idempotencyKey;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public String getPaymentTarget() {
        return paymentTarget;
    }

    public void setPaymentTarget(String paymentTarget) {
        this.paymentTarget = paymentTarget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getLateFeeApplied() {
        return lateFeeApplied;
    }

    public void setLateFeeApplied(BigDecimal lateFeeApplied) {
        this.lateFeeApplied = lateFeeApplied;
    }

    public ZonedDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(ZonedDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}