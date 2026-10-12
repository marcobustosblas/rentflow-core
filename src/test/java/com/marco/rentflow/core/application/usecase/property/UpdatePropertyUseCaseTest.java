package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.bankaccount.BankAccount;
import com.marco.rentflow.core.domain.bankaccount.exception.BankAccountNotFoundException;
import com.marco.rentflow.core.domain.bankaccount.exception.BankAccountOwnershipException;
import com.marco.rentflow.core.domain.bankaccount.ports.out.BankAccountRepository;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.PropertyStatus;
import com.marco.rentflow.core.domain.property.exception.PropertyNotFoundException;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UpdatePropertyUseCase — actualiza dirección, precio y cuenta bancaria de una propiedad.
 * Reglas de negocio:
 *  - La propiedad debe existir.
 *  - Solo el landlord dueño puede actualizarla.
 *  - Si se cambia la cuenta bancaria, debe existir y pertenecer al mismo landlord.
 */
@ExtendWith(MockitoExtension.class)
public class UpdatePropertyUseCaseTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private BankAccountRepository bankAccountRepository;
    @InjectMocks private UpdatePropertyUseCase useCase;

    private UUID propertyId;
    private UUID landlordId;
    private UUID bankAccountId;
    private Property property;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        landlordId = UUID.randomUUID();
        bankAccountId = UUID.randomUUID();

        // La propiedad está "viva" en memoria, lista para ser mutada.
        property = Property.reconstitute(
                propertyId,
                landlordId,
                null,
                "Av. Providencia 123",
                new Money(new BigDecimal("500000"), Currency.CLP),
                PropertyStatus.AVAILABLE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        bankAccount = mock(BankAccount.class);
    }

    // HAPPY PATH =)

    @Test
    void shouldUpdateProperty_whenLandlordOwnsIt() {
        // Cuando busquen la propiedad, return la del landlord.
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        // Cuando guarde, devuelvo lo mismo que entró.
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT — el use case muta la propiedad y la persiste.
        Property updated = useCase.execute(
                propertyId, landlordId, null,
                "Nueva Dirección 456",
                new BigDecimal("600000"),
                "CLP"
        );

        // ASSERT — verifico que la mutación ocurrió.
        assertThat(updated.getAddress()).isEqualTo("Nueva Dirección 456");
        assertThat(updated.getBasePrice().getAmount()).isEqualByComparingTo("600000");
    }

    @Test
    void shouldUpdateBankAccount_whenItBelongsToLandlord() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        // La cuenta bancaria existe y pertenece al mismo landlord.
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccount.getUserId()).thenReturn(landlordId);
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        Property updated = useCase.execute(
                propertyId, landlordId, bankAccountId,
                "Dir", new BigDecimal("500000"), "CLP");

        assertThat(updated.getBankAccountId()).isEqualTo(bankAccountId);
    }

    // ERROR PATHS

    @Test
    void shouldThrow_whenPropertyNotFound() {
        // El repo no encuentra la propiedad.
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                propertyId, landlordId, null,
                "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(PropertyNotFoundException.class);
    }


    @Test
    void shouldThrow_whenLandlordIsNotTheOwner() {
        // La propiedad existe, pero pertenece a OTRO landlord.
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        UUID otherLandlord = UUID.randomUUID(); // un landlord que no es el dueño

        assertThatThrownBy(() -> useCase.execute(
                propertyId, otherLandlord, null, "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(SecurityException.class);

        // Verificamos que NO se guardó nada.
        verify(propertyRepository, never()).save(any());
    }

    @Test
    void shouldThrow_whenBankAccountDoesNotExist() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        // La cuenta bancaria no existe.
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                propertyId, landlordId, bankAccountId, "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(BankAccountNotFoundException.class);
    }

}
