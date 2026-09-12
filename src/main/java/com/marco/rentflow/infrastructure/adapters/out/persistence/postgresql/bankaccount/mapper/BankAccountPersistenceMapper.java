package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount.mapper;

import com.marco.rentflow.core.domain.bankaccount.AccountType;
import com.marco.rentflow.core.domain.bankaccount.BankAccount;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount.BankAccountJpaEntity;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserJpaEntity;
import com.marco.rentflow.infrastructure.utils.DateConverter;


public class BankAccountPersistenceMapper {

    public static BankAccountJpaEntity toJpaEntity(BankAccount domain) {
        if (domain == null) return null;

        // Proxy para evitar N+1 o dependencias cíclicas
        UserJpaEntity userProxy = new UserJpaEntity();
        userProxy.setId(domain.getUserId());
        return new BankAccountJpaEntity(
                domain.getId(),
                userProxy,
                domain.getAccountType().name(),
                domain.getBankName(),
                domain.getAccountNumber(),
                domain.getHolderRut(),
                DateConverter.toZonedDateTime(domain.getCreatedAt()),
                DateConverter.toZonedDateTime(domain.getUpdatedAt())
        );
    }

    public static BankAccount toDomain(BankAccountJpaEntity entity) {
        if (entity == null) return null;

        return BankAccount.reconstitute(
                entity.getId(),
                entity.getUser().getId(), // Desempaqueto el UUID de la relación JPA
                entity.getBankName(),
                AccountType.valueOf(entity.getAccountType()),
                entity.getAccountNumber(),
                entity.getRut(),
                entity.getCreatedAt().toLocalDateTime(),
                entity.getUpdatedAt().toLocalDateTime()
        );
    }

}
