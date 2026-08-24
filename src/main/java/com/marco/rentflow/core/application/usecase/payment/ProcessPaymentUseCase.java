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
    private final UserRepository userRepository;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository,
                                 ContractRepository contractRepository,
                                 NotificationSenderPort notificationSenderPort,
                                 UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.contractRepository = contractRepository;
        this.notificationSenderPort = notificationSenderPort;
        this.userRepository = userRepository;
    }

    /* 1 */
    public PaymentRecord execute(UUID contractId, UUID actorTenantId, LocalDate dueDate,
                                 Money expectedAmount, String idempotencyKey) {
        /* 2 */
        Optional<PaymentRecord> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existingPayment.isPresent()) {
            // Retorna el pago exitoso anterior para evitar doble cobro
            return existingPayment.get();
        }

        /* 3 */
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        /* 4 */
        User userTenant = userRepository.findById(actorTenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        if (contract.getTenantId().equals(actorTenantId)) {
            throw new IllegalStateException("The contract does not belong to the provided tenant");
        }

        /* 5 */
        PaymentRecord newPayment = PaymentRecord.createPending(
                contractId, actorTenantId, dueDate, expectedAmount, idempotencyKey
        );

        return paymentRepository.save(newPayment);
    }

}
