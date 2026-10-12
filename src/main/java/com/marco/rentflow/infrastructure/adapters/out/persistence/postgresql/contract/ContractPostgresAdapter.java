package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract.mapper.ContractPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ContractPostgresAdapter implements ContractRepository {

    private final ContractSpringDataRepository springDataRepository;
    private final ContractPersistenceMapper mapper;

    public ContractPostgresAdapter(ContractSpringDataRepository springDataRepository, ContractPersistenceMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    /*
    El problema:
    1. En ContractPostgresAdapter.save(...), haces repository.save(entity) y luego intentas mapear la entidad guardada de vuelta a dominio con mapper.toDomain(...).
    2.Pero entity.getProperty() es un proxy lazy que NO tiene sesión activa cuando toDomain intenta acceder a property.getLandlord().getId().
    3.Resultado: LazyInitializationException.
     */

    @Override
    public RentalContract save(RentalContract contract) {
        ContractJpaEntity entity = mapper.toJpaEntity(contract);
        ContractJpaEntity saved = springDataRepository.save(entity);
        // Re-fetch con relaciones cargadas usando JOIN FETCH
        ContractJpaEntity hydrated = springDataRepository.findByIdWithRelations(saved.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Contract not found after save: " + saved.getId()));
        return mapper.toDomain(hydrated);
    }

    /*
    Ventaja: la query findByIdWithRelations trae property + landlord + tenant
    en una sola consulta. El mapper no dispara lazy loading.
     */

    @Override
    public Optional<RentalContract> findById(UUID id) {
        return springDataRepository.findByIdWithRelations(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<RentalContract> findByTenantId(UUID tenantId) {
        return springDataRepository.findAllByTenantIdWithRelations(tenantId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<RentalContract> findByPropertyLandlordId(UUID landlordId) {
        return springDataRepository.findAllByLandlordIdWithRelations(landlordId).stream()
                .map(mapper::toDomain)
                .toList();
    }

}
