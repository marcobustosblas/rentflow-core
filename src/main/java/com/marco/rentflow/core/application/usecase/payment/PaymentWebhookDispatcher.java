package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentTarget;

import java.time.LocalDateTime;

public class PaymentWebhookDispatcher {

    private final ProcessPaymentUseCase processPaymentUseCase;
    /** 21-9-26, 15:15 hrs
     * Aquí inyectaré en el futuro casos de uso específicos como:
     * private final ActivateSubscriptionUseCase activateSubscriptionUseCase;
     * private final NotifyRentReceiptUseCase notifyRentReceiptUseCase;
     */

    public PaymentWebhookDispatcher(ProcessPaymentUseCase processPaymentUseCase) {
        this.processPaymentUseCase = processPaymentUseCase;
    }

    /**
     * Recibe los datos crudos del Webhook, procesa el pago y enruta el flujo
     * de business según el objetivo (RENT o SAAS).
     */
    public void dispatcher(String idempotencyKey, Money amountPaid, LocalDateTime actualPaymentDate, String transactionRef, String receiptUrl) {
        // 1. Procesar el pago en el dominio (agnóstico)
        // Esto cambia el estado a PAID y lo guarda en la base de datos
        PaymentRecord settledPayment = processPaymentUseCase.execute(
                idempotencyKey,
                amountPaid,
                actualPaymentDate,
                transactionRef,
                receiptUrl
        );

        if (settledPayment.getTarget() == PaymentTarget.RENT) {
            handleRentPayment(settledPayment);
        } else if (settledPayment.getTarget() == PaymentTarget.SAAS) {
            handleSaasPayment(settledPayment);
        } else {
            throw new UnsupportedOperationException("Unknown payment target: " + settledPayment.getTarget());
        }
    }

    private void handleRentPayment(PaymentRecord payment) {
        // Aquí delega a la lógica específica de arriendos.
        // Ej: notifyRentReceiptUseCase.execute(payment.getReferenceId());
        System.out.println("[DISPATCHER] RENT Payment detected. Generating rent receipt for Contract ID: "
                + payment.getReferenceId());
    }

    private void handleSaasPayment(PaymentRecord payment) {
        // Aquí delega a la lógica específica del software.
        // Ej: activateSubscriptionUseCase.execute(payment.getReferenceId());
        System.out.println("[DISPATCHER] SAAS Payment detected. Activating premium plan for Landlord ID: "
                + payment.getReferenceId());
    }

}
