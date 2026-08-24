package com.marco.rentflow.core.domain.payment;

import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class PaymentRecord {
    private final UUID id;
    private final UUID contractId;
    private final UUID tenantId;
    private final String idempotencyKey;

    private final LocalDate dueDate;
    private LocalDate paymentDate;

    private Money paidAmount;
    private final Money expectedAmount;
    private Money lateFeeApplied;

    private PaymentStatus status;
    private String transactionReference;
    private String paymentReceiptUrl;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 1. CONSTRUCTOR PRIVADO (El Guardián Absoluto)
    private PaymentRecord(UUID id, UUID contractId, UUID tenantId, String idempotencyKey,
                          LocalDate dueDate, LocalDate paymentDate,
                          Money expectedAmount, Money paidAmount, Money lateFeeApplied,
                          PaymentStatus status, String transactionReference,
                          String paymentReceiptUrl,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "PaymentRecord ID cannot be null");
        this.contractId = Objects.requireNonNull(contractId, "Contract ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "Idempotency key cannot be null");
        this.dueDate = Objects.requireNonNull(dueDate, "Due date cannot be null");
        this.paymentDate = paymentDate;
        this.expectedAmount = Objects.requireNonNull(expectedAmount, "Expected amount cannot be null");
        this.paidAmount = Objects.requireNonNull(paidAmount, "Paid amount cannot be null");
        this.lateFeeApplied = Objects.requireNonNull(lateFeeApplied, "Late fee cannot be null");
        this.status = Objects.requireNonNull(status, "Payment status cannot be null");
        this.transactionReference = transactionReference;
        this.paymentReceiptUrl = paymentReceiptUrl;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // 2. FACTORY METHOD PARA NUEVOS (Capa de Aplicación)
    public static PaymentRecord createPending(UUID contractId, UUID tenantId,
                                              LocalDate dueDate, Money expectedAmount,
                                              String idempotencyKey) {
        Money zeroFee = new Money(BigDecimal.ZERO, expectedAmount.getCurrency());
        return new PaymentRecord(
                UUID.randomUUID(),
                contractId,
                tenantId,
                idempotencyKey,
                dueDate,
                null,
                expectedAmount,
                zeroFee,
                zeroFee,
                PaymentStatus.PENDING,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // 3. FACTORY METHOD PARA MAPEO DE BD (Capa de Infraestructura)
    public static PaymentRecord reconstitute(UUID id, UUID contractId, UUID tenantId, String idempotencyKey,
                                              LocalDate dueDate, LocalDate paymentDate,
                                              Money expectedAmount, Money paidAmount, Money lateFeeApplied,
                                              PaymentStatus status, String transactionReference,
                                              String paymentReceiptUrl,
                                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new PaymentRecord(
                id, contractId, tenantId, idempotencyKey,
                dueDate, paymentDate,
                expectedAmount, paidAmount, lateFeeApplied,
                status, transactionReference, paymentReceiptUrl,
                createdAt, updatedAt
        );
    }

    // MÉTODOS DE NEGOCIO Y TRANSICIÓN

    public void registerPayment(Money amountPaid,
                                LocalDate actualPaymentDate,
                                Money lateFee,
                                String transactionRef,
                                String receiptUrl) {

        if (this.status == PaymentStatus.PAID) {
            throw new IllegalStateException("Payment has already been settled");
        }

        Objects.requireNonNull(amountPaid, "Amount paid cannot be null");
        Objects.requireNonNull(actualPaymentDate, "Payment date cannot be null");

        // Calcular el total esperado (arriendo + multa)
        Money totalExpected = this.expectedAmount.add(
            lateFee != null ? lateFee : new Money(BigDecimal.ZERO, expectedAmount.getCurrency())
        );

        // Validar que lo que pagó el amountPaid sea suficiente
        if (amountPaid.getAmount().compareTo(totalExpected.getAmount()) < 0) {
            throw new IllegalArgumentException("Amount paid is less than expected total");
        }

        this.paidAmount = amountPaid;
        this.paymentDate = actualPaymentDate;
        this.lateFeeApplied = lateFee != null ? lateFee : new Money(BigDecimal.ZERO, expectedAmount.getCurrency());
        this.transactionReference = transactionRef;
        this.paymentReceiptUrl = receiptUrl;
        this.status = PaymentStatus.PAID;
        touch();
    }

    public void markAsOverdue(LocalDate currentDate) {
        if (this.status == PaymentStatus.PENDING && currentDate.isAfter(this.dueDate)) {
            this.status = PaymentStatus.OVERDUE;
        }
        touch();
    }

    public void cancel() {
        if (this.status == PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a paid payment");
        }
        this.status = PaymentStatus.CANCELLED;
        touch();
    }

    // MÉTODOS DE CONSULTA

    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    public boolean isOverdue() {
        return this.status == PaymentStatus.OVERDUE;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }
    // es bueno evaluar los 2 caminos, boolean puede ser true o false

    public Money getTotalPaid() {
        if (this.status != PaymentStatus.PAID) {
            return new Money(BigDecimal.ZERO, expectedAmount.getCurrency());
        }
        return this.paidAmount;
    }

    public Money getTotalExpected() {
        return this.expectedAmount.add(this.lateFeeApplied);
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS
    public UUID getId() { return id; }
    public UUID getContractId() { return contractId; }
    public UUID getTenantId() { return tenantId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public Money getExpectedAmount() { return expectedAmount; }
    public Money getPaidAmount() { return paidAmount; }
    public Money getLateFeeApplied() { return lateFeeApplied; }
    public PaymentStatus getStatus() { return status; }
    public String getTransactionReference() { return transactionReference; }
    public String getPaymentReceiptUrl() { return paymentReceiptUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

}

/**
 * public void registerPayment(Money amountPaid,
 *                                 LocalDate actualPaymentDate,
 *                                 Money lateFee,
 *                                 String transactionRef,
 *                                 String receiptUrl) {...}
 *    LocalDate actualPaymentDate -> ES la fecha del pago real
 */
