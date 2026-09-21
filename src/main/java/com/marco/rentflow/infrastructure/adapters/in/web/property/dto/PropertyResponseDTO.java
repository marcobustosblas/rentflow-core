package com.marco.rentflow.infrastructure.adapters.in.web.property.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PropertyResponseDTO(
        UUID id,
        UUID landlordId,
        UUID bankAccountId,
        String address,
        BigDecimal monthlyRentAmount,
        String currency,
        String status
) {}