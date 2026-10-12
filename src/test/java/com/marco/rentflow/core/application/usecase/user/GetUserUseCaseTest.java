package com.marco.rentflow.core.application.usecase.user;

import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserStatus;
import com.marco.rentflow.core.domain.user.exception.UserNotFoundException;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetUserUseCaseTest {

    private UserRepository userRepository;
    private GetUserUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new GetUserUseCase(userRepository);
    }

    @Test
    void shouldReturnUser_whenUserExists() {
        UUID userId = UUID.randomUUID();
        User user = User.reconstitute(
                userId,
                "Juan Pérez",
                "juan@mail.com",
                "hash",
                "12345678-9",
                "+56912345678",
                Set.of(Role.TENANT),
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = useCase.execute(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo("juan@mail.com");
    }

    @Test
    void shouldThrow_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

}
