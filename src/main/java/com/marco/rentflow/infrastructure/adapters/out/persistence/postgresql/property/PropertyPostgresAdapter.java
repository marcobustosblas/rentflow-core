package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property;

import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.PropertyStatus;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.property.mapper.PropertyPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component // el escudo de SPRING (como faltaba, spring es ciego a esta clase [9-9-26, 22:57 hr])
public class PropertyPostgresAdapter implements PropertyRepository {

    public final PropertySpringDataRepository springDataRepository;
    public final PropertyPersistenceMapper persistenceMapper;

    public PropertyPostgresAdapter(PropertySpringDataRepository repository, PropertyPersistenceMapper persistenceMapper) {
        this.springDataRepository = repository;
        this.persistenceMapper = persistenceMapper;
    }

    @Override
    public Property save(Property property) {
        PropertyJpaEntity entity = persistenceMapper.toJpaEntity(property);
        // se agrego esto para capturar el retorno de save() de Spring Data conteniendo la entidad persistida actualizada
        PropertyJpaEntity savedEntity = springDataRepository.save(entity);
        return persistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Property> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(persistenceMapper::toDomain);
    }
    @Override
    public List<Property> findByLandlordId(UUID landlordId) {
        return springDataRepository.findByLandlordId(landlordId).stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<Property> findByStatus(String status) {
        return springDataRepository.findByStatus(status)
                .stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public int countByLandlordId(UUID landlordId) {
        return (int) springDataRepository.countByLandlordId(landlordId);
    }

}
