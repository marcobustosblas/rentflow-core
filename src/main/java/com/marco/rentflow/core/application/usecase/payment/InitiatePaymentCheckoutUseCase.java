package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.application.port.out.PaymentGatewayPort;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class InitiatePaymentCheckoutUseCase {

    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;

    public InitiatePaymentCheckoutUseCase(ContractRepository contractRepository, PaymentRepository paymentRepository,
                                          PaymentGatewayPort gatewayPort) {
        this.contractRepository = contractRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGatewayPort = gatewayPort;
    }

    /* 1 */
    public String execute(UUID tenantId, UUID contractId, LocalDate paymentDate) {

        /* 2 */
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        /* 3 */
        if (!contract.getTenantId().equals(tenantId)) {
            throw new IllegalStateException("Unauthorized tenant");
        }

        /* 4 calculo de due date */
        LocalDate dueDate = contract.calculatePaymentDueDate(paymentDate.getYear(), paymentDate.getMonthValue());
        Money calculateTotal = contract.calculateTotalWithPenalty(paymentDate, dueDate);

        /* 5, 6, 7 - Reutilizar cobro pendiente si ya existe para este contrato y vencimiento */
        Optional<PaymentRecord> existingPending = paymentRepository.findByContractId(contractId).stream()
                .filter(p -> p.isPending() && p.getDueDate().equals(dueDate))
                .findFirst();

        PaymentRecord pendingPayment;
        if (existingPending.isPresent()) {
            pendingPayment = existingPending.get();
        } else {
            String idempotencyKey = "PAY-" + contractId + "-" + dueDate.getYear() + "-" + String.format("%02d", dueDate.getMonthValue());
            pendingPayment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, calculateTotal, idempotencyKey);
            paymentRepository.save(pendingPayment);
        }

        /* 8 Pedirle a Webpay/Stripe el link de pago seguro */
        return paymentGatewayPort.generateCheckoutUrl(pendingPayment.getIdempotencyKey(), calculateTotal);

    }
}
