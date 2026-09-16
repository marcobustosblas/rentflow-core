package com.marco.rentflow.infrastructure.adapters.in.web.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record PaymentCheckoutRequestDTO (

    @NotNull(message = "Tenant or Landlord ID is required")
    UUID userId,

    @NotNull(message = "Reference ID is required")
    UUID referenceId,

    @NotBlank(message = "Payment target is required")
    @Pattern(regexp = "^(RENT|SAAS)$", message = "Target must be RENT or SAAS")
    String paymentTarget,

    @NotNull(message = "Payment date is required")
    LocalDate paymentDate

) {}

