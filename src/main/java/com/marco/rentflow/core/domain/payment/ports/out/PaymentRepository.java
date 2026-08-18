package com.marco.rentflow.core.domain.payment.ports.out;

import com.marco.rentflow.core.domain.payment.PaymentRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    // Guarda un cobro nuevo o actualiza su estado (ej: de PENDING a PAID)
    PaymentRecord save(PaymentRecord payment);

    // Busca un registro de pago por su ID
    Optional<PaymentRecord> findById(UUID id);

    // Busca un pago usando la clave de idempotencia (vital para evitar duplicados)
    Optional<PaymentRecord> findByIdempotencyKey(String idempotencyKey);

    // Obtiene all el historial de pagos de un contrato específico
    List<PaymentRecord> findByContractId(UUID contractId);

    // Encuentra los cobros que están pendientes y cuya fecha límite ya pasó
    List<PaymentRecord> findPendingByDueDateBefore(LocalDate date);
}
