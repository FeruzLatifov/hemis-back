-- =====================================================
-- S002: SEED CORE PERMISSIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Core business permissions (dashboard, students, teachers, etc.)
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE)
-- =====================================================

-- Dashboard
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('dashboard', 'view', 'dashboard.view', 'View Dashboard', 'Access to main dashboard and statistics overview', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Students
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('students', 'view', 'students.view', 'View Students', 'View student list and detailed information', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('students', 'create', 'students.create', 'Create Students', 'Add new students to the system', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('students', 'edit', 'students.edit', 'Edit Students', 'Modify existing student information', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('students', 'delete', 'students.delete', 'Delete Students', 'Soft delete students (mark as deleted)', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('students', 'export', 'students.export', 'Export Students', 'Export student data to Excel/CSV', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Teachers
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'view', 'teachers.view', 'View Teachers', 'View teacher list and detailed information', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'create', 'teachers.create', 'Create Teachers', 'Add new teachers to the system', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'edit', 'teachers.edit', 'Edit Teachers', 'Modify existing teacher information', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'delete', 'teachers.delete', 'Delete Teachers', 'Soft delete teachers', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('teachers', 'export', 'teachers.export', 'Export Teachers', 'Export teacher data to Excel/CSV', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Universities
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('universities', 'view', 'universities.view', 'View Universities', 'View university list and information', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Reports
INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('reports', 'view', 'reports.view', 'View Reports', 'Access to reports section', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('reports', 'create', 'reports.create', 'Create Reports', 'Generate new reports', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

INSERT INTO permissions (resource, action, code, name, description, category, created_by)
VALUES ('reports', 'export', 'reports.export', 'Export Reports', 'Export reports to various formats', 'CORE', 'system')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name, description = EXCLUDED.description, category = EXCLUDED.category, updated_at = CURRENT_TIMESTAMP;

-- Verification
DO $$
DECLARE
    perm_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO perm_count FROM permissions WHERE category = 'CORE';
    RAISE NOTICE 'S002: % CORE permissions seeded', perm_count;
END $$;
