package com.marco.rentflow.infrastructure.adapters.in.web.user.mapper;

import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserRestMapper {

    @Mapping(source = "createdAt", target = "created")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "mapRolesToStrings")
    UserResponseDTO toResponseDTO(User user);

    @Named("mapRolesToStrings")
    default Set<String> mapRolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream().map(Enum::name).collect(Collectors.toSet());
    }

}

/**
 * (24-9-26, 16:43 hrs)
 * YO tenía esto en el UserResponseDTO: Set<String> roles pero el domain dice Set<Role> roles
 * la solution: user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
 * en algún momento pensé en el map y transformar cada elemento a String pero no pude concretarlo
 */
