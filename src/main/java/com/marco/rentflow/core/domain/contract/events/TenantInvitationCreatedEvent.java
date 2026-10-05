package com.marco.rentflow.core.domain.contract.events;

import com.marco.rentflow.core.domain.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Se dispara cuando se guarda en base de datos una invitación para un contrato.
 *
 * El token sin encriptar (rawToken) se incluye a propósito: es el ÚNICO momento
 * en que existe en memoria (nunca se guarda en BD, solo su hash).
 * Los que escuchen este evento deben tratarlo como un dato sensible y nunca imprimirlo en logs.
 */

public record TenantInvitationCreatedEvent(
        UUID contractId,
        UUID landlordId,
        String tenantEmail,
        String tenantFullName,
        String rawToken,
        LocalDateTime occurredAt
) implements DomainEvent {}
