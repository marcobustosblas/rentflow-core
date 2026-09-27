-- V2__add_tenant_invitations.sql
-- Adds tenant registration fields to contracts and creates the invitations table.

-- 1. EXTEND contracts table

ALTER TABLE contracts
    ALTER COLUMN tenant_id DROP NOT NULL; -- CRÍTICO: El contrato nace sin inquilino asignado

ALTER TABLE contracts
    ADD COLUMN tenant_email VARCHAR(255) NOT NULL,
    ADD COLUMN tenant_full_name VARCHAR(255) NOT NULL,
    ADD COLUMN tenant_rut VARCHAR(50) NOT NULL,
    ADD COLUMN contract_document_url VARCHAR(2000),
    ADD COLUMN source VARCHAR(50) NOT NULL;

-- Constraints de seguridad para contratos
ALTER TABLE contracts
    ADD CONSTRAINT chk_contract_source
        CHECK (source IN ('MANUAL', 'AI_EXTRACTED')),
    ADD CONSTRAINT chk_contract_dates
        CHECK (end_date > start_date);


-- 2. CREATE tenant_invitations table

CREATE TABLE tenant_invitations (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL,
    landlord_id UUID NOT NULL,
    tenant_email VARCHAR(255) NOT NULL,
    token_hash VARCHAR(500) NOT NULL UNIQUE, -- UNIQUE para evitar colisiones
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_invitation_contract
        FOREIGN KEY (contract_id) REFERENCES contracts(id),
    CONSTRAINT fk_invitation_landlord
        FOREIGN KEY (landlord_id) REFERENCES users(id),
    CONSTRAINT chk_invitation_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'REVOKED'))
);

-- Índices estratégicos
CREATE INDEX idx_tenant_invitations_contract_id ON tenant_invitations(contract_id);
CREATE INDEX idx_tenant_invitations_landlord_id ON tenant_invitations(landlord_id);
CREATE INDEX idx_tenant_invitations_status ON tenant_invitations(status);