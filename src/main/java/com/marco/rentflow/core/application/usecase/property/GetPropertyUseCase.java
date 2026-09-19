package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.exception.PropertyNotFoundException;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;

import java.util.UUID;

public class GetPropertyUseCase {

    public final PropertyRepository propertyRepository;

    public GetPropertyUseCase(PropertyRepository repository) {
        this.propertyRepository = repository;
    }

    /**/
    public Property execute(UUID propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));
    }

}
