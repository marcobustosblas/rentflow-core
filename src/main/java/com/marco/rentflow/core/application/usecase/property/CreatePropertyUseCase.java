package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.subscription.Subscription;
import com.marco.rentflow.core.domain.subscription.ports.out.SubscriptionRepository;

import java.util.UUID;

public class CreatePropertyUseCase {

    private final PropertyRepository propertyRepository;
    private final SubscriptionRepository subscriptionRepository;

    public CreatePropertyUseCase(PropertyRepository propertyRepository, SubscriptionRepository subscriptionRepository) {
        this.propertyRepository = propertyRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    /* Registro caso de uso para nueva propiedad */

    /* 1 */
    public Property execute(String address, Money basePrice, UUID landlordId, UUID bankAccountId) {

        /* 2 - Validar límites de la suscripción (Regla de Negocio) */
        Subscription subscription = subscriptionRepository.findByUserId(landlordId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        int currentCountPropertiesByLandlord = propertyRepository.countByLandlordId(landlordId);
        /* 3 */
        if (!subscription.canAddProperty(currentCountPropertiesByLandlord)){
            throw new IllegalStateException("Subscription limit exceeded. Please upgrade your plan.");
        }

        /* 4 - Crear la propiedad */
        Property property = Property.registerNew(address, basePrice, landlordId);

        /* 5 */
        if (bankAccountId != null) {
            property.assignPayoutAccount(bankAccountId);
        }
        /* 6 & 7 */
        return propertyRepository.save(property);
    }

}
