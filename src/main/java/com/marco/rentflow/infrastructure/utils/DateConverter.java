package com.marco.rentflow.infrastructure.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateConverter {
    private static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");

    private DateConverter() {
        // Constructor privado para clase utilitaria
    }

    public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        return localDateTime != null
                ? localDateTime.atZone(CHILE_ZONE)
                : null;
    }
}

/**
 * La línea DateConverter.toZonedDateTime(domain.getAcceptedAt()) puede pasar lo sgte:
 * En TenantInvitation, acceptedAt puede ser null (invitación PENDING aún no aceptada).
 * Por eso DateConverter.toZonedDateTime(LocalDateTime) devuelve null si el input es null
 * Si no maneja nulls, voy a tener NPE en cada invitación no aceptada.
 */