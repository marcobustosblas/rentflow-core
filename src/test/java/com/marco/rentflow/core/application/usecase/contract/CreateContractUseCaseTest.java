package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.ports.out.DomainEventPublisher;
import com.marco.rentflow.core.domain.common.ports.out.TokenHasher;
import com.marco.rentflow.core.domain.contract.ContractSource;
import com.marco.rentflow.core.domain.contract.ContractStatus;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.TenantInvitation;
import com.marco.rentflow.core.domain.contract.events.TenantInvitationCreatedEvent;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.contract.ports.out.TenantInvitationRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.PropertyStatus;
import com.marco.rentflow.core.domain.property.exception.PropertyNotAvailableException;
import com.marco.rentflow.core.domain.property.exception.PropertyNotFoundException;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateContractUseCase - Orchestration and Security")
class CreateContractUseCaseTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private TenantInvitationRepository tenantInvitationRepository;
    @Mock private TokenHasher tokenHasher;
    @Mock private DomainEventPublisher eventPublisher;

    @InjectMocks private CreateContractUseCase useCase;

    private UUID propertyId;
    private UUID landlordId;
    private String tenantEmail;
    private String tenantFullName;
    private String tenantRut;
    private Money rent;
    private Money deposit;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        landlordId = UUID.randomUUID();
        tenantEmail = "juan@rentflow.cl";
        tenantFullName = "Juan Inquilino";
        tenantRut = "12345678-9";
        rent = new Money(new BigDecimal("500000"), Currency.CLP);
        deposit = new Money(new BigDecimal("500000"), Currency.CLP);
        startDate = LocalDate.now().plusDays(1);
        endDate = startDate.plusYears(1);

        lenient().when(tokenHasher.hash(anyString())).thenReturn("hashed-token");
    }

    private CreateContractCommand buildCommand(UUID landlord, String email,
                                               String fullName, String rut) {
        return new CreateContractCommand(
                propertyId,
                landlord,
                email,
                fullName,
                rut,
                rent.getAmount(),
                deposit.getAmount(),
                rent.getCurrency().name(),
                5,
                null,
                startDate,
                endDate,
                null,
                ContractSource.MANUAL
        );
    }

    private Property availableProperty(UUID ownerId) {
        return Property.registerNew("Av. Providencia 123", rent, ownerId);
    }

    @Nested
    @DisplayName("1. Security and IDOR protection")
    class SecurityTests {

        @Test
        @DisplayName("Rejects creation when the property belongs to another landlord")
        void shouldThrow_whenLandlordMismatch() {
            UUID rogue = UUID.randomUUID();
            Property property = availableProperty(landlordId);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            CreateContractCommand command = buildCommand(rogue, tenantEmail, tenantFullName, tenantRut);

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> useCase.execute(command));

            assertEquals("The property does not belong to the provided landlord", ex.getMessage());
            verify(contractRepository, never()).save(any());
            verify(tenantInvitationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("2. Entity validation")
    class EntityValidationTests {

        @Test
        @DisplayName("Throws when property does not exist")
        void shouldThrow_whenPropertyNotFound() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            CreateContractCommand command = buildCommand(landlordId, tenantEmail, tenantFullName, tenantRut);

            assertThrows(PropertyNotFoundException.class, () -> useCase.execute(command));
        }

        @Test
        @DisplayName("Throws when property is not available")
        void shouldThrow_whenPropertyNotAvailable() {
            Property property = availableProperty(landlordId);
            property.markAsRented();

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            CreateContractCommand command = buildCommand(landlordId, tenantEmail, tenantFullName, tenantRut);

            assertThrows(PropertyNotAvailableException.class, () -> useCase.execute(command));
        }
    }

    @Nested
    @DisplayName("3. Success flow")
    class SuccessFlowTests {

        @Test
        @DisplayName("Creates contract, invitation, reserves property and persists all")
        void shouldCreateContractSuccessfully() {
            Property property = availableProperty(landlordId);

            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(contractRepository.save(any(RentalContract.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(tenantInvitationRepository.save(any(TenantInvitation.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            CreateContractCommand command = buildCommand(landlordId, tenantEmail, tenantFullName, tenantRut);

            RentalContract contract = useCase.execute(command);

            assertNotNull(contract);
            assertEquals(propertyId, contract.getPropertyId());
            assertEquals(landlordId, contract.getLandlordId());
            assertEquals(tenantEmail, contract.getTenantEmail());
            assertEquals(ContractStatus.PENDING_TENANT_SIGNUP, contract.getStatus());
            assertNull(contract.getTenantId(), "Tenant not assigned yet");

            assertEquals(PropertyStatus.RESERVED, property.getStatus());

            ArgumentCaptor<TenantInvitation> invitationCaptor =
                    ArgumentCaptor.forClass(TenantInvitation.class);
            verify(tenantInvitationRepository).save(invitationCaptor.capture());

            TenantInvitation saved = invitationCaptor.getValue();
            assertEquals(contract.getId(), saved.getContractId());
            assertEquals(landlordId, saved.getLandlordId());
            assertEquals(tenantEmail, saved.getTenantEmail());
            assertEquals("hashed-token", saved.getTokenHash());

            verify(propertyRepository).save(property);
            verify(contractRepository).save(any(RentalContract.class));
            // se agrega esto por el mock de eventPublisher
            verify(eventPublisher).publish(any(TenantInvitationCreatedEvent.class));
        }
    }
}