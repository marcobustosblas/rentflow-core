package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.bankaccount.BankAccount;
import com.marco.rentflow.core.domain.bankaccount.exception.BankAccountNotFoundException;
import com.marco.rentflow.core.domain.bankaccount.exception.BankAccountOwnershipException;
import com.marco.rentflow.core.domain.bankaccount.ports.out.BankAccountRepository;
import com.marco.rentflow.core.domain.common.exception.InsufficientRoleException;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.subscription.Subscription;
import com.marco.rentflow.core.domain.subscription.exception.SubscriptionNotFoundException;
import com.marco.rentflow.core.domain.subscription.ports.out.SubscriptionRepository;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.exception.UserNotFoundException;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreatePropertyUseCaseTest {

    @Mock private PropertyRepository propertyRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private BankAccountRepository bankAccountRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CreatePropertyUseCase useCase;

    private UUID landlordId;
    private UUID bankAccountId;
    private User landlord;
    private Subscription subscription;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        landlordId = UUID.randomUUID();
        bankAccountId = UUID.randomUUID();

        landlord = mock(User.class);
        subscription = mock(Subscription.class);
        bankAccount = mock(BankAccount.class);
    }

    // --- Happy Paths ---

    @Test
    void shouldCreateProperty_whenAllValidAndNoBankAccountProvided() {
        // arrange
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(landlord.canCreateProperty()).thenReturn(true);

        when(subscriptionRepository.findByLandlordId(landlordId)).thenReturn(Optional.of(subscription));
        when(propertyRepository.countByLandlordId(landlordId)).thenReturn(2);
        when(subscription.canAddProperty(2)).thenReturn(true);

        when(propertyRepository.save(any(Property.class))).thenAnswer(inv-> inv.getArgument(0));

        // Act
        Property result = useCase.execute(landlordId, null, "Av. Street 123", new BigDecimal("500000"), "CLP");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAddress()).isEqualTo("Av. Street 123");
        assertThat(result.getLandlordId()).isEqualTo(landlordId);
        assertThat(result.getBankAccountId()).isNull();

        verify(propertyRepository).save(any(Property.class));
        verify(bankAccountRepository, never()).findById(any());
    }

    @Test
    void shouldCreateProperty_whenAllValidAndBankAccountProvided() {
        // Arrange
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(landlord.canCreateProperty()).thenReturn(true);

        when(subscriptionRepository.findByLandlordId(landlordId)).thenReturn(Optional.of(subscription));
        when(propertyRepository.countByLandlordId(landlordId)).thenReturn(0);
        when(subscription.canAddProperty(0)).thenReturn(true);

        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccount.getUserId()).thenReturn(landlordId);

        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Property result = useCase.execute(landlordId, bankAccountId, "Dir", new BigDecimal("100"), "CLP");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBankAccountId()).isEqualTo(bankAccountId);
        verify(bankAccountRepository).findById(bankAccountId);
    }

    // --- Error Paths ---

    @Test
    void shouldThrow_whenUserNotFound() {
        when(userRepository.findById(landlordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(landlordId, null, "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldThrow_whenUserDoesNotHaveLandlordRole() {
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(landlord.canCreateProperty()).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(landlordId, null, "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(InsufficientRoleException.class)
                .hasMessageContaining("Only active LANDLORD accounts");
    }

    @Test
    void shouldThrow_whenSubscriptionNotFound() {
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(landlord.canCreateProperty()).thenReturn(true);
        when(subscriptionRepository.findByLandlordId(landlordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(landlordId, null, "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(SubscriptionNotFoundException.class);
    }

    /* AQUI DEBO VERIFICAR LOGICA 🔍 */
    @Test
    void shouldThrow_whenSubscriptionLimitExceeded() {
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(landlord.canCreateProperty()).thenReturn(true);

        when(subscriptionRepository.findByLandlordId(landlordId)).thenReturn(Optional.of(subscription));
        when(propertyRepository.countByLandlordId(landlordId)).thenReturn(5);
        when(subscription.canAddProperty(5)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(landlordId, null, "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Subscription limit exceeded");
    }

    @Test
    void shouldThrow_whenBankAccountProvidedButNotFound() {
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(landlord.canCreateProperty()).thenReturn(true);

        when(subscriptionRepository.findByLandlordId(landlordId)).thenReturn(Optional.of(subscription));
        when(propertyRepository.countByLandlordId(landlordId)).thenReturn(0);
        when(subscription.canAddProperty(0)).thenReturn(true);

        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(landlordId, bankAccountId, "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(BankAccountNotFoundException.class);
    }

    @Test
    void shouldThrow_whenBankAccountDoesNotBelongToLandlord() {
        when(userRepository.findById(landlordId)).thenReturn(Optional.of(landlord));
        when(landlord.canCreateProperty()).thenReturn(true);

        when(subscriptionRepository.findByLandlordId(landlordId)).thenReturn(Optional.of(subscription));
        when(propertyRepository.countByLandlordId(landlordId)).thenReturn(0);
        when(subscription.canAddProperty(0)).thenReturn(true);

        UUID otherUserId = UUID.randomUUID();
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccount.getUserId()).thenReturn(otherUserId); // Cuenta de otra persona

        assertThatThrownBy(() -> useCase.execute(landlordId, bankAccountId, "Dir", BigDecimal.TEN, "CLP"))
                .isInstanceOf(BankAccountOwnershipException.class)
                .hasMessageContaining("does not belong to the provided landlord");
    }

}
