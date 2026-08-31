package com.marco.rentflow.infrastructure.adapters.in.web.property.mapper;

import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.infrastructure.adapters.in.web.property.dto.PropertyResponseDTO;

public class PropertyRestMapper {

    public static PropertyResponseDTO toResponseDTO(Property property) {

        if (property == null) {
            return null;
        }

        PropertyResponseDTO dto = new PropertyResponseDTO();
        dto.setId(property.getId());
        dto.setLandlordId(property.getLandlordId());
        dto.setBankAccountId(property.getBankAccountId());
        dto.setAddress(property.getAddress());

        if (property.getBasePrice() != null) {
            dto.setMonthlyRentAmount(property.getBasePrice().getAmount());
            dto.setCurrency(property.getBasePrice().getCurrency().toString());
        }

        dto.setStatus(property.getStatus().toString());

        return dto;

    }
}
