package com.marco.rentflow.core.domain.subscription.exception;

import com.marco.rentflow.core.domain.common.exception.ResourceNotFoundException;

public class SubscriptionNotFoundException extends ResourceNotFoundException {

    public SubscriptionNotFoundException(String message) {
        super(message);
    }

}
