package com.marco.rentflow.infrastructure.config;

import com.marco.rentflow.core.domain.bankaccount.exception.BankAccountOwnershipException;
import com.marco.rentflow.core.domain.common.exception.*;
import com.marco.rentflow.core.domain.subscription.exception.SubscriptionLimitExceededException;
import com.marco.rentflow.core.domain.user.exception.InvalidRoleException;
import com.marco.rentflow.core.domain.user.exception.InvalidUserDataException;
import com.marco.rentflow.infrastructure.adapters.in.web.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 404 ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- 409 ---
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(SubscriptionLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleSubscriptionLimitExceeded(SubscriptionLimitExceededException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- 403 ---
    @ExceptionHandler(ForbiddenRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenRegistration(ForbiddenRegistrationException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InsufficientRoleException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientRole(InsufficientRoleException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(BankAccountOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleBankAccountOwnership(BankAccountOwnershipException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // --- 400 ---
    @ExceptionHandler(InvalidRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRole(InvalidRoleException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserData(InvalidUserDataException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyMismatch(CurrencyMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- 400 FALLBACK para cualquier DomainException no mapeada ---
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- 500 FALLBACK para excepciones no controladas ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // NUNCA exponer el mensaje de una excepción desconocida al cliente
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    // --- 400 DTO Validation Errors ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Extrae todos los errores del DTO y los une en un solo String
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("Validation error");

        return build(HttpStatus.BAD_REQUEST, errorMessage);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse(message, status.value(), LocalDateTime.now());
        return ResponseEntity.status(status).body(error);
    }
}
