package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentSpringDataRepository extends JpaRepository<PaymentJpaEntity, UUID> {
}
