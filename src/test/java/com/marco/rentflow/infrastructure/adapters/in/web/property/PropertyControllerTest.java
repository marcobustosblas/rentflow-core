package com.marco.rentflow.infrastructure.adapters.in.web.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marco.rentflow.core.application.usecase.property.CreatePropertyUseCase;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.property.Property;
import com.marco.rentflow.infrastructure.adapters.in.web.property.dto.PropertyRequestDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

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

    @Test
    void createProperty_ShouldReturn201Created() throws Exception {
        // 1. Preparar el JSON de entrada (El Request)
        PropertyRequestDTO requestDTO = new PropertyRequestDTO();
        requestDTO.setAddress("Av. Siempre Viva 123");
        requestDTO.setMonthlyRentAmount(new BigDecimal("500000"));
        requestDTO.setCurrency("CLP");
        requestDTO.setLandlordId(UUID.randomUUID());
        requestDTO.setBankAccountId(UUID.randomUUID());

        // 2. Simular el comportamiento del Caso de Uso (El Mock)
        Property mockProperty = createPropertyUseCase.execute(
                requestDTO.getAddress(),
                new Money(requestDTO.getMonthlyRentAmount(), Currency.valueOf(requestDTO.getCurrency())),
                requestDTO.getLandlordId(),
                requestDTO.getBankAccountId()
        );
        Mockito.when(createPropertyUseCase.execute(any(), any(), any(), any()))
                .thenReturn(mockProperty);

        // 3. Ejecutar el POST y verificar el estado HTTP 201
        mockMvc.perform(post("/api/v1/properties")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }

}
