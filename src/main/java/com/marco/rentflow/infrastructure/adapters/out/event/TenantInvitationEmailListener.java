package com.marco.rentflow.infrastructure.adapters.out.event;

import com.marco.rentflow.core.domain.common.ports.out.EmailSenderPort;
import com.marco.rentflow.core.domain.contract.events.TenantInvitationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

public class TenantInvitationEmailListener {

    private static final Logger log = LoggerFactory.getLogger(TenantInvitationEmailListener.class);

    private final EmailSenderPort emailSender;
    private final RetryTemplate retryTemplate;

    public TenantInvitationEmailListener(EmailSenderPort emailSender, RetryTemplate retryTemplate) {
        this.emailSender = emailSender;
        this.retryTemplate = retryTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(TenantInvitationCreatedEvent event) {
        log.info("Enviando correo de invitación para el contrato {} a {}",
                event.contractId(), event.tenantEmail());
        // nunca debo imprimir el rawToken en los logs: solo va en el cuerpo del email
        // retryTemplate ejecuta un code con reintentos si falla.
        retryTemplate.execute(context -> {
            emailSender.sendTenantInvitation(
                    event.tenantEmail(),
                    event.tenantFullName(),
                    event.rawToken(),
                    event.contractId()
            );
            return null;
        });
    }
}
