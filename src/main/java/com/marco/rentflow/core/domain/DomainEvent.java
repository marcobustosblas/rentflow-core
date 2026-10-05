package com.marco.rentflow.core.domain;

import java.time.LocalDateTime;

/**
 * Interfaz marcadora para los eventos de dominio.
 * Los eventos de dominio expresan hechos que ya ocurrieron en el negocio:
 * "TenantInvitationCreated", "ContractSigned", etc.
 */

public interface DomainEvent {
    LocalDateTime occurredAt();
}
