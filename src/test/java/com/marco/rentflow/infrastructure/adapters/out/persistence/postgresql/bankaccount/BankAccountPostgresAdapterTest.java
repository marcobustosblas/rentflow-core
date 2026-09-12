package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount;

import com.marco.rentflow.core.domain.bankaccount.AccountType;
import com.marco.rentflow.core.domain.bankaccount.BankAccount;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.AbstractIntegrationTest;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserPostgresAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class BankAccountPostgresAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private BankAccountPostgresAdapter bankAccountPostgresAdapter;

    // se agrego esto para persistir previamente el Usuario titular requerido por la FK user_id en la tabla bank_accounts
    @Autowired
    private UserPostgresAdapter userPostgresAdapter;

    private User createAndPersistUser(String email) {
        User user = User.registerNew(
                email,
                "password_hash_123",
                "Titular Cuenta",
                "12345678-5",
                "+56912345678",
                Role.LANDLORD
        );
        return userPostgresAdapter.save(user);
    }

    @Test
    @DisplayName("Debe guardar una cuenta bancaria en PostgreSQL y recuperarla con sus atributos intactos")
    void shouldSaveAndRetrieveBankAccount() {
        // Arrange
        User user = createAndPersistUser("bankuser1@rentflow.com");
        BankAccount newAccount = BankAccount.create(
                user.getId(),
                "Banco Estado",
                AccountType.CUENTA_CORRIENTE,
                "1234567890",
                "12345678-5"
        );

        // Act
        BankAccount savedAccount = bankAccountPostgresAdapter.save(newAccount);

        // Assert - se agrego esto para validar la persistencia y mapeo correcto en PostgreSQL real
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getId()).isNotNull();

        Optional<BankAccount> retrieved = bankAccountPostgresAdapter.findById(savedAccount.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getBankName()).isEqualTo("Banco Estado");
        assertThat(retrieved.get().getAccountType()).isEqualTo(AccountType.CUENTA_CORRIENTE);
        assertThat(retrieved.get().getAccountNumber()).isEqualTo("1234567890");
        assertThat(retrieved.get().getHolderRut()).isEqualTo("12345678-5");
        assertThat(retrieved.get().getUserId()).isEqualTo(user.getId());
        assertThat(retrieved.get().getCreatedAt()).isNotNull();
        assertThat(retrieved.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debe buscar todas las cuentas bancarias pertenecientes a un usuario especifico")
    void shouldFindBankAccountsByUserId() {
        // Arrange
        User user = createAndPersistUser("bankuser2@rentflow.com");
        // se agrego esto para usar los valores exactos del Enum AccountType (CUENTA_CORRIENTE, CUENTA_AHORRO)
        BankAccount acc1 = BankAccount.create(user.getId(), "Banco de Chile", AccountType.CUENTA_CORRIENTE, "111111", "12345678-5");
        BankAccount acc2 = BankAccount.create(user.getId(), "Banco Santander", AccountType.CUENTA_AHORRO, "222222", "12345678-5");

        bankAccountPostgresAdapter.save(acc1);
        bankAccountPostgresAdapter.save(acc2);

        // Act - se agrego esto para verificar la consulta por ID de usuario
        List<BankAccount> accounts = bankAccountPostgresAdapter.findByUserId(user.getId());

        // Assert
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(BankAccount::getBankName)
                .containsExactlyInAnyOrder("Banco de Chile", "Banco Santander");
    }

    @Test
    @DisplayName("Debe eliminar una cuenta bancaria existente por su ID")
    void shouldDeleteBankAccountById() {
        // Arrange
        User user = createAndPersistUser("bankuser3@rentflow.com");
        BankAccount account = BankAccount.create(user.getId(), "Scotiabank", AccountType.CUENTA_CORRIENTE, "999999", "12345678-5");
        BankAccount saved = bankAccountPostgresAdapter.save(account);

        // Act - se agrego esto para probar la operacion de eliminacion física en PostgreSQL
        bankAccountPostgresAdapter.deleteById(saved.getId());

        // Assert
        Optional<BankAccount> retrieved = bankAccountPostgresAdapter.findById(saved.getId());
        assertThat(retrieved).isEmpty();
    }
}
