package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContractSpringDataRepository extends JpaRepository<ContractJpaEntity, UUID> {
}
