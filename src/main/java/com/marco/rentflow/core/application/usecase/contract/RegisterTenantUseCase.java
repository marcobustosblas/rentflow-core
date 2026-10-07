package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.exception.ResourceAlreadyExistsException;
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
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public class RegisterTenantUseCase {

    private final UserRepository userRepository;
    private final TenantInvitationRepository invitationRepository;
    private final ContractRepository contractRepository;
    private final PropertyRepository propertyRepository;
    private final TokenHasher tokenHasher;

    public RegisterTenantUseCase(UserRepository userRepository, TenantInvitationRepository invitationRepository, ContractRepository contractRepository, PropertyRepository propertyRepository, TokenHasher tokenHasher) {
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.contractRepository = contractRepository;
        this.propertyRepository = propertyRepository;
        this.tokenHasher = tokenHasher;
    }

    public User execute(RegisterTenantCommand command) {

        // 0. Preconditions (cheap, no I/O)
        // acceptedTerms es una precondición de entrada.
        if (!command.acceptedTerms()) {
            throw new IllegalArgumentException("You must accept the contract terms");
        }

        // 1. Localizo la invitación mediante el hash del token sin procesar(raw).
        String tokenHash = tokenHasher.hash(command.rawToken());
        TenantInvitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidInvitationException("Invalid invitation token."));

        // 2. Valido el lifecycle de la invitation
        LocalDateTime now = LocalDateTime.now();
        if (!invitation.canBeAcceptedAt(now)) {
            // si está expirada Y sigue en estado PENDING, marcar como EXPIRED
            if (invitation.isExpired(now) && invitation.isPending()) {
                invitation.markAsExpired();
                invitationRepository.save(invitation);
            }
            throw new InvalidInvitationException(
                    "Invitation cannot be accepted. Current status: "
                            + invitation.getStatus());
        }

        // 3. Cargo el contrato y verifico que el tenant está esperando
        RentalContract contract = contractRepository.findById(invitation.getContractId())
                .orElseThrow(() -> new ContractNotFoundException(invitation.getContractId()));
        if (!contract.isPendingTenantSignup()) {
            throw new InvalidInvitationException(
                    "Contract is not awaiting a tenant. Status: " + contract.getStatus());
        }

        // 4. Resuelvo al tenant (crear uno nuevo o reutilizo el existente)
        String tenantEmail = invitation.getTenantEmail();
        User tenant = resolveTenant(command, tenantEmail);

        // 5. Assign the tenant to the contract (PENDING_TENANT_SIGNUP → ACTIVE)
        contract.assignTenant(tenant.getId());
        contractRepository.save(contract);

        // 6. Marco la invitation como 'accepted'
        invitation.markAsAccepted(now);
        invitationRepository.save(invitation);

        // 7. Mark the property as RENTED (tenant is now committed)
        Property property = propertyRepository.findById(contract.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException(contract.getPropertyId()));
        property.markAsRented();
        propertyRepository.save(property);

        return tenant;
    }

    /**
     * Resolves the tenant User:
     * If a TENANT user already exists with the invitation email,
     *       reuse it (do not create a duplicate).
     * Otherwise, create a new user with role TENANT.
     */
    private User resolveTenant(RegisterTenantCommand command, String tenantEmail) {

        Optional<User> existing = userRepository.findByEmail(tenantEmail);
        if (existing.isPresent()) {
            User user = existing.get();
            if (!user.canPayRent()) {
                throw new InvalidInvitationException(
                        "User with email " + tenantEmail
                                + " cannot act as a tenant. Status: " + user.getStatus()
                                + ", roles: " + user.getRoles());
            }
            return user;
        }

        // First-time registro
        if (userRepository.existsByRut(command.rut())) {
            throw new ResourceAlreadyExistsException(
                    "The RUT is already registered in the system.");
        }

        // TODO(C6-security): hash the raw password with a PasswordHasher port before persisting
        User tenant = User.registerNew(command.fullName(), tenantEmail, command.rawPassword(), command.rut(), command.phoneNumber(), Set.of(Role.TENANT));
        return userRepository.save(tenant);
    }

}
