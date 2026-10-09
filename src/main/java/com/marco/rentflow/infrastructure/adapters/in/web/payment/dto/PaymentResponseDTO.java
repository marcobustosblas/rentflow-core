package com.marco.rentflow.infrastructure.adapters.in.web.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponseDTO(
        UUID id,
        UUID referenceId,
        String target,
        BigDecimal expectedAmount,
        String currency,
        BigDecimal amountPaid,
        BigDecimal lateFeeApplied,
        String status,
        LocalDate dueDate,
        LocalDateTime paymentDate,
        String transactionReference,
        String paymentReceiptUrl,
        LocalDateTime createdAt
) {}
