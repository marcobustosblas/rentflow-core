package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract.mapper.ContractPersistenceMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContractPostgresAdapter implements ContractRepository {

    private final ContractSpringDataRepository springDataRepository;
    private final ContractPersistenceMapper mapper;

    public ContractPostgresAdapter(ContractSpringDataRepository springDataRepository, ContractPersistenceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public RentalContract save(RentalContract contract) {
        ContractJpaEntity entity = mapper.toJpaEntity(contract);
        ContractJpaEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<RentalContract> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<RentalContract> findByTenantId(UUID tenantId) {
        return springDataRepository.findByTenantId(tenantId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RentalContract> findByPropertyLandlordId(UUID landlordId) {
        return springDataRepository.findByPropertyLandlordId(landlordId).stream()
                .map(mapper::toDomain)
                .toList();
    }

}
