package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BankAccountSpringDataRepository extends JpaRepository<BankAccountJpaEntity, UUID> {

    // Busca todas las cuentas bancarias asociadas a un usuario específico
    List<BankAccountJpaEntity> findByUserId(UUID userId);

}
