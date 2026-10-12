package com.marco.rentflow.core.application.usecase.contract;

/**
 * Command for accepting a tenant invitation.
 *
 * Design:
 *  - The email, full name and RUT are NOT part of the command.
 *    They are read from the persisted "invitation" (legal contract data),
 *    which prevents a stolen token from being used to register
 *    different identity data.
 *  - acceptedTerms is validated on the backend for audit purposes.
 */

public record RegisterTenantCommand(
        String rawToken,
        String phoneNumber,
        String rawPassword,
        boolean acceptedTerms
) {}
