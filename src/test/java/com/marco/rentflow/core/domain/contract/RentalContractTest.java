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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RentalContract Aggregate Domain Tests")
class RentalContractTest {

    private final UUID propertyId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();
    private final UUID landlordId = UUID.randomUUID();

    private final Money standardRent = new Money(new BigDecimal("350000"), Currency.CLP);
    private final Money standardDeposit = new Money(new BigDecimal("350000"), Currency.CLP);

    private RentalContract createContract(LocalDate startDate, LocalDate endDate) {
        return createContract(propertyId, tenantId, landlordId, standardRent, standardDeposit, 5, new BigDecimal("0.01"), startDate, endDate);
    }

    private RentalContract createContract(UUID propId, UUID tenId, UUID landId, Money rent, Money deposit, int dueDay, BigDecimal penalty, LocalDate startDate, LocalDate endDate) {
        TenantInfo tenant = new TenantInfo(tenId, "tenant@test.com", "Tenant Name", "12345678-9");
        FinancialTerms terms = new FinancialTerms(rent, deposit, dueDay, penalty);
        return RentalContract.register(propId, landId, tenant, terms, startDate, endDate, null, ContractSource.MANUAL);
    }

    @Nested
    @DisplayName("Creation and Invariants Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create contract successfully with valid parameters")
        void shouldCreateSuccessfully() {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 12, 31);

            RentalContract contract = createContract(startDate, endDate);

            assertNotNull(contract.getId());
            assertEquals(ContractStatus.ACTIVE, contract.getStatus());
            assertTrue(contract.isActive());
        }

        @Test
        @DisplayName("Should throw exception when contract duration is less than 1 month")
        void shouldThrowExceptionWhenDurationIsTooShort() {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 1, 15);

            assertThrows(IllegalArgumentException.class, () -> createContract(startDate, endDate));
        }

        @Test
        @DisplayName("Should throw exception when deposit exceeds 2 months of rent")
        void shouldThrowExceptionWhenDepositIsTooHigh() {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 12, 31);
            Money highDeposit = new Money(new BigDecimal("1000000"), Currency.CLP);

            assertThrows(IllegalArgumentException.class, () -> createContract(propertyId, tenantId, landlordId, standardRent, highDeposit, 5, BigDecimal.ZERO, startDate, endDate));
        }

        @Test
        @DisplayName("Should throw exception when rent and deposit have different currencies")
        void shouldThrowExceptionWhenDifferentCurrencies() {
            Money rentCLP = new Money(new BigDecimal("350000"), Currency.CLP);
            Money depositUSD = new Money(new BigDecimal("500"), Currency.USD);

            assertThrows(IllegalArgumentException.class, () -> createContract(propertyId, tenantId, landlordId, rentCLP, depositUSD, 5, BigDecimal.ZERO, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        }

        @Test
        @DisplayName("Should throw exception when dates are null")
        void shouldThrowExceptionWhenDatesAreNull() {
            assertAll(
                    () -> assertThrows(NullPointerException.class, () ->
                            createContract(propertyId, tenantId, landlordId, standardRent, standardDeposit, 5, BigDecimal.ZERO, null, LocalDate.of(2026, 12, 31))),
                    () -> assertThrows(NullPointerException.class, () ->
                            createContract(propertyId, tenantId, landlordId, standardRent, standardDeposit, 5, BigDecimal.ZERO, LocalDate.of(2026, 1, 1), null))
            );
        }

        @ParameterizedTest
        @DisplayName("Should throw exception when payment due day is invalid")
        @ValueSource(ints = {0, 32, -1, 40})
        void shouldThrowExceptionWhenInvalidDueDay(int invalidDay) {
            assertThrows(IllegalArgumentException.class, () ->
                    createContract(propertyId, tenantId, landlordId, standardRent, standardDeposit, invalidDay, BigDecimal.ZERO, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
            );
        }

        @Test
        @DisplayName("Should throw exception when end date is before start date")
        void shouldThrowExceptionWhenEndDateBeforeStartDate() {
            LocalDate startDate = LocalDate.of(2026, 12, 31);
            LocalDate endDate = LocalDate.of(2026, 1, 1);

            assertThrows(IllegalArgumentException.class, () -> createContract(startDate, endDate));
        }

        @Test
        @DisplayName("Should extend an active contract without changing its status")
        void shouldExtendActiveContract() {
            LocalDate startDate = LocalDate.of(2026, 1, 1);
            LocalDate endDate = LocalDate.of(2026, 6, 30);

            RentalContract contract = createContract(startDate, endDate);

            assertEquals(ContractStatus.ACTIVE, contract.getStatus());

            LocalDate newEndDate = LocalDate.of(2026, 12, 31);
            contract.extendContract(newEndDate);

            assertEquals(newEndDate, contract.getEndDate());
            assertEquals(ContractStatus.ACTIVE, contract.getStatus());
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
            RentalContract contract = createContract(propertyId, tenantId, landlordId, standardRent, standardDeposit, dueDay, BigDecimal.ZERO, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            LocalDate calculatedDueDate = contract.calculatePaymentDueDate(year, month);
            assertEquals(LocalDate.of(year, month, expectedDay), calculatedDueDate);
        }

        @Test
        @DisplayName("Should calculate 0 late fee when payment is on time or early")
        void shouldCalculateZeroFeeWhenOnTimeOrEarly() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            LocalDate dueDate = LocalDate.of(2026, 3, 5);

            Money lateFeeOnTime = contract.calculateLateFee(LocalDate.of(2026, 3, 5), dueDate);
            assertEquals(new BigDecimal("0.00"), lateFeeOnTime.getAmount());

            Money lateFeeEarly = contract.calculateLateFee(LocalDate.of(2026, 3, 2), dueDate);
            assertEquals(new BigDecimal("0.00"), lateFeeEarly.getAmount());
        }

        @Test
        @DisplayName("Should calculate correct late fee for 3 days overdue")
        void shouldCalculateLateFeeWhenOverdue() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            LocalDate dueDate = LocalDate.of(2026, 3, 5);
            LocalDate paymentDate = LocalDate.of(2026, 3, 8); // 3 días de atraso

            Money lateFee = contract.calculateLateFee(paymentDate, dueDate);

            assertEquals(new BigDecimal("10500.00"), lateFee.getAmount());
        }

        @Test
        @DisplayName("Should calculate total with penalty correctly")
        void shouldCalculateTotalWithPenalty() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            LocalDate dueDate = LocalDate.of(2026, 3, 5);
            LocalDate paymentDate = LocalDate.of(2026, 3, 8);

            Money total = contract.calculateTotalWithPenalty(paymentDate, dueDate);
            assertEquals(new BigDecimal("360500.00"), total.getAmount());
        }

        @Test
        @DisplayName("Should detect upcoming expiration using a custom threshold")
        void shouldDetectAboutToExpire() {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().plusMonths(4);
            RentalContract contract = createContract(startDate, endDate);

            assertTrue(contract.isAboutToExpire(5));
            assertFalse(contract.isAboutToExpire(3));
        }

        @Test
        @DisplayName("Should return 0 remaining months when contract is strictly in the past")
        void shouldReturnZeroRemainingMonthsWhenExpired() {
            RentalContract contract = createContract(LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1));
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
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            contract.terminate();
            assertEquals(ContractStatus.TERMINATED, contract.getStatus());
            assertFalse(contract.isActive());
        }

        @Test
        @DisplayName("Should update monthly rent successfully")
        void shouldUpdateMonthlyRent() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            Money newRent = new Money(new BigDecimal("400000"), Currency.CLP);
            contract.updateMonthlyRent(newRent);

            assertEquals(new BigDecimal("400000"), contract.getMonthlyRent().getAmount());
        }

        @Test
        @DisplayName("Should throw exception when updating rent with different currency")
        void shouldThrowExceptionWhenUpdatingRentWithDifferentCurrency() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            Money newRentUSD = new Money(new BigDecimal("1000"), Currency.USD);

            assertThrows(IllegalArgumentException.class, () ->
                    contract.updateMonthlyRent(newRentUSD)
            );
        }

        @Test
        @DisplayName("Should throw exception when extending to an earlier date")
        void shouldThrowExceptionWhenExtendingToEarlierDate() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            assertThrows(IllegalArgumentException.class, () ->
                    contract.extendContract(LocalDate.of(2026, 6, 30))
            );
        }
    }

    @Nested
    @DisplayName("IPC Readjustment Tests")
    class IpcReadjustmentTests {

        @Test
        @DisplayName("Should readjust rent correctly by IPC and update last readjustment date")
        void shouldReadjustRentCorrectly() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            BigDecimal ipc = new BigDecimal("4.5");
            LocalDate readjustmentDate = LocalDate.of(2026, 7, 1);

            contract.readjustRentByIpc(ipc, readjustmentDate, 6);

            assertEquals(new BigDecimal("365750"), contract.getMonthlyRent().getAmount());
            assertEquals(readjustmentDate, contract.getLastReadjustmentDate());
        }

        @Test
        @DisplayName("Should throw exception when applying readjustment before minimum months passed")
        void shouldThrowExceptionWhenReadjustingTooSoon() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 12, 31));

            BigDecimal ipc = new BigDecimal("4.5");
            LocalDate firstReadjustment = LocalDate.of(2026, 7, 1);

            contract.readjustRentByIpc(ipc, firstReadjustment, 6);

            LocalDate earlyReadjustment = LocalDate.of(2026, 10, 1);

            assertThrows(IllegalArgumentException.class, () ->
                    contract.readjustRentByIpc(ipc, earlyReadjustment, 6)
            );
        }

        @Test
        @DisplayName("Should throw exception when IPC is zero or negative")
        void shouldThrowExceptionWhenIpcIsInvalid() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            LocalDate date = LocalDate.of(2026, 6, 1);

            assertThrows(IllegalArgumentException.class, () ->
                    contract.readjustRentByIpc(BigDecimal.ZERO, date, 6)
            );

            assertThrows(IllegalArgumentException.class, () ->
                    contract.readjustRentByIpc(new BigDecimal("-1.5"), date, 6)
            );
        }

        @Test
        @DisplayName("Should throw exception when readjustment date is out of contract bounds")
        void shouldThrowExceptionWhenReadjustmentDateIsOutOfBounds() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));

            BigDecimal ipc = new BigDecimal("4.5");

            assertThrows(IllegalArgumentException.class, () ->
                    contract.readjustRentByIpc(ipc, LocalDate.of(2025, 12, 31), 6)
            );

            assertThrows(IllegalArgumentException.class, () ->
                    contract.readjustRentByIpc(ipc, LocalDate.of(2027, 1, 1), 6)
            );
        }

        @Test
        @DisplayName("Should allow a second readjustment if exactly the minimum months have passed")
        void shouldAllowSecondReadjustment() {
            RentalContract contract = createContract(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 12, 31));

            BigDecimal ipc = new BigDecimal("4.5");

            LocalDate firstDate = LocalDate.of(2026, 6, 1);
            contract.readjustRentByIpc(ipc, firstDate, 6);

            LocalDate secondDate = LocalDate.of(2026, 12, 1);
            assertDoesNotThrow(() -> contract.readjustRentByIpc(ipc, secondDate, 6));
        }
    }

    @Nested
    @DisplayName("Reconstitution and Getters Tests (Coverage)")
    class ReconstitutionAndGettersTests {

        @Test
        @DisplayName("Should reconstitute contract from full constructor and test all getters")
        void shouldReconstituteAndTestGetters() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            LocalDate lastReadjustment = LocalDate.now().minusMonths(1);

            RentalContract contract = RentalContract.reconstitute(
                    id, propertyId, landlordId, tenantId,
                    "tenant@test.com", "Tenant Name", "12345678-9",
                    "http://doc.pdf", ContractSource.MANUAL,
                    standardRent, standardDeposit, 5, BigDecimal.ZERO,
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                    ContractStatus.ACTIVE, now, now, lastReadjustment
            );

            assertEquals(id, contract.getId());
            assertEquals(propertyId, contract.getPropertyId());
            assertEquals(tenantId, contract.getTenantId());
            assertEquals(landlordId, contract.getLandlordId());
            assertEquals("tenant@test.com", contract.getTenantEmail());
            assertEquals("Tenant Name", contract.getTenantFullName());
            assertEquals("12345678-9", contract.getTenantRut());
            assertEquals("http://doc.pdf", contract.getContractDocumentUrl());
            assertEquals(ContractSource.MANUAL, contract.getSource());
            assertEquals(standardRent, contract.getMonthlyRent());
            assertEquals(standardDeposit, contract.getDepositAmount());
            assertEquals(5, contract.getPaymentDueDay());
            assertEquals(LocalDate.of(2026, 1, 1), contract.getStartDate());
            assertEquals(LocalDate.of(2026, 12, 31), contract.getEndDate());
            assertEquals(ContractStatus.ACTIVE, contract.getStatus());
            assertEquals(now, contract.getCreatedAt());
            assertEquals(now, contract.getUpdatedAt());
            assertEquals(lastReadjustment, contract.getLastReadjustmentDate());
            assertNotNull(contract.toString());
        }
    }
}