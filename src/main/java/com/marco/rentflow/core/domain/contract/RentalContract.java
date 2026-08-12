package com.marco.rentflow.core.domain.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class RentalContract {
    private final UUID id;
    private final UUID propertyId;
    private final UUID tenantId;
    private final UUID landlordId;

    private BigDecimal monthlyRent; // Ingreso mensual recurrente
    private BigDecimal depositAmount; // Mes de Garantía (pago único inicial)
    private int paymentDueDay; // Día del mes en que vence el arriendo (ej.: 5 para el 5 de cada mes)
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 1. Constructor para NUEVO contrato (Creación desde cero)
    public RentalContract(UUID propertyId, UUID tenantId, UUID landlordId,
                          BigDecimal monthlyRent, BigDecimal depositAmount,
                          int paymentDueDay, LocalDate startDate, LocalDate endDate) {
        this(
                UUID.randomUUID(),
                propertyId,
                tenantId,
                landlordId,
                monthlyRent,
                depositAmount,
                paymentDueDay,
                startDate,
                endDate,
                ContractStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // 2. Constructor Completo (Reconstitución desde la Base de Datos)
    public RentalContract(UUID id, UUID propertyId, UUID tenantId, UUID landlordId,
                          BigDecimal monthlyRent, BigDecimal depositAmount, int paymentDueDay,
                          LocalDate startDate, LocalDate endDate, ContractStatus status,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Contract ID cannot be null");
        this.propertyId = Objects.requireNonNull(propertyId, "Property ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.landlordId = Objects.requireNonNull(landlordId, "Landlord ID cannot be null");

        this.monthlyRent = validatePositiveAmount(monthlyRent, "Monthly rent must be greater than zero");
        this.depositAmount = validateNonNegativeAmount(depositAmount, "Deposit amount cannot be negative");
        this.paymentDueDay = validatePaymentDueDay(paymentDueDay);
        this.startDate = Objects.requireNonNull(startDate, "Start date cannot be null");
        this.endDate = Objects.requireNonNull(endDate, "End date cannot be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        this.status = Objects.requireNonNull(status, "Contract status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // REGLAS Y VALIDACIONES DE DOMINIO

    public boolean isActive() {
        return this.status == ContractStatus.ACTIVE;
    }

    private static BigDecimal validatePositiveAmount(BigDecimal amount, String message) {
        Objects.requireNonNull(amount, message);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
        return amount;
    }

    private static BigDecimal validateNonNegativeAmount(BigDecimal amount, String message) {
        Objects.requireNonNull(amount, message);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
        return amount;
    }

    private static int validatePaymentDueDay(int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Payment due day must be between 1 and 31");
        }
        return day;
    }

    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS
    public UUID getId() { return id; }
    public UUID getPropertyId() { return propertyId; }
    public UUID getTenantId() { return tenantId; }
    public UUID getLandlordId() { return landlordId; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public int getPaymentDueDay() { return paymentDueDay; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ContractStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}