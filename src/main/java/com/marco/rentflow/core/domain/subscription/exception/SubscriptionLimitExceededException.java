package com.marco.rentflow.core.domain.subscription.exception;

import com.marco.rentflow.core.domain.common.exception.DomainException;

public class SubscriptionLimitExceededException extends DomainException {

    public SubscriptionLimitExceededException(String message) {
        super(message);
    }

}
