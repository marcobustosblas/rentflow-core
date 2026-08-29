package com.marco.rentflow.infrastructure.adapters.in.web.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ContractResponseDTO {

    private UUID id;
    private UUID propertyId;
    private UUID tenantId;
    private UUID landlordId;
    private BigDecimal monthlyRentAmount;
    private BigDecimal depositAmount;
    private String currency;
    private Integer paymentDueDay;
    private BigDecimal dailyPenaltyRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;

    public ContractResponseDTO() {};

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
