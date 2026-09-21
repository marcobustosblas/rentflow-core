package com.marco.rentflow.infrastructure.config;

import com.marco.rentflow.core.application.port.out.NotificationSenderPort;
import com.marco.rentflow.core.application.usecase.payment.PaymentWebhookDispatcher;
import com.marco.rentflow.core.application.usecase.payment.ProcessPaymentUseCase;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentUseCaseConfig {

    // se agregó esto para registrar ProcessPaymentUseCase como Spring Bean y permitir la inyeccion en PaymentController
    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(
            PaymentRepository paymentRepository,
            NotificationSenderPort notificationSenderPort) {
        return new ProcessPaymentUseCase(paymentRepository, notificationSenderPort);
    }

    @Bean
    public PaymentWebhookDispatcher paymentWebhookDispatcher(
            ProcessPaymentUseCase processPaymentUseCase) {
        return new PaymentWebhookDispatcher(processPaymentUseCase);
    }

}
