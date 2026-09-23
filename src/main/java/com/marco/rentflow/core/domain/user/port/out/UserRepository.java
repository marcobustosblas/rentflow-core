package com.marco.rentflow.core.domain.user.port.out;

import com.marco.rentflow.core.domain.user.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRut(String rut);

}
