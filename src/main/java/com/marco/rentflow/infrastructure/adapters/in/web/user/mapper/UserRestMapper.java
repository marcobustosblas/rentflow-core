package com.marco.rentflow.infrastructure.adapters.in.web.user.mapper;

import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class UserRestMapper {

    public UserResponseDTO toDto(User user) {
        if (user == null) return null;

        return new UserResponseDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRut(),
                user.getStatus().name()
        );
    }

}
