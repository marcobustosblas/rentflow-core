package com.marco.rentflow.core.domain.contract;

import java.util.Objects;
import java.util.UUID;

/** (25-9-26, 19:15 hrs, Arica)
 * Value Object holding tenant identity data as captured in the legal contract.
 *
 * The tenantId is nullable: it is null when the contract is registered
 * but the tenant has not yet accepted the invitation and created an account.
 * Once the tenant accepts, the RentalContract assigns the tenantId.
 */

public record TenantInfo(
        UUID tenantId,
        String tenantEmail,
        String tenantFullName,
        String tenantRut
) {
    public TenantInfo {
        // tenantId puede ser null
        Objects.requireNonNull(tenantEmail, "Tenant email cannot be null");
        Objects.requireNonNull(tenantFullName, "Tenant full name cannot be null");
        Objects.requireNonNull(tenantRut, "Tenant RUT cannot be null");

        if (tenantEmail.isBlank()) {
            throw new IllegalArgumentException("Tenant email cannot be blank");
        }
        if (!tenantEmail.contains("@")) {
            throw new IllegalArgumentException("Tenant email must be a valid email");
        }
        if (tenantFullName.isBlank()) {
            throw new IllegalArgumentException("Tenant full name cannot be blank");
        }
        if (tenantRut.isBlank()) {
            throw new IllegalArgumentException("Tenant RUT cannot be blank");
        }
    }

    public boolean hasRegisteredTenant() {
        return tenantId != null;
    }



}
