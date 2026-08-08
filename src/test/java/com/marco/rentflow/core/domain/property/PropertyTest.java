package com.marco.rentflow.core.domain.property;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyTest {

    private final UUID landlordId = UUID.randomUUID();

    @Test
    void shouldCreatePropertySuccessfully() {
        Property property = new Property(
                "Av. Diego Portales 123", new BigDecimal("350000.00"), landlordId);

        assertNotNull(property.getId());
        assertEquals("Av. Diego Portales 123", property.getAddress());
        assertEquals(PropertyStatus.AVAILABLE, property.getStatus());
        assertTrue(property.isAvailable());
        assertNull(property.getPayoutAccountId());
    }

    @Test
    void shouldAssignPayoutAccountSuccessfully() {
        Property property = new Property("Av. Diego Portales 123", new BigDecimal("350000.00"), landlordId);
        UUID bankAccountId = UUID.randomUUID();

        property.assignPayoutAccount(bankAccountId);

        assertEquals(bankAccountId, property.getPayoutAccountId());
    }

    @Test
    void shouldUpdateBasePriceSuccessfully() {
        Property property = new Property("Av. Diego Portales 123", new BigDecimal("350000.00"), landlordId);
        BigDecimal newPrice = new BigDecimal("400000.00");

        property.updateBasePrice(newPrice);

        assertEquals(newPrice, property.getBasePrice());
    }

    @Test
    void shouldUpdateAddressSuccessfully() {
        Property property = new Property(
                "Pje. Borgoño 123", new BigDecimal("350000.00"), landlordId);
        String newAddress = "Pje. Borgoña 123";

        property.updateAddress(newAddress);

        assertEquals(newAddress, property.getAddress());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingBasePriceToZeroOrNegative() {
        Property property = new Property(
                "Av. Diego Portales 123", new BigDecimal("350000.00"), landlordId);

        assertThrows(IllegalArgumentException.class,
                () -> property.updateBasePrice(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> property.updateBasePrice(new BigDecimal("-100.00")));
    }

    @Test
    void shouldChangeStatusTransitionsCorrectly() {
        Property property = new Property("Av. Diego Portales 123", new BigDecimal("350000.00"), landlordId);

        property.markAsRented();
        assertEquals(PropertyStatus.RENTED, property.getStatus());
        assertFalse(property.isAvailable());

        property.markUnderMaintenance();
        assertEquals(PropertyStatus.MAINTENANCE, property.getStatus());

        property.markAsAvailable();
        assertEquals(PropertyStatus.AVAILABLE, property.getStatus());
    }
}
