-- =====================================================
-- V003: CREATE PERMISSION TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Fine-grained permissions for RBAC
-- =====================================================

CREATE TABLE permission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL DEFAULT 'CUSTOM',

    -- Audit (AuditableEntity: 7)
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    CONSTRAINT chk_permission_category CHECK (
        category IN ('CORE', 'ADMIN', 'MENU', 'CUSTOM', 'REPORTS')
    ),
    CONSTRAINT chk_permission_action CHECK (
        action IN ('view', 'create', 'edit', 'delete', 'export', 'import', 'manage', 'access', 'sync', 'approve')
    )
);

CREATE INDEX idx_permission_resource ON permission(resource);
CREATE INDEX idx_permission_category ON permission(category);
CREATE INDEX idx_permission_resource_action ON permission(resource, action);
CREATE INDEX idx_permission_deleted ON permission(deleted_at) WHERE deleted_at IS NULL;

-- Partial UNIQUE: soft-delete'dan keyin code qayta ishlatilishi mumkin
CREATE UNIQUE INDEX uq_permission_code ON permission(code) WHERE deleted_at IS NULL;
