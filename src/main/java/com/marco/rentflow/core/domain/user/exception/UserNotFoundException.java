package com.marco.rentflow.core.domain.user.exception;

import com.marco.rentflow.core.domain.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(UUID id) {
        super("User not found with ID: " + id);
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

}
