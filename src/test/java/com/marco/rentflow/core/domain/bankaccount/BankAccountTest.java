package com.marco.rentflow.core.domain.bankaccount;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BankAccount Entity Domain Tests")
public class BankAccountTest {

    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("Should create bank account successfully with valid RUT format")
    void shouldCreateBankAccountSuccessfully() {
        BankAccount account = new BankAccount(userId, "Banco Estado", AccountType.CUENTA_RUT, "12345678", "12345678-9");

        assertNotNull(account.getId());
        assertEquals(userId, account.getUserId());
        assertEquals("Banco Estado", account.getBankName());
        assertEquals(AccountType.CUENTA_RUT, account.getAccountType());
        assertEquals("12345678", account.getAccountNumber());
        assertEquals("12345678-9", account.getHolderRut());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    @DisplayName("Should update account details successfully")
    void shouldUpdateDetailsSuccessfully() {
        BankAccount account = new BankAccount(userId, "Banco Estado", AccountType.CUENTA_RUT, "12345678", "12345678-9");

        account.updateDetails("Banco Chile", AccountType.CUENTA_CORRIENTE, "98765432");

        assertEquals("Banco Chile", account.getBankName());
        assertEquals(AccountType.CUENTA_CORRIENTE, account.getAccountType());
        assertEquals("98765432", account.getAccountNumber());
    }

    @Test
    @DisplayName("Should update holder RUT successfully when valid")
    void shouldUpdateHolderRutSuccessfully() {
        BankAccount account = new BankAccount(userId, "Banco Estado", AccountType.CUENTA_RUT, "12345678", "12345678-9");

        account.updateHolderRut("98765432-1");

        assertEquals("98765432-1", account.getHolderRut());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when holder RUT format is invalid")
    void shouldThrowExceptionWhenRutIsInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                new BankAccount(userId, "Banco Estado", AccountType.CUENTA_RUT, "12345678", "123456789")
        );
    }

    @Test
    @DisplayName("Should throw exception when required fields are blank or null")
    void shouldThrowExceptionWhenFieldsAreBlankOrNull() {
        assertThrows(NullPointerException.class, () ->
                new BankAccount(userId, null, AccountType.CUENTA_RUT, "12345678", "12345678-9")
        );
        assertThrows(IllegalArgumentException.class, () ->
                new BankAccount(userId, "   ", AccountType.CUENTA_RUT, "12345678", "12345678-9")
        );
        assertThrows(IllegalArgumentException.class, () ->
                new BankAccount(userId, "Banco Estado", AccountType.CUENTA_RUT, "", "12345678-9")
        );
    }

}
