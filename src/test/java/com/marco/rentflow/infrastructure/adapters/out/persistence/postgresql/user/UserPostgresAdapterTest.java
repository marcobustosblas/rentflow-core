package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user;

import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class UserPostgresAdapterTest extends AbstractIntegrationTest {

    // inyecto el adaptador real y no un mock
    @Autowired
    private UserPostgresAdapter userPostgresAdapter;

    @Test
    @DisplayName("Debe guardar un Usuario en PostgreSQL y recuperarlo con todos sus atributos intactos")
    void shouldSaveAndRetrieveUser() {
        // a. ARRANGE (preparo el dato de Dominio)
        User newUser = User.registerNew(
                "marco.admin@rentflow.com",
                "hashed_password_123",
                "Marco Bustos Blas",
                "18313678-1",
                "+569 9 6509 0590",
                Role.ADMIN
        );
        // b. ACT (Acción: Guardarlo en la base de datos real)
        User savedUser = userPostgresAdapter.save(newUser);

        // c. ASSERT (Verificar que se generó el ID en BD)
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();

        // d. ACT 2 (Acción: Leerlo desde la base de datos para probar el Mapper de vuelta)
        Optional<User> retrievedUser = userPostgresAdapter.findById(savedUser.getId());

        // e. ASSERT 2 (Verificar integridad absoluta)
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getEmail()).isEqualTo("marco.admin@rentflow.com");
        assertThat(retrievedUser.get().getFullName()).isEqualTo("Marco Bustos Blas");
        assertThat(retrievedUser.get().getRut()).isEqualTo("18313678-1");
        assertThat(retrievedUser.get().getPhoneNumber()).isEqualTo("+569 9 6509 0590");
        // se agrego esto para comparar la colección Set<Role> que retorna getRoles() utilizando contains() en vez de isEqualTo() que fallaba al comparar un Set con un Enum directo
        assertThat(retrievedUser.get().getRoles()).contains(Role.ADMIN);

        // Verifica que las fechas de auditoría migraron de Java a SQL y volvieron correctamente
        assertThat(retrievedUser.get().getCreatedAt()).isNotNull();
        assertThat(retrievedUser.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debe retornar un Usuario existente cuando se busca por email")
    void shouldFindUserByEmail() {
        // Arrange
        User newUser = User.registerNew(
                "tenant.test@rentflow.com",
                "hashed_password_456",
                "Juan Pérez",
                "15555444-K",
                "+569 8 7654 3210",
                Role.TENANT
        );
        userPostgresAdapter.save(newUser);

        // Act
        Optional<User> foundUser = userPostgresAdapter.findByEmail("tenant.test@rentflow.com");

        // Assert - se agrego esto para validar la consulta findByEmail con un usuario real persistido
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
                "exists.test@rentflow.com",
                "hashed_password_789",
                "Maria Lopez",
                "12345678-5",
                "+569 7 1111 2222",
                Role.LANDLORD
        );
        userPostgresAdapter.save(newUser);

        // Act & Assert - se agrego esto para probar la existencia por email en BD real
        assertThat(userPostgresAdapter.existsByEmail("exists.test@rentflow.com")).isTrue();
        assertThat(userPostgresAdapter.existsByEmail("notfound@rentflow.com")).isFalse();
    }

}
