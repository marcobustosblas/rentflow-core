package com.marco.rentflow.infrastructure.adapters.in.web.user.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String fullName,
        String email,
        String rut,
        String phoneNumber,
        String status,
        Set<String> roles,
        LocalDateTime created
        // agrego los roles como String para el JSON
) {}
