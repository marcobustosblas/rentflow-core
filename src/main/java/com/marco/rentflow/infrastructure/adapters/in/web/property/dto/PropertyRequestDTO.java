package com.marco.rentflow.infrastructure.adapters.in.web.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PropertyRequestDTO(
        @NotNull(message = "Landlord ID is required")
        UUID landlordId,

        UUID payoutAccountId, // opcional, puedo asignarlo after

        @NotBlank(message = "Address cannot be empty")
        String address,

        @NotNull(message = "Base price is mandatory")
        @Positive(message = "Rent amount must be greater than zero")
        BigDecimal basePrice,

        @NotBlank(message = "Currency is mandatory")
        @Pattern(regexp = "^(CLP|UF|USD)$", message = "Currency must be CLP, UF, or USD")
        String currency
) {}