package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContractSpringDataRepository extends JpaRepository<ContractJpaEntity, UUID> {
    @Query("""
        SELECT c FROM ContractJpaEntity c
        JOIN FETCH c.property p
        JOIN FETCH p.landlord
        LEFT JOIN FETCH c.tenant
        WHERE c.id = :id
        """)
    Optional<ContractJpaEntity> findByIdWithRelations(@Param("id") UUID id);

    @Query("""
        SELECT c FROM ContractJpaEntity c
        JOIN FETCH c.property p
        JOIN FETCH p.landlord
        LEFT JOIN FETCH c.tenant
        WHERE p.landlord.id = :landlordId
        """)
    List<ContractJpaEntity> findAllByLandlordIdWithRelations(@Param("landlordId") UUID landlordId);
}
