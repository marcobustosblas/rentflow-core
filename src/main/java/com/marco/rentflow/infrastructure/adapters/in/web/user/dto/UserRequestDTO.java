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
        String password, // El usuario lo envía plano. En la W9 el Caso de Uso will hash it.

        @NotBlank(message = "RUT is required")
        String rut,

        String phoneNumber, // como es opcional, no le pongo @NotBlank

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "^(TENANT|LANDLORD)$", message = "Role must be TENANT or LANDLORD")
        String role
) {}
