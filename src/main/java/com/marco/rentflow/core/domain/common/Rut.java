package com.marco.rentflow.core.domain.common;

import java.util.Objects;

public class Rut {
    private final String value;
    private static final String RUT_REGEX = "^\\d{7,8}-[0-9Kk]$";

    public Rut(String rutInput) {
        Objects.requireNonNull(rutInput, "RUT cannot be null");
        if (rutInput.trim().isEmpty()) {
            throw new IllegalArgumentException("RUT cannot be empty");
        }

        String cleanRut = rutInput.trim().toUpperCase();
        if (!cleanRut.matches(RUT_REGEX)) {
            throw new IllegalArgumentException("Invalid RUT format. Expected format: 12345678-9");
        }

        this.value = cleanRut;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rut rut = (Rut) o;
        return value.equals(rut.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}