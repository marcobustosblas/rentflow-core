package com.marco.rentflow.infrastructure.adapters.in.web.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentWebhookRequestDTO {

    private String idempotencyKey;
    private BigDecimal amountPaid;
    private String currency;
    private LocalDate paymentDate;
    private String transactionRef;
    private String receiptUrl;

    public PaymentWebhookRequestDTO() {};

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public String getReceiptUrl() {
        return receiptUrl;
    }

    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }
}


/**
 * (Sun 30-8, 19:30 hr)
 * Este DTO envía los datos del pago
 * Este DTO está diseñado específicamente para atrapar el formato de datos que Webpay ENVÍA.
 * **/

