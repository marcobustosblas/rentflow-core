package com.marco.rentflow.core.domain.contract;

import com.marco.rentflow.core.domain.common.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

public class RentalContract {
    private final UUID id;
    private final UUID propertyId;
    private final UUID tenantId;
    private final UUID landlordId;

    private Money monthlyRent; // Ingreso mensual recurrente
    private Money depositAmount; // Mes de Garantía (pago único inicial)
    private int paymentDueDay; // Día del mes en que vence el arriendo
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate lastReadjustmentDate;


    // FACTORY METHOD (Creación desde cero)

    public static RentalContract create(UUID propertyId, UUID tenantId, UUID landlordId,
                                        Money monthlyRent, Money depositAmount,
                                        int paymentDueDay, LocalDate startDate, LocalDate endDate) {

        validateMinimumPeriod(startDate, endDate);
        validateDepositLimit(monthlyRent, depositAmount);

        return new RentalContract(
                UUID.randomUUID(),
                propertyId,
                tenantId,
                landlordId,
                monthlyRent,
                depositAmount,
                paymentDueDay,
                startDate,
                endDate,
                ContractStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }


    // CONSTRUCTOR COMPLETO (Reconstitución BD)

    public RentalContract(UUID id, UUID propertyId, UUID tenantId, UUID landlordId,
                          Money monthlyRent, Money depositAmount, int paymentDueDay,
                          LocalDate startDate, LocalDate endDate, ContractStatus status,
                          LocalDateTime createdAt, LocalDateTime updatedAt, LocalDate lastReadjustmentDate) {

        this.id = Objects.requireNonNull(id, "Contract ID cannot be null");
        this.propertyId = Objects.requireNonNull(propertyId, "Property ID cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        this.landlordId = Objects.requireNonNull(landlordId, "Landlord ID cannot be null");

        this.monthlyRent = Objects.requireNonNull(monthlyRent, "Monthly rent cannot be null");
        this.depositAmount = Objects.requireNonNull(depositAmount, "Deposit amount cannot be null");

        validateMinimumPeriod(startDate, endDate);
        validateDepositLimit(monthlyRent, depositAmount);

        this.paymentDueDay = validatePaymentDueDay(paymentDueDay);
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = Objects.requireNonNull(status, "Contract status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
        this.lastReadjustmentDate = lastReadjustmentDate;
    }


    // LÓGICA FINANCIERA Y CÁLCULOS

    public LocalDate calculatePaymentDueDate(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int validDay = Math.min(this.paymentDueDay, yearMonth.lengthOfMonth());
        return LocalDate.of(year, month, validDay);
    }

    public boolean isOverdue(LocalDate paymentDate, LocalDate dueDate) {
        return paymentDate.isAfter(dueDate);
    }

    public Money calculateLateFee(LocalDate paymentDate, LocalDate dueDate, BigDecimal dailyPenaltyRate) {
        // step 1: Validar que ningún parámetro sea null
        Objects.requireNonNull(paymentDate, "Payment date cannot be null");
        Objects.requireNonNull(dueDate, "Due date cannot be null");
        Objects.requireNonNull(dailyPenaltyRate, "Daily penalty rate cannot be null");
        // ¿Dónde se pone dailyPenaltyRate? -> En el servicio o aplicación:
        // BigDecimal dailyPenaltyRate = new BigDecimal("0.01"); // 1% diario

        // step 2: Verificar si está atrasado
        if (!isOverdue(paymentDate, dueDate)) {
            // Retorna 0.00 de multa, manteniendo la moneda del contrato
            return new Money(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), this.monthlyRent.getCurrency());
        }

        // step 3: Calcular días de atraso:
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, paymentDate);

        // step 4: Calcular multa:
        BigDecimal daysMultiplier = BigDecimal.valueOf(daysOverdue);

        BigDecimal penaltyAmount = this.monthlyRent.getAmount()
                .multiply(dailyPenaltyRate)
                .multiply(daysMultiplier)
                .setScale(2, RoundingMode.HALF_UP);

        // step 5: Retornar Money con la moneda correcta
        return new Money(penaltyAmount, this.monthlyRent.getCurrency());
    }

    public Money calculateTotalWithPenalty(LocalDate paymentDate, LocalDate dueDate, BigDecimal dailyPenaltyRate) {
        Money lateFee = calculateLateFee(paymentDate, dueDate, dailyPenaltyRate);
        return this.monthlyRent.add(lateFee); // Money garantiza que ambas monedas sean iguales
    }


    // MÉTODOS DE MUTACIÓN Y TRANSICIÓN

    public void updateMonthlyRent(Money newRent) {
        Objects.requireNonNull(newRent, "New rent cannot be null");
        if (monthlyRent.getCurrency() != newRent.getCurrency()) {
            throw new IllegalArgumentException("Cannot change contract currency");
        }
        this.monthlyRent = newRent;
        touch();
    }

    public void extendContract(LocalDate newEndDate) {
        Objects.requireNonNull(newEndDate, "New end date cannot be null");
        if (newEndDate.isBefore(this.endDate)) {
            throw new IllegalArgumentException("Renewal end date must be after current end date");
        }
        validateMinimumPeriod(this.startDate, newEndDate);
        this.endDate = newEndDate;
        if (this.status == ContractStatus.EXPIRED) {
            this.status = ContractStatus.ACTIVE;
        }
        touch();
    }

    public long getRemainingMonths() {
        if (LocalDate.now().isAfter(this.endDate)) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(LocalDate.now(), this.endDate);
    }

    public boolean isAboutToExpire(int monthsThreshold) {
        return getRemainingMonths() <= monthsThreshold;
    }

    public void terminate() {
        this.status = ContractStatus.TERMINATED;
        touch();
    }

    public void expire() {
        this.status = ContractStatus.EXPIRED;
        touch();
    }

    public boolean isActive() {
        return this.status == ContractStatus.ACTIVE;
    }


    // INVARIANTES PRIVADAS DE NEGOCIO

    private static void validateMinimumPeriod(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        long months = ChronoUnit.MONTHS.between(start, end);
        if (months < 1) {
            throw new IllegalArgumentException("Contract must be for at least 1 month");
        }
    }

    // Validar que se calcule 2 meses de arriendo (mes + mes garantía)
    private static void validateDepositLimit(Money rent, Money deposit) {
        if (rent.getCurrency() != deposit.getCurrency()) {
            throw new IllegalArgumentException("Rent and deposit must use the same currency");
        }
        Money twoMonthsRent = rent.multiply(BigDecimal.valueOf(2));
        if (deposit.isGreaterThan(twoMonthsRent)) {
            throw new IllegalArgumentException("Deposit cannot exceed 2 months of rent");
        }
    }

    private static int validatePaymentDueDay(int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Payment due day must be between 1 and 31");
        }
        return day;
    }

    // Calculate IPC

    public void readjustRentByIpc(BigDecimal ipcPercentage, LocalDate readjustmentDate, int minMonthsBetweenAdjustments) {
        Objects.requireNonNull(ipcPercentage, "IPC percentage cannot be null");
        Objects.requireNonNull(readjustmentDate, "Readjustment date cannot be null");

        if (ipcPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("IPC percentage must be greater than zero");
        }
        if (readjustmentDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("Readjustment date cannot be before contract start");
        }
        if (readjustmentDate.isAfter(this.endDate)) {
            throw new IllegalArgumentException("Readjustment date cannot be after contract end");
        }

        // Aquí uso la memoria y la variable flexible
        if (this.lastReadjustmentDate != null) {
            long monthsBetween = ChronoUnit.MONTHS.between(lastReadjustmentDate, readjustmentDate);
            if (monthsBetween < minMonthsBetweenAdjustments) {
                throw new IllegalArgumentException(
                        "Readjustment can only be applied every " + minMonthsBetweenAdjustments + " months minimum"
                );
            }
        }

        BigDecimal valuePercentage = ipcPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal factor = BigDecimal.ONE.add(valuePercentage);

        BigDecimal newAmount = this.monthlyRent.getAmount()
                .multiply(factor)
                .setScale(0, RoundingMode.HALF_UP);

        this.monthlyRent = new Money(newAmount, this.monthlyRent.getCurrency());
        this.lastReadjustmentDate = readjustmentDate; // Actualizo la memoria
        touch();
    }

    public LocalDate getLastReadjustmentDate() {
        return lastReadjustmentDate;
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("{id=%s, property=%s, tenant=%s, rent=%s, status=%s}",
                id, propertyId, tenantId, monthlyRent, status);
    }

    // GETTERS
    public UUID getId() { return id; }
    public UUID getPropertyId() { return propertyId; }
    public UUID getTenantId() { return tenantId; }
    public UUID getLandlordId() { return landlordId; }
    public Money getMonthlyRent() { return monthlyRent; }
    public Money getDepositAmount() { return depositAmount; }
    public int getPaymentDueDay() { return paymentDueDay; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ContractStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}