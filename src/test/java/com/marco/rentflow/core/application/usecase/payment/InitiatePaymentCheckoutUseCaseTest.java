package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.application.port.out.PaymentGatewayPort;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentStatus;
import com.marco.rentflow.core.domain.payment.PaymentTarget;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InitiatePaymentCheckoutUseCase - Pruebas de Checkout e Idempotencia")
public class InitiatePaymentCheckoutUseCaseTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayPort paymentGatewayPort;

    @InjectMocks
    private InitiatePaymentCheckoutUseCase useCase;

    private UUID tenantId;
    private UUID contractId;
    private UUID propertyId;
    private UUID landlordId;
    private RentalContract contract;
    private LocalDate paymentDate;
    private LocalDate dueDate;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        contractId = UUID.randomUUID();
        propertyId = UUID.randomUUID();
        landlordId = UUID.randomUUID();
        paymentDate = LocalDate.of(2026, 3, 5); // 5 de Marzo 2026

        Money rent = new Money(new BigDecimal("500000"), Currency.CLP);
        Money deposit = new Money(new BigDecimal("500000"), Currency.CLP);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        contract = RentalContract.create(
                propertyId, tenantId, landlordId, rent, deposit, 5, new BigDecimal("0.01"), startDate, endDate
        );
        // Usamos la misma fecha de vencimiento que calcula el contrato (ej. 2026-03-05)
        dueDate = contract.calculatePaymentDueDate(paymentDate.getYear(), paymentDate.getMonthValue());
    }

    @Nested
    @DisplayName("1. Seguridad e Identificación de Inquilino (IDOR)")
    class AuthorizationTests {

        @Test
        @DisplayName("Debe lanzar excepción si el contrato no existe")
        void shouldThrowExceptionWhenContractNotFound() {
            when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(tenantId, contractId, paymentDate)
            );

            assertEquals("Contract not found", exception.getMessage());
        }

        @Test
        @DisplayName("Debe rechazar la solicitud si el tenantId no corresponde al titular del contrato")
        void shouldThrowExceptionWhenTenantIsNotOwner() {
            UUID rogueTenantId = UUID.randomUUID();
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> useCase.execute(rogueTenantId, contractId, paymentDate)
            );

            assertEquals("Unauthorized tenant", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("2. Prevención de Duplicidad de Cobro (Regla del && Lógico)")
    class BillingDuplicityPreventionTests {

        @Test
        @DisplayName("Debe reutilizar el cobro PENDING existente sin crear un registro duplicado (Evita doble clic)")
        void shouldReuseExistingPendingPaymentWhenAvailable() {
            // ARRANGE
            String expectedIdempotencyKey = "PAY-" + contractId + "-2026-03";
            Money expectedTotal = new Money(new BigDecimal("500000"), Currency.CLP);

            // Simular un pago PENDING existente para el mismo vencimiento
            PaymentRecord existingPending = PaymentRecord.createPending(
                    contractId, PaymentTarget.RENT, dueDate, expectedTotal, expectedIdempotencyKey
            );

            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(paymentRepository.findByContractId(contractId)).thenReturn(List.of(existingPending));
            when(paymentGatewayPort.generateCheckoutUrl(eq(expectedIdempotencyKey), any(Money.class)))
                    .thenReturn("https://webpay.cl/checkout/session123");

            // ACT
            String checkoutUrl = useCase.execute(tenantId, contractId, paymentDate);

            // ASSERT
            assertEquals("https://webpay.cl/checkout/session123", checkoutUrl);
            verify(paymentRepository, never()).save(any()); // ¡REGLA CLAVE! No guarda duplicados
            verify(paymentGatewayPort, times(1)).generateCheckoutUrl(eq(expectedIdempotencyKey), any(Money.class));
        }

        @Test
        @DisplayName("Debe generar un nuevo PaymentRecord cuando no existe un pago PENDING para el mes actual")
        void shouldCreateNewPaymentRecordWhenNoPendingExists() {
            // ARRANGE
            when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
            when(paymentRepository.findByContractId(contractId)).thenReturn(List.of()); // Lista vacía
            when(paymentGatewayPort.generateCheckoutUrl(anyString(), any(Money.class)))
                    .thenReturn("https://webpay.cl/checkout/new_session");

            // ACT
            String checkoutUrl = useCase.execute(tenantId, contractId, paymentDate);

            // ASSERT
            assertEquals("https://webpay.cl/checkout/new_session", checkoutUrl);
            verify(paymentRepository, times(1)).save(any(PaymentRecord.class)); // Guarda la nueva intención de pago
        }
    }
}
