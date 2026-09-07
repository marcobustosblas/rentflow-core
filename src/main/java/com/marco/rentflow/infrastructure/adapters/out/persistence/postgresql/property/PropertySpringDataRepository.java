package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property;

import com.marco.rentflow.core.domain.property.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertySpringDataRepository extends JpaRepository<PropertyJpaEntity, UUID> {
    List<PropertyJpaEntity> findByLandlordId(UUID landlordId);
    List<PropertyJpaEntity> findAllAvailable(String status);
    long countByLandlordId(UUID landlordId);
}
