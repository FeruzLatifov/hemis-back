-- =====================================================
-- S004: SEED ROLE-PERMISSION MAPPINGS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-15
-- Purpose: Assign permissions to the 5 system roles.
--          All role-permission assignments consolidated here.
-- Strategy: IDEMPOTENT (ON CONFLICT DO NOTHING for junction table)
-- Merged from: S004 (base) + S003b STEP 9 + S009 + S016 role assignments
-- =====================================================
-- Roles: SUPER_ADMIN, MINISTRY_ADMIN, UNIVERSITY_ADMIN, VIEWER, REPORT_VIEWER
-- =====================================================

-- =====================================================
-- SUPER_ADMIN: ALL permissions (full access)
-- =====================================================
-- Simple CROSS JOIN — SUPER_ADMIN gets every permission in the system.
-- This covers all 84 permissions (30 base + 54 feature).
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- =====================================================
-- MINISTRY_ADMIN: Core business + admin user management
-- =====================================================
-- From S004: All CORE category permissions (S002 base CORE)
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MINISTRY_ADMIN'
  AND p.category = 'CORE'
ON CONFLICT DO NOTHING;

-- From S016: users.manage + users.view for user administration
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MINISTRY_ADMIN'
  AND p.code IN ('users.manage', 'users.view')
ON CONFLICT DO NOTHING;

-- Menu structure + audit logs access
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MINISTRY_ADMIN'
  AND p.code IN ('system.menu.view', 'audit.view')
ON CONFLICT DO NOTHING;

-- =====================================================
-- UNIVERSITY_ADMIN: Scoped core permissions + user management
-- =====================================================
-- From S004: CORE permissions for dashboard, students, teachers, reports
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'UNIVERSITY_ADMIN'
  AND p.category = 'CORE'
  AND p.resource IN ('dashboard', 'students', 'teachers', 'reports')
ON CONFLICT DO NOTHING;

-- From S016: users.view + users.manage for university-scoped user admin
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'UNIVERSITY_ADMIN'
  AND p.code IN ('users.view', 'users.manage')
ON CONFLICT DO NOTHING;

-- Menu structure access (navigation)
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'UNIVERSITY_ADMIN'
  AND p.code = 'system.menu.view'
ON CONFLICT DO NOTHING;

-- =====================================================
-- VIEWER: View-only permissions across most modules
-- =====================================================
-- From S004: Base safe view permissions + menu structure
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.code IN (
    'dashboard.view', 'students.view', 'teachers.view',
    'universities.view', 'reports.view', 'system.menu.view'
  )
ON CONFLICT DO NOTHING;

-- From S003b: Specific menu permissions for OTM_ADMIN/VIEWER
-- Students (7), Teachers (4), Science (6), select Reports (3)
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.code IN (
    -- Students (7)
    'students.view', 'students.list.view', 'students.directions.view', 'students.groups.view',
    'students.diplomas.view', 'students.scholarships.view', 'students.certificates.view',
    -- Teachers (4)
    'teachers.view', 'teachers.list.view', 'teachers.positions.view', 'teachers.qualifications.view',
    -- Science (6)
    'science.view', 'science.researchers.view', 'science.projects.view',
    'science.publications.view', 'science.methodical.view', 'science.intellectual.view',
    -- Select Reports
    'reports.students.view', 'reports.teachers.view', 'reports.academic.view'
  )
ON CONFLICT DO NOTHING;

-- From S003b: All *.view permissions for institutions, students, teachers, science, reports, rating
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.action = 'view'
  AND (
    p.code LIKE 'institutions.%'
    OR p.code LIKE 'students.%'
    OR p.code LIKE 'teachers.%'
    OR p.code LIKE 'science.%'
    OR p.code LIKE 'reports.%'
    OR p.code LIKE 'rating.%'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- REPORT_VIEWER: Reports, rating, and institutions
-- =====================================================
-- From S004: All reports resource permissions + menu structure
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'REPORT_VIEWER'
  AND (p.resource = 'reports' OR p.code = 'system.menu.view')
ON CONFLICT DO NOTHING;

-- From S003b: Explicit institutions, reports sub-menus, rating
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'REPORT_VIEWER'
  AND p.code IN (
    -- Institutions (5)
    'institutions.view', 'institutions.universities.view', 'institutions.faculties.view',
    'institutions.departments.view', 'institutions.attached-specialities.view',
    -- Reports (7)
    'reports.view', 'reports.students.view', 'reports.teachers.view', 'reports.institutions.view',
    'reports.academic.view', 'reports.research.view', 'reports.economic.view',
    -- Rating (5)
    'rating.view', 'rating.administrative.view', 'rating.academic.view',
    'rating.scientific.view', 'rating.gpa.view'
  )
ON CONFLICT DO NOTHING;

-- From S003b: Pattern-based catch-all for reports.*, rating.*, institutions.*
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'REPORT_VIEWER'
  AND (
    p.code LIKE 'reports.%'
    OR p.code LIKE 'rating.%'
    OR p.code LIKE 'institutions.%'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    total_mappings INTEGER;
    sa_count INTEGER;
    ma_count INTEGER;
    ua_count INTEGER;
    vw_count INTEGER;
    rv_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_mappings FROM role_permissions WHERE assigned_by = 'system';

    SELECT COUNT(*) INTO sa_count FROM role_permissions rp
        JOIN roles r ON rp.role_id = r.id WHERE r.code = 'SUPER_ADMIN' AND rp.assigned_by = 'system';
    SELECT COUNT(*) INTO ma_count FROM role_permissions rp
        JOIN roles r ON rp.role_id = r.id WHERE r.code = 'MINISTRY_ADMIN' AND rp.assigned_by = 'system';
    SELECT COUNT(*) INTO ua_count FROM role_permissions rp
        JOIN roles r ON rp.role_id = r.id WHERE r.code = 'UNIVERSITY_ADMIN' AND rp.assigned_by = 'system';
    SELECT COUNT(*) INTO vw_count FROM role_permissions rp
        JOIN roles r ON rp.role_id = r.id WHERE r.code = 'VIEWER' AND rp.assigned_by = 'system';
    SELECT COUNT(*) INTO rv_count FROM role_permissions rp
        JOIN roles r ON rp.role_id = r.id WHERE r.code = 'REPORT_VIEWER' AND rp.assigned_by = 'system';

    RAISE NOTICE 'S004: % total role-permission mappings (SUPER_ADMIN=%, MINISTRY_ADMIN=%, UNIVERSITY_ADMIN=%, VIEWER=%, REPORT_VIEWER=%)',
        total_mappings, sa_count, ma_count, ua_count, vw_count, rv_count;
END $$;
