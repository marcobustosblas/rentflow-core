package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "rut", length = 20)
    private String rut;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "roles", nullable = false)
    private String roles; // Guardado como String separado por comas

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserJpaEntity() {}

    public UserJpaEntity(UUID id, String email, String passwordHash, String fullName, String rut, String phoneNumber, String roles, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.rut = rut;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y métodos @PrePersist / @PreUpdate...
    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getRut() { return rut; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getRoles() { return roles; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}