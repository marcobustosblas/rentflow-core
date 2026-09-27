package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.contract;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenant_invitations")
public class TenantInvitationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @Column(name = "landlord_id", nullable = false)
    private UUID landlordId;

    @Column(name = "tenant_email", nullable = false, length = 255)
    private String tenantEmail;

    @Column(name = "token_hash", nullable = false, unique = true, length = 500)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;

    /**
     * InvitationStatus: 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'REVOKED'
     */
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "accepted_at")
    private ZonedDateTime acceptedAt;

    /** JPA requires a no-args constructor. */
    protected TenantInvitationJpaEntity() {}

    public TenantInvitationJpaEntity(UUID id,
                                     UUID contractId,
                                     UUID landlordId,
                                     String tenantEmail,
                                     String tokenHash,
                                     ZonedDateTime expiresAt,
                                     String status,
                                     ZonedDateTime createdAt,
                                     ZonedDateTime acceptedAt) {
        this.id = id;
        this.contractId = contractId;
        this.landlordId = landlordId;
        this.tenantEmail = tenantEmail;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.status = status;
        this.createdAt = createdAt;
        this.acceptedAt = acceptedAt;
    }

    @PrePersist
    protected void onCreate() {
        // Respect timestamps provided by the mapper; fallback to now() if absent.
        if (this.createdAt == null) {
            this.createdAt = ZonedDateTime.now();
        }
    }

    // --- Getters ---
    public UUID getId() { return id; }
    public UUID getContractId() { return contractId; }
    public UUID getLandlordId() { return landlordId; }
    public String getTenantEmail() { return tenantEmail; }
    public String getTokenHash() { return tokenHash; }
    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public String getStatus() { return status; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getAcceptedAt() { return acceptedAt; }

    // --- Setters ---
    public void setId(UUID id) { this.id = id; }
    public void setContractId(UUID contractId) { this.contractId = contractId; }
    public void setLandlordId(UUID landlordId) { this.landlordId = landlordId; }
    public void setTenantEmail(String tenantEmail) { this.tenantEmail = tenantEmail; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }
    public void setAcceptedAt(ZonedDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
}