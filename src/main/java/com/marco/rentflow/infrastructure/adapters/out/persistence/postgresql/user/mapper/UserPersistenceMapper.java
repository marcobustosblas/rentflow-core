package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.mapper;

import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.UserStatus;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user.UserJpaEntity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class UserPersistenceMapper {

    private UserPersistenceMapper() {}

    public static UserJpaEntity toJpaEntity(User domain) {
        if (domain == null) return null;

        // Convertir Set<Role> a String separado por comas: "LANDLORD,TENANT"
        String rolesString = domain.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        return new UserJpaEntity(
                domain.getId(),
                domain.getEmail(),
                domain.getPasswordHash(),
                domain.getFullName(),
                domain.getRut(),
                domain.getPhoneNumber(),
                rolesString,
                domain.getStatus().name(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        // Convertir String "LANDLORD,TENANT" a Set<Role>
        Set<Role> rolesSet = Arrays.stream(entity.getRoles().split(","))
                .map(String::trim)
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        // Usar el Factory Method de reconstitución de tu Dominio
        return User.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getRut(),
                entity.getPhoneNumber(),
                rolesSet,
                UserStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}