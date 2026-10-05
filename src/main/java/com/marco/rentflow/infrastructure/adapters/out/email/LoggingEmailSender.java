package com.marco.rentflow.infrastructure.adapters.out.email;

import com.marco.rentflow.core.domain.common.ports.out.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementación temporal de EmailSenderPort para la Semana 9.
 *
 * En lugar de enviar un correo real, imprime el link de aceptación en la consola.
 * Esto me permite probar el flujo completo sin usar credenciales SMTP reales aún.
 *
 * TODO(W10): Reemplazar con el adaptador real de JavaMailSender (o proveedor SMTP).
 */
@Component
public class LoggingEmailSender implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void sendTenantInvitation(
            String toEmail,
            String tenantFullName,
            String rawToken,
            UUID contractId) {
        // En entorno de desarrollo (DEV), imprimimos el token para probar el flujo.
        // TODO(W10): Nunca imprimir tokens en logs cuando se use un proveedor de correos real
        log.info(
                "[CORREO DEV] Para: {} | Hola {}, acepta tu invitación aquí: " +
                "http://localhost:5173/accept?token={} (Contrato: {})",
                toEmail, tenantFullName, rawToken, contractId);
    }
}
