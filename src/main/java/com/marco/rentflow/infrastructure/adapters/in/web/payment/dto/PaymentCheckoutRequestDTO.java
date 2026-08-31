package com.marco.rentflow.infrastructure.adapters.in.web.payment.dto;

import java.time.LocalDate;
import java.util.UUID;

public class PaymentCheckoutRequestDTO {

    private UUID tenantId;
    private UUID contractId;
    private LocalDate paymentDate;

    public PaymentCheckoutRequestDTO() {}

    public UUID getTenantId() {
        return tenantId;
    }
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getContractId() {
        return contractId;
    }
    public void setContractId(UUID contractId) {
        this.contractId = contractId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

}
