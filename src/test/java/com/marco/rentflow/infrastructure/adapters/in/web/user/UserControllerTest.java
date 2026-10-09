package com.marco.rentflow.infrastructure.adapters.in.web.user;

import com.marco.rentflow.core.application.usecase.user.GetUserUseCase;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserStatus;
import com.marco.rentflow.core.domain.user.exception.UserNotFoundException;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.user.mapper.UserRestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private GetUserUseCase getUserUseCase;
    @MockBean private UserRestMapper userRestMapper;

    @Test
    void shouldReturnUser_whenUserExists() throws Exception {
        UUID id = UUID.randomUUID();
        User user = User.reconstitute(id, "Juan Pérez", "juan@mail.com", "hash",
                "12345678-9", "+56912345678", Set.of(Role.TENANT),
                UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());

        UserResponseDTO dto = new UserResponseDTO(
                id, "Juan Pérez", "juan@mail.com", "12345678-9", "+56912345678",
                "ACTIVE", Set.of("TENANT"), LocalDateTime.now());

        when(getUserUseCase.execute(id)).thenReturn(user);
        when(userRestMapper.toResponseDTO(user)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@mail.com"))
                .andExpect(jsonPath("$.roles[0]").value("TENANT"));
    }

    @Test
    void shouldReturn404_whenUserNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(getUserUseCase.execute(any())).thenThrow(new UserNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/users/{id}", id))
                .andExpect(status().isNotFound());
    }

}
