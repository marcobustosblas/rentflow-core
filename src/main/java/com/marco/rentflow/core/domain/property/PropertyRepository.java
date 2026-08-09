package com.marco.rentflow.core.domain.property;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {

    Property save(Property property);
    Optional<Property> findById(UUID id);
    List<Property> findByLandlordId(UUID landlordId);
    List<Property> findAllAvailable();

}
