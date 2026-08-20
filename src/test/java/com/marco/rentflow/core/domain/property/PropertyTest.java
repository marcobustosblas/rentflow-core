package com.marco.rentflow.core.domain.property;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.exception.PropertyNotFoundException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class PropertyTest {

    private final UUID landlordId = UUID.randomUUID();

    @Test
    void shouldCreatePropertySuccessfully() {
        Property property = Property.registerNew(
                "Av. Diego Portales 123",
                new Money(new BigDecimal("350000.00"), Currency.CLP),
                landlordId);

        assertNotNull(property.getId());
        assertEquals("Av. Diego Portales 123", property.getAddress());
        assertEquals(PropertyStatus.AVAILABLE, property.getStatus());
        assertTrue(property.isAvailable());
        assertNull(property.getPayoutAccountId());
    }

    @Test
    void shouldAssignPayoutAccountSuccessfully() {
        Property property = Property.registerNew(
                "Av. Diego Portales 123",
                new Money(new BigDecimal("350000.00"), Currency.CLP),
                landlordId);

        UUID bankAccountId = UUID.randomUUID();

        property.assignPayoutAccount(bankAccountId);

        assertEquals(bankAccountId, property.getPayoutAccountId());
    }

    @Test
    void shouldUpdateBasePriceSuccessfully() {
        Property property = Property.registerNew(
                "Av. Diego Portales 123",
                new Money(new BigDecimal("350000.00"), Currency.CLP),
                landlordId);

        Money newPrice = new Money(new BigDecimal("400000.00"), Currency.CLP);

        property.updateBasePrice(newPrice);

        assertEquals(newPrice, property.getBasePrice());
    }

    @Test
    void shouldUpdateAddressSuccessfully() {
        Property property = Property.registerNew(
                "Pje. Gorgoña 123",
                new Money(new BigDecimal("350000.00"), Currency.CLP),
                landlordId);
        String newAddress = "Pje. Borgoña 123";

        property.updateAddress(newAddress);

        assertEquals(newAddress, property.getAddress());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingBasePriceToZeroOrNegative() {
        Property property = Property.registerNew(
                "Av. Diego Portales 123",
                new Money(new BigDecimal("350000.00"), Currency.CLP),
                landlordId);

        assertThrows(IllegalArgumentException.class,
                () -> property.updateBasePrice(new Money(new BigDecimal("0.00"), Currency.CLP)));
        assertThrows(IllegalArgumentException.class,
                () -> property.updateBasePrice(new Money(new BigDecimal("-100.00"), Currency.CLP)));
    }

    @Test
    void shouldChangeStatusTransitionsCorrectly() {
        Property property = Property.registerNew(
                "Av. Diego Portales 123",
                new Money(new BigDecimal("350000.00"), Currency.CLP),
                landlordId);

        property.markAsRented();
        assertEquals(PropertyStatus.RENTED, property.getStatus());
        assertFalse(property.isAvailable());

        property.markUnderMaintenance();
        assertEquals(PropertyStatus.MAINTENANCE, property.getStatus());

        property.markAsAvailable();
        assertEquals(PropertyStatus.AVAILABLE, property.getStatus());
    }

    // Tests para cubrir el coverage

    @Test
    void shouldThrowExceptionWhenAddressIsInvalid() {
        Property property = Property.registerNew(
                "Av. Diego Portales 123",
                new Money(new BigDecimal("350000.00"), Currency.CLP),
                landlordId);

        assertThrows(NullPointerException.class, () -> property.updateAddress(null));
        assertThrows(IllegalArgumentException.class, () -> property.updateAddress("   "));
    }

    @Test
    void shouldThrowExceptionWhenRequiredFieldsAreNull() {
        // Pruebas para el constructor inicial
        assertThrows(NullPointerException.class, () -> Property.registerNew(null, new Money(new BigDecimal("350000.00"), Currency.CLP), landlordId));
        assertThrows(NullPointerException.class, () -> Property.registerNew("Direction", null, landlordId));
        assertThrows(NullPointerException.class, () -> Property.registerNew("Direction", new Money(new BigDecimal("350000.00"), Currency.CLP), null));

        // Prueba para el constructor completo (Mapper)
        assertThrows(NullPointerException.class, () ->
                Property.reconstitute(UUID.randomUUID(), landlordId, null, "Dir", null, PropertyStatus.AVAILABLE, java.time.LocalDateTime.now(), java.time.LocalDateTime.now())
        );
    }

    @Test
    void shouldCoverAllGetters() {
        Property property = Property.registerNew(
                "Av. Diego Portales 123",
                new Money(new BigDecimal("350000.00"), Currency.CLP),
                landlordId);

        assertNotNull(property.getCreatedAt());
        assertNotNull(property.getUpdatedAt());
        assertEquals(landlordId, property.getLandlordId());
    }

    @Test
    void shouldInstantiateExceptions() {
        assertNotNull(new PropertyNotFoundException(UUID.randomUUID()));
    }
}
