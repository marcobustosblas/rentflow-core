package com.marco.rentflow.infrastructure.adapters.in.web.auth;

import com.marco.rentflow.core.application.usecase.user.RegisterUserUseCase;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.exception.InvalidRoleException;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.user.mapper.UserRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserRestMapper userRestMapper;

    public AuthController(RegisterUserUseCase registerUserUseCase, UserRestMapper userRestMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.userRestMapper = userRestMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerLandlord(@Valid @RequestBody UserRequestDTO request) {

        /* Zero-Trust: Intercepto cualquier intento de inyectar roles privilegiados o de inquilino */
        if (!"LANDLORD".equalsIgnoreCase(request.role())) {
            throw new InvalidRoleException("Public registration is strictly restricted to LANDLORD accounts.");
        }

        User registeredUser = registerUserUseCase.execute(
                request.fullName(),
                request.email(),
                request.password(),
                request.rut(),
                request.phoneNumber(),
                Role.LANDLORD
        );

        UserResponseDTO response = userRestMapper.toResponseDTO(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
