package com.marco.rentflow.core.domain.contract.ports.out;

import com.marco.rentflow.core.domain.contract.RentalContract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository {

    RentalContract save(RentalContract contract);
    Optional<RentalContract> findById(UUID id);
    List<RentalContract> findByTenantId(UUID tenantId);
    List<RentalContract> findByLandlordId(UUID landlordId);

}
