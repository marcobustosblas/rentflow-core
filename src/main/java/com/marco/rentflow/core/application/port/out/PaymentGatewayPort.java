package com.marco.rentflow.core.application.port.out;

import com.marco.rentflow.core.domain.common.Money;

public interface PaymentGatewayPort {
    /**
     * Se comunica con la pasarela de pagos externa (ej. Webpay) para generar un link de cobro seguro.
     */
    String generateCheckoutUrl(String idempotencyKey, Money amount);
}
