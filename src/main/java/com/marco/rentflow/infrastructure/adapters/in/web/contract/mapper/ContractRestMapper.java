package com.marco.rentflow.infrastructure.adapters.in.web.contract.mapper;

import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractRestMapper {

    // En mi dominio 'RentalContract', el monto del arriendo es un Value Object 'Money' llamado 'monthlyRent'
    // MapStruct mapeará automáticamente los demás campos (como depositAmount o paymentDueDay) si se llaman igual en la entidad.
    @Mapping(target = "monthlyRentAmount", source = "monthlyRent.amount")
    @Mapping(target = "depositAmount", source = "depositAmount.amount")
    @Mapping(target = "currency", source = "monthlyRent.currency")
    ContractResponseDTO toDto(RentalContract contract);

}