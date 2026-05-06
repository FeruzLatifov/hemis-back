-- =====================================================
-- S002: SEED BASE PERMISSIONS (30 permissions)
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-15
-- Purpose: Core business + admin permissions for the base system.
--          These are the foundational CRUD/view permissions for
--          all major resources (dashboard, students, teachers,
--          universities, reports, users, roles, permissions, settings).
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE SET)
-- Merged from: S002 (15 CORE) + S003 (15 ADMIN)
-- =====================================================

-- =====================================================
-- CORE PERMISSIONS (15)
-- =====================================================

-- Dashboard (1)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('dashboard', 'view', 'dashboard.view', 'View Dashboard', 'Access to main dashboard and statistics overview', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- Students (5)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students', 'view', 'students.view', 'View Students', 'View student list and detailed information', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students', 'create', 'students.create', 'Create Students', 'Add new students to the system', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students', 'edit', 'students.edit', 'Edit Students', 'Modify existing student information', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students', 'delete', 'students.delete', 'Delete Students', 'Soft delete students (mark as deleted)', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('students', 'export', 'students.export', 'Export Students', 'Export student data to Excel/CSV', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- Teachers (5)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'view', 'teachers.view', 'View Teachers', 'View teacher list and detailed information', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'create', 'teachers.create', 'Create Teachers', 'Add new teachers to the system', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'edit', 'teachers.edit', 'Edit Teachers', 'Modify existing teacher information', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'delete', 'teachers.delete', 'Delete Teachers', 'Soft delete teachers', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'export', 'teachers.export', 'Export Teachers', 'Export teacher data to Excel/CSV', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- Universities (1 CORE)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'view', 'universities.view', 'View Universities', 'View university list and information', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- Reports (3)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports', 'view', 'reports.view', 'View Reports', 'Access to reports section', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports', 'create', 'reports.create', 'Create Reports', 'Generate new reports', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('reports', 'export', 'reports.export', 'Export Reports', 'Export reports to various formats', 'CORE', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- ADMIN PERMISSIONS (15)
-- =====================================================

-- Universities Admin (3)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'create', 'universities.create', 'Create Universities', 'Add new universities (Ministry only)', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'edit', 'universities.edit', 'Edit Universities', 'Modify university information', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'delete', 'universities.delete', 'Delete Universities', 'Soft delete universities', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- Users Admin (4)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('users', 'view', 'users.view', 'View Users', 'View system users', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('users', 'create', 'users.create', 'Create Users', 'Add new users to system', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('users', 'edit', 'users.edit', 'Edit Users', 'Modify user information', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('users', 'delete', 'users.delete', 'Delete Users', 'Deactivate users', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- Roles Admin (4)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'view', 'roles.view', 'View Roles', 'View system roles', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'create', 'roles.create', 'Create Roles', 'Create new roles', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'edit', 'roles.edit', 'Edit Roles', 'Modify role information', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('roles', 'delete', 'roles.delete', 'Delete Roles', 'Remove roles', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- Permissions Admin (2)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('permissions', 'view', 'permissions.view', 'View Permissions', 'View all permissions', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('permissions', 'manage', 'permissions.manage', 'Manage Permissions', 'Assign permissions to roles', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- System Settings Admin (2)
INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('settings', 'view', 'settings.view', 'View Settings', 'View system settings', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

INSERT INTO permission (resource, action, code, name, description, category, created_by)
VALUES ('settings', 'edit', 'settings.edit', 'Edit Settings', 'Modify system settings', 'ADMIN', 'system')
ON CONFLICT (code) WHERE deleted_at IS NULL DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, resource = EXCLUDED.resource,
    action = EXCLUDED.action, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP, updated_by = 'system';

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    total_count INTEGER;
    core_count INTEGER;
    admin_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO core_count FROM permission WHERE category = 'CORE';
    SELECT COUNT(*) INTO admin_count FROM permission WHERE category = 'ADMIN';
    total_count := core_count + admin_count;

    IF total_count < 30 THEN
        RAISE WARNING 'S002: Expected >= 30 base permissions, found % (CORE=%, ADMIN=%)', total_count, core_count, admin_count;
    ELSE
        RAISE NOTICE 'S002: % base permissions seeded (CORE=%, ADMIN=%)', total_count, core_count, admin_count;
    END IF;
END $$;
