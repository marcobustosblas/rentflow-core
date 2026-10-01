package com.marco.rentflow.core.application.usecase.contract;

/**
 * Command for accepting a tenant invitation.
 * Note: the email is NOT part of the command. It is read from the
 * °invitation° itself, which prevents a stolen token from being used
 * to register a different email (replay attack prevention).
 */

public record RegisterTenantCommand(
        String rawToken,
        String fullName,
        String rut,
        String phoneNumber,
        String rawPassword
) {}
