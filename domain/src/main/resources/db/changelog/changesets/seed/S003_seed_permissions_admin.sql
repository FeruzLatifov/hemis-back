-- =====================================================
-- S003: SEED ADMIN PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Administrative permissions (users, roles, settings)
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE)
-- =====================================================

-- Universities (Admin)
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'create', 'universities.create', 'Create Universities', 'Add new universities (Ministry only)', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'edit', 'universities.edit', 'Edit Universities', 'Modify university information', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'delete', 'universities.delete', 'Delete Universities', 'Soft delete universities', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Users (Admin)
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('users', 'view', 'users.view', 'View Users', 'View system users', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('users', 'create', 'users.create', 'Create Users', 'Add new users to system', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('users', 'edit', 'users.edit', 'Edit Users', 'Modify user information', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('users', 'delete', 'users.delete', 'Delete Users', 'Deactivate users', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Roles (Admin)
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'view', 'roles.view', 'View Roles', 'View system roles', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'create', 'roles.create', 'Create Roles', 'Create new roles', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'edit', 'roles.edit', 'Edit Roles', 'Modify role information', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'delete', 'roles.delete', 'Delete Roles', 'Remove roles', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Permissions (Admin)
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('permissions', 'view', 'permissions.view', 'View Permissions', 'View all permissions', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('permissions', 'manage', 'permissions.manage', 'Manage Permissions', 'Assign permissions to roles', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- System Settings (Admin)
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('settings', 'view', 'settings.view', 'View Settings', 'View system settings', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('settings', 'edit', 'settings.edit', 'Edit Settings', 'Modify system settings', 'ADMIN', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Verification
DO $$
DECLARE
    perm_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO perm_count FROM permissions WHERE category = 'ADMIN';
    RAISE NOTICE 'S003: % ADMIN permissions seeded', perm_count;
END $$;
