package com.marco.rentflow.core.domain.payment.ports.out;

import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentTarget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    // Guarda un cobro nuevo o actualiza su estado (ej: de PENDING a PAID)
    PaymentRecord save(PaymentRecord payment);

    // Busca un registro de pago por su ID
    Optional<PaymentRecord> findById(String id);

    // Busca un pago usando la clave de idempotencia (vital para evitar duplicados)
    Optional<PaymentRecord> findByIdempotencyKey(String idempotencyKey);

    // Method de conveniencia para casos de uso de arriendos
    List<PaymentRecord> findByContractId(UUID contractId);

    // Method agnóstico puro (El motor real)
    List<PaymentRecord> findByReferenceIdAndTarget(UUID referenceId, PaymentTarget target);

    List<PaymentRecord> findByStatusAndDueDateBefore(String status, LocalDate date);
}
