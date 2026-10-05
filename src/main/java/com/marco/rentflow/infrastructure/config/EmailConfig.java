package com.marco.rentflow.infrastructure.config;

import com.marco.rentflow.core.domain.common.ports.out.EmailSenderPort;
import com.marco.rentflow.infrastructure.adapters.out.event.TenantInvitationEmailListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class EmailConfig {

    /**
     * Plantilla de reintentos para correos salientes: 3 intentos con pausas exponenciales.
     * Maneja caídas temporales del servidor de correos sin romper todo el flujo.
     */
    @Bean
    public RetryTemplate invitationRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        ExponentialBackOffPolicy backOff =new ExponentialBackOffPolicy();
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000L);

        template.setBackOffPolicy(backOff);
        template.setRetryPolicy(new SimpleRetryPolicy(3));

        return template;
    }

    @Bean
    public TenantInvitationEmailListener tenantInvitationEmailListener(
            EmailSenderPort emailSender,
            RetryTemplate invitationRetryTemplate) {
        return new TenantInvitationEmailListener(emailSender, invitationRetryTemplate);
    }
}
