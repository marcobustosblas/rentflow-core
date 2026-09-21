package com.marco.rentflow.infrastructure.adapters.in.web.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentWebhookRequestDTO(

        @NotBlank(message = "Idempotency key is required")
        String idempotencyKey,

        @NotNull(message = "Amount paid is required")
        @Positive(message = "Amount paid must be greater than zero")
        BigDecimal amountPaid,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^(CLP|UF|USD)$", message = "Currency must be CLP, UF, or USD")
        String currency,

        @NotNull(message = "Payment date is required")
        LocalDateTime paymentDate,

        @NotBlank(message = "Transaction reference is required")
        String transactionRef,

        @NotBlank(message = "Receipt URL is required")
        String receiptUrl

) {}