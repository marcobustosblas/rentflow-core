package com.marco.rentflow.core.application.usecase.user;

import com.marco.rentflow.core.domain.common.exception.ForbiddenRegistrationException;
import com.marco.rentflow.core.domain.common.exception.ResourceAlreadyExistsException;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RegisterUserUseCase — solo permite crear LANDLORDs por endpoint público.
 * El endpoint público NO permite crear PLATFORM_ADMIN ni TENANT.
 * Este test cubre el happy path + los 4 guards de seguridad.
 */
@ExtendWith(MockitoExtension.class)
public class RegisterUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private RegisterUserUseCase useCase;

    private UUID landlordId;

    @BeforeEach
    void setUp() {
        landlordId = UUID.randomUUID();
    }

    // -- Happy path --

    @Test
    void shouldRegisterLandlord_whenAllValid() {
        // Arrange
        when(userRepository.existsByEmail("marco@gmail.com")).thenReturn(false);
        when(userRepository.existsByRut("18313111-1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv->inv.getArgument(0));

        // ACT
        User result = useCase.execute(
                "Marco Sabroso",
                "marco@gmail.com",
                "password123",
                "18313111-1",
                "+56965090511",
                Role.LANDLORD);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("marco@gmail.com");
        assertThat(result.getRoles()).containsExactly(Role.LANDLORD);

        verify(userRepository, times(1)).save(any(User.class));
    }

    // GUARDS DE SEGURIDAD — rechaza roles no autorizados

    @Test
    void shouldThrow_whenRoleIsPlatformAdmin() {
        assertThatThrownBy(() -> useCase.execute(
                "Hacker",
                "hacker@mail.com",
                "password123",
                "12345678-9",
                null,
                Role.PLATFORM_ADMIN))
                .isInstanceOf(ForbiddenRegistrationException.class);

        // Confirmamos que el use case hizo fail-fast SIN consultar la BD.
        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrow_whenRoleIsTenant() {
        // Los TENANTs solo nacen por invitación, no por registro público.

        assertThatThrownBy(() -> useCase.execute(
                "Juan",
                "juan@mail.com",
                "password123",
                "12345678-9",
                null,
                Role.TENANT))
                .isInstanceOf(ForbiddenRegistrationException.class);

        verifyNoInteractions(userRepository);
    }

    // VALIDACIÓN DE DUPLICADOS

    @Test
    void shouldThrow_whenEmailAlreadyExists() {
        // El use case debe abortar antes de verificar el RUT.
        when(userRepository.existsByEmail("marco@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(
                "Marco",
                "marco@mail.com",
                "password123",
                "12345678-9",
                null,
                Role.LANDLORD))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        // Verificamos que NO siguió al siguiente check ni intentó guardar.
        verify(userRepository, never()).existsByRut(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrow_whenRutAlreadyExists() {
        // Email libre, pero RUT duplicado.
        when(userRepository.existsByEmail("marco@mail.com")).thenReturn(false);
        when(userRepository.existsByRut("12345678-9")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(
                "Marco",
                "marco@mail.com",
                "password123",
                "12345678-9",
                null,
                Role.LANDLORD))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        // Verificamos que no intentó guardar.
        verify(userRepository, never()).save(any());
    }
}
