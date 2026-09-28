package com.marco.rentflow.core.domain.user;

public enum UserStatus {
    /** Registered but email not yet verified. Cannot operate. */
    PENDING_VERIFICATION,

    /** Fully operational. Can use the platform according to its roles. */
    ACTIVE,

    /** Blocked by a PLATFORM_ADMIN. Reversible via reactivate(). */
    SUSPENDED,

    /** Soft-deleted. Terminal state. Row kept in DB for audit. */
    DELETED
}
