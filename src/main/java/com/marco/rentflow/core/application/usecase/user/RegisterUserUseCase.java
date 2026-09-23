package com.marco.rentflow.core.application.usecase.user;

import com.marco.rentflow.core.domain.common.exception.ForbiddenRegistrationException;
import com.marco.rentflow.core.domain.user.exception.InvalidRoleException;
import com.marco.rentflow.core.domain.common.exception.ResourceAlreadyExistsException;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.exception.InvalidUserDataException;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;

import java.util.Set;

public class RegisterUserUseCase {

    private final UserRepository userRepository;

    public RegisterUserUseCase(UserRepository repository) {
        this.userRepository = repository;
    }

    public User execute(String fullName, String email, String rawPassword,
                        String rut, String phoneNumber, Role requestedRole) {

        if (requestedRole == null) {
            throw new InvalidRoleException("Role cannot be null.");
        }

        // los registros públicos son restringidos por:
        if (requestedRole == Role.PLATFORM_ADMIN) {
            throw new ForbiddenRegistrationException(
                    "Cannot register a platform administrator via public endpoints.");
        }
        if (requestedRole == Role.TENANT) {
            throw new ForbiddenRegistrationException(
                    "TENANT accounts must be invited by a landlord.");
        }

        // check la unicidad del email & rut
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("The email address is already in use.");
        }
        if (userRepository.existsByRut(rut)) {
            throw new ResourceAlreadyExistsException("The RUT is already registered in the system.");
        }

        // construyo el domain sabroso
        // (fullName not blank, email contains '@', rawPassword length, rut not blank).
        User newUser;
        try {
            // TODO(W9-security): inject PasswordHasher port and hash rawPassword before this call.
            newUser = User.registerNew(fullName, email, rawPassword, rut, phoneNumber, Set.of(requestedRole));
        } catch (IllegalArgumentException e) {
            throw new InvalidUserDataException(e.getMessage());
        }

        return userRepository.save(newUser);
    }

}
