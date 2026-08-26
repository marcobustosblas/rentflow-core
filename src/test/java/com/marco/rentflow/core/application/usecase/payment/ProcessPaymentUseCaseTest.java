package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.application.port.out.NotificationSenderPort;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentStatus;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPaymentUseCase - Pruebas de Procesamiento de Pago y Webhooks")
public class ProcessPaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationSenderPort notificationSenderPort;

    @InjectMocks
    private ProcessPaymentUseCase useCase;

    private String idempotencyKey;
    private Money expectedAmount;
    private LocalDate paymentDate;
    private UUID contractId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        idempotencyKey = "PAY-CONTRACT-123-2026-03";
        expectedAmount = new Money(new BigDecimal("500000"), Currency.CLP);
        paymentDate = LocalDate.now();
        contractId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("1. Idempotencia y Resiliencia ante Reintentos de Red")
    class IdempotencyAndResilienceTests {

        @Test
        @DisplayName("Debe lanzar excepción si el Token de Webpay / IdempotencyKey no existe en la BD")
        void shouldThrowExceptionWhenPaymentNotFound() {
            String fakeToken = "TOKEN-FANTASMA-123";

            when(paymentRepository.findByIdempotencyKey(fakeToken))
                    .thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(fakeToken, expectedAmount, paymentDate)
            );

            assertEquals("Pending payment not found for idempotency key: " + fakeToken, exception.getMessage());
            verify(paymentRepository, never()).save(any());
            verify(notificationSenderPort, never()).sendPaymentReceipt(any());
        }

        @Test
        @DisplayName("Debe retornar el pago sin modificar ni re-notificar si ya estaba en estado PAID (Idempotencia pura)")
        void shouldReturnExistingPaymentWhenAlreadyPaid() {
            PaymentRecord paidRecord = PaymentRecord.createPending(
                    contractId, tenantId, paymentDate, expectedAmount, idempotencyKey
            );
            paidRecord.registerPayment(expectedAmount, paymentDate, new Money(BigDecimal.ZERO, Currency.CLP), "REF-123", "receipt_url");

            when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(paidRecord));

            PaymentRecord result = useCase.execute(idempotencyKey, expectedAmount, paymentDate);

            assertEquals(PaymentStatus.PAID, result.getStatus());
            verify(paymentRepository, never()).save(any());
            verify(notificationSenderPort, never()).sendPaymentReceipt(any());
        }
    }

    @Nested
    @DisplayName("2. Integridad Financiera y Validación de Fraude")
    class FinancialIntegrityTests {

        @Test
        @DisplayName("Debe lanzar excepción si el monto pagado es menor al esperado (Rechazo de pago insuficiente)")
        void shouldThrowExceptionWhenPaymentIsInsufficient() {
            Money insufficientAmount = new Money(new BigDecimal("400000"), Currency.CLP); // 100 mil menos
            PaymentRecord pendingRecord = PaymentRecord.createPending(
                    contractId, tenantId, paymentDate, expectedAmount, idempotencyKey
            );

            when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(pendingRecord));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(idempotencyKey, insufficientAmount, paymentDate)
            );

            assertEquals("Amount paid is less than expected total", exception.getMessage());
            verify(paymentRepository, never()).save(any());
            verify(notificationSenderPort, never()).sendPaymentReceipt(any());
        }

        @Test
        @DisplayName("Debe rechazar el procesamiento si la moneda del pago no coincide con la moneda esperada del contrato")
        void shouldThrowExceptionWhenCurrencyMismatch() {
            Money usdAmount = new Money(new BigDecimal("500"), Currency.USD); // Moneda distinta (USD vs CLP)
            PaymentRecord pendingRecord = PaymentRecord.createPending(
                    contractId, tenantId, paymentDate, expectedAmount, idempotencyKey
            );

            when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(pendingRecord));

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(idempotencyKey, usdAmount, paymentDate)
            );

            assertEquals("Payment currency does not match expected currency", exception.getMessage());
            verify(paymentRepository, never()).save(any());
            verify(notificationSenderPort, never()).sendPaymentReceipt(any());
        }
    }

    @Nested
    @DisplayName("3. Procesamiento Exitoso de Pago")
    class SuccessfulPaymentTests {

        @Test
        @DisplayName("Debe registrar el pago, persisitr con referencia de transacción y notificar al usuario")
        void shouldProcessPaymentSuccessfullyWithReference() {
            PaymentRecord pendingRecord = PaymentRecord.createPending(
                    contractId, tenantId, paymentDate, expectedAmount, idempotencyKey
            );

            when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(pendingRecord));
            when(paymentRepository.save(any(PaymentRecord.class)))
                    .thenReturn(pendingRecord);

            PaymentRecord result = useCase.execute(idempotencyKey, expectedAmount, paymentDate, "TBK-TRANS-999", "https://receipts.rentflow.cl/doc.pdf");

            assertEquals(PaymentStatus.PAID, result.getStatus());
            assertEquals("TBK-TRANS-999", result.getTransactionReference());
            assertEquals("https://receipts.rentflow.cl/doc.pdf", result.getPaymentReceiptUrl());

            verify(paymentRepository, times(1)).save(pendingRecord);
            verify(notificationSenderPort, times(1)).sendPaymentReceipt(pendingRecord);
        }

        @Test
        @DisplayName("Debe permitir ejecutar la sobrecarga básica de 3 parámetros usando idempotencyKey como referencia")
        void shouldProcessPaymentSuccessfullyWithBasicOverload() {
            PaymentRecord pendingRecord = PaymentRecord.createPending(
                    contractId, tenantId, paymentDate, expectedAmount, idempotencyKey
            );

            when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(pendingRecord));
            when(paymentRepository.save(any(PaymentRecord.class)))
                    .thenReturn(pendingRecord);

            PaymentRecord result = useCase.execute(idempotencyKey, expectedAmount, paymentDate);

            assertEquals(PaymentStatus.PAID, result.getStatus());
            assertEquals(idempotencyKey, result.getTransactionReference());
            verify(paymentRepository, times(1)).save(pendingRecord);
            verify(notificationSenderPort, times(1)).sendPaymentReceipt(pendingRecord);
        }
    }
}
