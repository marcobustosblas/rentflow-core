package com.marco.rentflow.infrastructure.adapters.in.web.user;

import com.marco.rentflow.core.application.usecase.user.GetUserUseCase;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.user.mapper.UserRestMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final UserRestMapper userRestMapper;

    public UserController(GetUserUseCase useCase, UserRestMapper userRestMapper) {
        this.getUserUseCase = useCase;
        this.userRestMapper = userRestMapper;
    }

    @GetMapping("/{id}")
    // TODO(M5-security): @PreAuthorize("hasRole('PLATFORM_ADMIN') or #id == principal.id")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable UUID id) {
        User user = getUserUseCase.execute(id);
        return ResponseEntity.ok(userRestMapper.toResponseDTO(user));
    }
}

/*
(9-10, 11:21 am
Nota sobre seguridad: GET /users/{id} debería estar restringido a "el propio usuario o un admin".
En M5 lo harás con @PreAuthorize. Por ahora, TODO.
 */
