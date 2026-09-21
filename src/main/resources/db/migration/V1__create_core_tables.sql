-- V1__create_core_tables.sql

-- 1. USERS

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    rut VARCHAR(20),
    phone_number VARCHAR(50),
    roles VARCHAR(255) NOT NULL, -- Guardará "LANDLORD" o "LANDLORD,TENANT"
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. SUBSCRIPTIONS
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    landlord_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_type VARCHAR(50) NOT NULL,
    billing_cycle VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    max_properties INT NOT NULL,
    max_storage_mb INT NOT NULL,
    current_period_start TIMESTAMP NOT NULL, -- Agregado
    current_period_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. BANK ACCOUNTS
CREATE TABLE bank_accounts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_type VARCHAR(50) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    rut VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. PROPERTIES
CREATE TABLE properties (
    id UUID PRIMARY KEY,
    landlord_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    payout_account_id UUID REFERENCES bank_accounts(id) ON DELETE SET NULL,
    address VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    base_price NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL, -- Agregado para el Value Object Money
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. CONTRACTS
CREATE TABLE contracts (
    id UUID PRIMARY KEY,
    property_id UUID NOT NULL REFERENCES properties(id) ON DELETE RESTRICT,
    tenant_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    due_day INT NOT NULL CHECK (due_day >= 1 AND due_day <= 31),
    rent_amount NUMERIC(19, 4) NOT NULL, -- Agregado
    deposit_amount NUMERIC(19, 4) NOT NULL, -- Agregado
    currency VARCHAR(3) NOT NULL, -- Agregado
    daily_penalty NUMERIC(19, 4) NOT NULL,
    last_readjustment_date DATE, -- Agregado
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. PAYMENTS
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    reference_id UUID NOT NULL, -- [ID POLIMÓRFICO]: UUID de la entidad origen. Si target='RENT', es el id del Contrato. Si target='SAAS', es el id de la Suscripción. NO tiene Foreign Key restrictiva.
    payment_target VARCHAR(50) NOT NULL, -- [ENRUTADOR DE NEGOCIO]: Define el contexto exacto ('RENT' o 'SAAS'). Le dicta al backend a qué tabla pertenece el reference_id
    status VARCHAR(50) NOT NULL,
    expected_amount NUMERIC(19, 4) NOT NULL, -- Agregado: La Fuente de la Verdad (Zero Trust)
    amount_paid NUMERIC(19, 4),
    currency VARCHAR(3) NOT NULL, -- Agregado
    late_fee_applied NUMERIC(19, 4) DEFAULT 0,
    due_date DATE, -- Agregado: Fecha límite original
    payment_date TIMESTAMP, -- Removido el TIME ZONE para encajar perfecto con LocalDateTime
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    transaction_reference VARCHAR(255), -- Agregado: Referencia de pasarela
    payment_receipt_url VARCHAR(500), -- Agregado: URL del recibo
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);