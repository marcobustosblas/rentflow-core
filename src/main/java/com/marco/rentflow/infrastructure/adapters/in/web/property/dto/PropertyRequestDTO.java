package com.marco.rentflow.infrastructure.adapters.in.web.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PropertyRequestDTO(
        @NotBlank(message = "Address is required")
        String address,

        @NotNull(message = "Landlord ID is required")
        UUID landlordId,

        @NotNull(message = "Bank Account ID is required")
        UUID bankAccountId,

        @NotNull(message = "Rent amount is required")
        @Positive(message = "Rent amount must be greater than zero")
        BigDecimal monthlyRentAmount,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^(CLP|UF|USD)$", message = "Currency must be CLP, UF, or USD")
        String currency
) {}