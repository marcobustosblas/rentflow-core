package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property.mapper;

import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.PropertyStatus;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property.PropertyJpaEntity;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserJpaEntity;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount.BankAccountJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PropertyPersistenceMapper {

    public PropertyJpaEntity toJpaEntity(Property domain) {
        if (domain == null) return null;

        // Crear proxy del Landlord
        UserJpaEntity landlordProxy = new UserJpaEntity();
        landlordProxy.setId(domain.getLandlordId());

        // Crear proxy de la cuenta bancaria (si existe)
        BankAccountJpaEntity accountProxy = null;
        if (domain.getBankAccountId() != null) {
            accountProxy = new BankAccountJpaEntity();
            accountProxy.setId(domain.getBankAccountId());
        }

        return new PropertyJpaEntity(
                domain.getId(),
                landlordProxy,
                accountProxy,
                domain.getAddress(),
                domain.getStatus().name(),
                domain.getBasePrice().getAmount(),
                domain.getBasePrice().getCurrency().name()
        );
    }

    public Property toDomain(PropertyJpaEntity entity) {
        if (entity == null) return null;

        Money basePrice = new Money(
                entity.getBasePrice(),
                Currency.valueOf(entity.getCurrency())
        );

        UUID accountId = entity.getPayoutAccount() != null ? entity.getPayoutAccount().getId() : null;

        // Utilizamos el factory method/constructor de tu Dominio
        return Property.reconstitute(
                entity.getId(),
                entity.getLandlord().getId(),
                accountId,
                entity.getAddress(),
                basePrice,
                PropertyStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt().toLocalDateTime(),
                entity.getUpdatedAt().toLocalDateTime()
        );
    }
}