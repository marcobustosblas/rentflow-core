package com.marco.rentflow.core.domain.contract.exception;

import java.util.UUID;

public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(UUID id) {
        super("Contract not found with id: " + id);
    }
}
