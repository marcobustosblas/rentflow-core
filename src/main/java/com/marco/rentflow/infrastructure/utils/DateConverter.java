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