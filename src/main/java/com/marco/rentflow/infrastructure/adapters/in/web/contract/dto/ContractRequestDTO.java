package com.marco.rentflow.infrastructure.adapters.in.web.contract.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ContractRequestDTO {

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Landlord ID is required")
    private UUID landlordId;

    // Datos Financieros

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be greater than zero")
    private BigDecimal monthlyRentAmount;

    @NotNull(message = "Deposit amount is required")
    @PositiveOrZero(message = "Deposit cannot be negative")
    private BigDecimal depositAmount; // Lo que pide de garantía

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(CLP|UF|USD)$", message = "Currency must be CLP, UF, or USD")
    private String currency;

    // Reglas del Contrato

    @NotNull(message = "Payment due day is required")
    @Min(value = 1, message = "Payment day must be between 1 and 31")
    @Max(value = 31, message = "Payment day must be between 1 and 31")
    private Integer paymentDueDay; // Día de pago (ej. 5)

    @NotNull(message = "Penalty rate is required")
    @PositiveOrZero(message = "Penalty rate cannot be negative")
    private BigDecimal dailyPenaltyRate; // Multa diaria (ej. 0.01 para 1%)

    // Fechas
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    public ContractRequestDTO() {};

    public UUID getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(UUID propertyId) {
        this.propertyId = propertyId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(UUID landlordId) {
        this.landlordId = landlordId;
    }

    public BigDecimal getMonthlyRentAmount() {
        return monthlyRentAmount;
    }

    public void setMonthlyRentAmount(BigDecimal monthlyRentAmount) {
        this.monthlyRentAmount = monthlyRentAmount;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPaymentDueDay() {
        return paymentDueDay;
    }

    public void setPaymentDueDay(Integer paymentDueDay) {
        this.paymentDueDay = paymentDueDay;
    }

    public BigDecimal getDailyPenaltyRate() {
        return dailyPenaltyRate;
    }

    public void setDailyPenaltyRate(BigDecimal dailyPenaltyRate) {
        this.dailyPenaltyRate = dailyPenaltyRate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
