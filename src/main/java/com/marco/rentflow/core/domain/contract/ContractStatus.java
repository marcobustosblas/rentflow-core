package com.marco.rentflow.core.domain.contract;

public enum ContractStatus {
    /**
     * Contract registered by the landlord with all legal data.
     * Waiting for the tenant to accept the invitation and create their account.
     * The property is marked as rented but the tenant has no access yet.
     */
    PENDING_TENANT_SIGNUP,

    /**
     * Tenant has accepted the invitation and has access to the contract.
     * The contract is operational: rent is due, payments can be made.
     */
    ACTIVE,

    /**
     * Contract terminated early by mutual agreement or eviction.
     * Used when the relationship ends before the natural end date.
     */
    TERMINATED,

    /**
     * Contract reached its natural end date (endDate passed).
     * Used when the relationship ends as planned.
     */
    EXPIRED
}
