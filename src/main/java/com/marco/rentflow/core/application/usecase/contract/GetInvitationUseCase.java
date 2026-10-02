package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.ports.out.TokenHasher;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.TenantInvitation;
import com.marco.rentflow.core.domain.contract.exception.ContractNotFoundException;
import com.marco.rentflow.core.domain.contract.exception.InvalidInvitationException;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.contract.ports.out.TenantInvitationRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.exception.PropertyNotFoundException;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;

import java.time.LocalDateTime;

public class GetInvitationUseCase {

    private final TenantInvitationRepository tenantInvitationRepository;
    private final ContractRepository contractRepository;
    private final PropertyRepository propertyRepository;
    private final TokenHasher tokenHasher;

    public GetInvitationUseCase(TenantInvitationRepository tenantInvitationRepository, ContractRepository contractRepository, PropertyRepository propertyRepository, TokenHasher tokenHasher) {
        this.tenantInvitationRepository = tenantInvitationRepository;
        this.contractRepository = contractRepository;
        this.propertyRepository = propertyRepository;
        this.tokenHasher = tokenHasher;
    }

    public InvitationPreviewResponse execute(String rawToken) {

        // 1. Hash the token and find the invitation
        String tokenHash = tokenHasher.hash(rawToken);
        TenantInvitation invitation = tenantInvitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidInvitationException("Invalid invitation token."));

        // 2. Reject expired or non-pending invitations
        LocalDateTime now = LocalDateTime.now();
        if (!invitation.canBeAcceptedAt(now)) {
            throw new InvalidInvitationException(
                    "Invitation is no longer valid. Status: " + invitation.getStatus());
        }

        // 3. Load the contract to expose public-facing details
        RentalContract contract = contractRepository.findById(invitation.getContractId())
                .orElseThrow(() -> new ContractNotFoundException(invitation.getContractId()));

        // 4. Load the property for the address
        Property property = propertyRepository.findById(contract.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException(contract.getPropertyId()));

        // 5. Return only the data the tenant needs to see
        return new InvitationPreviewResponse(
                invitation.getTenantEmail(),
                property.getAddress(),
                contract.getMonthlyRent().getAmount(),
                contract.getMonthlyRent().getCurrency().name(),
                contract.getDepositAmount().getAmount(),
                contract.getPaymentDueDay(),
                contract.getStartDate(),
                contract.getEndDate(),
                invitation.getExpiresAt()
        );
    }

}
