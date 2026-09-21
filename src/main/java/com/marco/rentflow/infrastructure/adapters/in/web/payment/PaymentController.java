package com.marco.rentflow.infrastructure.adapters.in.web.payment;

import com.marco.rentflow.core.application.usecase.payment.InitiatePaymentCheckoutUseCase;
import com.marco.rentflow.core.application.usecase.payment.PaymentWebhookDispatcher;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentCheckoutRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentCheckoutResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentWebhookRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final InitiatePaymentCheckoutUseCase checkoutUseCase;
    private final PaymentWebhookDispatcher webhookDispatcher; // 1. aquí ahora le meto el dispatcher

    public PaymentController(InitiatePaymentCheckoutUseCase checkoutUseCase, PaymentWebhookDispatcher paymentWebhookDispatcher) {
        this.checkoutUseCase = checkoutUseCase;
        this.webhookDispatcher = paymentWebhookDispatcher;
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentCheckoutResponseDTO> initiateCheckout(@Valid @RequestBody PaymentCheckoutRequestDTO request) {
        InitiatePaymentCheckoutUseCase.CheckoutResult result = checkoutUseCase.execute(
                request.userId(),
                request.referenceId(),
                request.paymentTarget(),
                request.paymentDate()
        );
        // 2. Instance el Record de salida pasando los 3 argumentos obligatorios
        // Dejo el UUID en null por ahora, ya que el Gateway de pago se encargará del ID en esta etapa
        return ResponseEntity.ok(new PaymentCheckoutResponseDTO(null, result.checkoutResult(), "PENDING"));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@Valid @RequestBody PaymentWebhookRequestDTO request) {
        Currency currency = Currency.valueOf(request.currency());
        Money amountPaid = new Money(request.amountPaid(), currency);

        // 3. El Controlador ahora actúa como simple pasarela, el Dispatcher toma el control
        webhookDispatcher.dispatcher(
                request.idempotencyKey(),
                amountPaid,
                request.paymentDate(),
                request.transactionRef(),
                request.receiptUrl()
        );
        return ResponseEntity.ok().build();
    }

}
