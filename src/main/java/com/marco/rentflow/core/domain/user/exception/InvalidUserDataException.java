package com.marco.rentflow.core.domain.user.exception;

import com.marco.rentflow.core.domain.common.exception.DomainException;

public class InvalidUserDataException extends DomainException {

    public InvalidUserDataException(String message) {
        super(message);
    }

}
