package com.marco.rentflow.infrastructure.adapters.in.web.property.mapper;

import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.infrastructure.adapters.in.web.property.dto.PropertyResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PropertyRestMapper {

    // Mapea los valores anidados de Value Object Money hacia los campos planos del DTO
    // target -> el campo del objeto destino, el DTO
    // source -> el campo del objeto origen, la entidad
    @Mapping(target = "monthlyRentAmount", source = "basePrice.amount")
    @Mapping(target = "currency", source = "basePrice.currency")
    PropertyResponseDTO toDto(Property property);

}