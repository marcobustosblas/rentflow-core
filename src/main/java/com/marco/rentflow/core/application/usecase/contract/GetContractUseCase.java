package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.exception.ContractNotFoundException;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;

import java.util.UUID;

public class GetContractUseCase {

    private final ContractRepository contractRepository;

    public GetContractUseCase(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    /**/
    public RentalContract execute(UUID contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }

}
