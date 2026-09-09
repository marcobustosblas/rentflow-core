package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.bankaccount.BankAccount;
import com.marco.rentflow.core.domain.bankaccount.ports.out.BankAccountRepository;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.subscription.Subscription;
import com.marco.rentflow.core.domain.subscription.ports.out.SubscriptionRepository;

import java.util.Optional;
import java.util.UUID;

public class CreatePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BankAccountRepository bankAccountRepository;

    public CreatePropertyUseCase(
            PropertyRepository propertyRepository,
            SubscriptionRepository subscriptionRepository,
            BankAccountRepository bankAccountRepository) {
        this.propertyRepository = propertyRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    /* Registro caso de uso para nueva propiedad */

    /* 1 */
    public Property execute(String address, Money basePrice, UUID landlordId, UUID bankAccountId) {

        /* 2 - Validar límites de la suscripción (Regla de Negocio) */
        Subscription subscription = subscriptionRepository.findByLandlordId(landlordId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        int currentCountPropertiesByLandlord = propertyRepository.countByLandlordId(landlordId);
        /* 3 */
        if (!subscription.canAddProperty(currentCountPropertiesByLandlord)){
            throw new IllegalStateException("Subscription limit exceeded. Please upgrade your plan.");
        }

        /* Validar propiedad de la cuenta bancaria */
        if (bankAccountId != null) {
            BankAccount account = bankAccountRepository.findById(bankAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));
            if (!account.getUserId().equals(landlordId)) {
                throw new IllegalStateException("The bank account does not belong to the provided landlord");
            }
        }

        /* 4 - Crear la propiedad */
        Property property = Property.registerNew(address, basePrice, landlordId);

        /* 5 - Asignar la cuenta (Ya sé que es segura) */
        if (bankAccountId != null) {
            property.assignPayoutAccount(bankAccountId);
        }
        /* 6 & 7 */
        return propertyRepository.save(property);
    }

}
