package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateContractUseCase - Pruebas de Orquestación y Seguridad IDOR")
public class CreateContractUseCaseTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateContractUseCase useCase;

    private UUID propertyId;
    private UUID tenantId;
    private UUID landlordId;
    private Money rent;
    private Money deposit;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        landlordId = UUID.randomUUID();
        rent = new Money(new BigDecimal("500000"), Currency.CLP);
        deposit = new Money(new BigDecimal("500000"), Currency.CLP);
        startDate = LocalDate.now();
        endDate = startDate.plusYears(1);
    }

    @Nested
    @DisplayName("1. Seguridad y Controles de Acceso (IDOR y Roles)")
    class SecurityAndAuthorizationTests {

        @Test
        @DisplayName("Debe rechazar la creación si la propiedad pertenece a otro landlord (Protección IDOR)")
        void shouldThrowExceptionWhenLandlordMismatch() {
            UUID rogueLandlordId = UUID.randomUUID();
            Property property = Property.registerNew("Av. Providencia 123", rent, landlordId);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> useCase.execute(propertyId, tenantId, rogueLandlordId, rent, deposit, 5, null, startDate, endDate)
            );

            assertEquals("The property does not belong to the provided landlord", exception.getMessage());
            verify(contractRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe rechazar la creación si el usuario indicado no posee el rol de TENANT")
        void shouldThrowExceptionWhenUserIsNotTenant() {
            Property property = Property.registerNew("Av. Providencia 123", rent, landlordId);
            User nonTenantUser = User.registerNew("pedro@rentflow.cl", "hash123", "Pedro Landlord", "12345678-9", "+56911112222", Role.LANDLORD);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(userRepository.findById(tenantId)).thenReturn(Optional.of(nonTenantUser));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> useCase.execute(propertyId, tenantId, landlordId, rent, deposit, 5, null, startDate, endDate)
            );

            assertEquals("User does not have TENANT privileges", exception.getMessage());
            verify(contractRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("2. Validación de Estado y Existencia de Entidades")
    class EntityValidationTests {

        @Test
        @DisplayName("Debe lanzar excepción si la propiedad no existe en el sistema")
        void shouldThrowExceptionWhenPropertyNotFound() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(propertyId, tenantId, landlordId, rent, deposit, 5, null, startDate, endDate)
            );

            assertEquals("Property not found", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción si la propiedad ya no está disponible para arriendo")
        void shouldThrowExceptionWhenPropertyNotAvailable() {
            Property property = Property.registerNew("Av. Providencia 123", rent, landlordId);
            property.markAsRented(); // Ya está arrendada

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> useCase.execute(propertyId, tenantId, landlordId, rent, deposit, 5, null, startDate, endDate)
            );

            assertEquals("Property is not available for rent", exception.getMessage());
        }

        @Test
        @DisplayName("Debe lanzar excepción si el tenant no existe en la base de datos")
        void shouldThrowExceptionWhenTenantNotFound() {
            Property property = Property.registerNew("Av. Providencia 123", rent, landlordId);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(userRepository.findById(tenantId)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> useCase.execute(propertyId, tenantId, landlordId, rent, deposit, 5, null, startDate, endDate)
            );

            assertEquals("Tenant not found", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("3. Flujo Exitoso y Transiciones de Estado")
    class SuccessFlowTests {

        @Test
        @DisplayName("Debe crear el contrato, marcar propiedad como RENTED y persisitir ambas entidades")
        void shouldCreateContractSuccessfully() {
            Property property = Property.registerNew("Av. Providencia 123", rent, landlordId);
            User tenantUser = User.registerNew("juan@rentflow.cl", "hash123", "Juan Inquilino", "12345678-9", "+56911112222", Role.TENANT);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(userRepository.findById(tenantId)).thenReturn(Optional.of(tenantUser));
            when(contractRepository.save(any(RentalContract.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RentalContract contract = useCase.execute(
                    propertyId, tenantId, landlordId, rent, deposit, 5, new BigDecimal("0.01"), startDate, endDate
            );

            assertNotNull(contract);
            assertEquals(propertyId, contract.getPropertyId());
            assertEquals(tenantId, contract.getTenantId());
            assertEquals(landlordId, contract.getLandlordId());

            assertFalse(property.isAvailable(), "La propiedad debe pasar a estado RENTED");
            verify(propertyRepository, times(1)).save(property);
            verify(contractRepository, times(1)).save(any(RentalContract.class));
        }
    }
}
