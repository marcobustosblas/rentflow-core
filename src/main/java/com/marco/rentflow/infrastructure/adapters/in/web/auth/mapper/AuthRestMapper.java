package com.marco.rentflow.infrastructure.adapters.in.web.auth.mapper;

import com.marco.rentflow.core.application.usecase.contract.InvitationPreviewResponse;
import com.marco.rentflow.core.application.usecase.contract.RegisterTenantCommand;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.dto.InvitationPreviewDTO;
import com.marco.rentflow.infrastructure.adapters.in.web.auth.dto.TenantRegistrationRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class AuthRestMapper {

    /**
     * TenantRegistrationRequestDTO (web) → RegisterTenantCommand (application).
     */
    public RegisterTenantCommand toCommand(TenantRegistrationRequestDTO dto) {
        return new RegisterTenantCommand(
                dto.rawToken(),
                dto.phoneNumber(),
                dto.rawPassword(),
                dto.acceptedTerms()
        );
    }

    public InvitationPreviewDTO toPreviewDto(InvitationPreviewResponse response) {
        return new InvitationPreviewDTO(
                response.tenantEmail(),
                response.tenantFullName(),
                response.tenantRut(),
                response.propertyAddress(),
                response.monthlyRent(),
                response.currency(),
                response.depositAmount(),
                response.paymentDueDay(),
                response.startDate(),
                response.endDate(),
                response.expiresAt()
        );
    }

}
