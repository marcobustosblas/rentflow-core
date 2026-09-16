package com.marco.rentflow.infrastructure.adapters.in.web.payment;

import com.marco.rentflow.core.application.usecase.payment.InitiatePaymentCheckoutUseCase;
import com.marco.rentflow.core.application.usecase.payment.ProcessPaymentUseCase;
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
    private final ProcessPaymentUseCase processPaymentUseCase;

    public PaymentController(
            InitiatePaymentCheckoutUseCase initiatePaymentCheckoutUseCase,
            ProcessPaymentUseCase processPaymentUseCase) {
        this.checkoutUseCase = initiatePaymentCheckoutUseCase;
        this.processPaymentUseCase = processPaymentUseCase;
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentCheckoutResponseDTO> initiateCheckout(@Valid @RequestBody PaymentCheckoutRequestDTO request) {
        String checkoutUrl = checkoutUseCase.execute(
                request.userId(),
                request.referenceId(),
                request.paymentTarget(),
                request.paymentDate()
        );
        // 2. Instancio el Record de salida pasando los 3 argumentos obligatorios
        // Dejo el UUID en null por ahora, ya que el Gateway de pago se encargará del ID en esta etapa
        return ResponseEntity.ok(new PaymentCheckoutResponseDTO(null, checkoutUrl, "PENDING"));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@Valid @RequestBody PaymentWebhookRequestDTO request) {
        Currency currency = Currency.valueOf(request.currency());
        Money amountPaid = new Money(request.amountPaid(), currency);

        // Ahora request.getPaymentDate() devuelve un LocalDateTime, encajando perfecto con el caso de uso
        processPaymentUseCase.execute(
                request.idempotencyKey(),
                amountPaid,
                request.paymentDate(),
                request.transactionRef(),
                request.receiptUrl()
        );
        return ResponseEntity.ok().build();
    }

}
