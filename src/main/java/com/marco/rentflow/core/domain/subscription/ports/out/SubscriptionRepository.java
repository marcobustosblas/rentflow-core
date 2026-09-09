package com.marco.rentflow.core.domain.subscription.ports.out;

import com.marco.rentflow.core.domain.subscription.Subscription;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {

    // Guarda o actualiza una suscripción
    Subscription save(Subscription subscription);

    Optional<Subscription> findById(UUID id);

    // Busca el plan activo de un usuario (Landlord)
    Optional<Subscription> findByLandlordId(UUID landlordId);

}
