package com.marco.rentflow.infrastructure.adapters.in.web.payment;

import com.marco.rentflow.core.application.usecase.payment.GetPaymentByIdUseCase;
import com.marco.rentflow.core.application.usecase.payment.InitiatePaymentCheckoutUseCase;
import com.marco.rentflow.core.application.usecase.payment.ListPaymentsByContractUseCase;
import com.marco.rentflow.core.application.usecase.payment.PaymentWebhookDispatcher;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentCheckoutRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentCheckoutResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentWebhookRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.mapper.PaymentRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final InitiatePaymentCheckoutUseCase checkoutUseCase;
    private final PaymentWebhookDispatcher webhookDispatcher; // 1. aquí ahora le meto el dispatcher
    private final GetPaymentByIdUseCase getPaymentByIdUseCase;
    private final ListPaymentsByContractUseCase listPaymentsByContractUseCase;
    private final PaymentRestMapper paymentRestMapper;

    public PaymentController(
            InitiatePaymentCheckoutUseCase checkoutUseCase,
            PaymentWebhookDispatcher webhookDispatcher,
            GetPaymentByIdUseCase getPaymentByIdUseCase,
            ListPaymentsByContractUseCase listPaymentsByContractUseCase,
            PaymentRestMapper paymentRestMapper) {
        this.checkoutUseCase = checkoutUseCase;
        this.webhookDispatcher = webhookDispatcher;
        this.getPaymentByIdUseCase = getPaymentByIdUseCase;
        this.listPaymentsByContractUseCase = listPaymentsByContractUseCase;
        this.paymentRestMapper = paymentRestMapper;
    }

    @PostMapping("/checkout")
    // TODO(M5-security): @PreAuthorize("hasRole('TENANT')")
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
    // TODO(M5-security): @PreAuthorize("isAnonymous()") // webhook público, firmado por el proveedor
    // TODO(M5-security): verify webhook signature from the payment provider
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

    @GetMapping("/{id}")
    // TODO(M5-security): @PreAuthorize("hasRole('LANDLORD') or hasRole('TENANT')")
    public ResponseEntity<PaymentResponseDTO> getById(@PathVariable UUID id) {
        PaymentRecord payment = getPaymentByIdUseCase.execute(id);
        return ResponseEntity.ok(paymentRestMapper.toResponseDto(payment));
    }

    @GetMapping("/contract/{contractId}")
    // TODO(M5-security): @PreAuthorize("hasRole('LANDLORD') or hasRole('TENANT')")
    public ResponseEntity<List<PaymentResponseDTO>> listByContract(@PathVariable UUID contractId) {
        List<PaymentRecord> payments = listPaymentsByContractUseCase.execute(contractId);
        List<PaymentResponseDTO> responses = paymentRestMapper.toResponseDtoList(payments);
        return ResponseEntity.ok(responses);
    }

}

/**
 * Nota importante sobre /webhook: es un endpoint público pero no anónimo.
 * Lo llama el gateway de pago (Stripe, Transbank), no un usuario.
 * Debe validar la firma HMAC que envía el proveedor.
 * Ese es un TODO de seguridad crítico que en M5 se resolverá
 */
