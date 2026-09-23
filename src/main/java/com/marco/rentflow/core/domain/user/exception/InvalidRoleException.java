package com.marco.rentflow.core.domain.user.exception;

import com.marco.rentflow.core.domain.common.exception.DomainException;

public class InvalidRoleException extends DomainException {

    public InvalidRoleException(String message) {
        super(message);
    }

}
