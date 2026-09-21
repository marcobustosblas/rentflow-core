package com.marco.rentflow.core.application.usecase.property;

import com.marco.rentflow.core.domain.bankaccount.BankAccount;
import com.marco.rentflow.core.domain.bankaccount.exception.BankAccountNotFoundException;
import com.marco.rentflow.core.domain.bankaccount.ports.out.BankAccountRepository;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.exception.PropertyNotFoundException;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdatePropertyUseCase {

    public final PropertyRepository propertyRepository;
    public final BankAccountRepository bankAccountRepository;

    public UpdatePropertyUseCase(PropertyRepository propertyRepository, BankAccountRepository bankAccountRepository) {
        this.propertyRepository = propertyRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    /**/
    public Property execute(UUID propertyId, UUID landlordId, UUID payoutAccountId, String address, BigDecimal basePrice, String currency) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyNotFoundException(propertyId));
        // blindar contra ataques donde un usuario malicioso intente modificar la propiedad de otro usuario
        if (!property.getLandlordId().equals(landlordId)) {
            throw new SecurityException("Only the actual owner can modify the property");
        }

        // 1. Actualización parcial de Cuenta Bancaria
        if (payoutAccountId != null && !payoutAccountId.equals(property.getBankAccountId())) {
            BankAccount account = bankAccountRepository.findById(payoutAccountId)
                    .orElseThrow(() -> new BankAccountNotFoundException(payoutAccountId));
            // Validar que la cuenta pertenezca al mismo dueño de la propiedad
            if (!account.getUserId().equals(landlordId)) {
                throw new IllegalStateException("The bank account does not belong to the property owner");
            }
            property.assignPayoutAccount(payoutAccountId);
        }

        // 2. Actualización parcial de Dirección
        if (address != null) {
            property.updateAddress(address);
        }

        // 3. Actualización parcial de Precio/Moneda
        if (basePrice != null || currency != null) {
            // Si no me envían el precio nuevo, conservo el viejo
            BigDecimal finalPrice = (basePrice != null) ? basePrice : property.getBasePrice().getAmount();
            // Si no me envían la moneda nueva, conservo la vieja
            String finalCurrency = (currency != null) ? currency : property.getBasePrice().getCurrency().name();

            property.updateBasePrice(new Money(finalPrice, Currency.valueOf(finalCurrency)));
        }

        return propertyRepository.save(property);
    }

}
