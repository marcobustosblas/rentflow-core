package com.marco.rentflow.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marco.rentflow.core.application.usecase.contract.GetInvitationUseCase;
import com.marco.rentflow.core.application.usecase.contract.InvitationPreviewResponse;
import com.marco.rentflow.core.application.usecase.contract.RegisterTenantCommand;
import com.marco.rentflow.core.application.usecase.contract.RegisterTenantUseCase;
import com.marco.rentflow.core.application.usecase.user.RegisterUserUseCase;
import com.marco.rentflow.core.domain.contract.exception.InvalidInvitationException;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.UserStatus;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.AuthController;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.dto.InvitationPreviewDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.dto.TenantRegistrationRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.mapper.AuthRestMapper;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.user.mapper.UserRestMapper;
import com.marco.rentflow.infrastructure.config.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RegisterUserUseCase registerUserUseCase;
    @MockBean private RegisterTenantUseCase registerTenantUseCase;
    @MockBean private GetInvitationUseCase getInvitationUseCase;
    @MockBean private UserRestMapper userRestMapper;
    @MockBean private AuthRestMapper authRestMapper;

    // -------- POST /register (Landlord) --------

    @Test
    void shouldRegisterLandlord_whenRequestIsValid() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "Marco Bustos", "marco@mail.com", "password123",
                "12345678-9", "+56912345678", "LANDLORD");

        User created = User.reconstitute(
                UUID.randomUUID(),
                "Marco Bustos",
                "marco@mail.com",
                "hash",
                "12345678-9",
                "+56912345678",
                Set.of(Role.LANDLORD),
                UserStatus.PENDING_VERIFICATION,
                LocalDateTime.now(), LocalDateTime.now());

        UserResponseDTO responseDto = new UserResponseDTO(
                created.getId(),
                created.getFullName(),
                created.getEmail(),
                created.getRut(),
                created.getPhoneNumber(),
                created.getStatus().name(),
                created.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                created.getCreatedAt()
        );

        when(registerUserUseCase.execute(
                eq("Marco Bustos"), eq("marco@mail.com"), eq("password123"),
                eq("12345678-9"), eq("+56912345678"), eq(Role.LANDLORD)))
                .thenReturn(created);
        when(userRestMapper.toResponseDTO(created)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("marco@mail.com"))
                .andExpect(jsonPath("$.roles[0]").value("LANDLORD"));
    }

    // -------- GET /invitations/{token} --------

    @Test
    void shouldReturnPreview_whenInvitationIsValid() throws Exception {
        String rawToken = "abc-123";

        InvitationPreviewResponse preview = new InvitationPreviewResponse(
                "juan@mail.com",
                "Juan Pérez",
                "12345678-9",
                "Av. Providencia 1234",
                new BigDecimal("500000"),
                "CLP",
                new BigDecimal("500000"),
                5,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusYears(1),
                LocalDateTime.now().plusDays(7));

        InvitationPreviewDTO dto = new InvitationPreviewDTO(
                preview.tenantEmail(), preview.tenantFullName(), preview.tenantRut(),
                preview.propertyAddress(), preview.monthlyRent(), preview.currency(),
                preview.depositAmount(), preview.paymentDueDay(),
                preview.startDate(), preview.endDate(), preview.expiresAt());

        when(getInvitationUseCase.execute(eq(rawToken))).thenReturn(preview);
        when(authRestMapper.toPreviewDto(preview)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/auth/invitations/{token}", rawToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantEmail").value("juan@mail.com"))
                .andExpect(jsonPath("$.tenantFullName").value("Juan Pérez"))
                .andExpect(jsonPath("$.propertyAddress").value("Av. Providencia 1234"))
                .andExpect(jsonPath("$.monthlyRent").value(500000))
                .andExpect(jsonPath("$.currency").value("CLP"));
    }

    @Test
    void shouldReturnError_whenTokenIsInvalid() throws Exception {
        when(getInvitationUseCase.execute(any()))
                .thenThrow(new InvalidInvitationException("Invalid invitation token."));

        mockMvc.perform(get("/api/v1/auth/invitations/{token}", "bad"))
                .andExpect(status().is4xxClientError());
    }

    // ---------- POST /register/tenant ----------

    @Test
    void shouldRegisterTenant_whenRequestIsValid() throws Exception {
        TenantRegistrationRequestDTO request = new TenantRegistrationRequestDTO(
                "abc-123", "Juan Pérez", "12345678-9", "+56912345678",
                "password123", true);

        RegisterTenantCommand expectedCommand = new RegisterTenantCommand(
                "abc-123", "Juan Pérez", "12345678-9", "+56912345678", "password123", true);

        User created = User.reconstitute(
                UUID.randomUUID(),
                "Juan Pérez",
                "juan@mail.com",
                "hash",
                "12345678-9",
                "+56912345678",
                Set.of(Role.TENANT),
                UserStatus.PENDING_VERIFICATION,
                LocalDateTime.now(), LocalDateTime.now());

        UserResponseDTO responseDto = new UserResponseDTO(
                created.getId(),
                created.getFullName(),
                created.getEmail(),
                created.getRut(),
                created.getPhoneNumber(),
                created.getStatus().name(),
                created.getRoles().stream().map(Enum::name).collect(Collectors.toSet()),
                created.getCreatedAt()
        );

        // Aquí falló el reconstitute del User.

        when(authRestMapper.toCommand(any(TenantRegistrationRequestDTO.class)))
                .thenReturn(expectedCommand);
        when(registerTenantUseCase.execute(expectedCommand)).thenReturn(created);
        when(userRestMapper.toResponseDTO(created)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/auth/register/tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan@mail.com"))
                .andExpect(jsonPath("$.roles[0]").value("TENANT"));
    }

}
