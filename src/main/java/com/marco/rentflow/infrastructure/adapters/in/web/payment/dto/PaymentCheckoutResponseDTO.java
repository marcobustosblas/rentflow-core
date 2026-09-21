package com.marco.rentflow.infrastructure.adapters.in.web.payment.dto;

import java.util.UUID;

public record PaymentCheckoutResponseDTO(
        UUID paymentId,
        String checkoutUrl,
        String status
) {}