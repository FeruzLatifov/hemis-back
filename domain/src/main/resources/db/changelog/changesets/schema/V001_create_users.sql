-- =====================================================
-- V001: AUTH MODULE — users + password_history + password_reset_token
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Core authentication tables
-- =====================================================

CREATE TABLE users (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Authentication
    username VARCHAR(255) NOT NULL UNIQUE,
    username_lowercase VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    password_encryption VARCHAR(50),
    email VARCHAR(255) UNIQUE,

    -- Personal Information
    name VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    middle_name VARCHAR(255),
    full_name VARCHAR(255),
    position VARCHAR(255),

    -- User Settings
    language VARCHAR(20),
    time_zone VARCHAR(50),
    time_zone_auto BOOLEAN,
    locale VARCHAR(20),

    -- User Context (Multi-tenancy)
    user_type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    university_id VARCHAR(255),
    phone VARCHAR(50),

    -- Person link — connects login account to person registry.
    -- NULL for B2B/service/system accounts.
    -- DEFERRED FK: constraint added in V009_create_employee.sql (employee table must exist first)
    -- Pattern: Banner GOBTPAC.PIDM → SPRIDEN.PIDM
    employee_id UUID,

    -- Legacy CUBA Relations
    group_id UUID,
    group_names VARCHAR(255),

    -- Account Status
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER DEFAULT 0,
    locked_at TIMESTAMP,

    -- Security Settings
    ip_mask VARCHAR(200),
    change_password_at_logon BOOLEAN,

    -- Multi-tenancy
    sys_tenant_id VARCHAR(255),
    dtype VARCHAR(100),

    -- Versioning (Optimistic Locking)
    version INTEGER DEFAULT 1,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    -- Constraints
    CONSTRAINT chk_user_type CHECK (
        user_type IN ('UNIVERSITY', 'MINISTRY', 'ORGANIZATION', 'SYSTEM')
    )
);

-- Comments
COMMENT ON TABLE users IS 'Core user accounts for authentication';
COMMENT ON COLUMN users.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp (null = active)';
COMMENT ON COLUMN users.locked_at IS 'Timestamp when account was locked (auto-unlock after 15 min)';
COMMENT ON COLUMN users.employee_id IS 'Link to employee (person registry). NULL for B2B/service/system accounts. Pattern: Banner GOBTPAC→SPRIDEN';

-- Foreign Keys (old-hemis standartiga mos — sec_user da ham FK bor edi)
ALTER TABLE users ADD CONSTRAINT fk_users_university
    FOREIGN KEY (university_id) REFERENCES hemishe_e_university(code)
    ON DELETE SET NULL ON UPDATE CASCADE;

-- Indexes
CREATE INDEX idx_users_username_lowercase ON users(username_lowercase);
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_university_id ON users(university_id) WHERE university_id IS NOT NULL;
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_active ON users(active) WHERE active = TRUE;
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_employee_id ON users(employee_id) WHERE employee_id IS NOT NULL;

-- =====================================================
-- PASSWORD HISTORY (parol qayta ishlatishni oldini olish)
-- =====================================================
CREATE TABLE password_history (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    created_by    VARCHAR(50)
);

CREATE INDEX idx_password_history_user_id ON password_history(user_id);

-- =====================================================
-- PASSWORD RESET TOKEN (parol tiklash)
-- =====================================================
CREATE TABLE password_reset_token (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used       BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by VARCHAR(50)
);

CREATE INDEX idx_prt_token ON password_reset_token(token);
CREATE INDEX idx_prt_user_expires ON password_reset_token(user_id, expires_at);
CREATE INDEX idx_prt_user_created ON password_reset_token(user_id, created_at);
