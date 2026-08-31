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
                request.getTenantId(),
                request.getContractId(),
                request.getPaymentDate()
        );
        return ResponseEntity.ok(new PaymentCheckoutResponseDTO(checkoutUrl));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@Valid @RequestBody PaymentWebhookRequestDTO request) {
        Currency currency = Currency.valueOf(request.getCurrency());
        Money amountPaid = new Money(request.getAmountPaid(), currency);

        processPaymentUseCase.execute(
                request.getIdempotencyKey(),
                amountPaid,
                request.getPaymentDate(),
                request.getTransactionRef(),
                request.getReceiptUrl()
        );
        return ResponseEntity.ok().build();
    }

}
