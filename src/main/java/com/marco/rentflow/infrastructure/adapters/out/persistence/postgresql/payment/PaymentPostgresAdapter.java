package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.payment;

import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentStatus;
import com.marco.rentflow.core.domain.payment.PaymentTarget;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.payment.mapper.PaymentPersistenceMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentPostgresAdapter implements PaymentRepository {

    private final PaymentSpringDataRepository springDataRepository;

    public PaymentPostgresAdapter(PaymentSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public PaymentRecord save(PaymentRecord payment) {
        PaymentJpaEntity entity = PaymentPersistenceMapper.toJpaEntity(payment);
        PaymentJpaEntity savedEntity = springDataRepository.save(entity);
        return PaymentPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PaymentRecord> findById(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            return springDataRepository.findById(uuid)
                    .map(PaymentPersistenceMapper::toDomain);
        } catch (IllegalArgumentException e) {
            // Protección contra inyecciones de strings malformados en el ID
            return Optional.empty();
        }
    }

    @Override
    public Optional<PaymentRecord> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey)
                .map(PaymentPersistenceMapper::toDomain);
    }

    @Override
    public List<PaymentRecord> findByContractId(UUID contractId) {
        // Redirige internamente al method agnóstico inyectando RENT
        return findByReferenceIdAndTarget(contractId, PaymentTarget.RENT);
    }

    @Override
    public List<PaymentRecord> findByReferenceIdAndTarget(UUID referenceId, PaymentTarget target) {
        return springDataRepository.findByReferenceIdAndPaymentTarget(referenceId, target.name()).stream()
                .map(PaymentPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<PaymentRecord> findByStatusAndDueDateBefore(String status, LocalDate date) {
        // Puedo pasar directamente PaymentStatus.PENDING.name() desde el Caso de Uso o quemarlo aquí si prefiero
        return springDataRepository.findByStatusAndDueDateBefore(status, date)
                .stream()
                .map(PaymentPersistenceMapper::toDomain)
                .toList();
    }
}