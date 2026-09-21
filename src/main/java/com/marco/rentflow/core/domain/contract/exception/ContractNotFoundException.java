package com.marco.rentflow.core.domain.contract.exception;

import com.marco.rentflow.core.domain.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ContractNotFoundException extends ResourceNotFoundException {

    public ContractNotFoundException(UUID id) {
        super("Contract not found with id: " + id);
    }
}
