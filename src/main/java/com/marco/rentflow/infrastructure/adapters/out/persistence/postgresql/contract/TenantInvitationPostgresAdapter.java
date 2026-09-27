package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import com.marco.rentflow.core.domain.contract.TenantInvitation;
import com.marco.rentflow.core.domain.contract.ports.out.TenantInvitationRepository;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract.mapper.TenantInvitationPersistenceMapper;

import java.util.Optional;

public class TenantInvitationPostgresAdapter implements TenantInvitationRepository {

    private final TenantInvitationSpringDataRepository springDataRepository;
    private final TenantInvitationPersistenceMapper mapper;

    public TenantInvitationPostgresAdapter(TenantInvitationSpringDataRepository springDataRepository,
                                           TenantInvitationPersistenceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public TenantInvitation save(TenantInvitation invitation) {
        TenantInvitationJpaEntity entity = mapper.toJpaEntity(invitation);
        TenantInvitationJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TenantInvitation> findByTokenHash(String tokenHash) {
        return springDataRepository.findByTokenHash(tokenHash)
                .map(mapper::toDomain);
    }

}
