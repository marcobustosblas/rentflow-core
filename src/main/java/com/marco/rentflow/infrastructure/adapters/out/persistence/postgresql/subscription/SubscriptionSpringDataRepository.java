package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionSpringDataRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {
}
