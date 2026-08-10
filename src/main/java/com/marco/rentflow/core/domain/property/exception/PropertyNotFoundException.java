package com.marco.rentflow.core.domain.property.exception;

import java.util.UUID;

public class PropertyNotFoundException extends RuntimeException{

    public PropertyNotFoundException(UUID id) {
        super("Property not found with ID: " + id);
    }

}
