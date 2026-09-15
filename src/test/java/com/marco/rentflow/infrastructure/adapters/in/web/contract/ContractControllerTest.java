package com.marco.rentflow.infrastructure.adapters.in.web.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marco.rentflow.core.application.usecase.contract.CreateContractUseCase;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.RentalContract;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContractController.class)
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateContractUseCase createContractUseCase;

    @BeforeEach
    void setup() {
        // Registra el módulo de fechas para que Jackson sepa convertir LocalDate a JSON
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createContract_ShouldReturn201Created() throws Exception {
        // 1. Instanciar el Record pasando todo por el constructor (sin setters)
        ContractRequestDTO requestDTO = new ContractRequestDTO(
                UUID.randomUUID(),                  // propertyId
                UUID.randomUUID(),                  // tenantId
                UUID.randomUUID(),                  // landlordId
                new BigDecimal("500000"),       // monthlyRentAmount
                new BigDecimal("500000"),       // depositAmount
                "CLP",                              // currency
                5,                                  // paymentDueDay
                new BigDecimal("0.01"),         // dailyPenaltyRate
                LocalDate.now(),                    // startDate
                LocalDate.now().plusYears(1)        // endDate
        );

        // 2. Extraer usando métodos planos (sin 'get')
        RentalContract mockContract = RentalContract.create(
                requestDTO.propertyId(),
                requestDTO.tenantId(),
                requestDTO.landlordId(),
                new Money(requestDTO.monthlyRentAmount(), Currency.CLP),
                new Money(requestDTO.depositAmount(), Currency.CLP),
                requestDTO.paymentDueDay(),
                requestDTO.dailyPenaltyRate(),
                requestDTO.startDate(),
                requestDTO.endDate()
        );

        Mockito.when(createContractUseCase.execute(
                        any(), any(), any(),
                        any(), any(), anyInt(),
                        any(), any(), any()))
                .thenReturn(mockContract);

        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }
}
