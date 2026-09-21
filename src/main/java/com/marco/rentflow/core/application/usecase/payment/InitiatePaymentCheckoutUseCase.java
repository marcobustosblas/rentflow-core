package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.application.port.out.PaymentGatewayPort;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentTarget;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;

import java.time.LocalDate;
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

    /* Recibe los IDs reales desde el Controller */
    public CheckoutResult execute(UUID userId, UUID referenceId, String paymentTarget, LocalDate paymentDate) {

        // 1. Transformar el String del Controller al Enum del Dominio
        PaymentTarget target = PaymentTarget.valueOf(paymentTarget.toUpperCase());

        LocalDate dueDate;
        Money calculateTotal;

        // 2. ENRUTADOR POLIMÓRFICO: Aplicar reglas según el Target
        switch (target) {
            case RENT -> {
                /* 2. Buscar el contrato */
                RentalContract contract = contractRepository.findById(referenceId)
                        .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

                /* 3. Validar autorización */
                if (!contract.getTenantId().equals(userId)) {
                    throw new IllegalStateException("Unauthorized tenant");
                }

                /* 4 calculo de fecha límite y total */
                dueDate = contract.calculatePaymentDueDate(paymentDate.getYear(), paymentDate.getMonthValue());
                calculateTotal = contract.calculateTotalWithPenalty(paymentDate, dueDate);
            }
            case SAAS -> {
                // Aquí irá la lógica de suscripciones de la Fase 2 (buscar Landlord, calcular cuota PRO, etc.)
                throw new UnsupportedOperationException("SAAS payments are not yet enabled in Phase 1");
            }
            default -> throw new IllegalArgumentException("Unsupported payment target");
        }

        // 3. Generar Idempotency Key blindada incluyendo el Target
        String idempotencyKey = "PAY-" + target.name() + "-" + referenceId + "-" +
                dueDate.getYear() + "-" + String.format("%02d", dueDate.getMonthValue());


        // 4. Buscar cobro pendiente o crear uno nuevo INYECTANDO el target dinámico
        PaymentRecord pendingPayment = paymentRepository.findByContractId(referenceId).stream()
                .filter(p -> p.isPending() && p.getDueDate().equals(dueDate) && p.getTarget()==target)
                .findFirst()
                .orElseGet(()-> {
                    PaymentRecord newPayment = PaymentRecord.createPending(
                            referenceId,
                            target,
                            dueDate,
                            calculateTotal,
                            idempotencyKey
                    );
                    return paymentRepository.save(newPayment);
                });

        /* Pedirle a Web.pay/Stripe el link de pago seguro */
        // 5. Solicitar URL a la pasarela (Stripe/Web.pay)
        String url = paymentGatewayPort.generateCheckoutUrl(pendingPayment.getIdempotencyKey(), calculateTotal);

        return new CheckoutResult(pendingPayment.getId(), url, pendingPayment.getStatus().name());

    }

    /* Estructura de salida exclusiva de la capa de Aplicación */
    public record CheckoutResult(UUID paymentId, String checkoutResult, String status){}
}


/*
(18-9,15:07hrs)
referenceId, // referenceId (El ID de la entidad que origina el cobro)
 */