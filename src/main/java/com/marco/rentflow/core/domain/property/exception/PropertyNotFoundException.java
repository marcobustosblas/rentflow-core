package com.marco.rentflow.core.domain.property.exception;

import com.marco.rentflow.core.domain.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class PropertyNotFoundException extends ResourceNotFoundException {

    public PropertyNotFoundException(UUID id) {
        super("Property not found with ID: " + id);
    }

}
