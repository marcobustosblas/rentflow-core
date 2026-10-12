package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.domain.common.exception.ResourceNotFoundException;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.exceptions.PaymentNotFoundException;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;

import java.util.UUID;

/**
 * Read-only use case: fetch a PaymentRecord by ID.
 */
public class GetPaymentByIdUseCase {

    private final PaymentRepository paymentRepository;

    public GetPaymentByIdUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentRecord execute(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with id: " + paymentId));
    }

}
