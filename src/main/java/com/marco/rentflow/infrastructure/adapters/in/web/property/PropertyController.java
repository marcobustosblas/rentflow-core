package com.marco.rentflow.infrastructure.adapters.in.web.property;

import com.marco.rentflow.core.application.usecase.property.CreatePropertyUseCase;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.infrastructure.adapters.in.web.property.dto.PropertyRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.property.dto.PropertyResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.property.mapper.PropertyRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final CreatePropertyUseCase createPropertyUseCase;

    public PropertyController(CreatePropertyUseCase useCase) {
        this.createPropertyUseCase = useCase;
    }

    @PostMapping
    public ResponseEntity<PropertyResponseDTO> create(@RequestBody PropertyRequestDTO requestDTO) {
        // a- Transformar datos del front al value object del dominio
        Money basePrice = new Money(
                requestDTO.getMonthlyRentAmount(),
                Currency.valueOf(requestDTO.getCurrency())
        );
        // b- Delego el trabajo duro al Caso de Uso (El Orquestador)
        Property createdProperty = createPropertyUseCase.execute(
                requestDTO.getAddress(),
                basePrice,
                requestDTO.getLandlordId(),
                requestDTO.getBankAccountId()
        );
        // c- Traducir la Entidad de vuelta a un DTO seguro para la web
        PropertyResponseDTO responseDTO = PropertyRestMapper.toRequestDTO(createdProperty);
        // d- Devolver HTTP 201 (Created) con el JSON mapeado
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    // --- Esqueletos para GET y PUT (plan de la W4) ---

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> getProperty(@PathVariable UUID id) {
        // En el futuro: Property p = getPropertyUseCase.execute(id);
        // return ResponseEntity.ok(PropertyRestMapper.toResponseDTO(p));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> updateProperty(@PathVariable UUID id, @RequestBody PropertyRequestDTO requestDTO) {
        // En el futuro: UpdatePropertyUseCase...
        return ResponseEntity.ok().build();
    }

}
