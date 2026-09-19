package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;

import java.util.List;
import java.util.UUID;

public class ListLandlordPropertiesUseCase {

    private final PropertyRepository propertyRepository;

    public ListLandlordPropertiesUseCase(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    /**/
    public List<Property> execute(UUID landlordId) {
        return propertyRepository.findByLandlordId(landlordId);
    }

}
