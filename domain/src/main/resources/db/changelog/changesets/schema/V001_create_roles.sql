-- =====================================================
-- V002: CREATE ROLE TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Role-based access control
-- =====================================================

CREATE TABLE role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    role_type VARCHAR(50) NOT NULL DEFAULT 'CUSTOM',
    active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit (AuditableEntity: 7)
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    CONSTRAINT chk_role_type CHECK (
        role_type IN ('SYSTEM', 'UNIVERSITY', 'CUSTOM')
    )
);

CREATE INDEX idx_role_active ON role(active) WHERE active = TRUE;
CREATE INDEX idx_role_type ON role(role_type);
CREATE INDEX idx_role_deleted ON role(deleted_at) WHERE deleted_at IS NULL;
