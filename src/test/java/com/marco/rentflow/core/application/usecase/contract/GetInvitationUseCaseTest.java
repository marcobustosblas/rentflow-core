package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.ports.out.TokenHasher;
import com.marco.rentflow.core.domain.contract.*;
import com.marco.rentflow.core.domain.contract.exception.InvalidInvitationException;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.contract.ports.out.TenantInvitationRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.PropertyStatus;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetInvitationUseCaseTest {

    private TenantInvitationRepository invitationRepository;
    private ContractRepository contractRepository;
    private PropertyRepository propertyRepository;
    private TokenHasher tokenHasher;

    private GetInvitationUseCase useCase;

    private static final String RAW_TOKEN = "raw-token-123";
    private static final String TOKEN_HASH = "hashed-token-123";

    @BeforeEach
    void setUp() {
        invitationRepository = mock(TenantInvitationRepository.class);
        contractRepository = mock(ContractRepository.class);
        propertyRepository = mock(PropertyRepository.class);
        tokenHasher = mock(TokenHasher.class);

        useCase = new GetInvitationUseCase(
                invitationRepository, contractRepository, propertyRepository, tokenHasher);
    }

    @Test
    void shouldReturnPreview_whnInvitationIsValidAndPending() {
        // Arrange
        when(tokenHasher.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        UUID contractId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        String address = "Av. Viva Arica 742";
        String tenantEmail = "tenant@mail.com";

        TenantInvitation invitation = TenantInvitation.reconstitute(
                UUID.randomUUID(),
                contractId,
                UUID.randomUUID(),
                tenantEmail,
                TOKEN_HASH,
                LocalDateTime.now().plusDays(5),
                InvitationStatus.PENDING,
                LocalDateTime.now(),
                null);

        TenantInfo tenantInfo = new TenantInfo(null, tenantEmail, "Tenant", "111-1");
        FinancialTerms terms = new FinancialTerms(
                new Money(new BigDecimal("400000"), Currency.CLP),
                new Money(new BigDecimal("400000"), Currency.CLP),
                5, BigDecimal.ZERO);

        RentalContract contract = RentalContract.register(
                propertyId, UUID.randomUUID(), tenantInfo, terms,
                LocalDate.now().plusDays(1), LocalDate.now().plusYears(1),
                null, ContractSource.MANUAL);

        Property property = Property.reconstitute(
                propertyId, UUID.randomUUID(), UUID.randomUUID(), address,
                new Money(new BigDecimal("400000"), Currency.CLP), PropertyStatus.RESERVED,
                LocalDateTime.now(), LocalDateTime.now());

        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(invitation));
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        // Act
        InvitationPreviewResponse response = useCase.execute(RAW_TOKEN);

        // Assert
        assertThat(response.tenantEmail()).isEqualTo(tenantEmail);
        assertThat(response.propertyAddress()).isEqualTo(address);
        assertThat(response.monthlyRent()).isEqualByComparingTo(new BigDecimal("400000"));
        assertThat(response.currency()).isEqualTo("CLP");
        assertThat(response.paymentDueDay()).isEqualTo(5);
    }

    @Test
    void shouldThrow_whenInvitationIsExpired() {
        // Arrange
        when(tokenHasher.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        TenantInvitation expired = TenantInvitation.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "tenant@mail.com",
                TOKEN_HASH,
                LocalDateTime.now().minusDays(1), //venció ayer
                InvitationStatus.PENDING,
                LocalDateTime.now(),
                null);

        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(expired));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(RAW_TOKEN))
                .isInstanceOf(InvalidInvitationException.class)
                .hasMessageContaining("no longer valid");
    }

    @Test
    void shouldThrow_whenInvitationIsAlreadyAccepted() {
        // Arrange
        when(tokenHasher.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);

        TenantInvitation accepted = TenantInvitation.reconstitute(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "tenant@mail.com",
                TOKEN_HASH,
                LocalDateTime.now().plusDays(5),
                InvitationStatus.ACCEPTED,
                LocalDateTime.now(),
                LocalDateTime.now());

        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(accepted));

        // Act & Assert
        assertThatThrownBy(()-> useCase.execute(RAW_TOKEN))
                .isInstanceOf(InvalidInvitationException.class)
                .hasMessageContaining("no longer valid");
    }

    @Test
    void shouldThrow_whenTokenNotFound() {
        // Arrange
        when(tokenHasher.hash(RAW_TOKEN)).thenReturn(TOKEN_HASH);
        when(invitationRepository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(RAW_TOKEN))
                .isInstanceOf(InvalidInvitationException.class)
                .hasMessageContaining("Invalid invitation token");
    }

}
