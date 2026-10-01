package com.marco.rentflow.infrastructure.adapters.in.web.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marco.rentflow.core.application.usecase.payment.InitiatePaymentCheckoutUseCase;
import com.marco.rentflow.core.application.usecase.payment.PaymentWebhookDispatcher;
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
import java.time.LocalDateTime;
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
    private PaymentWebhookDispatcher webhookDispatcher;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void initiateCheckout_ShouldReturn200AndUrl() throws Exception {
        PaymentCheckoutRequestDTO requestDTO = new PaymentCheckoutRequestDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RENT",
                LocalDate.now()
        );

        String fakeWebpayUrl = "https://webpay.cl/pagar/123";
        InitiatePaymentCheckoutUseCase.CheckoutResult mockResult =
                new InitiatePaymentCheckoutUseCase.CheckoutResult(UUID.randomUUID(), fakeWebpayUrl, "PENDING");

        Mockito.when(checkoutUseCase.execute(any(), any(), any(), any())).thenReturn(mockResult);

        mockMvc.perform(post("/api/v1/payments/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value(fakeWebpayUrl));
    }

    @Test
    void handleWebhook_ShouldReturn200OkEmptyBody() throws Exception {
        PaymentWebhookRequestDTO requestDTO = new PaymentWebhookRequestDTO(
                "PAY-RENT-123-2026-03",
                new BigDecimal("500000"),
                "CLP",
                LocalDateTime.now(),
                "TX-999",
                "https://receipts.org/rx-999"
        );

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }
}
