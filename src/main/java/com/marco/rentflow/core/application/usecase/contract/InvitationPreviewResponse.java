package com.marco.rentflow.core.application.usecase.contract;

import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Public-facing projection of a tenant invitation.
 * Exposes only what the tenant needs to see before accepting:
 * no tokens, no internal IDs, no landlord private data.
 */


public record InvitationPreviewResponse(
        String tenantEmail,
        String propertyAddress,
        BigDecimal monthlyRent,
        String currency,
        BigDecimal depositAmount,
        int paymentDueDay,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime expiresAt
) {}
