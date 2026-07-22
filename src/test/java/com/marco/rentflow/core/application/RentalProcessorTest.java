package com.marco.rentflow.core.application;

import com.marco.rentflow.core.application.port.out.TenantNotificationGateway;
import com.marco.rentflow.core.domain.model.RentalContract;
import com.marco.rentflow.core.application.usecase.RentalProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.math.BigDecimal;

public class RentalProcessorTest {

    @Test
    void shouldCalculateTotalAmountWithPenaltyWhenPaymentIsLate() {
        // Arrange
        RentalContract contract = new RentalContract(
                1L,
                new BigDecimal("500.00"),
                5,
                new BigDecimal("10.00"));
        TenantNotificationGateway gatewayMock = Mockito.mock(TenantNotificationGateway.class);
        RentalProcessor processor = new RentalProcessor(gatewayMock);

        // Act
        BigDecimal totalToPay = processor.processPayment(contract, 10, "tenant@email.com");

        // Assert
        assertEquals(new BigDecimal("550.00"), totalToPay);
        verify(gatewayMock, times(1)).sendNotification(eq("tenant@email.com"), anyString());

    }

}
