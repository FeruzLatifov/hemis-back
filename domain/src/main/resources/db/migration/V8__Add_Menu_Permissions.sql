-- =====================================================
-- HEMIS Backend - Menu Permissions
-- =====================================================
-- Version: V8
-- Purpose: Add menu-specific permissions for dynamic menu system
--
-- Permissions Added:
-- - dashboard.view
-- - students.view, teachers.view, universities.view
-- - reports.view, reports.students.view, reports.teachers.view
--
-- Role Assignments:
-- - All roles get dashboard.view
-- - SUPER_ADMIN gets all menu permissions
-- =====================================================

-- =====================================================
-- Add Menu Permissions
-- =====================================================

-- Dashboard permission
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'dashboard', 'view', 'dashboard.view', 'View Dashboard', 'Access to main dashboard', 'CORE', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Students permission
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'students', 'view', 'students.view', 'View Students', 'View student list and details', 'CORE', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Teachers permission
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'teachers', 'view', 'teachers.view', 'View Teachers', 'View teacher list and details', 'CORE', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Universities permission
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'universities', 'view', 'universities.view', 'View Universities', 'View university list', 'CORE', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Reports permissions
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
(gen_random_uuid(), 'reports', 'view', 'reports.view', 'View Reports', 'Access to reports section', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.students', 'view', 'reports.students.view', 'View Student Reports', 'View student-related reports', 'REPORTS', CURRENT_TIMESTAMP),
(gen_random_uuid(), 'reports.teachers', 'view', 'reports.teachers.view', 'View Teacher Reports', 'View teacher-related reports', 'REPORTS', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- =====================================================
-- Assign Permissions to Roles
-- =====================================================

-- Assign dashboard permission to ALL active roles
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE p.code = 'dashboard.view'
  AND r.deleted_at IS NULL
  AND p.deleted_at IS NULL
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Assign ALL menu permissions to SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN (
    'dashboard.view',
    'students.view',
    'teachers.view',
    'universities.view',
    'reports.view',
    'reports.students.view',
    'reports.teachers.view'
  )
  AND r.deleted_at IS NULL
  AND p.deleted_at IS NULL
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- =====================================================
-- Verification Query (commented out)
-- =====================================================
-- SELECT
--   r.code as role_code,
--   r.name as role_name,
--   COUNT(p.id) as permission_count
-- FROM roles r
-- LEFT JOIN role_permissions rp ON r.id = rp.role_id
-- LEFT JOIN permissions p ON rp.permission_id = p.id
-- WHERE r.deleted_at IS NULL
-- GROUP BY r.id, r.code, r.name
-- ORDER BY r.code;

-- =====================================================
-- Migration Complete
-- =====================================================
-- Permissions added: 7 (dashboard, students, teachers, universities, reports + 2 report sub-items)
-- SUPER_ADMIN: Granted all 7 menu permissions
-- All roles: Granted dashboard.view permission
-- =====================================================
