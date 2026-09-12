package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user;

import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserRepository;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.mapper.UserPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPostgresAdapter implements UserRepository {

    private final UserSpringDataRepository springDataRepository;

    public UserPostgresAdapter(UserSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public User save(User user) {
        // a. Traducir de Dominio a JPA
        UserJpaEntity entity = UserPersistenceMapper.toJpaEntity(user);

        // b. Persistir y capturar el retorno (para fechas de auditoría @PrePersist)
        UserJpaEntity savedEntity = springDataRepository.save(entity);

        // c. Devolver Dominio puro al Caso de Uso
        return UserPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepository.findByEmail(email)
                .map(UserPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }
}