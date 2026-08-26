package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.application.port.out.NotificationSenderPort;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentStatus;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcessPaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationSenderPort notificationSenderPort;

    @InjectMocks
    private ProcessPaymentUseCase useCase;

    @Test
    @DisplayName("Debe lanzar excepción si el Token de Webpay no existe en la BD")
    void shouldThrowExceptionWhenPaymentNotFound() {

        // ARRANGE
        String fakeToken = "TOKEN-FANTASMA-123";
        Money amountPaid = new Money(new BigDecimal("500000"), Currency.CLP);
        LocalDate paymentDate = LocalDate.now();

        // WHEN
        when(paymentRepository.findByIdempotencyKey(fakeToken))
                .thenReturn(Optional.empty());

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(fakeToken, amountPaid, paymentDate)
        );

        assertEquals("Pending payment not found for idempotency key: " + fakeToken, exception.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe retornar el pago sin guardar si ya estaba en estado PAID")
    void shouldReturnExistingPaymentWhenAlreadyPaid() {

        // ARRANGE
        String idempotencyKey = "PAY-123";
        Money amountPaid = new Money(new BigDecimal("500000"), Currency.CLP);
        LocalDate paymentDate = LocalDate.now();

        // Crear un pago y pasarle manualmente a PAID
        PaymentRecord paidRecord = PaymentRecord.createPending(
                UUID.randomUUID(), UUID.randomUUID(), paymentDate, amountPaid, idempotencyKey);
        paidRecord.registerPayment(amountPaid, paymentDate, new Money(BigDecimal.ZERO, Currency.CLP), "REF", "url");

        // WHEN
        when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(paidRecord));

        // ACT
        PaymentRecord result = useCase.execute(idempotencyKey, amountPaid, paymentDate);

        // ASSERT
        assertEquals(PaymentStatus.PAID, result.getStatus());
        verify(paymentRepository, never()).save(any());
        verify(notificationSenderPort, never()).sendPaymentReceipt(any());

    }

    @Test
    @DisplayName("Debe lanzar excepción si el monto pagado es menor al esperado")
    void shouldThrowExceptionWhenPaymentIsInsufficient() {

        // ARRANGE
        String idempotencyKey = "PAY-123";
        Money expectedAmount = new Money(new BigDecimal("500000"), Currency.CLP);
        Money insufficientAmount = new Money(new BigDecimal("400000"), Currency.CLP); // 100 mil menos
        LocalDate paymentDate = LocalDate.now();

        PaymentRecord pendingRecord = PaymentRecord.createPending(
                UUID.randomUUID(), UUID.randomUUID(), paymentDate, expectedAmount, idempotencyKey
        );

        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(pendingRecord));

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(idempotencyKey, insufficientAmount, paymentDate)
        );

        assertEquals("Amount paid is less than expected total", exception.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe registrar el pago, persistir y notificar exitosamente")
    void shouldProcessPaymentSuccessfully() {

        // ARRANGE
        String idempotencyKey = "PAY-123";
        Money amountPaid = new Money(new BigDecimal("500000"), Currency.CLP);
        LocalDate paymentDate = LocalDate.now();

        PaymentRecord pendingRecord = PaymentRecord.createPending(
                UUID.randomUUID(), UUID.randomUUID(), paymentDate, amountPaid, idempotencyKey
        );

        // WHEN
        when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(pendingRecord));
        when(paymentRepository.save(any(PaymentRecord.class)))
                .thenReturn(pendingRecord);

        // ACT
        PaymentRecord result = useCase.execute(idempotencyKey, amountPaid, paymentDate, "TBK-123", "url.com");

        // ASSERT
        assertEquals(PaymentStatus.PAID, result.getStatus());
        assertEquals("TBK-123", result.getTransactionReference());
        verify(paymentRepository, times(1)).save(pendingRecord); // Asegura que se guardó exactamente 1 vez
        verify(notificationSenderPort, times(1)).sendPaymentReceipt(pendingRecord); // Asegura que se envió el correo
    }

}
