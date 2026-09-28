package com.marco.rentflow.infrastructure.adapters.in.web.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequestDTO(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        String password, // TODO(W9-security): El payload viaja en claro por HTTPS, el UseCase will hash it.

        @NotBlank(message = "RUT is required")
        String rut,

        String phoneNumber, // como es opcional, no le pongo @NotBlank

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "(?i)^(LANDLORD)$", message = "Public registration is only allowed for LANDLORD accounts")
        String role
) {}
