package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.payment.mapper;

import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentStatus;
import com.marco.rentflow.core.domain.payment.PaymentTarget;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.payment.PaymentJpaEntity;

import java.math.BigDecimal;

public class PaymentPersistenceMapper {

    private PaymentPersistenceMapper() {}

    public static PaymentJpaEntity toJpaEntity(PaymentRecord domain) {
        if (domain == null) return null;

        return new PaymentJpaEntity(
                domain.getId(),
                domain.getReferenceId(),
                domain.getTarget().name(),
                domain.getStatus().name(),
                domain.getExpectedAmount().getAmount(),
                domain.getAmountPaid() != null ? domain.getAmountPaid().getAmount() : null,
                domain.getExpectedAmount().getCurrency().name(),
                domain.getLateFeeApplied() != null ? domain.getLateFeeApplied().getAmount() : BigDecimal.ZERO,
                domain.getDueDate(),
                domain.getPaymentDate(),
                domain.getIdempotencyKey(),
                domain.getTransactionReference(),
                domain.getPaymentReceiptUrl(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public static PaymentRecord toDomain(PaymentJpaEntity entity) {
        if (entity == null) return null;

        Currency currency = Currency.valueOf(entity.getCurrency());
        Money expectedAmount = new Money(entity.getExpectedAmount(), currency);

        Money amountPaid = entity.getAmountPaid() != null
                ? new Money(entity.getAmountPaid(), currency)
                : null;

        Money lateFeeApplied = entity.getLateFeeApplied() != null
                ? new Money(entity.getLateFeeApplied(), currency)
                : null;

        return PaymentRecord.reconstitute(
                entity.getId(),
                entity.getReferenceId(),
                PaymentTarget.valueOf(entity.getPaymentTarget()),
                entity.getIdempotencyKey(),
                entity.getDueDate(),
                entity.getPaymentDate(),
                expectedAmount,
                amountPaid,
                lateFeeApplied,
                PaymentStatus.valueOf(entity.getStatus()),
                entity.getTransactionReference(),
                entity.getPaymentReceiptUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}