package com.marco.rentflow.core.domain.common.ports.out;

import java.util.UUID;

/**
 * Puerto para enviar correos transaccionales.
 * El dominio sabe QUÉ hay que enviar; el adaptador sabrá CÓMO enviarlo.
 */

public interface EmailSenderPort {
    /**
     * Envía un correo de invitación a un inquilino.
     *
     * @param toEmail        correo del inquilino
     * @param tenantFullName nombre del inquilino para personalizar el correo
     * @param rawToken       el token de un solo uso para incluir en el link de aceptación
     * @param contractId     referencia del contrato para dar contexto
     */
    void sendTenantInvitation(
            String toEmail,
            String tenantFullName,
            String rawToken,
            UUID contractId
    );
}
