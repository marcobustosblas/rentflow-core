package com.marco.rentflow.core.domain.property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Property {
    private final UUID id;
    private final UUID landlordId;
    private UUID payoutAccountId;
    private String address;
    private BigDecimal basePrice;
    private PropertyStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor para propiedad NUEVA
    public Property(String address, BigDecimal basePrice, UUID landlordId) {
        this(UUID.randomUUID(), landlordId, null, address, basePrice, PropertyStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now());
    }

    // Constructor completo para reconstitución desde Infraestructura / Mapper
    public Property(UUID id, UUID landlordId, UUID payoutAccountId, String address, BigDecimal basePrice, PropertyStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "Property ID cannot be null");
        this.landlordId = Objects.requireNonNull(landlordId, "Landlord ID cannot be null");
        this.payoutAccountId = payoutAccountId;
        this.address = Objects.requireNonNull(address, "Address cannot be null");
        this.basePrice = Objects.requireNonNull(basePrice, "Base price cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    // REGLAS Y MÉTODOS DE DOMINIO

    public void assignPayoutAccount(UUID bankAccountId) {
        this.payoutAccountId = Objects.requireNonNull(bankAccountId, "Payout account ID cannot be null");
        touch();
    }

    public void updateBasePrice(BigDecimal newPrice) {
        Objects.requireNonNull(newPrice, "Base price cannot be null");
        if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Base price must be greater than zero");
        }
        this.basePrice = newPrice;
        touch();
    }

    public void updateAddress(String newAddress) {
        Objects.requireNonNull(newAddress, "Address cannot be null");
        if (newAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }
        this.address = newAddress;
        touch();
    }

    public void markAsRented() {
        this.status = PropertyStatus.RENTED;
        touch();
    }

    public void markAsAvailable() {
        this.status = PropertyStatus.AVAILABLE;
        touch();
    }

    public void markUnderMaintenance() {
        this.status = PropertyStatus.MAINTENANCE;
        touch();
    }

    public boolean isAvailable() {
        return this.status == PropertyStatus.AVAILABLE;
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // GETTERS
    public UUID getId() { return id; }
    public UUID getLandlordId() { return landlordId; }
    public UUID getPayoutAccountId() { return payoutAccountId; }
    public String getAddress() { return address; }
    public BigDecimal getBasePrice() { return basePrice; }
    public PropertyStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}