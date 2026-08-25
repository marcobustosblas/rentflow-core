package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.application.port.out.NotificationSenderPort;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class ProcessPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final NotificationSenderPort notificationSenderPort;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository,
                                 ContractRepository contractRepository,
                                 NotificationSenderPort notificationSenderPort) {
        this.paymentRepository = paymentRepository;
        this.contractRepository = contractRepository;
        this.notificationSenderPort = notificationSenderPort;
    }

    /* 1 - El sistema recibe la orden de pago (Webpay/Stripe) */
    public PaymentRecord execute(UUID contractId, UUID actorTenantId, LocalDate paymentDate,
                                 Money amountPaid, String idempotencyKey) {
        /* 2 - Idempotencia: Prevenir doble cobro */
        Optional<PaymentRecord> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existingPayment.isPresent()) {
            // Retorna el pago exitoso anterior para evitar doble cobro
            return existingPayment.get();
        }

        /* 3 - Buscar el contrato */
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        /* 4 - Validar Tenant e IDOR */
        if (!contract.getTenantId().equals(actorTenantId)) {
            throw new IllegalStateException("The contract does not belong to the provided tenant");
        }

        /* 5 - Validar que el contrato esté en estado `ACTIVE` */
        if (!contract.isActive()) {
            throw new IllegalStateException("Contract is not ACTIVE. Cannot process payment.");
        }

        /* 6 - Calcular deuda real delegando al contrato */
        // Calculo cuál era el día límite de este mes en particular
        LocalDate dueDate = contract.calculatePaymentDueDate(paymentDate.getYear(), paymentDate.getMonthValue());
        // Ahora con esa fecha de vencimiento (due date) calculo el pago total:
        Money totalDue = contract.calculateTotalWithPenalty(paymentDate, dueDate);

        if (totalDue.isGreaterThan(amountPaid)) {
            throw new IllegalArgumentException("Insufficient payment amount. Total due is: " + totalDue.getAmount());
        }

        /* 7 - El sistema crea un `PaymentRecord` */
        PaymentRecord record = PaymentRecord.createPending(
                contractId, actorTenantId, dueDate, totalDue, idempotencyKey);
        record.registerPayment(amountPaid, paymentDate, );

    }

}
