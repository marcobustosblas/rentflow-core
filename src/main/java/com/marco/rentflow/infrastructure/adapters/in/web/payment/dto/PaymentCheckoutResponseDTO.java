package com.marco.rentflow.infrastructure.adapters.in.web.payment.dto;

public class PaymentCheckoutResponseDTO {

    private String checkoutUrl;

    public PaymentCheckoutResponseDTO(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }
    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }
}
