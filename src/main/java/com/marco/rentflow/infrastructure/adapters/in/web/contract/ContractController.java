package com.marco.rentflow.infrastructure.adapters.in.web.contract;

import com.marco.rentflow.core.application.usecase.contract.CreateContractUseCase;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.mapper.ContractRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final CreateContractUseCase contractUseCase;

    public ContractController(CreateContractUseCase useCase) {
        this.contractUseCase = useCase;
    }

    @PostMapping
    public ResponseEntity<ContractResponseDTO> create(@RequestBody ContractRequestDTO requestDTO) {

        Currency currency = Currency.valueOf(requestDTO.getCurrency());
        Money monthlyRent = new Money(requestDTO.getMonthlyRentAmount(), currency);
        Money depositAmount = new Money(requestDTO.getDepositAmount(), currency);

        RentalContract contract = contractUseCase.execute(
                requestDTO.getPropertyId(),
                requestDTO.getTenantId(),
                requestDTO.getLandlordId(),
                monthlyRent,
                depositAmount,
                requestDTO.getPaymentDueDay(),
                requestDTO.getDailyPenaltyRate(),
                requestDTO.getStartDate(),
                requestDTO.getEndDate()
        );

        ContractResponseDTO responseDTO = ContractRestMapper.toResponseDTO(contract);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

}
