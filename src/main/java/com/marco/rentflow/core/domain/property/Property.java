package com.marco.rentflow.core.domain.property;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Property {
    private final UUID id;
    private final String address;
    private PropertyStatus status;
    private final BigDecimal basePrice;
    private final UUID landlordId;

    // Constructor para propiedad nueva (Auto-genera UUID)
    public Property(String address, PropertyStatus status, BigDecimal basePrice, UUID landlordId) {
        this(UUID.randomUUID(), address, status, basePrice, landlordId);
    }

    // Constructor para reconstituir propiedad existente desde BD
    public Property(UUID id, String address, PropertyStatus status, BigDecimal basePrice, UUID landlordId) {
        this.id = Objects.requireNonNull(id, "Property ID cannot be null");
        this.address = Objects.requireNonNull(address, "Address cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.basePrice = Objects.requireNonNull(basePrice, "Base price cannot be null");
        this.landlordId = Objects.requireNonNull(landlordId, "Landlord ID cannot be null");
    }

    // Comportamiento de dominio: Transición de estado
    public void changeStatus(PropertyStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "New status cannot be null");
    }

    public UUID getId() { return id; }
    public String getAddress() { return address; }
    public PropertyStatus getStatus() { return status; }
    public BigDecimal getBasePrice() { return basePrice; }
    public UUID getLandlordId() { return landlordId; }

}
