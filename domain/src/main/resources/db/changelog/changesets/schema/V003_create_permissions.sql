-- =====================================================
-- V003: CREATE PERMISSIONS TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Fine-grained permissions for RBAC
-- =====================================================

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL DEFAULT 'CUSTOM',

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
    CONSTRAINT chk_permission_category CHECK (
        category IN ('CORE', 'ADMIN', 'MENU', 'CUSTOM', 'REPORTS')
    ),
    CONSTRAINT chk_permission_action CHECK (
        action IN ('view', 'create', 'edit', 'delete', 'export', 'import', 'manage', 'access')
    )
);

-- Comments
COMMENT ON TABLE permissions IS 'Fine-grained permissions (resource.action pattern)';
COMMENT ON COLUMN permissions.code IS 'Unique permission code (e.g., students.view)';
COMMENT ON COLUMN permissions.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN permissions.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_permissions_code ON permissions(code);
CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_category ON permissions(category);
CREATE INDEX idx_permissions_resource_action ON permissions(resource, action);
CREATE INDEX idx_permissions_deleted_at ON permissions(deleted_at) WHERE deleted_at IS NULL;
