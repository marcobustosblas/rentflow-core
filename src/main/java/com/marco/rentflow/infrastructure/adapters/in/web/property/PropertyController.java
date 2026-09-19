package com.marco.rentflow.infrastructure.adapters.in.web.property;

import com.marco.rentflow.core.application.usecase.property.*;

import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.infrastructure.adapters.in.web.property.dto.PropertyRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.property.dto.PropertyResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.property.mapper.PropertyRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final CreatePropertyUseCase createPropertyUseCase;
    private final GetPropertyUseCase getPropertyUseCase;
    private final ListLandlordPropertiesUseCase listLandlordPropertiesUseCase;
    private final ListPropertiesByStatusUseCase listPropertiesByStatusUseCase;
    private final UpdatePropertyUseCase updatePropertyUseCase;
    private final PropertyRestMapper mapper;

    public PropertyController(CreatePropertyUseCase createPropertyUseCase, GetPropertyUseCase getPropertyUseCase, ListLandlordPropertiesUseCase listLandlordPropertiesUseCase, ListPropertiesByStatusUseCase listPropertiesByStatusUseCase, UpdatePropertyUseCase updatePropertyUseCase, PropertyRestMapper propertyRestMapper) {
        this.createPropertyUseCase = createPropertyUseCase;
        this.getPropertyUseCase = getPropertyUseCase;
        this.listLandlordPropertiesUseCase = listLandlordPropertiesUseCase;
        this.listPropertiesByStatusUseCase = listPropertiesByStatusUseCase;
        this.updatePropertyUseCase = updatePropertyUseCase;
        this.mapper = propertyRestMapper;
    }

    @PostMapping
    public ResponseEntity<PropertyResponseDTO> create(@Valid @RequestBody PropertyRequestDTO request) {
        /*
         * ZERO TRUST: Por ahora, el DTO trae el landlordId.
         * En la Fase 2 (Seguridad), eliminaré landlordId del DTO y lo
         * extraeré directamente del Token JWT inyectado en el SecurityContext.
         */

        // a- Delego el trabajo duro al Caso de Uso (El Orquestador)
        Property createdProperty = createPropertyUseCase.execute(
                request.landlordId(),
                request.payoutAccountId(),
                request.address(),
                request.basePrice(),
                request.currency()
        );
        // c- Traducir la Entidad de vuelta a un DTO seguro para la web
        PropertyResponseDTO response = mapper.toDto(createdProperty);
        // d- Devolver HTTP 201 (Created) con el JSON mapeado
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> getById(@PathVariable UUID id) {
        var property = getPropertyUseCase.execute(id);
        return ResponseEntity.ok(mapper.toDto(property));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> update(
            @PathVariable UUID id, @Valid @RequestBody PropertyRequestDTO request) {
        var propertyUpdated = updatePropertyUseCase.execute(
                id,
                request.landlordId(),
                request.payoutAccountId(),
                request.address(),
                request.basePrice(),
                request.currency());
        return ResponseEntity.ok(mapper.toDto(propertyUpdated));
    }

    @GetMapping("/landlord/{landlordId}")
    public ResponseEntity<List<PropertyResponseDTO>> getByLandLord(@PathVariable UUID landlordId) {
        List<Property> properties = listLandlordPropertiesUseCase.execute(landlordId);
        var response = properties.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<PropertyResponseDTO>> getByStatus
            (@RequestParam(required = false, defaultValue = "AVAILABLE") String status){
        var properties = listPropertiesByStatusUseCase.execute(status);
        var response = properties.stream().map(mapper::toDto).toList();
        return ResponseEntity.ok(response);
    }

}
