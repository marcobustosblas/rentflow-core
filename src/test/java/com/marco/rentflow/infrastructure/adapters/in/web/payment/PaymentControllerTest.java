package com.marco.rentflow.infrastructure.adapters.in.web.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marco.rentflow.core.application.usecase.payment.GetPaymentByIdUseCase;
import com.marco.rentflow.core.application.usecase.payment.InitiatePaymentCheckoutUseCase;
import com.marco.rentflow.core.application.usecase.payment.ListPaymentsByContractUseCase;
import com.marco.rentflow.core.application.usecase.payment.PaymentWebhookDispatcher;
import com.marco.rentflow.core.domain.payment.PaymentRecord;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentCheckoutRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.dto.PaymentWebhookRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.payment.mapper.PaymentRestMapper;
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
import java.util.List;
import java.util.UUID;

// import static java.lang.reflect.Array.get; ESTE me daba error!
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private InitiatePaymentCheckoutUseCase checkoutUseCase;

    @MockBean
    private PaymentWebhookDispatcher webhookDispatcher;

    @MockBean private PaymentRestMapper paymentRestMapper;
    @MockBean private GetPaymentByIdUseCase getPaymentByIdUseCase;
    @MockBean private ListPaymentsByContractUseCase listPaymentsByContractUseCase;

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

        when(checkoutUseCase.execute(any(), any(), any(), any())).thenReturn(mockResult);

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

    @Test
    void shouldReturnPayment_whenPaymentExists() throws Exception {
        UUID id = UUID.randomUUID();
        PaymentRecord payment = mock(PaymentRecord.class);
        PaymentResponseDTO dto = mock(PaymentResponseDTO.class);

        when(getPaymentByIdUseCase.execute(id)).thenReturn(payment);
        when(paymentRestMapper.toResponseDto(payment)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/payments/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnPaymentsList_whenContractHasPayments() throws Exception {
        UUID contractId = UUID.randomUUID();
        List<PaymentRecord> payments = List.of(mock(PaymentRecord.class), mock(PaymentRecord.class));
        List<PaymentResponseDTO> dtos = List.of(mock(PaymentResponseDTO.class), mock(PaymentResponseDTO.class));

        when(listPaymentsByContractUseCase.execute(contractId)).thenReturn(payments);
        when(paymentRestMapper.toResponseDto(any(PaymentRecord.class)))
                .thenReturn(dtos.get(0))
                .thenReturn(dtos.get(1));

        mockMvc.perform(get("/api/v1/payments/contract/{contractId}", contractId))
                .andExpect(status().isOk());
    }

}
