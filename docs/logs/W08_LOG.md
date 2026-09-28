# Week 8 Development Log: Domain Hardening & Invitation Persistence

## Objectives
* Harden the `User` and `RentalContract` aggregates with strict business invariants.
* Introduce the `TenantInvitation` model to support secure tenant onboarding.
* Update the relational schema with defense-in-depth constraints.

## Executed Tasks
1. **User Aggregate Hardening:** Refactored `User` with explicit role/status lifecycles 
and blocked privileged self-registration at the Application layer.
2. **Contract Refactoring:** Aligned `RentalContract` with the new registration model using
`TenantInfo` and `FinancialTerms` Value Objects. Added `ContractSource`.
3. **Database Schema Update:** Applied Flyway migration `V2__add_tenant_invitations.sql` implementing
`CHECK`, `NOT NULL`, and `UNIQUE` constraints to enforce domain rules at the database level.
4. **Persistence Adapters:** Implemented `TenantInvitationJpaEntity`, mappers, and 
repository adapters to support the new invitation flow.