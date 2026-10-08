package com.marco.rentflow.core.application.usecase.payment;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.exception.ResourceNotFoundException;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.core.domain.payment.PaymentStatus;
import com.marco.rentflow.core.domain.payment.PaymentTarget;
import com.marco.rentflow.core.domain.payment.ports.out.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetPaymentByIdUseCaseTest {

    private PaymentRepository paymentRepository;
    private GetPaymentByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        useCase = new GetPaymentByIdUseCase(paymentRepository);
    }

    @Test
    void shouldReturnPayment_whenPaymentExists() {
        UUID paymentId = UUID.randomUUID();
        PaymentRecord payment = mockPaymentRecord(paymentId);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        PaymentRecord result = useCase.execute(paymentId);

        assertThat(result.getId()).isEqualTo(paymentId);
    }

    @Test
    void shouldThrow_whenPaymentNotFound() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(paymentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(paymentId.toString());
    }

    private PaymentRecord mockPaymentRecord(UUID id) {
        return PaymentRecord.reconstitute(
                id,
                UUID.randomUUID(),
                PaymentTarget.RENT,
                "idem-key-" + id,
                LocalDate.now().plusDays(5),
                null,
                new Money(new BigDecimal("500000"), Currency.CLP),
                new Money(BigDecimal.ZERO, Currency.CLP),
                new Money(BigDecimal.ZERO, Currency.CLP),
                PaymentStatus.PENDING,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

}
