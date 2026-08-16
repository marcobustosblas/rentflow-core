package com.marco.rentflow.core.domain.payment;

public enum PaymentStatus {
    PENDING,    // Cobro generado, pendiente de pago
    PAID,       // Pagado exitosamente
    OVERDUE,    // Vencido / En mora
    CANCELLED   // Anulado / Condonado por el arrendador
}
