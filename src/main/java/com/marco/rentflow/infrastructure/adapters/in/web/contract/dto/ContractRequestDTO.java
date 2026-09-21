package com.marco.rentflow.infrastructure.adapters.in.web.contract.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractRequestDTO (
        @NotNull(message = "Property ID is required")
        UUID propertyId,

        @NotNull(message = "Tenant ID is required")
        UUID tenantId,
        @NotNull(message = "Landlord ID is required")
        UUID landlordId,

        // Datos Financieros

        @NotNull(message = "Rent amount is required")
        @Positive(message = "Rent amount must be greater than zero")
        BigDecimal rentAmount,

        @NotNull(message = "Deposit amount is required")
        @PositiveOrZero(message = "Deposit cannot be negative")
        BigDecimal depositAmount, // Lo que pide de garantía

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^(CLP|UF|USD)$", message = "Currency must be CLP, UF, or USD")
        String currency,

        // Reglas del Contrato

        @NotNull(message = "Payment due day is required")
        @Min(value = 1, message = "Payment day must be between 1 and 31")
        @Max(value = 31, message = "Payment day must be between 1 and 31")
        Integer paymentDueDay, // Día de pago (ej. 5)

        // @NotNull(message = "Penalty rate is required") esto esta mal por que la multa es opcional
        @PositiveOrZero(message = "Penalty rate cannot be negative")
        BigDecimal dailyPenaltyRate, // Multa diaria (ej. 0.01 para 1%)

        // Fechas
        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or in the future")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @Future(message = "End date must be a future date")
        LocalDate endDate
) {}

