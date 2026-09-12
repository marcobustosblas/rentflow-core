package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractSpringDataRepository extends JpaRepository<ContractJpaEntity, UUID> {
    // Navegación JPA: Busca el Tenant por su ID en el proxy
    List<ContractJpaEntity> findByTenantId(UUID tenantId);

    // Navegación JPA: Entra a Property y busca el Landlord
    List<ContractJpaEntity> findByPropertyLandlordId(UUID landlordId);
}
