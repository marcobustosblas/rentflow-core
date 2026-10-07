package com.marco.rentflow.infrastructure.adapters.in.web.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for accepting a tenant invitation.
 *
 * Design decisions:
 *  - The email, full name and RUT are NOT part of this DTO.
 *    They are read from the invitation (legal contract data).
 *    This prevents a stolen token from being used to register
 *    different identity data (replay attack prevention).
 *  - acceptedTerms is validated on the backend too (not just UX)
 *    so that we can audit explicit consent.
 */
public record TenantRegistrationRequestDTO(
        @NotBlank(message = "El token de invitación es obligatorio")
        String rawToken,

        // Optional: tenants may want to provide a phone number for 2FA later.
        String phoneNumber,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String rawPassword,

        @NotNull(message = "You must accept the contract terms")
        Boolean acceptedTerms
) {}
