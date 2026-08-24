package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserRepository;

import java.time.LocalDate;
import java.util.UUID;

public class CreateContractUseCase {

    private final PropertyRepository propertyRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;

    public CreateContractUseCase(PropertyRepository propertyRepository, ContractRepository contractRepository, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
    }

    /* 1 */
    public RentalContract execute( UUID propertyId, UUID tenantId, UUID landlordId,
                        Money standardRent, Money standardDeposit,
                        int paymentDueDay, java.math.BigDecimal dailyPenaltyRate,
                        LocalDate startDate, LocalDate endDate) {

        /* Buscar la propiedad */
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

        /* ¿La propiedad realmente le pertenece al Landlord que está intentando crear el contrato? */
        if (!property.getLandlordId().equals(landlordId)) {
            throw new IllegalStateException("The property does not belong to the provided landlord");
        }

        /* 2 - Validar disponibilidad de la Propiedad */
        if (!property.isAvailable()) {
            throw new IllegalStateException("Property is not available for rent");
        }

        /* Validar al Tenant (Debe existir y tener el rol correcto) */
        User tenant = userRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        if (!tenant.isTenant()) {
            throw new IllegalStateException("User does not have TENANT privileges");
        }

        /* 3 - El sistema crea el agregado RentalContract con estado ACTIVE */
        RentalContract contract = RentalContract.create(
                propertyId, tenantId, landlordId,
                standardRent, standardDeposit,
                paymentDueDay, dailyPenaltyRate != null ? dailyPenaltyRate : java.math.BigDecimal.ZERO,
                startDate, endDate
        );

        /* 4 - El sistema actualiza el estado de la Property a RENTED */
        property.markAsRented();

        /* 5 - Se persisten ambos agregados */
        propertyRepository.save(property);

        /* 6 - Retorna el detalle del contrato */
        return contractRepository.save(contract);

    }

}
