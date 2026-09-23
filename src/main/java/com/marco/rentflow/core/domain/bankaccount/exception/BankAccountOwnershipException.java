package com.marco.rentflow.core.domain.bankaccount.exception;

import com.marco.rentflow.core.domain.common.exception.DomainException;

public class BankAccountOwnershipException extends DomainException {
    public BankAccountOwnershipException(String message) {
        super(message);
    }
}
