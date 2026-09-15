package com.marco.rentflow.infrastructure.adapters.in.web.user.dto;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String fullName,
        String email,
        String rut,
        String status
) {}
