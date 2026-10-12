package com.marco.rentflow.infrastructure.adapters.in.web.auth.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Public-facing preview of an invitation.
 *
 * Shown to the tenant BEFORE they accept, so they see exactly
 * what they are signing. Contains only data the tenant has the
 * right to see: no tokens, no contract IDs, no internal state.
 *
 * The email, fullName and RUT are included here so the frontend
 * can render them as read-only fields. The frontend must NOT
 * allow editing them: the backend ignores any change anyway.
 */
public record InvitationPreviewDTO(
        String tenantEmail,
        String tenantFullName,
        String tenantRut,
        String propertyAddress,
        BigDecimal monthlyRent,
        String currency,
        BigDecimal depositAmount,
        int paymentDueDay,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime expiresAt
) {}
