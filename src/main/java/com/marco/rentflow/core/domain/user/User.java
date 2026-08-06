package com.marco.rentflow.core.domain.user;

import java.time.LocalDateTime;
import java.util.*;

public class User {
    private final UUID id;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phoneNumber;
    private final Set<Role> roles;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(String email, String passwordHash, String fullName, String phoneNumber, Role initialRole) {
        this(UUID.randomUUID(), email, passwordHash, fullName, phoneNumber, Set.of(initialRole), UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now());
    }

    public User(UUID id, String email, String passwordHash, String fullName, String phoneNumber, Set<Role> roles, UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
        this.phoneNumber = phoneNumber;
        this.roles = new HashSet<>(Objects.requireNonNull(roles, "Role cannot be null"));
        if (this.roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // MÉTODOS Y REGLAS DE DOMINIO

    public void changeEmail(String newEmail) {
        Objects.requireNonNull(newEmail, "Email cannot be null");
        if (newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        this.email = newEmail;
        touch();
    }

    public void changePassword(String newPasswordHash) {
        Objects.requireNonNull(newPasswordHash, "New password hash cannot be null");
        if (newPasswordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash cannot be empty");
        }
        this.passwordHash = newPasswordHash;
        touch();
    }

    public void updateFullName(String newFullName) {
        Objects.requireNonNull(newFullName, "Full name cannot be null");
        if (newFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
        this.fullName = newFullName;
        touch();
    }

    public void changePhoneNumber(String newPhoneNumber) {
        Objects.requireNonNull(newPhoneNumber, "Phone Number cannot be null");
        if (newPhoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone Number cannot be empty");
        }
        this.phoneNumber = newPhoneNumber;
        touch();
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        touch();
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
        touch();
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
        touch();
    }

    public void addRole(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        this.roles.add(role);
        touch();
    }

    public void removeRole(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        this.roles.remove(role);
        touch();
    }

    public boolean isLandlord() {
        return this.roles.contains(Role.LANDLORD);
    }

    public boolean isTenant() {
        return this.roles.contains(Role.TENANT);
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public Set<Role> getRoles() { return Collections.unmodifiableSet(roles); }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

}
