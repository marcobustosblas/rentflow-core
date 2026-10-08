package com.marco.rentflow.core.domain.payment.exceptions;

import com.marco.rentflow.core.domain.common.exception.ResourceNotFoundException;

public class PaymentNotFoundException extends ResourceNotFoundException {
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
