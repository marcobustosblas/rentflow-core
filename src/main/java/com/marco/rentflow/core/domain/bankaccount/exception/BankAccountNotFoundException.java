package com.marco.rentflow.core.domain.bankaccount.exception;

import com.marco.rentflow.core.domain.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class BankAccountNotFoundException extends ResourceNotFoundException {

    public BankAccountNotFoundException(UUID id) {
          super("Bank Account not found with ID: " + id);
    }

}

