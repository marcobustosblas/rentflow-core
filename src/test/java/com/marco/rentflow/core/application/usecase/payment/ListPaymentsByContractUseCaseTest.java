package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListPaymentsByContractUseCaseTest {

    private PaymentRepository paymentRepository;
    private ListPaymentsByContractUseCase useCase;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        useCase = new ListPaymentsByContractUseCase(paymentRepository);
    }

    @Test
    void shouldReturnPayments_whenContractHasPayments() {
        UUID contractId = UUID.randomUUID();
        List<PaymentRecord> payments = List.of(
                mock(PaymentRecord.class),
                mock(PaymentRecord.class));

        when(paymentRepository.findByContractId(contractId)).thenReturn(payments);

        List<PaymentRecord> result = useCase.execute(contractId);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnEmptyList_whenContractHasNoPayments() {
        UUID contractId = UUID.randomUUID();
        when(paymentRepository.findByContractId(contractId)).thenReturn(List.of());

        List<PaymentRecord> result = useCase.execute(contractId);

        assertThat(result).isEmpty();
    }
}