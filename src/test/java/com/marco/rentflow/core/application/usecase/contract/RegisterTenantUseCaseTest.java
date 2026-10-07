package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.exception.ResourceAlreadyExistsException;
import com.marco.rentflow.core.domain.common.ports.out.TokenHasher;
import com.marco.rentflow.core.domain.contract.*;
import com.marco.rentflow.core.domain.contract.exception.InvalidInvitationException;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.contract.ports.out.TenantInvitationRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.PropertyStatus;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserStatus;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class RegisterTenantUseCaseTest {

    private UserRepository userRepository;
    private TenantInvitationRepository invitationRepository;
    private ContractRepository contractRepository;
    private PropertyRepository propertyRepository;
    private TokenHasher tokenHasher;

    private RegisterTenantUseCase useCase;

    private static final String RAW_TOKEN = "raw-token-abc";
    private static final String TOKEN_HASH = "hashed-token-abc";
    private static final String TENANT_EMAIL = "juan@example.com";

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        invitationRepository = mock(TenantInvitationRepository.class);
        contractRepository = mock(ContractRepository.class);
        propertyRepository = mock(PropertyRepository.class);
        tokenHasher = mock(TokenHasher.class);

        useCase = new RegisterTenantUseCase(userRepository, invitationRepository, contractRepository, propertyRepository, tokenHasher);
        when(tokenHasher.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
    }

    // --- Happy path: new tenant ---

    @Test
    void shouldRegisterNewTenant_whenInvitationIsValid() {
        TenantInvitation invitation = pendingInvitation();
        RentalContract contract = pendingContract();
        Property property = availableProperty(contract.getPropertyId());

        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(invitation));
        when(contractRepository.findById(invitation.getContractId())).thenReturn(Optional.of(contract));
        when(userRepository.findByEmail(TENANT_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.existsByRut("12345678-9")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(propertyRepository.findById(contract.getPropertyId())).thenReturn(Optional.of(property));

        RegisterTenantCommand command = new RegisterTenantCommand(
                RAW_TOKEN, "Juan Pérez", "12345678-9", "+56912345678", "password123", true);

        User result = useCase.execute(command);

        assertThat(result.getEmail()).isEqualTo(TENANT_EMAIL);
        assertThat(result.getRoles()).containsExactly(Role.TENANT);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(property.getStatus()).isEqualTo(PropertyStatus.RENTED);

        verify(userRepository).save(any(User.class));
        verify(contractRepository).save(contract);
        verify(invitationRepository).save(invitation);
        verify(propertyRepository).save(property);
    }

    // --- Error: invitation not found ---

    @Test
    void shouldThrow_whenInvitationNotFound() {
        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        RegisterTenantCommand command = new RegisterTenantCommand(
                RAW_TOKEN, "Juan Pérez", "12345678-9", null, "password123", true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidInvitationException.class)
                .hasMessageContaining("Invalid invitation token");
    }

    // --- Error: invitation already accepted ---

    @Test
    void shouldThrow_whenInvitationAlreadyAccepted() {
        TenantInvitation invitation = pendingInvitation();
        invitation.markAsAccepted(LocalDateTime.now());

        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(invitation));

        RegisterTenantCommand command = new RegisterTenantCommand(
                RAW_TOKEN, "Juan Pérez", "12345678-9", null, "password123", true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidInvitationException.class)
                .hasMessageContaining("cannot be accepted");
    }

    // --- Duplicate email: reuse existing user ---

    @Test
    void shouldReuseExistingTenant_whenEmailAlreadyRegistered() {
        TenantInvitation invitation = pendingInvitation();
        RentalContract contract = pendingContract();
        Property property = availableProperty(contract.getPropertyId());

        User existing = User.reconstitute(
                UUID.randomUUID(),
                "Juan Pérez",
                TENANT_EMAIL,
                "hash",
                "12345678-9",
                null,
                Set.of(Role.TENANT),
                UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(invitation));
        when(contractRepository.findById(invitation.getContractId())).thenReturn(Optional.of(contract));
        when(userRepository.findByEmail(TENANT_EMAIL)).thenReturn(Optional.of(existing));
        when(propertyRepository.findById(contract.getPropertyId())).thenReturn(Optional.of(property));

        RegisterTenantCommand command = new RegisterTenantCommand(
                RAW_TOKEN, "Juan Pérez", "12345678-9", null, "password123", true);

        User result = useCase.execute(command);

        assertThat(result.getId()).isEqualTo(existing.getId());
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE);

        // No new user is created
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).existsByRut(anyString());
    }

    // --- Duplicate RUT (first-time registration) ---

    @Test
    void shouldThrow_whenRutAlreadyExists() {
        TenantInvitation invitation = pendingInvitation();
        RentalContract contract = pendingContract();

        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(invitation));
        when(contractRepository.findById(invitation.getContractId())).thenReturn(Optional.of(contract));
        when(userRepository.findByEmail(TENANT_EMAIL)).thenReturn(Optional.empty());
        when(userRepository.existsByRut("12345678-9")).thenReturn(true);

        RegisterTenantCommand command = new RegisterTenantCommand(
                RAW_TOKEN, "Juan Pérez", "12345678-9", null, "password123", true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("RUT");
    }

    // --Helpers --

    private TenantInvitation pendingInvitation() {
        return TenantInvitation.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                TENANT_EMAIL, TOKEN_HASH,
                LocalDateTime.now().plusDays(7),
                InvitationStatus.PENDING,
                LocalDateTime.now(),
                null
        );
    }

    private RentalContract pendingContract() {
        TenantInfo tenant = new TenantInfo(
                null, TENANT_EMAIL, "Juan Pérez", "12345678-9");

        FinancialTerms terms = new FinancialTerms(
                new Money(new BigDecimal("500000"), Currency.CLP),
                new Money(new BigDecimal("500000"), Currency.CLP),
                5,
                BigDecimal.ZERO
        );

        return RentalContract.register(
                UUID.randomUUID(),
                UUID.randomUUID(),
                tenant, terms,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusYears(1),
                null,
                ContractSource.MANUAL
        );
    }

    private Property availableProperty(UUID propertyId) {
        return Property.reconstitute(
                propertyId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Av. Providencia 1234",
                new Money(new BigDecimal("500000"), Currency.CLP),
                PropertyStatus.AVAILABLE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
