package com.marco.rentflow.core.domain.payment;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentRecord Aggregate Domain Tests")
public class PaymentRecordTest {

    private final UUID contractId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();
    private final Money rentAmount = new Money(new BigDecimal("350000"), Currency.CLP);
    private final LocalDate dueDate = LocalDate.of(2026, 3, 5); // Vence el 5 de Marzo
    private final String idempotencyKey = "COBRO-MARZO-2026";

    @Nested
    @DisplayName("1-The Birth of Debt Collection")
    class CreationTests {

        @Test
        @DisplayName("It must generate a pending charge with $0 paid and no fines")
        void shouldCreatePendingPaymentSuccessfully() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            assertNotNull(payment.getId());
            assertEquals(PaymentStatus.PENDING, payment.getStatus());
            assertTrue(payment.isPending());
            assertEquals(idempotencyKey, payment.getIdempotencyKey());

            // Verificamos que nace con $0 pagado y $0 multa
            assertEquals(new BigDecimal("0"), payment.getPaidAmount().getAmount());
            assertEquals(new BigDecimal("0"), payment.getLateFeeApplied().getAmount());
        }
    }

    @Nested
    @DisplayName("2-The Model Tenant")
    class OnTimePaymentTests {

        @Test
        @DisplayName("Payment must be successfully recorded when the full amount is paid on time")
        void shouldRegisterPaymentSuccessfully() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            LocalDate paymentDate = LocalDate.of(2026, 3, 3); // Paga 2 días antes
            String transactionRef = "TEF-123456";
            String receiptUrl = "https://s3.aws.com/receipts/123.pdf";

            payment.registerPayment(rentAmount, paymentDate, null, transactionRef, receiptUrl);

            assertTrue(payment.isPaid());
            assertEquals(rentAmount.getAmount(), payment.getTotalPaid().getAmount());
            assertEquals(transactionRef, payment.getTransactionReference());
            assertEquals(receiptUrl, payment.getPaymentReceiptUrl());
        }
    }

    @Nested
    @DisplayName("3-The Delinquent Tenant and Late Fees")
    class OverduePaymentTests {

        @Test
        @DisplayName("Should change status to OVERDUE when current date exceeds due date")
        void shouldMarkAsOverdueWhenDatePasses() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            LocalDate currentDate = LocalDate.of(2026, 3, 6); // 6 de Marzo (Atrasado)
            payment.markAsOverdue(currentDate);

            assertTrue(payment.isOverdue());
        }

        @Test
        @DisplayName("Should accept payment when it includes the late fee")
        void shouldRegisterPaymentWithLateFee() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            Money lateFee = new Money(new BigDecimal("10500"), Currency.CLP); // Multa de 3 días
            Money totalToPay = rentAmount.add(lateFee); // $360.500

            payment.registerPayment(totalToPay, LocalDate.of(2026, 3, 8), lateFee, "TEF-999", "url");

            assertTrue(payment.isPaid());
            assertEquals(new BigDecimal("360500"), payment.getTotalPaid().getAmount());
            assertEquals(new BigDecimal("360500"), payment.getTotalExpected().getAmount());
        }

        @Nested
        @DisplayName("Late Fee Calculation by Days Overdue (Parametric Integration)")
        class LateFeeCalculationTests {

            @ParameterizedTest
            @DisplayName("Should calculate correct late fee for different days overdue")
            @CsvSource({
                    "1, 3500.00",    // 1 día de atraso → $3.500.00
                    "3, 10500.00",   // 3 días → $10.500.00
                    "5, 17500.00",   // 5 días → $17.500.00
                    "7, 24500.00",   // 7 días → $24.500.00
                    "10, 35000.00",  // 10 días → $35.000.00
                    "15, 52500.00",  // 15 días → $52.500.00
                    "30, 105000.00"  // 30 días → $105.000.00
            })
            void shouldCalculateLateFeeForDifferentDaysOverdue(int daysOverdue, String expectedLateFeeAmount) {
                // Variables necesarias para simular el contrato
                UUID localPropertyId = UUID.randomUUID();
                UUID localLandlordId = UUID.randomUUID();
                Money localStandardRent = new Money(new BigDecimal("350000"), Currency.CLP);
                Money localStandardDeposit = new Money(new BigDecimal("350000"), Currency.CLP);

                // Given: Contrato con arriendo de $350.000
                RentalContract contract = RentalContract.create(
                        localPropertyId, tenantId, localLandlordId,
                        localStandardRent, localStandardDeposit,
                        5,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31)
                );

                // Given: Registro de pago pendiente
                PaymentRecord payment = PaymentRecord.createPending(
                        contract.getId(), tenantId, dueDate, localStandardRent, idempotencyKey
                );

                // When: El inquilino paga 'X' días tarde
                LocalDate paymentDate = dueDate.plusDays(daysOverdue);
                BigDecimal dailyPenaltyRate = new BigDecimal("0.01"); // 1% diario

                // El contrato calcula la multa exacta
                Money lateFee = contract.calculateLateFee(paymentDate, dueDate, dailyPenaltyRate);

                // Sumo el total y lo registramos
                Money totalToPay = localStandardRent.add(lateFee);
                payment.registerPayment(totalToPay, paymentDate, lateFee, "TEF-" + daysOverdue, "url");

                // Then: Verifico que la matemática y el estado sean perfectos
                BigDecimal expectedLateFee = new BigDecimal(expectedLateFeeAmount);
                assertEquals(expectedLateFee, lateFee.getAmount());
                assertEquals(localStandardRent.getAmount().add(expectedLateFee), payment.getTotalPaid().getAmount());
                assertEquals(localStandardRent.getAmount().add(expectedLateFee), payment.getTotalExpected().getAmount());
            }
        }

        @Test
        @DisplayName("Should NOT mark as overdue if payment is not pending or date is not past due")
        void shouldNotMarkAsOverdueIfPaidOrNotPastDue() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            // 1: Intento marcarlo atrasado el mismo día de vencimiento (no debe cambiar)
            payment.markAsOverdue(dueDate);
            assertEquals(PaymentStatus.PENDING, payment.getStatus());

            // 2: Lo pago, y luego intento marcarlo como atrasado (no debe cambiar)
            payment.registerPayment(rentAmount, dueDate, null, "REF", "url");
            payment.markAsOverdue(dueDate.plusDays(5)); // Han pasado 5 días, pero ya está pagado

            assertEquals(PaymentStatus.PAID, payment.getStatus()); // Sigue pagado
        }
    }

    @Nested
    @DisplayName("4-The Cheating Tenant (Security Rules)")
    class EdgeCasesAndSecurityTests {

        @Test
        @DisplayName("Debe rechazar el pago si intenta pagar menos de lo que debe")
        void shouldThrowExceptionWhenPayingLessThanExpected() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            Money insufficientPayment = new Money(new BigDecimal("200000"), Currency.CLP); // Paga solo una parte

            assertThrows(IllegalArgumentException.class, () ->
                    payment.registerPayment(insufficientPayment, LocalDate.of(2026, 3, 5), null, "TEF", "url")
            );
        }

        @Test
        @DisplayName("Debe rechazar un segundo pago si ya estaba pagado")
        void shouldThrowExceptionWhenRegisteringPaymentTwice() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            payment.registerPayment(rentAmount,
                    LocalDate.of(2026, 3, 5),
                    null, "TEF-1", "url");

            // Intenta pagar de nuevo el mismo recibo
            assertThrows(IllegalStateException.class, () ->
                    payment.registerPayment(rentAmount,
                            LocalDate.of(2026, 3, 5),
                            null, "TEF-2", "url")
            );
        }

        @Test
        @DisplayName("Debe rechazar/cancelar un cobro si ya fue pagado (prevención de fraude)")
        void shouldThrowExceptionWhenCancellingPaidRecord() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            payment.registerPayment(rentAmount, LocalDate.of(2026, 3, 5), null, "TEF", "url");

            // El dueño intenta cancelarlo mágicamente fraudulentamente
            assertThrows(IllegalStateException.class, payment::cancel);
        }
    }

    // Test para pasar el coverage
    @Nested
    @DisplayName("Reconstitution, Getters and Edge Cases (Coverage)")
    class ReconstitutionAndEdgeCasesTests {

        @Test
        @DisplayName("Should reconstitute payment from full constructor and test all getters")
        void shouldReconstituteAndTestGetters() {
            LocalDateTime now = LocalDateTime.now();
            LocalDate payDate = LocalDate.now();

            PaymentRecord payment = PaymentRecord.reconstitute(
                    UUID.randomUUID(), contractId, tenantId, idempotencyKey,
                    dueDate, payDate, rentAmount, rentAmount,
                    new Money(BigDecimal.ZERO, Currency.CLP),
                    PaymentStatus.PAID, "REF-123", "http://receipt.com",
                    now, now
            );

            assertNotNull(payment.getId());
            assertEquals(contractId, payment.getContractId());
            assertEquals(tenantId, payment.getTenantId());
            assertEquals(idempotencyKey, payment.getIdempotencyKey());
            assertEquals(dueDate, payment.getDueDate());
            assertEquals(payDate, payment.getPaymentDate());
            assertEquals(rentAmount, payment.getExpectedAmount());
            assertEquals(rentAmount, payment.getPaidAmount());
            assertNotNull(payment.getLateFeeApplied());
            assertEquals(PaymentStatus.PAID, payment.getStatus());
            assertEquals("REF-123", payment.getTransactionReference());
            assertEquals("http://receipt.com", payment.getPaymentReceiptUrl());
            assertEquals(now, payment.getCreatedAt());
            assertEquals(now, payment.getUpdatedAt());
        }

        @Test
        @DisplayName("Should cancel a pending payment successfully")
        void shouldCancelPendingPayment() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            payment.cancel();
            assertEquals(PaymentStatus.CANCELLED, payment.getStatus());
        }

        @Test
        @DisplayName("getTotalPaid should return zero when status is not PAID")
        void shouldReturnZeroTotalPaidWhenNotPaid() {
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );

            assertEquals(BigDecimal.ZERO, payment.getTotalPaid().getAmount());
        }
    }

    @Nested
    @DisplayName("Status Query Methods Coverage")
    class StatusQueryMethodsTests {

        @Test
        @DisplayName("Should correctly return boolean values for all status queries")
        void shouldReturnCorrectBooleanForStatusQueries() {
            // 1. Estado PENDING (Nace pendiente)
            PaymentRecord payment = PaymentRecord.createPending(
                    contractId, tenantId, dueDate, rentAmount, idempotencyKey
            );
            assertTrue(payment.isPending());
            assertFalse(payment.isPaid());
            assertFalse(payment.isOverdue());

            // 2. Lo cambio a OVERDUE para evaluar los casos contrarios
            LocalDate pastDate = dueDate.plusDays(5);
            payment.markAsOverdue(pastDate);

            // JaCoCo ahora ve la rama FALSE de isPending
            assertFalse(payment.isPending());
            assertFalse(payment.isPaid());
            assertTrue(payment.isOverdue());
        }
    }

}
