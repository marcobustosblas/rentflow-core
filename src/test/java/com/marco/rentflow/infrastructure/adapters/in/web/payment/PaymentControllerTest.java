package com.marco.rentflow.infrastructure.adapters.in.web.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marco.rentflow.core.application.usecase.payment.InitiatePaymentCheckoutUseCase;
import com.marco.rentflow.core.application.usecase.payment.ProcessPaymentUseCase;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentCheckoutRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentWebhookRequestDTO;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InitiatePaymentCheckoutUseCase checkoutUseCase;

    @MockBean
    private ProcessPaymentUseCase processPaymentUseCase;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void initiateCheckout_ShouldReturn200AndUrl() throws Exception {
        PaymentCheckoutRequestDTO requestDTO = new PaymentCheckoutRequestDTO();
        requestDTO.setTenantId(UUID.randomUUID());
        requestDTO.setContractId(UUID.randomUUID());
        requestDTO.setPaymentDate(LocalDate.now());

        String fakeWebpayUrl = "https://webpay.cl/pagar/123";
        Mockito.when(checkoutUseCase.execute(any(), any(), any())).thenReturn(fakeWebpayUrl);

        mockMvc.perform(post("/api/v1/payments/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value(fakeWebpayUrl)); // Valida que el JSON tenga la URL
    }

    @Test
    void handleWebhook_ShouldReturn200OkEmptyBody() throws Exception {
        PaymentWebhookRequestDTO requestDTO = new PaymentWebhookRequestDTO();
        requestDTO.setIdempotencyKey("PAY-RENT-123-2026-03");
        requestDTO.setAmountPaid(new BigDecimal("500000"));
        requestDTO.setCurrency("CLP");
        requestDTO.setPaymentDate(LocalDate.now());
        requestDTO.setTransactionRef("TX-999");
        requestDTO.setReceiptUrl("https://receipts.org/rx-999");

        // processPaymentUseCase retorna void en el controller (lo ignoro), pero simulo éxito devolviendo un objeto dummy o nada.
        // Como no asigno el retorno en el Controller, Mockito devuelve null por defecto, lo cual es perfecto.

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }
}
