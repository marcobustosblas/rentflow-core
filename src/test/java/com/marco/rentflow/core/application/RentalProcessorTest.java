package com.marco.rentflow.core.application;

import com.marco.rentflow.core.application.port.out.TenantNotificationGateway;
import com.marco.rentflow.core.domain.exception.TenantNotificationException;
import com.marco.rentflow.core.domain.model.RentalContract;
import com.marco.rentflow.core.application.usecase.RentalProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

        assertEquals(1L, contract.getContractId());
    }

    @Test
    void shouldThrowTenantNotificationExceptionWhenEmailIsInvalid() {
        // Arrange
        TenantNotificationGateway gatewayMock = Mockito.mock(TenantNotificationGateway.class);
        RentalProcessor processor = new RentalProcessor(gatewayMock);
        RentalContract contract = new RentalContract(
                1L,
                new BigDecimal("500.00"),
                5,
                new BigDecimal("10.00"));
        String invalidEmail = " "; // Email vacío o nulo

        // 2. Act & 3. Assert
        assertThrows(TenantNotificationException.class,
                ()-> processor.processPayment(contract, 10, invalidEmail)
        );
        verify(gatewayMock, never()).sendNotification(anyString(), anyString());
    }

    @Test
    void shouldCalculateTotalAmountWithoutPenaltyWhenPaymentIsOnTime() {
        // Arrange
        RentalContract contract = new RentalContract(
                1L,
                new BigDecimal("500.00"),
                5, new BigDecimal("10.00"));
        TenantNotificationGateway gatewayMock = Mockito.mock(TenantNotificationGateway.class);
        RentalProcessor processor = new RentalProcessor(gatewayMock);

        // Act (paga el día 3, está bien)
        BigDecimal totalToPay = processor.processPayment(contract, 3, "tenant@gmail.com");

        // Assert
        assertEquals(new BigDecimal("500.00"), totalToPay);
        verify(gatewayMock, times(1)).sendNotification(eq("tenant@gmail.com"), anyString());

    }

    @Test
    void shouldThrowTenantNotificationExceptionWhenEmailIsNull() {
        // Arrange
        TenantNotificationGateway gatewayMock = Mockito.mock(TenantNotificationGateway.class);
        RentalProcessor processor = new RentalProcessor(gatewayMock);
        RentalContract contract = new RentalContract(
                1L,
                new BigDecimal("500.00"),
                5, new BigDecimal("10.00"));

        // Act & Assert (Evalúo explícitamente y sabrosamente el valor null)
        assertThrows(TenantNotificationException.class,
                () -> processor.processPayment(contract, 10, null)
        );
        verify(gatewayMock, never()).sendNotification(anyString(), anyString());
    }
}
