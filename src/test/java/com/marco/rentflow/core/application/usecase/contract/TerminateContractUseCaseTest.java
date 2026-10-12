package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.ContractSource;
import com.marco.rentflow.core.domain.contract.ContractStatus;
import com.marco.rentflow.core.domain.contract.FinancialTerms;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.TenantInfo;
import com.marco.rentflow.core.domain.contract.exception.ContractNotFoundException;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TerminateContractUseCase — termina (cancela) un contrato.
 * Reglas:
 *  - El contrato debe existir.
 *  - Solo el landlord dueño puede terminarlo.
 *  - Solo se puede terminar si está en estado ACTIVE o PENDING_TENANT_SIGNUP.
 */
@ExtendWith(MockitoExtension.class)
public class TerminateContractUseCaseTest {

    @Mock private ContractRepository contractRepository;
    @Mock private PropertyRepository propertyRepository;
    @InjectMocks private TerminateContractUseCase useCase;

    private UUID contractId;
    private UUID landlordId;
    private RentalContract contract;
    private Property property;

    @BeforeEach
    void setUp() {
        landlordId = UUID.randomUUID();
        property = mock(Property.class);

        // creo un contrato ACTIVE listo para ser terminado.
        TenantInfo tenant = new TenantInfo(
                UUID.randomUUID(), "juan@mail.com", "Juan", "12345678-9");
        FinancialTerms terms = new FinancialTerms(
                new Money(new BigDecimal("500000"), Currency.CLP),
                new Money(new BigDecimal("500000"), Currency.CLP),
                5, BigDecimal.ZERO);

        contract = RentalContract.register(
                UUID.randomUUID(), landlordId, tenant, terms,
                LocalDate.now(), LocalDate.now().plusYears(1),
                null, ContractSource.MANUAL);
        contractId = contract.getId();
    }

    // HAPPY PATH

    @Test
    void shouldTerminate_whenLandlordOwnsContract() {
        UUID propertyId = contract.getPropertyId();

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        when(property.getLandlordId()).thenReturn(landlordId);

        when(contractRepository.save(any(RentalContract.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        RentalContract terminated = useCase.execute(contractId, landlordId);

        // ASSERT — el contrato cambió de estado.
        assertThat(terminated.getStatus()).isEqualTo(ContractStatus.TERMINATED);
        verify(property).markAsAvailable();
        verify(contractRepository).save(contract);
        verify(propertyRepository).save(property);
    }

    // ERROR PATHS

    @Test
    void shouldThrow_whenContractNotFound() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(contractId, landlordId))
                .isInstanceOf(ContractNotFoundException.class);

        verify(contractRepository, never()).save(any());
    }

    @Test
    void shouldThrow_whenLandlordIsNotOwner() {
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // me falta añadir esto
        when(propertyRepository.findById(contract.getPropertyId())).thenReturn(Optional.of(property)); // -> simulates the second line of method: /* 2 - Buscar la propiedad asociada */
        // ahora la propiedad pertenece a un landlord real y no al impostor desgraciado
        when(property.getLandlordId()).thenReturn(landlordId);

        UUID otherLandlord = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(contractId, otherLandlord))
                .isInstanceOf(SecurityException.class);

        verify(contractRepository, never()).save(any());
    }

    @Test
    void shouldThrow_whenContractAlreadyTerminated() {
        // Termino el contrato primero.
        contract.terminate();

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        // Intento terminar de nuevo → la entidad lanza InvalidContractStateException.
        assertThatThrownBy(() -> useCase.execute(contractId, landlordId))
                .isInstanceOf(RuntimeException.class);
    }
}
