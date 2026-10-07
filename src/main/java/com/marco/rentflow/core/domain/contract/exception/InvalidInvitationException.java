package com.marco.rentflow.core.domain.contract.exception;

import com.marco.rentflow.core.domain.common.exception.DomainException;

public class InvalidInvitationException extends DomainException {
    public InvalidInvitationException(String message) {
        super(message);
    }
}
