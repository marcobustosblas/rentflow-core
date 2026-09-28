package com.marco.rentflow.core.domain.contract.ports.out;

import com.marco.rentflow.core.domain.contract.TenantInvitation;

import java.util.Optional;

public interface TenantInvitationRepository {

    TenantInvitation save(TenantInvitation invitation);
    Optional<TenantInvitation> findByTokenHash(String tokenHash);

}
