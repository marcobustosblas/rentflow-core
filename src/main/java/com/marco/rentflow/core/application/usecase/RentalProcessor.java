package com.marco.rentflow.core.application.usecase;

import com.marco.rentflow.core.application.port.out.TenantNotificationGateway;
import com.marco.rentflow.core.domain.exception.TenantNotificationException;
import com.marco.rentflow.core.domain.model.RentalContract;

import java.math.BigDecimal;

public class RentalProcessor {

    private final TenantNotificationGateway notificationGateway;

    public RentalProcessor(TenantNotificationGateway notificationGateway) {
        this.notificationGateway = notificationGateway;
    }

    public BigDecimal processPayment(RentalContract contract, int paymentDay, String tenantEmail) {
        if (tenantEmail == null || tenantEmail.trim().isEmpty()) {
            throw new TenantNotificationException("Invalid tenant email contact");
        }
        BigDecimal totalAmount;
        if (paymentDay <= contract.getDueDay()) {
            totalAmount = contract.getBaseAmount();
        } else {
            int lateDays = paymentDay - contract.getDueDay();
            BigDecimal penalty = contract.getDailyPenaltyFee().multiply(BigDecimal.valueOf(lateDays));
            totalAmount = contract.getBaseAmount().add(penalty);
        }
        this.notificationGateway.sendNotification(tenantEmail, "Payment processed successfully. Total: " + totalAmount);
        return totalAmount;
    }

}
