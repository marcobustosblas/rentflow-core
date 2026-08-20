package com.marco.rentflow.core.domain.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rut Value Object Tests")
class RutTest {

    @Test
    @DisplayName("Should create valid Rut and format to uppercase")
    void shouldCreateValidRut() {
        Rut rut = new Rut("12345678-k"); // Letra minúscula
        assertEquals("12345678-K", rut.getValue()); // Debe guardarlo en mayúscula
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for invalid RUT formats")
    @ValueSource(strings = {"1234567-89", "12.345.678-9", "abcdefgh-i", "123-K", ""})
    void shouldThrowExceptionForInvalidRuts(String invalidRut) {
        assertThrows(IllegalArgumentException.class, () -> new Rut(invalidRut));
    }

    @Test
    @DisplayName("Should throw exception when RUT is null")
    void shouldThrowExceptionWhenNull() {
        assertThrows(NullPointerException.class, () -> new Rut(null));
    }

    @Test
    @DisplayName("Should evaluate all branches in equals method (Coverage)")
    void shouldEvaluateAllEqualsBranches() {
        Rut rut1 = new Rut("12345678-9");
        Rut rut2 = new Rut("12345678-9");
        Rut rut3 = new Rut("98765432-1");

        // 1. RAMA: o == null (¡Llamada manual obligatoria para engañar a JUnit!)
        assertFalse(rut1.equals(null));

        // 2. RAMA: getClass() != o.getClass() (¡Llamada manual con un String!)
        assertFalse(rut1.equals("12345678-9"));

        // 3. RAMA: if (this == o) return true;
        assertTrue(rut1.equals(rut1));

        // 4. Camino normal: son exactamente iguales en valor
        assertTrue(rut1.equals(rut2));

        // 5. Camino normal: son objetos de la misma clase pero valores distintos
        assertFalse(rut1.equals(rut3));

        // HashCode
        assertEquals(rut1.hashCode(), rut2.hashCode());
    }
}