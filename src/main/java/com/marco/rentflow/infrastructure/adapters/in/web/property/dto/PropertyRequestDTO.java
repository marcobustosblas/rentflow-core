package com.marco.rentflow.infrastructure.adapters.in.web.property.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PropertyRequestDTO {

    private String address;
    private UUID landlordId;
    private UUID bankAccountId;

    private BigDecimal monthlyRentAmount;
    private String currency;

    public PropertyRequestDTO() {}

    // Getters y Setters
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public UUID getLandlordId() {
        return landlordId;
    }
    public void setLandlordId(UUID landlordId) {
        this.landlordId = landlordId;
    }

    public UUID getBankAccountId() {
        return bankAccountId;
    }
    public void setBankAccountId(UUID bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public BigDecimal getMonthlyRentAmount() {
        return monthlyRentAmount;
    }
    public void setMonthlyRentAmount(BigDecimal monthlyRentAmount) {
        this.monthlyRentAmount = monthlyRentAmount;
    }

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
