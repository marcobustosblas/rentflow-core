package com.marco.rentflow.core.domain.contract;

import com.marco.rentflow.core.domain.contract.exception.InvalidInvitationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class TenantInvitation {

    private final UUID id;
    private final UUID contractId;
    private final UUID landlordId;
    private final String tenantEmail;
    private final String tokenHash;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    private InvitationStatus status;
    private LocalDateTime acceptedAt;

    private TenantInvitation(UUID id,
                             UUID contractId,
                             UUID landlordId,
                             String tenantEmail,
                             String tokenHash,
                             LocalDateTime expiresAt,
                             InvitationStatus status,
                             LocalDateTime createdAt,
                             LocalDateTime acceptedAt) {
        this.id = Objects.requireNonNull(id);
        this.contractId = Objects.requireNonNull(contractId);
        this.landlordId = Objects.requireNonNull(landlordId);
        this.tenantEmail = Objects.requireNonNull(tenantEmail);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.acceptedAt = acceptedAt;
    }

    public static TenantInvitation create(
            UUID contractId, UUID landlordId, String tenantEmail,
            String tokenHash, LocalDateTime expiresAt) {
        if (expiresAt.isBefore(LocalDateTime.now())) {
            throw new InvalidInvitationException("Expiration date must be in the future.");
        }
        return new TenantInvitation(UUID.randomUUID(), contractId, landlordId, tenantEmail,
                tokenHash, expiresAt, InvitationStatus.PENDING, LocalDateTime.now(), null
        );
    }

    public static TenantInvitation reconstitute(UUID id,
                                                UUID contractId,
                                                UUID landlordId,
                                                String tenantEmail,
                                                String tokenHash,
                                                LocalDateTime expiresAt,
                                                InvitationStatus status,
                                                LocalDateTime createdAt,
                                                LocalDateTime acceptedAt) {
        return new TenantInvitation(id, contractId, landlordId, tenantEmail,
                tokenHash, expiresAt, status, createdAt, acceptedAt);
    }

    /**
     *  STATES QUERIES
     */

    public boolean isPending() {
        return this.status == InvitationStatus.PENDING;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public boolean canBeAcceptedAt(LocalDateTime now) {
        return isPending() && !isExpired(now);
    }

    /**
     *  STATES TRANSITIONS
     */

    public void markAsExpired() {
        if (!isPending()) {
            throw new InvalidInvitationException(
                    "Only PENDING invitations can be marked as EXPIRED. Current: " + status);
        }
        this.status = InvitationStatus.EXPIRED;
    }

    public void markAsAccepted(LocalDateTime acceptedTime) {
        if (!isPending()) {
            throw new InvalidInvitationException(
                    "Only PENDING invitations can be ACCEPTED. Current: " + status);
        }
        if (isExpired(acceptedTime)) {
            throw new InvalidInvitationException("Cannot accept an expired invitation.");
        }
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedAt = acceptedTime;
    }

    public void revoke() {
        if (!isPending()) {
            throw new InvalidInvitationException(
                    "Only PENDING invitations can be REVOKED. Current: " + status);
        }
        this.status = InvitationStatus.REVOKED;
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getContractId() {
        return contractId;
    }

    public UUID getLandlordId() {
        return landlordId;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
}
