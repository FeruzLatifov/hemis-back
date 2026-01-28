-- =====================================================
-- V002: CREATE ROLES TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Role-based access control - roles table only
-- =====================================================

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    role_type VARCHAR(50) NOT NULL DEFAULT 'CUSTOM',
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    -- Versioning
    version INTEGER DEFAULT 1,

    -- Constraints
    CONSTRAINT chk_role_type CHECK (
        role_type IN ('SYSTEM', 'UNIVERSITY', 'CUSTOM')
    )
);

-- Comments
COMMENT ON TABLE roles IS 'System roles for RBAC';
COMMENT ON COLUMN roles.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN roles.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_roles_code ON roles(code);
CREATE INDEX idx_roles_active ON roles(active) WHERE active = TRUE;
CREATE INDEX idx_roles_role_type ON roles(role_type);
CREATE INDEX idx_roles_deleted_at ON roles(deleted_at) WHERE deleted_at IS NULL;
