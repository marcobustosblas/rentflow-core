package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.contract.ContractStatus;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.exception.ContractNotFoundException;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.core.domain.property.exception.PropertyNotFoundException;
import com.marco.rentflow.core.domain.property.ports.out.PropertyRepository;

import java.util.UUID;

public class TerminateContractUseCase {

    private final ContractRepository contractRepository;
    private final PropertyRepository propertyRepository;

    public TerminateContractUseCase(ContractRepository contractRepository, PropertyRepository propertyRepository) {
        this.contractRepository = contractRepository;
        this.propertyRepository = propertyRepository;
    }

    public RentalContract execute(UUID contractId, UUID landlordId) {

        /* 1 - Buscar el contrato */
        RentalContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));

        /* 2 - Buscar la propiedad asociada */
        Property property = propertyRepository.findById(contract.getPropertyId())
                .orElseThrow(() -> new PropertyNotFoundException(contractId));

        /* 3 - Seguridad: Solo el dueño de la propiedad puede terminar el contrato */
        if (!property.getLandlordId().equals(landlordId)) {
            throw new SecurityException("Only the landlord can terminate this contract");
        }

        /* 4 - Validar que el contrato esté activo (no puedo terminar algo ya terminado) */
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE contracts can be terminated");
        }

        /* 5 - Lógica de Negocio: Actualizar estados */
        contract.terminate();
        // Liberar la propiedad para que vuelva al mercado
        property.markAsAvailable();

        /* 6 - persistir */
        propertyRepository.save(property);
        return contractRepository.save(contract);

    }

}
