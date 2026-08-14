package com.marco.rentflow.core.domain.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RentalContract Aggregate Domain Tests")
class RentalContractTest {

    private final UUID propertyId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();
    private final UUID landlordId = UUID.randomUUID();

    private final Money standardRent = new Money(new BigDecimal("350000"), Currency.CLP);
    private final Money standardDeposit = new Money(new BigDecimal("350000"), Currency.CLP);

    @Nested
    @DisplayName("Creation and Invariants Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create contract successfully with valid parameters")
        void shouldCreateSuccessfully() {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 12, 31);

            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, startDate, endDate
            );

            assertNotNull(contract.getId());
            assertEquals(ContractStatus.ACTIVE, contract.getStatus());
            assertTrue(contract.isActive());
        }

        @Test
        @DisplayName("Should throw exception when contract duration is less than 1 month")
        void shouldThrowExceptionWhenDurationIsTooShort() {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 15); // Solo 15 días

            assertThrows(IllegalArgumentException.class, () -> RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, startDate, endDate
            ));
        }

        @Test
        @DisplayName("Should throw exception when deposit exceeds 2 months of rent")
        void shouldThrowExceptionWhenDepositIsTooHigh() {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 12, 31);
            Money highDeposit = new Money(new BigDecimal("1000000"), Currency.CLP); // Excede el doble

            assertThrows(IllegalArgumentException.class, () -> RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, highDeposit,
                    5, startDate, endDate
            ));
        }

        @Test
        @DisplayName("Should throw exception when rent and deposit have different currencies")
        void shouldThrowExceptionWhenDifferentCurrencies() {
            Money rentCLP = new Money(new BigDecimal("350000"), Currency.CLP);
            Money depositUSD = new Money(new BigDecimal("500"), Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> RentalContract.create(
                    propertyId, tenantId, landlordId,
                    rentCLP, depositUSD,
                    5, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
            ));
        }

        @Test
        @DisplayName("Should throw exception when dates are null")
        void shouldThrowExceptionWhenDatesAreNull() {
            assertAll(
                    () -> assertThrows(NullPointerException.class, () ->
                            RentalContract.create(propertyId, tenantId, landlordId,
                                    standardRent, standardDeposit, 5, null, LocalDate.of(2026, 12, 31))),
                    () -> assertThrows(NullPointerException.class, () ->
                            RentalContract.create(propertyId, tenantId, landlordId,
                                    standardRent, standardDeposit, 5, LocalDate.of(2026, 1, 1), null))
            );
        }

        @ParameterizedTest
        @DisplayName("Should throw exception when payment due day is invalid")
        @ValueSource(ints = {0, 32, -1, 40})
        void shouldThrowExceptionWhenInvalidDueDay(int invalidDay) {
            assertThrows(IllegalArgumentException.class, () ->
                    RentalContract.create(propertyId, tenantId, landlordId,
                            standardRent, standardDeposit,
                            invalidDay, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
            );
        }
    }

    @Nested
    @DisplayName("Financial and Date Calculation Tests")
    class FinancialTests {

        @ParameterizedTest(name = "Year {0}, Month {1} with due day {2} should return day {3}")
        @CsvSource({
                "2026, 1, 31, 31",
                "2026, 2, 31, 28",
                "2024, 2, 31, 29",
                "2026, 4, 31, 30"
        })
        @DisplayName("Should calculate correct payment due date adjusting for month length")
        void shouldAdjustDueDateForShorterMonths(int year, int month, int dueDay, int expectedDay) {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    dueDay, LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );

            LocalDate calculatedDueDate = contract.calculatePaymentDueDate(year, month);
            assertEquals(LocalDate.of(year, month, expectedDay), calculatedDueDate);
        }

        @Test
        @DisplayName("Should calculate 0 late fee when payment is on time or early")
        void shouldCalculateZeroFeeWhenOnTimeOrEarly() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );

            LocalDate dueDate = LocalDate.of(2026, 3, 5);
            BigDecimal penaltyRate = new BigDecimal("0.01");

            // Prueba pagando justo a tiempo
            Money lateFeeOnTime = contract.calculateLateFee(LocalDate.of(2026, 3, 5), dueDate, penaltyRate);
            assertEquals(new BigDecimal("0.00"), lateFeeOnTime.getAmount());

            // Prueba pagando por adelantado
            Money lateFeeEarly = contract.calculateLateFee(LocalDate.of(2026, 3, 2), dueDate, penaltyRate);
            assertEquals(new BigDecimal("0.00"), lateFeeEarly.getAmount());
        }

        @Test
        @DisplayName("Should calculate correct late fee for 3 days overdue")
        void shouldCalculateLateFeeWhenOverdue() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );

            LocalDate dueDate = LocalDate.of(2026, 3, 5);
            LocalDate paymentDate = LocalDate.of(2026, 3, 8); // 3 días de atraso
            BigDecimal penaltyRate = new BigDecimal("0.01"); // 1% diario = $3.500 * 3 días = $10.500

            Money lateFee = contract.calculateLateFee(paymentDate, dueDate, penaltyRate);

            assertEquals(new BigDecimal("10500.00"), lateFee.getAmount());
        }

        @Test
        @DisplayName("Should calculate total with penalty correctly")
        void shouldCalculateTotalWithPenalty() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
            );

            LocalDate dueDate = LocalDate.of(2026, 3, 5);
            LocalDate paymentDate = LocalDate.of(2026, 3, 8);
            BigDecimal penaltyRate = new BigDecimal("0.01");

            Money total = contract.calculateTotalWithPenalty(paymentDate, dueDate, penaltyRate);
            // 350,000 + 10,500 = 360,500
            assertEquals(new BigDecimal("360500.00"), total.getAmount());
        }

        @Test
        @DisplayName("Should detect upcoming expiration using a custom threshold")
        void shouldDetectAboutToExpire() {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().plusMonths(4);
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, startDate, endDate
            );

            assertTrue(contract.isAboutToExpire(5)); // 4 meses es menor o igual a 5
            assertFalse(contract.isAboutToExpire(3)); // 4 meses NO es menor a 3
        }

        @Test
        @DisplayName("Should return 0 remaining months when contract is strictly in the past")
        void shouldReturnZeroRemainingMonthsWhenExpired() {
            // Contrato del año 2020 al 2021 (totalmente en el pasado)
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1)
            );
            contract.expire();

            assertEquals(0, contract.getRemainingMonths());
        }
    }

    @Nested
    @DisplayName("State Mutation Tests")
    class MutationTests {

        @Test
        @DisplayName("Should terminate contract successfully")
        void shouldTerminateContract() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
            );

            contract.terminate();
            assertEquals(ContractStatus.TERMINATED, contract.getStatus());
            assertFalse(contract.isActive());
        }

        @Test
        @DisplayName("Should allow termination of an expired contract")
        void shouldAllowTerminationOfExpiredContract() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)
            );
            contract.expire();
            contract.terminate();

            assertEquals(ContractStatus.TERMINATED, contract.getStatus());
        }

        @Test
        @DisplayName("Should update monthly rent successfully")
        void shouldUpdateMonthlyRent() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
            );

            Money newRent = new Money(new BigDecimal("400000"), Currency.CLP);
            contract.updateMonthlyRent(newRent);

            assertEquals(new BigDecimal("400000"), contract.getMonthlyRent().getAmount());
        }

        @Test
        @DisplayName("Should throw exception when updating rent with different currency")
        void shouldThrowExceptionWhenUpdatingRentWithDifferentCurrency() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
            );

            Money newRentUSD = new Money(new BigDecimal("1000"), Currency.USD);

            assertThrows(IllegalArgumentException.class, () ->
                    contract.updateMonthlyRent(newRentUSD)
            );
        }

        @Test
        @DisplayName("Should extend contract successfully and reactivate if expired")
        void shouldExtendContractSuccessfully() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)
            );
            contract.expire(); // Lo expiramos a propósito

            contract.extendContract(LocalDate.of(2026, 12, 31));

            assertEquals(LocalDate.of(2026, 12, 31), contract.getEndDate());
            assertEquals(ContractStatus.ACTIVE, contract.getStatus());
            assertTrue(contract.isActive());
        }

        @Test
        @DisplayName("Should throw exception when extending to an earlier date")
        void shouldThrowExceptionWhenExtendingToEarlierDate() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)
            );

            assertThrows(IllegalArgumentException.class, () ->
                    contract.extendContract(LocalDate.of(2026, 6, 30)) // Fecha anterior al fin actual
            );
        }
    }

    @Nested
    @DisplayName("IPC Readjustment Tests")
    class IpcReadjustmentTests {

        @Test
        @DisplayName("Should readjust rent correctly by IPC and update last readjustment date")
        void shouldReadjustRentCorrectly() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );

            // IPC de 4.5% aplicado el 1 de Julio de 2026
            BigDecimal ipc = new BigDecimal("4.5");
            LocalDate readjustmentDate = LocalDate.of(2026, 7, 1);

            contract.readjustRentByIpc(ipc, readjustmentDate, 6);

            // 350,000 * 1.045 = 365,750
            assertEquals(new BigDecimal("365750"), contract.getMonthlyRent().getAmount());
            assertEquals(readjustmentDate, contract.getLastReadjustmentDate());
        }

        @Test
        @DisplayName("Should throw exception when applying readjustment before minimum months passed")
        void shouldThrowExceptionWhenReadjustingTooSoon() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2027, 12, 31)
            );

            BigDecimal ipc = new BigDecimal("4.5");
            LocalDate firstReadjustment = LocalDate.of(2026, 7, 1);

            // Primer reajuste exitoso (pactado cada 6 meses mínimo)
            contract.readjustRentByIpc(ipc, firstReadjustment, 6);

            // Intento de segundo reajuste solo 3 meses después (Octubre)
            LocalDate earlyReadjustment = LocalDate.of(2026, 10, 1);

            assertThrows(IllegalArgumentException.class, () ->
                    contract.readjustRentByIpc(ipc, earlyReadjustment, 6)
            );
        }

        @Test
        @DisplayName("Should throw exception when IPC is zero or negative")
        void shouldThrowExceptionWhenIpcIsInvalid() {
            RentalContract contract = RentalContract.create(
                    propertyId, tenantId, landlordId,
                    standardRent, standardDeposit,
                    5,
                    LocalDate.of(2026, 1, 1),
                    LocalDate.of(2026, 12, 31)
            );

            //LocalDate date = LocalDate.of(2026, 6, 1);
            LocalDate date = LocalDate.now();

            assertThrows(IllegalArgumentException.class, () ->
                    contract.readjustRentByIpc(BigDecimal.ZERO, date, 6)
            );

            assertThrows(IllegalArgumentException.class, () ->
                    contract.readjustRentByIpc(new BigDecimal("-1.5"), date, 6)
            );
        }
    }

}