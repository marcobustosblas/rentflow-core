package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.contract.ContractSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateContractCommand (
        UUID propertyId,
        UUID landlordId,
        String tenantEmail,
        String tenantFullName,
        String tenantRut,
        BigDecimal rentAmount,
        BigDecimal depositAmount,
        String currency,
        int paymentDueDay,
        BigDecimal dailyPenaltyRate,
        LocalDate startDate,
        LocalDate endDate,
        String contractDocumentUrl,
        ContractSource source
) {}
