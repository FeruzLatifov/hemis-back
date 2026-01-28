-- =====================================================
-- V005: CREATE ROLE_PERMISSIONS JUNCTION TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Many-to-many relationship between roles and permissions
-- =====================================================

CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(50),

    -- Composite Primary Key
    PRIMARY KEY (role_id, permission_id),

    -- Foreign Keys
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Comments
COMMENT ON TABLE role_permissions IS 'Junction table for role-permission assignments';

-- Indexes for JOIN performance
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
