package com.marco.rentflow.infrastructure.adapters.in.web.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class PropertyRequestDTO {

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Landlord ID is required")
    private UUID landlordId;

    @NotNull(message = "Bank Account ID is required")
    private UUID bankAccountId;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent amount must be greater than zero")
    private BigDecimal monthlyRentAmount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(CLP|UF|USD)$", message = "Currency must be CLP, UF, or USD")
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
