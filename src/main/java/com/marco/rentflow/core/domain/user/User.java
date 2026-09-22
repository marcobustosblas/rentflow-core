package com.marco.rentflow.core.domain.user;

import com.marco.rentflow.core.domain.common.Rut;

import java.time.LocalDateTime;
import java.util.*;

public class User {
    private final UUID id;
    private String fullName;
    private String email;
    private String passwordHash;
    private Rut rut; // Evolución: Usamos el Value Object
    private String phoneNumber;
    private final Set<Role> roles;
    private UserStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 1. CONSTRUCTOR PRIVADO
    private User(UUID id,
                 String fullName,
                 String email,
                 String passwordHash,
                 Rut rut,
                 String phoneNumber,
                 Set<Role> roles,
                 UserStatus status,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "Password hash cannot be null");
        this.rut = rut; // Puede ser null inicialmente si el usuario no lo ingresa en el registro rápido
        this.phoneNumber = phoneNumber;
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("User must have at least one role");
        }
        // Defensive copy: prevents external mutation of the injected Set
        this.roles = new HashSet<>(roles);
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // 2. FACTORY METHOD: Registro Nuevo
    public static User registerNew(String fullName, String email, String passwordHash,
                                   String rutInput, String phoneNumber, Set<Role> roles) {
        Rut newRut = (rutInput != null && !rutInput.trim().isEmpty()) ? new Rut(rutInput) : null;
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (passwordHash == null || passwordHash.length() < 8) {
            throw new IllegalArgumentException("Password hash must be at least 8 characters");
        }
        LocalDateTime now = LocalDateTime.now();
        return new User(
                UUID.randomUUID(),
                fullName,
                email,
                passwordHash,
                newRut,
                phoneNumber,
                roles,
                UserStatus.PENDING_VERIFICATION,
                now,
                now
        );
    }

    // 3. FACTORY METHOD: Reconstitución desde la BD
    public static User reconstitute(UUID id,
                                    String fullName,
                                    String email,
                                    String passwordHash,
                                    String rutInput,
                                    String phoneNumber,
                                    Set<Role> roles,
                                    UserStatus status,
                                    LocalDateTime createdAt,
                                    LocalDateTime updatedAt) {
        Rut existingRut = (rutInput != null) ? new Rut(rutInput) : null;
        return new User(id, email, passwordHash, fullName, existingRut, phoneNumber, roles, status, createdAt, updatedAt);
    }

    // --- SEMANTIC DOMAIN METHODS ---

    public boolean canCreateProperty() {
        return this.status == UserStatus.ACTIVE && this.roles.contains(Role.LANDLORD);
    }

    public boolean canPayRent() {
        return this.status == UserStatus.ACTIVE && this.roles.contains(Role.TENANT);
    }

    public boolean canManageSubscriptions() {
        return this.status == UserStatus.ACTIVE && this.roles.contains(Role.PLATFORM_ADMIN);
    }

    public boolean canImpersonate() {
        return this.status == UserStatus.ACTIVE && this.roles.contains(Role.PLATFORM_ADMIN);
    }

    public void addRole(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        this.roles.add(role);
        touch();
    }

    public void removeRole(Role role) {
        if (this.roles.size() == 1 && this.roles.contains(role)) {
            throw new IllegalStateException("User must have at least one role.");
        }
        this.roles.remove(role);
        touch();
    }

    // --- STATE TRANSITIONS ---

    public void verify() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already verified.");
        }
        if (this.status == UserStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot verify a suspended user. Reactivate first.");
        }
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("Cannot verify a deleted user.");
        }
        this.status = UserStatus.ACTIVE;
        touch();
    }

    public void updateRut(String rutString) {
        this.rut = new Rut(rutString); // El Value Object valida automáticamente
        touch();
    }

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

    public void suspend() {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("Cannot suspend a deleted user.");
        }
        if (this.status == UserStatus.SUSPENDED) {
            return; // idempotent
        }
        this.status = UserStatus.SUSPENDED;
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

    public void reactivate() {
        if (this.status != UserStatus.SUSPENDED) {
            throw new IllegalStateException("Only suspended users can be reactivated.");
        }
        this.status = UserStatus.ACTIVE;
        touch();
    }

    public boolean isLandlord() {
        return this.roles.contains(Role.LANDLORD);
    }

    public boolean isTenant() {
        return this.roles.contains(Role.TENANT);
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS
    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }

    // Devuelve el String directo para mayor comodidad de quienes lo llamen (como el DTO)
    public String getRut() { return rut != null ? rut.getValue() : null; }

    public String getPhoneNumber() { return phoneNumber; }
    public Set<Role> getRoles() { return Set.copyOf(roles); }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}