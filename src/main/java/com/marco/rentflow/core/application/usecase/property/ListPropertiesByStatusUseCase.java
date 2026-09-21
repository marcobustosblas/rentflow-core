package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;

import java.util.List;

public class ListPropertiesByStatusUseCase {

    public final PropertyRepository propertyRepository;

    public ListPropertiesByStatusUseCase(PropertyRepository repository) {
        this.propertyRepository = repository;
    }

    /**/
    public List<Property> execute(String status) {
        return propertyRepository.findByStatus(status.toUpperCase());
    }

}
