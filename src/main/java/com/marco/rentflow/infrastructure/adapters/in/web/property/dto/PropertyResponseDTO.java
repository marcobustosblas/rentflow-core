package com.marco.rentflow.infrastructure.adapters.in.web.property.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PropertyResponseDTO {

    private UUID id;
    private UUID landlordId;
    private UUID bankAccountId;
    private String address;
    private BigDecimal monthlyRentAmount;
    private String currency;
    private String status;

    public PropertyResponseDTO() {}

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
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

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
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

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

}
