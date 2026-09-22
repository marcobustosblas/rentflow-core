package com.marco.rentflow.core.domain.common.exception;

/**
 * Base exception for all domain-level business rule violations.
 */

public abstract class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
