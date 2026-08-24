package com.marco.rentflow.core.application.port.out;

import com.marco.rentflow.core.domain.payment.PaymentRecord;

public interface NotificationSenderPort {
    void sendPaymentReceipt(PaymentRecord paymentRecord);
}
