package com.marco.rentflow.infrastructure.adapters.in.web.exception;

import java.time.LocalDateTime;

public record ErrorResponse (
        String message,
        int status,
        LocalDateTime timestamp
) {}
