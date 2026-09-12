package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class PaymentJpaEntity {

    @Id
    private UUID id;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "payment_target", nullable = false, length = 50)
    private String paymentTarget;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "expected_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal expectedAmount;

    @Column(name = "amount_paid", precision = 19, scale = 4)
    private BigDecimal amountPaid;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "late_fee_applied", precision = 19, scale = 4)
    private BigDecimal lateFeeApplied;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Column(name = "transaction_reference", length = 255)
    private String transactionReference;

    @Column(name = "payment_receipt_url", length = 500)
    private String paymentReceiptUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PaymentJpaEntity() {}

    public PaymentJpaEntity(UUID id, UUID referenceId, String paymentTarget, String status,
                            BigDecimal expectedAmount, BigDecimal amountPaid, String currency,
                            BigDecimal lateFeeApplied, LocalDate dueDate, LocalDateTime paymentDate,
                            String idempotencyKey, String transactionReference, String paymentReceiptUrl,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.referenceId = referenceId;
        this.paymentTarget = paymentTarget;
        this.status = status;
        this.expectedAmount = expectedAmount;
        this.amountPaid = amountPaid;
        this.currency = currency;
        this.lateFeeApplied = lateFeeApplied;
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;
        this.idempotencyKey = idempotencyKey;
        this.transactionReference = transactionReference;
        this.paymentReceiptUrl = paymentReceiptUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters...
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }
    public String getPaymentTarget() { return paymentTarget; }
    public void setPaymentTarget(String paymentTarget) { this.paymentTarget = paymentTarget; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getLateFeeApplied() { return lateFeeApplied; }
    public void setLateFeeApplied(BigDecimal lateFeeApplied) { this.lateFeeApplied = lateFeeApplied; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public String getPaymentReceiptUrl() { return paymentReceiptUrl; }
    public void setPaymentReceiptUrl(String paymentReceiptUrl) { this.paymentReceiptUrl = paymentReceiptUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}