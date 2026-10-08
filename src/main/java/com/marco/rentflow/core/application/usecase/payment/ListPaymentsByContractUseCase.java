package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;

import java.util.List;
import java.util.UUID;

/**
 * Read-only use case: list all payments for a given contract.
 * Returns an empty list if the contract has no payments.
 */
public class ListPaymentsByContractUseCase {

    private final PaymentRepository paymentRepository;

    public ListPaymentsByContractUseCase(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<PaymentRecord> execute(UUID contractId) {
        return paymentRepository.findByContractId(contractId);
    }

}
