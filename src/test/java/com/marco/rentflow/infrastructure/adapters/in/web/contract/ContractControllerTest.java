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
        ContractRequestDTO requestDTO = new ContractRequestDTO();
        requestDTO.setPropertyId(UUID.randomUUID());
        requestDTO.setTenantId(UUID.randomUUID());
        requestDTO.setLandlordId(UUID.randomUUID());
        requestDTO.setMonthlyRentAmount(new BigDecimal("500000"));
        requestDTO.setDepositAmount(new BigDecimal("500000"));
        requestDTO.setCurrency("CLP");
        requestDTO.setPaymentDueDay(5);
        requestDTO.setDailyPenaltyRate(new BigDecimal("0.01"));
        requestDTO.setStartDate(LocalDate.now());
        requestDTO.setEndDate(LocalDate.now().plusYears(1));

        RentalContract mockContract = RentalContract.create(
                requestDTO.getPropertyId(), requestDTO.getTenantId(), requestDTO.getLandlordId(),
                new Money(requestDTO.getMonthlyRentAmount(), Currency.CLP),
                new Money(requestDTO.getDepositAmount(), Currency.CLP),
                requestDTO.getPaymentDueDay(), requestDTO.getDailyPenaltyRate(),
                requestDTO.getStartDate(), requestDTO.getEndDate()
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
