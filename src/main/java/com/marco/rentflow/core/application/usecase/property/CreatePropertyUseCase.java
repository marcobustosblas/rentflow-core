package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.bankaccount.BankAccount;
import com.marco.rentflow.core.domain.bankaccount.exception.BankAccountNotFoundException;
import com.marco.rentflow.core.domain.bankaccount.exception.BankAccountOwnershipException;
import com.marco.rentflow.core.domain.bankaccount.ports.out.BankAccountRepository;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.common.exception.InsufficientRoleException;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.subscription.Subscription;
import com.marco.rentflow.core.domain.subscription.exception.SubscriptionNotFoundException;
import com.marco.rentflow.core.domain.subscription.ports.out.SubscriptionRepository;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.exception.UserNotFoundException;
import com.marco.rentflow.core.domain.user.port.out.UserRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class CreatePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public CreatePropertyUseCase(
            PropertyRepository propertyRepository, SubscriptionRepository subscriptionRepository,
            BankAccountRepository bankAccountRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    /* Registro caso de uso para nueva propiedad */

    public Property execute(UUID landlordId, UUID bankAccountId, String address, BigDecimal basePrice, String currency) {

        /* 1 Validate user identity and permissions */
        User user = userRepository.findById(landlordId)
                .orElseThrow(()->new UserNotFoundException(landlordId));
        if (!user.canCreateProperty()) {
            throw new InsufficientRoleException(
                    "Only active LANDLORD accounts can register properties.");
        }

        /* 2 - Validar límites de la suscripción (Regla de Negocio) */
        Subscription subscription = subscriptionRepository.findByLandlordId(landlordId)
                .orElseThrow(() -> new SubscriptionNotFoundException("Subscription not found for landlord: " + landlordId));

        int currentCountPropertiesByLandlord = propertyRepository.countByLandlordId(landlordId);
        // TODO(W9): enforce property limit via optimistic locking or DB constraint
        // to close the race condition on countByLandlordId.
        /* 3 */
        if (!subscription.canAddProperty(currentCountPropertiesByLandlord)){
            throw new IllegalStateException("Subscription limit exceeded. Please upgrade your plan.");
        }

        /* Validar propiedad de la cuenta bancaria */
        if (bankAccountId != null) {
            BankAccount account = bankAccountRepository.findById(bankAccountId)
                    .orElseThrow(() -> new BankAccountNotFoundException(bankAccountId));
            if (!account.getUserId().equals(landlordId)) {
                throw new BankAccountOwnershipException("The bank account does not belong to the provided landlord");
            }
        }

        /* 4 - Crear la propiedad */
        // (ajuste hecho del priceAsMoney -> 19-9-26, 16:35 hr)
        Money priceAsMoney = new Money(basePrice, Currency.valueOf(currency));
        Property property = Property.registerNew(address, priceAsMoney, landlordId);

        /* 5 - Asignar la cuenta (Ya sé que es segura) */
        if (bankAccountId != null) {
            property.assignPayoutAccount(bankAccountId);
        }
        /* 6 & 7 */
        return propertyRepository.save(property);
    }

}
