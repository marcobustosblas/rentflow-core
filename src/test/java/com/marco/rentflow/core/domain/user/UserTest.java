package com.marco.rentflow.core.domain.user;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void shouldCreateUserSuccessfully() {
        User user = new User("marco@rentflow.com", "Marco Developer", Role.ADMIN);

        assertNotNull(user.getId());
        assertEquals("marco@rentflow.com", user.getEmail());
        assertEquals("Marco Developer", user.getFullName());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void shouldReconstituteUserWithExistingUuid() {
        UUID existingId = UUID.randomUUID();
        User user = new User(existingId, "marco@rentflow.com", "Marco Developer", Role.LANDLORD);

        assertEquals(existingId, user.getId());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new User(null, "Marco Developer", Role.ADMIN);
        });
    }

}
