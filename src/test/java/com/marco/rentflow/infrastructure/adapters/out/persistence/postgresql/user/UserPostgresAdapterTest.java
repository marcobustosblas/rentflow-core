package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user;

import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class UserPostgresAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private UserPostgresAdapter userPostgresAdapter;

    @Test
    @DisplayName("Debe guardar un Usuario en PostgreSQL y recuperarlo con todos sus atributos intactos")
    void shouldSaveAndRetrieveUser() {
        // a. ARRANGE
        User newUser = User.registerNew(
                "Marco Bustos Blas",
                "marco.admin@rentflow.com",
                "hashed_password_123",
                "18313678-1",
                "+569 9 6509 0590",
                Set.of(Role.PLATFORM_ADMIN)
        );
        // b. ACT
        User savedUser = userPostgresAdapter.save(newUser);

        // c. ASSERT
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();

        // d. ACT 2
        Optional<User> retrievedUser = userPostgresAdapter.findById(savedUser.getId());

        // e. ASSERT 2
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("marco.admin@rentflow.com");
        assertThat(retrievedUser.get().getFullName()).isEqualTo("Marco Bustos Blas");
        assertThat(retrievedUser.get().getRut()).isEqualTo("18313678-1");
        assertThat(retrievedUser.get().getPhoneNumber()).isEqualTo("+569 9 6509 0590");
        assertThat(retrievedUser.get().getRoles()).contains(Role.PLATFORM_ADMIN);

        assertThat(retrievedUser.get().getCreatedAt()).isNotNull();
        assertThat(retrievedUser.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debe retornar un Usuario existente cuando se busca por email")
    void shouldFindUserByEmail() {
        // Arrange
        User newUser = User.registerNew(
                "Juan Pérez",
                "tenant.test@rentflow.com",
                "hashed_password_456",
                "15555444-K",
                "+569 8 7654 3210",
                Set.of(Role.TENANT)
        );
        userPostgresAdapter.save(newUser);

        // Act
        Optional<User> foundUser = userPostgresAdapter.findByEmail("tenant.test@rentflow.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("tenant.test@rentflow.com");
        assertThat(foundUser.get().getRoles()).contains(Role.TENANT);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty si se busca un Usuario que no existe en BD")
    void shouldReturnEmptyWhenUserDoesNotExist() {
        // Arrange
        String fakeEmail = "fantasma@rentflow.com";
        // Act
        Optional<User> retrievedUser = userPostgresAdapter.findByEmail(fakeEmail);
        // Assert
        assertThat(retrievedUser).isEmpty();
    }

    @Test
    @DisplayName("Debe verificar si existe un usuario por email")
    void shouldCheckIfExistsByEmail() {
        // Arrange
        User newUser = User.registerNew(
                "Maria Lopez",
                "exists.test@rentflow.com",
                "hashed_password_789",
                "12345678-5",
                "+569 7 1111 2222",
                Set.of(Role.LANDLORD)
        );
        userPostgresAdapter.save(newUser);

        // Act & Assert
        assertThat(userPostgresAdapter.existsByEmail("exists.test@rentflow.com")).isTrue();
        assertThat(userPostgresAdapter.existsByEmail("notfound@rentflow.com")).isFalse();
    }

}
