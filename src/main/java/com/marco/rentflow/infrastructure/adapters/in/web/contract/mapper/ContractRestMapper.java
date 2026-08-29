package com.marco.rentflow.infrastructure.adapters.in.web.contract.mapper;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractResponseDTO;

public class ContractRestMapper {

    public static ContractResponseDTO toResponseDTO(RentalContract contract) {

        if (contract == null) return null;

        ContractResponseDTO dto = new ContractResponseDTO();
        dto.setId(contract.getId());
        dto.setPropertyId(contract.getPropertyId());
        dto.setTenantId(contract.getTenantId());
        dto.setLandlordId(contract.getLandlordId());

        dto.setMonthlyRentAmount(contract.getMonthlyRent().getAmount());
        dto.setDepositAmount(contract.getDepositAmount().getAmount());
        dto.setCurrency(contract.getMonthlyRent().getCurrency().toString());
        dto.setPaymentDueDay(contract.getPaymentDueDay());
        dto.setDailyPenaltyRate(contract.getDailyPenaltyRate());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setStatus(contract.getStatus().toString());

        return dto;
    }
}
