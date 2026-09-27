package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract.mapper;

import com.marco.rentflow.core.domain.contract.TenantInvitation;
import com.marco.rentflow.core.domain.contract.InvitationStatus;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract.TenantInvitationJpaEntity;
import com.marco.rentflow.infrastructure.utils.DateConverter;
import org.springframework.stereotype.Component;

@Component
public class TenantInvitationPersistenceMapper {

    public TenantInvitationJpaEntity toJpaEntity(TenantInvitation domain) {
        if (domain == null) return null;

        return new TenantInvitationJpaEntity(
                domain.getId(),
                domain.getContractId(),
                domain.getLandlordId(),
                domain.getTenantEmail(),
                domain.getTokenHash(),
                DateConverter.toZonedDateTime(domain.getExpiresAt()),
                domain.getStatus().name(),
                DateConverter.toZonedDateTime(domain.getCreatedAt()),
                DateConverter.toZonedDateTime(domain.getAcceptedAt())
        );
    }

    public TenantInvitation toDomain(TenantInvitationJpaEntity entity) {
        if (entity == null) return null;

        return TenantInvitation.reconstitute(
                entity.getId(),
                entity.getContractId(),
                entity.getLandlordId(),
                entity.getTenantEmail(),
                entity.getTokenHash(),
                entity.getExpiresAt().toLocalDateTime(),
                InvitationStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt().toLocalDateTime(),
                entity.getAcceptedAt() != null ? entity.getAcceptedAt().toLocalDateTime() : null
        );
    }
}