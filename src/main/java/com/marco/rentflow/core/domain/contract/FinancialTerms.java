package com.marco.rentflow.core.domain.contract;

import com.marco.rentflow.core.domain.common.Money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object holding the financial terms of a RentalContract:
 * monthly rent, deposit, payment day and daily penalty rate.
 */

public record FinancialTerms(
        Money monthlyRent,
        Money depositAmount,
        int paymentDueDay,
        BigDecimal dailyPenaltyRate
) {
    public static final BigDecimal DEFAULT_DAILY_PENALTY_RATE = new BigDecimal("0.01");

    public FinancialTerms {
        Objects.requireNonNull(monthlyRent, "Monthly rent cannot be null");
        Objects.requireNonNull(depositAmount, "Deposit cannot be null");
        Objects.requireNonNull(dailyPenaltyRate, "Daily penalty rate cannot be null");

        if (monthlyRent.getCurrency() != depositAmount.getCurrency()) {
            throw new IllegalArgumentException("Rent and deposit must use the same currency");
        }
        if (depositAmount.isGreaterThan(monthlyRent.multiply(BigDecimal.valueOf(2)))) {
            throw new IllegalArgumentException("Deposit cannot exceed 2 months of rent");
        }
        if (paymentDueDay < 1 || paymentDueDay > 31) {
            throw new IllegalArgumentException("Payment due day must be between 1 and 31");
        }
        if (dailyPenaltyRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Daily penalty rate cannot be negative");
        }
    }

    /**
     * Convenience factory for creating FinancialTerms with the default penalty rate.
     */
    public static FinancialTerms of(Money monthlyRent,
                                    Money depositAmount,
                                    int paymentDueDay) {
        return new FinancialTerms(monthlyRent, depositAmount, paymentDueDay,
                DEFAULT_DAILY_PENALTY_RATE);
    }
}
