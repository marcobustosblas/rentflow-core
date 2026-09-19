package com.marco.rentflow.infrastructure.config;

import com.marco.rentflow.core.application.port.out.NotificationSenderPort;
import com.marco.rentflow.core.application.port.out.PaymentGatewayPort;
import com.marco.rentflow.core.application.usecase.contract.CreateContractUseCase;
import com.marco.rentflow.core.application.usecase.payment.InitiatePaymentCheckoutUseCase;
import com.marco.rentflow.core.application.usecase.payment.ProcessPaymentUseCase;
import com.marco.rentflow.core.application.usecase.property.*;
import com.marco.rentflow.core.domain.bankaccount.ports.out.BankAccountRepository;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.subscription.ports.out.SubscriptionRepository;
import com.marco.rentflow.core.domain.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateContractUseCase createContractUseCase(
            PropertyRepository propertyRepository,
            ContractRepository contractRepository,
            UserRepository userRepository) {
        // Spring automáticamente inyectará el ContractPostgresAdapter aquí
        return new CreateContractUseCase(propertyRepository, contractRepository, userRepository);
    }

    // se agregó esto para proveer una implementación por defecto de PaymentGatewayPort para el Spring Context
    @Bean
    public PaymentGatewayPort paymentGatewayPort() {
        return (idempotencyKey, amount) -> "https://checkout.rentflow.com/pay/" + idempotencyKey;
    }

    // se agregó esto para proveer una implementacion por defecto de NotificationSenderPort para el Spring Context
    @Bean
    public NotificationSenderPort notificationSenderPort() {
        return paymentRecord -> {};
    }

    // se agregó esto para registrar InitiatePaymentCheckoutUseCase como Spring Bean y permitir la inyeccion en PaymentController
    @Bean
    public InitiatePaymentCheckoutUseCase initiatePaymentCheckoutUseCase(
            ContractRepository contractRepository,
            PaymentRepository paymentRepository,
            PaymentGatewayPort paymentGatewayPort) {
        return new InitiatePaymentCheckoutUseCase(contractRepository, paymentRepository, paymentGatewayPort);
    }

    // se agregó esto para registrar ProcessPaymentUseCase como Spring Bean y permitir la inyeccion en PaymentController
    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(
            PaymentRepository paymentRepository,
            NotificationSenderPort notificationSenderPort) {
        return new ProcessPaymentUseCase(paymentRepository, notificationSenderPort);
    }

    /* PROPERTY USES CASES */

    @Bean
    public CreatePropertyUseCase createPropertyUseCase(
            PropertyRepository propertyRepository,
            SubscriptionRepository subscriptionRepository,
            BankAccountRepository bankAccountRepository) {
        return new CreatePropertyUseCase(propertyRepository, subscriptionRepository, bankAccountRepository);
    }

    @Bean
    public GetPropertyUseCase getPropertyUseCase(PropertyRepository propertyRepository) {
        return new GetPropertyUseCase(propertyRepository);
    }

    @Bean
    public ListLandlordPropertiesUseCase listLandlordPropertiesUseCase(PropertyRepository propertyRepository) {
        return new ListLandlordPropertiesUseCase(propertyRepository);
    }

    @Bean
    public ListPropertiesByStatusUseCase listPropertiesByStatusUseCase(PropertyRepository propertyRepository) {
        return new ListPropertiesByStatusUseCase(propertyRepository);
    }

    @Bean
    public UpdatePropertyUseCase updatePropertyUseCase(
            PropertyRepository propertyRepository,
            BankAccountRepository bankAccountRepository) {
        return new UpdatePropertyUseCase(propertyRepository, bankAccountRepository);
    }

}
