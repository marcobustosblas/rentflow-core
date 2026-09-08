package com.marco.rentflow.core.domain.bankaccount;

import com.marco.rentflow.core.domain.common.Rut;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class BankAccount {
    private static final String RUT_REGEX = "^\\d{7,8}-[0-9Kk]$";

    private final UUID id;
    private final UUID userId;
    private String bankName;
    private AccountType accountType; // Ej: "CuentaRUT", "Cuenta Corriente"
    private String accountNumber;
    private String holderRut; // RUT del titular para validación Nivel 1
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 1. CONSTRUCTOR PRIVADO (El Guardián Absoluto)
    private BankAccount(UUID id, UUID userId, String bankName, AccountType accountType,
                        String accountNumber, String holderRut,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.bankName = validateNotBlank(bankName, "Bank name cannot be null");
        this.accountType = Objects.requireNonNull(accountType, "Account type cannot be null");
        this.accountNumber = validateNotBlank(accountNumber, "Account number cannot be null");
        this.holderRut = validateAndCleanRut(holderRut);
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // 2. FACTORY METHOD PARA NUEVOS (Capa de Aplicación)
    public static BankAccount create(UUID userId, String bankName, AccountType accountType,
                                     String accountNumber, String holderRut) {
        return new BankAccount(
                UUID.randomUUID(), userId, bankName, accountType,
                accountNumber, holderRut, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // 3. FACTORY METHOD PARA MAPEO DE BD (Capa de Infraestructura)
    public static BankAccount reconstitute(UUID id, UUID userId, String bankName, AccountType accountType,
                                           String accountNumber, String holderRut,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new BankAccount(
                id, userId, bankName, accountType,
                accountNumber, holderRut, createdAt, updatedAt
        );
    }

    // MÉTODOS Y REGLAS DE DOMINIO

    public void updateDetails(String bankName, AccountType accountType, String accountNumber) {
        this.bankName = validateNotBlank(bankName, "Bank name cannot be null");
        this.accountType = Objects.requireNonNull(accountType, "Account type cannot be null");
        this.accountNumber = validateNotBlank(accountNumber, "Account number cannot be null");
        touch();
    }

    public void updateHolderRut(String newHolderRut) {
        this.holderRut = validateAndCleanRut(newHolderRut);
        touch();
    }

    // MÉTODOS PRIVADOS AUXILIARES

    private static String validateAndCleanRut(String rutInput) {
        Rut clean = new Rut(rutInput);
        return clean.getValue();
    }

    private static String validateNotBlank(String value, String message) {
        Objects.requireNonNull(value, message);
        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getBankName() { return bankName; }
    public AccountType getAccountType() { return accountType; }
    public String getAccountNumber() { return accountNumber; }
    public String getHolderRut() { return holderRut; }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

/*
(11-08) Solo a modo de recordar ante tenia:
if (value.trim().isEmpty()) pero lo cambie a
if (value.isBlank())
 */