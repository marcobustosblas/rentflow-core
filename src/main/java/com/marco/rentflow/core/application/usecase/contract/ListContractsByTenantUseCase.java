package com.marco.rentflow.core.application.usecase.contract;

import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.core.domain.contract.ports.out.ContractRepository;

import java.util.List;
import java.util.UUID;

public class ListContractsByTenantUseCase {

    private final ContractRepository repository;

    public ListContractsByTenantUseCase(ContractRepository repository) {
        this.repository = repository;
    }

    /**/
    public List<RentalContract> execute(UUID tenantId) {
        return repository.findByTenantId(tenantId);
    }

}
