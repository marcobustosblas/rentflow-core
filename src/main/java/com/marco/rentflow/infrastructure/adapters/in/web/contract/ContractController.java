package com.marco.rentflow.infrastructure.adapters.in.web.contract;

import com.marco.rentflow.core.application.usecase.contract.CreateContractUseCase;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.mapper.ContractRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final CreateContractUseCase createContractUseCase;
    private final ContractRestMapper contractRestMapper; // Inyección de MapStruct

    public ContractController(CreateContractUseCase createContractUseCase, ContractRestMapper contractRestMapper) {
        this.createContractUseCase = createContractUseCase;
        this.contractRestMapper = contractRestMapper;
    }

    @PostMapping
    public ResponseEntity<ContractResponseDTO> create(@Valid @RequestBody ContractRequestDTO requestDTO) {

        Currency currency = Currency.valueOf(requestDTO.currency());
        Money monthlyRent = new Money(requestDTO.monthlyRentAmount(), currency);
        Money depositAmount = new Money(requestDTO.depositAmount(), currency);

        RentalContract contract = createContractUseCase.execute(
                requestDTO.propertyId(),
                requestDTO.tenantId(),
                requestDTO.landlordId(),
                monthlyRent,
                depositAmount,
                requestDTO.paymentDueDay(),
                requestDTO.dailyPenaltyRate(),
                requestDTO.startDate(),
                requestDTO.endDate()
        );

        ContractResponseDTO responseDTO = contractRestMapper.toDto(contract);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

}
