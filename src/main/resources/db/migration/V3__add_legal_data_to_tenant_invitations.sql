-- V3__add_legal_data_to_tenant_invitations.sql
--
-- Make tenant invitations self-contained with the legal data
-- captured from the signed contract. Previously this data lived
-- only in the contract aggregate; keeping it in the invitation
-- removes the need to load the contract for simple previews.
--
-- The table is empty at this point, so we can enforce NOT NULL
-- directly without a backfill step.

ALTER TABLE tenant_invitations
    ADD COLUMN tenant_full_name VARCHAR(255) NOT NULL,
    ADD COLUMN tenant_rut VARCHAR(50) NOT NULL;
