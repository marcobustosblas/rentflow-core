package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.ports.out.DomainEventPublisher;
import com.marco.rentflow.core.domain.common.ports.out.TokenHasher;
import com.marco.rentflow.core.domain.contract.FinancialTerms;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.TenantInfo;
import com.marco.rentflow.core.domain.contract.TenantInvitation;
import com.marco.rentflow.core.domain.contract.events.TenantInvitationCreatedEvent;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.contract.ports.out.TenantInvitationRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.exception.PropertyNotAvailableException;
import com.marco.rentflow.core.domain.property.exception.PropertyNotFoundException;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import java.math.BigDecimal;

public class CreateContractUseCase {

    private static final int INVITATION_TTL_DAYS = 7;
    private final PropertyRepository propertyRepository;
    private final ContractRepository contractRepository;
    private final TenantInvitationRepository tenantInvitationRepository;
    private final TokenHasher tokenHasher;
    private final DomainEventPublisher eventPublisher;

    public CreateContractUseCase(PropertyRepository propertyRepository, ContractRepository contractRepository, TenantInvitationRepository tenantInvitationRepository, TokenHasher tokenHasher, DomainEventPublisher eventPublisher) {
        this.propertyRepository = propertyRepository;
        this.contractRepository = contractRepository;
        this.tenantInvitationRepository = tenantInvitationRepository;
        this.tokenHasher = tokenHasher;
        this.eventPublisher = eventPublisher;
    }

    /* 1 */
    public RentalContract execute(CreateContractCommand command) {

        /* 1 - Validaciones temporales */
        if (command.endDate().isBefore(command.startDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        /* 2 - Buscar y validar la propiedad */
        Property property = propertyRepository.findById(command.propertyId())
                .orElseThrow(() -> new PropertyNotFoundException(command.propertyId()));

        /* ¿La propiedad realmente le pertenece al Landlord que está intentando crear el contrato? */
        if (!property.getLandlordId().equals(command.landlordId())) {
            throw new IllegalStateException("The property does not belong to the provided landlord");
        }

        /* Validar disponibilidad de la Propiedad */
        if (!property.isAvailable()) {
            throw new PropertyNotAvailableException("Property is not available for rent");
        }

        /* 3 - Construir Value Objects */
        TenantInfo tenantInfo = new TenantInfo(
                null, // El usuario NO existe aún, se asignará al ACEPTAR la invitación
                command.tenantEmail(),
                command.tenantFullName(),
                command.tenantRut()
        );

        /* Convertir primitivos a Value Objects */
        Currency currencyEnum = Currency.valueOf(command.currency());
        Money rentMoney = new Money(command.rentAmount(), currencyEnum);
        Money depositMoney = new Money(command.depositAmount(), currencyEnum);

        BigDecimal penalty = command.dailyPenaltyRate() != null
                ? command.dailyPenaltyRate()
                : BigDecimal.ZERO;

        FinancialTerms terms = new FinancialTerms(rentMoney, depositMoney, command.paymentDueDay(), penalty);

        /* 4 - Registrar el contrato delegando al Dominio */
        RentalContract contract = RentalContract.register(
                command.propertyId(),
                command.landlordId(),
                tenantInfo,
                terms,
                command.startDate(),
                command.endDate(),
                command.contractDocumentUrl(),
                command.source()
        );

        /* 5 - Create the tenant invitation with a hashed token
        * The raw token is intentionally kept in memory only:
        * it will be sent by email in C6 (post-commit event). Never persist it.
        */
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = tokenHasher.hash(rawToken);

        TenantInvitation invitation= TenantInvitation.create(
                contract.getId(),
                command.landlordId(),
                command.tenantEmail(),
                command.tenantFullName(),   // ← nuevo
                command.tenantRut(),        // ← nuevo
                tokenHash,
                LocalDateTime.now().plusDays(INVITATION_TTL_DAYS)
        );

        /* 6 - Reserve the property. It becomes RENTED only when the tenant accepts. */
        property.markAsReserved();

        /* 7 - Se persisten todos los agregados para usarlo en el GetInvitationUseCase */
        propertyRepository.save(property);
        RentalContract savedContract = contractRepository.save(contract);
        tenantInvitationRepository.save(invitation);

        /* TODO(C6): Despachar el correo electrónico post-commit usando el rawToken */
        /* TODO(C6): publish TenantInvitationCreatedEvent with rawToken to dispatch the email after commit (AFTER_COMMIT listener). */
        // notificationPort.sendTenantInvitationEmail(command.tenantEmail(), rawToken);

        /* 8.
        *  Publish a domain event. The listener runs AFTER_COMMIT,
        * so the email is only sent if the transaction succeeds.
        * */
        eventPublisher.publish(new TenantInvitationCreatedEvent(
                savedContract.getId(),
                command.landlordId(),
                command.tenantEmail(),
                command.tenantFullName(),
                rawToken,
                LocalDateTime.now()
        ));

        /* Retorna el detalle del contrato */
        return savedContract;

    }

}
