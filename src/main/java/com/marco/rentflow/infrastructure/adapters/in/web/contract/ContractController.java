package com.marco.rentflow.infrastructure.adapters.in.web.contract;

import com.marco.rentflow.core.application.usecase.contract.*;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.mapper.ContractRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final CreateContractUseCase createContractUseCase;
    private final GetContractUseCase getContractUseCase;
    private final ListContractsByLandlordUseCase listContractsByLandlordUseCase;
    private final ListContractsByTenantUseCase listContractsByTenantUseCase;
    private final TerminateContractUseCase terminateContractUseCase;
    private final ContractRestMapper mapper; // Inyección de MapStruct

    public ContractController(CreateContractUseCase createContractUseCase, GetContractUseCase getContractUseCase, ListContractsByLandlordUseCase listContractsByLandlordUseCase, ListContractsByTenantUseCase listContractsByTenantUseCase, TerminateContractUseCase terminateContractUseCase, ContractRestMapper contractRestMapper) {
        this.createContractUseCase = createContractUseCase;
        this.getContractUseCase = getContractUseCase;
        this.listContractsByLandlordUseCase = listContractsByLandlordUseCase;
        this.listContractsByTenantUseCase = listContractsByTenantUseCase;
        this.terminateContractUseCase = terminateContractUseCase;
        this.mapper = contractRestMapper;
    }

    @PostMapping
    public ResponseEntity<ContractResponseDTO> create(@Valid @RequestBody ContractRequestDTO request) {

        RentalContract contract = createContractUseCase.execute(
                request.propertyId(),
                request.tenantId(),
                request.landlordId(),
                request.rentAmount(),
                request.depositAmount(),
                request.currency(),
                request.paymentDueDay(),
                request.dailyPenaltyRate(),
                request.startDate(),
                request.endDate()
        );

        ContractResponseDTO response = mapper.toDto(contract);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponseDTO> getById(@PathVariable UUID id) {
        RentalContract contract = getContractUseCase.execute(id);
        return ResponseEntity.ok(mapper.toDto(contract));
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<ContractResponseDTO>> getByLandlord(@PathVariable UUID landlordId) {
        var contracts = listContractsByLandlordUseCase.execute(landlordId);
        var responses = contracts.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<ContractResponseDTO>> getByTenant(@PathVariable UUID tenantId) {
        List<RentalContract> contracts = listContractsByTenantUseCase.execute(tenantId);
        List<ContractResponseDTO> responses = contracts.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{id}/terminate")
    public ResponseEntity<ContractResponseDTO> terminate(@PathVariable UUID id,
                                                         @RequestParam UUID landlordId) {
        // landlordId viene como RequestParam provisoriamente hasta implementar SecurityContext
        RentalContract terminatedContract = terminateContractUseCase.execute(id, landlordId);
        return ResponseEntity.ok(mapper.toDto(terminatedContract));
    }

}
