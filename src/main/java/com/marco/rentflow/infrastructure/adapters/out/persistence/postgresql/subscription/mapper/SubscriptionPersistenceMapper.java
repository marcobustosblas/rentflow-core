package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.subscription.mapper;

import com.marco.rentflow.core.domain.subscription.Subscription;
import com.marco.rentflow.core.domain.subscription.PlanType;
import com.marco.rentflow.core.domain.subscription.BillingCycle;
import com.marco.rentflow.core.domain.subscription.SubscriptionStatus;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.subscription.SubscriptionJpaEntity;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserJpaEntity;

public class SubscriptionPersistenceMapper {

    private SubscriptionPersistenceMapper() {}

    public static SubscriptionJpaEntity toJpaEntity(Subscription domain) {
        if (domain == null) return null;

        UserJpaEntity landlordProxy = new UserJpaEntity();
        landlordProxy.setId(domain.getLandlordId());

        return new SubscriptionJpaEntity(
                domain.getId(),
                landlordProxy,
                domain.getPlanType().name(),
                domain.getBillingCycle().name(),
                domain.getStatus().name(),
                domain.getMaxProperties(),
                domain.getMaxStorageMb(),
                domain.getCurrentPeriodStart(),
                domain.getCurrentPeriodEnd(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public static Subscription toDomain(SubscriptionJpaEntity entity) {
        if (entity == null) return null;

        return Subscription.reconstitute(
                entity.getId(),
                entity.getLandlord().getId(),
                PlanType.valueOf(entity.getPlanType()),
                BillingCycle.valueOf(entity.getBillingCycle()),
                SubscriptionStatus.valueOf(entity.getStatus()),
                entity.getMaxProperties(),
                entity.getMaxStorageMb(),
                entity.getCurrentPeriodStart(),
                entity.getCurrentPeriodEnd(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}