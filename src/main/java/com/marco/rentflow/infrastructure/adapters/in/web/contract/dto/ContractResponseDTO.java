package com.marco.rentflow.infrastructure.adapters.in.web.contract.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ContractResponseDTO(
        UUID id,
        UUID propertyId,
        UUID tenantId,
        UUID landlordId,
        BigDecimal monthlyRentAmount,
        BigDecimal depositAmount,
        String currency,
        Integer paymentDueDay,
        BigDecimal dailyPenaltyRate,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {}