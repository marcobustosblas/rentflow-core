package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantInvitationSpringDataRepository extends JpaRepository<TenantInvitationJpaEntity, UUID> {

    Optional<TenantInvitationJpaEntity> findByTokenHash(String tokenHash);
    List<TenantInvitationJpaEntity> findByContractId(UUID contractId);
    List<TenantInvitationJpaEntity> findByStatus(String status);

}