package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.application.port.out.NotificationSenderPort;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final NotificationSenderPort notificationSenderPort;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository,
                                 NotificationSenderPort notificationSenderPort) {
        this.paymentRepository = paymentRepository;
        this.notificationSenderPort = notificationSenderPort;
    }

    /* 1 - Webhook recibe: Token de la transacción, Cuánto se pagó realmente, Cuándo se pagó */
    public PaymentRecord execute(String idempotencyKey, Money amountPaid, LocalDate actualPaymentDate) {

        // 2. Buscar la intención de pago previa (La cotización congelada)
        PaymentRecord payment = paymentRepository.findById(idempotencyKey)
                .orElseThrow(() -> new IllegalArgumentException("Pending payment not found"));

        // 3. Seguridad - Idempotencia
        if (payment.isPaid()) {
            return payment;
        }

        // 4 & 5. TRANSICIÓN DE ESTADO
        Money lateFeeZero = new Money(BigDecimal.ZERO, amountPaid.getCurrency());
        payment.registerPayment(amountPaid, actualPaymentDate, lateFeeZero, idempotencyKey, "url-recibo-aws-s3");

        // 6 & 7 Persistir y notificar
        PaymentRecord savedPayment = paymentRepository.save(payment);
        notificationSenderPort.sendPaymentReceipt(savedPayment);

        // 8
        return savedPayment;

    }

}
