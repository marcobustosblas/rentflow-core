package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.payment;

import com.marco.rentflow.core.domain.payment.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentSpringDataRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);

    List<PaymentJpaEntity> findByContractId(UUID contractId);

    List<PaymentJpaEntity> findByReferenceIdAndPaymentTarget(UUID referenceId, String paymentTarget);

    List<PaymentJpaEntity> findByStatusAndDueDateBefore(String status, LocalDate date);

}
