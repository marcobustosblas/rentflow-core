package com.marco.rentflow.infrastructure.adapters.in.web.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentWebhookRequestDTO {

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    @NotNull(message = "Amount paid is required")
    @Positive(message = "Amount paid must be greater than zero")
    private BigDecimal amountPaid;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^(CLP|UF|USD)$", message = "Currency must be CLP, UF, or USD")
    private String currency;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotBlank(message = "Transaction reference is required")
    private String transactionRef;

    @NotBlank(message = "Receipt URL is required")
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

