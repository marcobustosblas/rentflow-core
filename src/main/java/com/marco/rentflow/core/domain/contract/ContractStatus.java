package com.marco.rentflow.core.domain.contract;

public enum ContractStatus {
    DRAFT,       // Borrador / En preparación
    ACTIVE,      // Contrato vigente y activo
    TERMINATED,  // Finalizado anticipadamente de mutuo acuerdo o rescisión
    EXPIRED      // Vencido por cumplimiento de plazo
}
