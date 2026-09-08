package com.marco.rentflow.core.domain.payment;

import com.marco.rentflow.core.domain.common.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class PaymentRecord {
    private final UUID id;

    // Asociación Polimórfica (Agnóstica)
    private final UUID referenceId;
    private final PaymentTarget target;

    private final String idempotencyKey;

    private final LocalDate dueDate;
    private LocalDateTime paymentDate; // Cambiado a LocalDateTime para mayor precisión en transacciones

    private Money amountPaid; // Renombrado para alinear con BD y Mapper
    private final Money expectedAmount;
    private Money lateFeeApplied;

    private PaymentStatus status;
    private String transactionReference;
    private String paymentReceiptUrl;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 1. CONSTRUCTOR PRIVADO (El Guardián Absoluto)
    private PaymentRecord(UUID id, UUID referenceId, PaymentTarget target, String idempotencyKey,
                          LocalDate dueDate, LocalDateTime paymentDate,
                          Money expectedAmount, Money amountPaid, Money lateFeeApplied,
                          PaymentStatus status, String transactionReference,
                          String paymentReceiptUrl,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "PaymentRecord ID cannot be null");
        this.referenceId = Objects.requireNonNull(referenceId, "Reference ID cannot be null");
        this.target = Objects.requireNonNull(target, "Payment target cannot be null");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "Idempotency key cannot be null");
        this.dueDate = dueDate; // Puede ser null si es un pago inmediato (ej. Suscripción instantánea)
        this.paymentDate = paymentDate;
        this.expectedAmount = Objects.requireNonNull(expectedAmount, "Expected amount cannot be null");
        this.amountPaid = amountPaid; // Puede ser null si está PENDING
        this.lateFeeApplied = lateFeeApplied;
        this.status = Objects.requireNonNull(status, "Payment status cannot be null");
        this.transactionReference = transactionReference;
        this.paymentReceiptUrl = paymentReceiptUrl;
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // 2. FACTORY METHOD PARA NUEVOS (Capa de Aplicación)
    public static PaymentRecord createPending(UUID referenceId, PaymentTarget target,
                                              LocalDate dueDate, Money expectedAmount,
                                              String idempotencyKey) {
        return new PaymentRecord(
                UUID.randomUUID(),
                referenceId,
                target,
                idempotencyKey,
                dueDate,
                null,
                expectedAmount,
                null, // Aún no hay monto pagado
                null, // Aún no hay multas aplicadas
                PaymentStatus.PENDING,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // 3. FACTORY METHOD PARA MAPEO DE BD (Capa de Infraestructura)
    public static PaymentRecord reconstitute(UUID id, UUID referenceId, PaymentTarget target,
                                             String idempotencyKey, LocalDate dueDate, LocalDateTime paymentDate,
                                             Money expectedAmount, Money amountPaid, Money lateFeeApplied,
                                             PaymentStatus status, String transactionReference,
                                             String paymentReceiptUrl,
                                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new PaymentRecord(
                id, referenceId, target, idempotencyKey,
                dueDate, paymentDate,
                expectedAmount, amountPaid, lateFeeApplied,
                status, transactionReference, paymentReceiptUrl,
                createdAt, updatedAt
        );
    }

    // === MÉTODOS DE NEGOCIO Y TRANSICIÓN ===

    public void registerPayment(Money newAmountPaid,
                                LocalDateTime actualPaymentDate,
                                Money lateFee,
                                String transactionRef,
                                String receiptUrl) {

        if (this.status == PaymentStatus.PAID) {
            throw new IllegalStateException("Payment has already been settled");
        }

        Objects.requireNonNull(newAmountPaid, "Amount paid cannot be null");
        Objects.requireNonNull(actualPaymentDate, "Payment date cannot be null");

        if (this.expectedAmount.getCurrency() != newAmountPaid.getCurrency()) {
            throw new IllegalArgumentException("Payment currency does not match expected currency");
        }

        // expectedAmount ya incluye el total esperado base
        if (newAmountPaid.getAmount().compareTo(this.expectedAmount.getAmount()) < 0) {
            throw new IllegalArgumentException("Amount paid is less than expected total");
        }

        this.amountPaid = newAmountPaid;
        this.paymentDate = actualPaymentDate;

        if (lateFee != null) {
            if (lateFee.getCurrency() != expectedAmount.getCurrency()) {
                throw new IllegalArgumentException("Late fee currency does not match expected currency");
            }
            this.lateFeeApplied = lateFee;
        } else {
            this.lateFeeApplied = new Money(BigDecimal.ZERO, expectedAmount.getCurrency());
        }

        this.transactionReference = transactionRef;
        this.paymentReceiptUrl = receiptUrl;
        this.status = PaymentStatus.PAID;
        touch();
    }

    public void markAsOverdue(LocalDate currentDate) {
        if (this.status == PaymentStatus.PENDING && this.dueDate != null && currentDate.isAfter(this.dueDate)) {
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

    // === MÉTODOS DE CONSULTA ===

    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    public boolean isOverdue() {
        return this.status == PaymentStatus.OVERDUE;
    }

    public boolean isPending() {
        return this.status == PaymentStatus.PENDING;
    }

    public Money getTotalPaid() {
        if (this.status != PaymentStatus.PAID || this.amountPaid == null) {
            return new Money(BigDecimal.ZERO, expectedAmount.getCurrency());
        }
        return this.amountPaid;
    }

    public Money getTotalExpected() {
        Money total = this.expectedAmount;
        if (this.lateFeeApplied != null) {
            total = total.add(this.lateFeeApplied);
        }
        return total;
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // === GETTERS ===
    public UUID getId() { return id; }
    public UUID getReferenceId() { return referenceId; }
    public PaymentTarget getTarget() { return target; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public Money getExpectedAmount() { return expectedAmount; }
    public Money getAmountPaid() { return amountPaid; }
    public Money getLateFeeApplied() { return lateFeeApplied; }
    public PaymentStatus getStatus() { return status; }
    public String getTransactionReference() { return transactionReference; }
    public String getPaymentReceiptUrl() { return paymentReceiptUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}