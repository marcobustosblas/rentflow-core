package com.marco.rentflow.core.domain.subscription;

public enum PlanType {

    STARTED(5),     // Límite de 5 propiedades
    PRO(20),        // Límite de 20 propiedades
    ENTERPRISE(100); // Límite de 100 propiedades

    private final int propertyLimit;

    PlanType(int propertyLimit) {
        this.propertyLimit = propertyLimit;
    }

    public int getPropertyLimit() {
        return propertyLimit;
    }

}
