package com.marco.rentflow.core.domain.subscription;

public enum PlanType {

    STARTER(3),       // Hasta 3 propiedades ($27.900 CLP / mes)
    PRO(7),           // Hasta 7 propiedades ($64.900 CLP / mes)
    ENTERPRISE(13);   // Hasta 13 propiedades ($119.900 CLP / mes)

    private final int propertyLimit;

    PlanType(int propertyLimit) {
        this.propertyLimit = propertyLimit;
    }

    public int getPropertyLimit() {
        return propertyLimit;
    }

}
