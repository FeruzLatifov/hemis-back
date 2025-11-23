-- =====================================================
-- V001: CREATE USERS TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Core authentication - users table only
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
    entity_code VARCHAR(255),
    university_id VARCHAR(255),
    phone VARCHAR(50),

    -- Legacy CUBA Relations
    group_id UUID,
    group_names VARCHAR(255),

    -- Account Status
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INTEGER DEFAULT 0,

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

-- Indexes
CREATE INDEX idx_users_username_lowercase ON users(username_lowercase);
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_entity_code ON users(entity_code) WHERE entity_code IS NOT NULL;
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_active ON users(active) WHERE active = TRUE;
CREATE INDEX idx_users_user_type ON users(user_type);
