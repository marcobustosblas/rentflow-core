package com.marco.rentflow.core.domain.user;

import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID id;
    private final String email;
    private final String fullName;
    private final Role role;

    public User(String email, String fullName, Role role) {
        this(UUID.randomUUID(), email, fullName, role);
    }

    public User(UUID id, String email, String fullName, Role role) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }
}
