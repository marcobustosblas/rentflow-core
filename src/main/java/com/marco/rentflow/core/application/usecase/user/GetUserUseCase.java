package com.marco.rentflow.core.application.usecase.user;

import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.exception.UserNotFoundException;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;

import java.util.UUID;

/**
 * Read-only use case: fetch a User by ID.
 * Throws UserNotFoundException if the user does not exist.
 */
public class GetUserUseCase {

    private final UserRepository userRepository;

    public GetUserUseCase(UserRepository repository) {
        this.userRepository = repository;
    }

    public User execute(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + userId));
    }

}
