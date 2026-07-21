package com.marco.rentflow.core.domain.model;

import java.math.BigDecimal;

public class RentalContract {
    private final Long contractId;
    private final BigDecimal baseAmount;
    private final int dueDay;
    private final BigDecimal dailyPenaltyFee;

    public RentalContract(Long contractId, BigDecimal baseAmount, int dueDay, BigDecimal dailyPenaltyFee) {
        this.contractId = contractId;
        this.baseAmount = baseAmount;
        this.dueDay = dueDay;
        this.dailyPenaltyFee = dailyPenaltyFee;
    }

    public Long getContractId() {
        return contractId;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public int getDueDay() {
        return dueDay;
    }

    public BigDecimal getDailyPenaltyFee() {
        return dailyPenaltyFee;
    }
}
