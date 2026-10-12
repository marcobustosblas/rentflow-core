package com.marco.rentflow.infrastructure.adapters.in.web.auth;

import com.marco.rentflow.core.application.usecase.contract.GetInvitationUseCase;
import com.marco.rentflow.core.application.usecase.contract.InvitationPreviewResponse;
import com.marco.rentflow.core.application.usecase.contract.RegisterTenantCommand;
import com.marco.rentflow.core.application.usecase.contract.RegisterTenantUseCase;
import com.marco.rentflow.core.application.usecase.user.RegisterUserUseCase;
import com.marco.rentflow.core.domain.user.Role;
import com.marco.rentflow.core.domain.user.User;
import com.marco.rentflow.core.domain.user.exception.InvalidRoleException;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.dto.InvitationPreviewDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.dto.TenantRegistrationRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.mapper.AuthRestMapper;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserRequestDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.user.dto.UserResponseDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.user.mapper.UserRestMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final RegisterTenantUseCase registerTenantUseCase;
    private final GetInvitationUseCase getInvitationUseCase;
    private final UserRestMapper userRestMapper;
    private final AuthRestMapper authRestMapper;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          RegisterTenantUseCase registerTenantUseCase,
                          GetInvitationUseCase getInvitationUseCase,
                          UserRestMapper userRestMapper,
                          AuthRestMapper authRestMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.registerTenantUseCase = registerTenantUseCase;
        this.getInvitationUseCase = getInvitationUseCase;
        this.userRestMapper = userRestMapper;
        this.authRestMapper = authRestMapper;
    }

    @PostMapping("/register")
    // TODO(M5-security): @PreAuthorize("isAnonymous()")
    public ResponseEntity<UserResponseDTO> registerLandlord(@Valid @RequestBody UserRequestDTO request) {

        /* Zero-Trust: Intercepto cualquier intento de inyectar roles privilegiados o de inquilino */
        if (!"LANDLORD".equalsIgnoreCase(request.role())) {
            throw new InvalidRoleException("Public registration is strictly restricted to LANDLORD accounts.");
        }

        User registeredUser = registerUserUseCase.execute(
                request.fullName(),
                request.email(),
                request.password(),
                request.rut(),
                request.phoneNumber(),
                Role.LANDLORD
        );

        UserResponseDTO response = userRestMapper.toResponseDTO(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    // ------- C7: invitation preview (public) -------

    /**
     * Public endpoint. Called by the frontend when the tenant opens
     * the email link. The token travels in the path (not in the query)
     * because it identifies a specific resource.
     *
     * The email, full name and RUT are returned so the frontend can
     * show them as read-only fields. The backend ignores any attempt
     * to override these values during registration.
     */
    @GetMapping("/invitations/{token}")
    // TODO(M5-security): @PreAuthorize("isAnonymous()")
    public ResponseEntity<InvitationPreviewDTO> previewInvitation(@PathVariable("token") String rawToken) {
        InvitationPreviewResponse response = getInvitationUseCase.execute(rawToken);
        return ResponseEntity.ok(authRestMapper.toPreviewDto(response));
    }

    // ------- C7: tenant registration (public) -------

    /**
     * Public endpoint. Accepts the raw token + the data the tenant
     * provides. The email, full name and RUT are read from the
     * invitation on the backend; only password and phoneNumber come
     * from the request body.
     */
    @PostMapping("/register/tenant")
    // TODO(M5-security): @PreAuthorize("isAnonymous()")
    public ResponseEntity<UserResponseDTO> registerTenant(@Valid @RequestBody TenantRegistrationRequestDTO request) {
        RegisterTenantCommand command = authRestMapper.toCommand(request);
        User tenant = registerTenantUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userRestMapper.toResponseDTO(tenant));
    }

}
