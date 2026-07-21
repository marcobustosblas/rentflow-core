package com.marco.rentflow.core.domain.exception;

public class TenantNotificationException extends RuntimeException {
    public TenantNotificationException(String message) {
        super(message);
    }
}
