package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PropertySpringDataRepository extends JpaRepository<PropertyJpaEntity, UUID> {
}
