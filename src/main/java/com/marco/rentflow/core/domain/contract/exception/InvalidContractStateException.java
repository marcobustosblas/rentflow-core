package com.marco.rentflow.core.domain.contract.exception;

public class InvalidContractStateException extends RuntimeException {
    public InvalidContractStateException(String message) {
        super(message);
    }
}
