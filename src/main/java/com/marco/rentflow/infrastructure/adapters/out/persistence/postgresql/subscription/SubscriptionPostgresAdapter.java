package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.subscription;

import com.marco.rentflow.core.domain.subscription.Subscription;
import com.marco.rentflow.core.domain.subscription.ports.out.SubscriptionRepository;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.subscription.mapper.SubscriptionPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionPostgresAdapter implements SubscriptionRepository {

    private final SubscriptionSpringDataRepository springDataRepository;

    public SubscriptionPostgresAdapter(SubscriptionSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionJpaEntity entity = SubscriptionPersistenceMapper.toJpaEntity(subscription);
        SubscriptionJpaEntity savedEntity = springDataRepository.save(entity);
        return SubscriptionPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(SubscriptionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Subscription> findByLandlordId(UUID landlordId) {
        return springDataRepository.findByLandlordId(landlordId)
                .map(SubscriptionPersistenceMapper::toDomain);
    }
}