package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract.mapper;

import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ContractStatus;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract.ContractJpaEntity;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property.PropertyJpaEntity;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ContractPersistenceMapper {

    private ContractPersistenceMapper() {}

    public ContractJpaEntity toJpaEntity(RentalContract domain) {
        if (domain == null) return null;

        PropertyJpaEntity propertyProxy = new PropertyJpaEntity();
        propertyProxy.setId(domain.getPropertyId());

        UserJpaEntity tenantProxy = new UserJpaEntity();
        tenantProxy.setId(domain.getTenantId());

        return new ContractJpaEntity(
                domain.getId(),
                propertyProxy,
                tenantProxy,
                domain.getStatus().name(),
                domain.getStartDate(),
                domain.getEndDate(),
                domain.getPaymentDueDay(),
                domain.getMonthlyRent().getAmount(),
                domain.getDepositAmount().getAmount(),
                domain.getMonthlyRent().getCurrency().name(),
                domain.getDailyPenaltyRate(),
                domain.getLastReadjustmentDate()
        );
    }

    public RentalContract toDomain(ContractJpaEntity entity) {
        if (entity == null) return null;

        Currency currency = Currency.valueOf(entity.getCurrency());
        Money rentAmount = new Money(entity.getRentAmount(), currency);
        Money depositAmount = new Money(entity.getDepositAmount(), currency);

        // Extraigo el landlordId navegando por la relación de JPA
        UUID landlordId = entity.getProperty().getLandlord().getId();

        return RentalContract.reconstitute(
                entity.getId(),
                entity.getProperty().getId(),
                entity.getTenant().getId(),
                landlordId,
                rentAmount,
                depositAmount,
                entity.getDueDay(),
                entity.getDailyPenalty(),
                entity.getStartDate(),
                entity.getEndDate(),
                ContractStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt().toLocalDateTime(),
                entity.getUpdatedAt().toLocalDateTime(),
                entity.getLastReadjustmentDate()
        );
    }
}