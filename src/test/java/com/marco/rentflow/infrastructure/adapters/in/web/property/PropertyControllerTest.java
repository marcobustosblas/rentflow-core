package com.marco.rentflow.infrastructure.adapters.in.web.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marco.rentflow.core.application.usecase.property.*;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.infrastructure.adapters.in.web.property.dto.PropertyRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.property.mapper.PropertyRestMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PropertyController.class)
public class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreatePropertyUseCase createPropertyUseCase;

    @MockBean
    private GetPropertyUseCase getPropertyUseCase;

    @MockBean
    private ListLandlordPropertiesUseCase listLandlordPropertiesUseCase;

    @MockBean
    private ListPropertiesByStatusUseCase listPropertiesByStatusUseCase;

    @MockBean
    private UpdatePropertyUseCase updatePropertyUseCase;

    @MockBean
    private PropertyRestMapper mapper;

    @Test
    void createProperty_ShouldReturn201Created() throws Exception {
        UUID landlordId = UUID.randomUUID();
        UUID payoutAccountId = UUID.randomUUID();

        PropertyRequestDTO requestDTO = new PropertyRequestDTO(
                landlordId,
                payoutAccountId,
                "Av. Siempre Viva 123",
                new BigDecimal("500000"),
                "CLP"
        );

        Property mockProperty = Property.registerNew(
                "Av. Siempre Viva 123",
                new Money(new BigDecimal("500000"), Currency.CLP),
                landlordId
        );

        Mockito.when(createPropertyUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(mockProperty);

        mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }
}
