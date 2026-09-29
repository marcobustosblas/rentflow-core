package com.marco.rentflow.core.domain.property;

public enum PropertyStatus {
    AVAILABLE,
    RESERVED,  // ← contrato pendiente, tenant aún no acepta
    RENTED,
    MAINTENANCE
}
