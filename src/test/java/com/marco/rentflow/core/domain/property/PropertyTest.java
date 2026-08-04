package com.marco.rentflow.core.domain.property;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

public class PropertyTest {
    
    private final UUID landlordId = UUID.randomUUID();
    
    @Test
    void shouldCreatePropertySuccessfully() {
        Property property = new Property("Street Avocado 371", PropertyStatus.AVAILABLE, new BigDecimal("350000.00"), landlordId);
    
        assertNotNull(property.getId());
        assertEquals("Street Avocado 371", property.getAddress());
        assertEquals(PropertyStatus.AVAILABLE, property.getStatus());
        assertEquals(new BigDecimal("350000.00"), property.getBasePrice());
        assertNotNull(property.getLandlordId());
    }

    @Test
    void shouldChangePropertyStatusSuccessfully() {
        Property property = new Property("Street Avocado 371", PropertyStatus.AVAILABLE, new BigDecimal("350000.00"), landlordId);

        property.changeStatus(PropertyStatus.RENTED);

        assertEquals(PropertyStatus.RENTED, property.getStatus());
    }
}
