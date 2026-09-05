package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BankAccountSpringDataRepository extends JpaRepository<BankAccountJpaEntity, UUID> {
}
