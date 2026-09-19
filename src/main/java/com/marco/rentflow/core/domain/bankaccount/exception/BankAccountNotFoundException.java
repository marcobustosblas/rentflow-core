package com.marco.rentflow.core.domain.bankaccount.exception;

import java.util.UUID;

public class BankAccountNotFoundException extends RuntimeException {

    public BankAccountNotFoundException(UUID id) {
          super("Bank Account not found with ID: " + id);
    }

}

