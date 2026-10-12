package com.marco.rentflow.infrastructure.adapters.in.web.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marco.rentflow.core.application.usecase.contract.*;
import com.marco.rentflow.core.domain.common.Currency;
import com.marco.rentflow.core.domain.common.Money;
import com.marco.rentflow.core.domain.contract.*;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.dto.ContractRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.contract.mapper.ContractRestMapper;
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

    @MockBean
    private GetContractUseCase getContractUseCase;

    @MockBean
    private ListContractsByLandlordUseCase listContractsByLandlordUseCase;

    @MockBean
    private ListContractsByTenantUseCase listContractsByTenantUseCase;

    @MockBean
    private TerminateContractUseCase terminateContractUseCase;

    @MockBean
    private ContractRestMapper mapper;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createContract_ShouldReturn201Created() throws Exception {
        UUID propertyId = UUID.randomUUID();
        UUID landlordId = UUID.randomUUID();

        ContractRequestDTO requestDTO = new ContractRequestDTO(
                propertyId,
                landlordId,
                "tenant@test.com",
                "Tenant Name",
                "12345678-9",
                new BigDecimal("500000"),
                new BigDecimal("500000"),
                "CLP",
                5,
                new BigDecimal("0.01"),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusYears(1),
                "http://doc.pdf",
                "MANUAL"
        );

        TenantInfo tenantInfo = new TenantInfo(UUID.randomUUID(), "tenant@test.com", "Tenant Name", "12345678-9");
        FinancialTerms terms = new FinancialTerms(
                new Money(new BigDecimal("500000"), Currency.CLP),
                new Money(new BigDecimal("500000"), Currency.CLP),
                5,
                new BigDecimal("0.01")
        );

        RentalContract mockContract = RentalContract.register(
                propertyId, landlordId, tenantInfo, terms,
                requestDTO.startDate(), requestDTO.endDate(),
                requestDTO.contractDocumentUrl(), ContractSource.MANUAL
        );

        Mockito.when(createContractUseCase.execute(any(CreateContractCommand.class)))
                .thenReturn(mockContract);

        mockMvc.perform(post("/api/v1/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());
    }
}
