-- =====================================================
-- S004: SEED ROLE-PERMISSION MAPPINGS
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-15
-- Updated: 2026-03-21 — OTM_API role with full CRUD (old-hemis OTM equivalent)
-- Purpose: Assign permissions to 7 system roles
-- Strategy: IDEMPOTENT (ON CONFLICT DO NOTHING)
--
-- Old-hemis permission mapping:
--   OTM: 919 perms (SCREEN + ENTITY CRUD + SERVICE) → OTM_API: full CRUD
--   Ministry: 420 perms (SCREEN + ENTITY read) → MINISTRY_ADMIN: core + admin
--   Inspeksiya: 166 perms (SCREEN + read) → INSPECTOR: view only
--   ReportAdmin: 963 perms → REPORT_VIEWER: reports + rating
-- =====================================================

-- =====================================================
-- SUPER_ADMIN: ALL permissions (full access)
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- =====================================================
-- OTM_API: Full CRUD — old-hemis "OTM" role equivalent
-- This is what Univer uses for B2B sync (258 universities)
-- Old-hemis OTM had: entity CRUD + REST API enabled + all screens
-- =====================================================
-- Give ALL permissions (same as old-hemis OTM which had 919 perms)
-- OTM user needs: create/read/update/delete students, teachers, departments, etc.
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'OTM_API'
  AND (
    -- Students: full CRUD (old-hemis: hemishe_EStudent:create/read/update/delete = 1)
    p.code LIKE 'students.%'
    -- Teachers: full CRUD
    OR p.code LIKE 'teachers.%'
    -- Institutions: full access
    OR p.code LIKE 'institutions.%'
    -- Universities: view (OTM sees own university)
    OR p.code LIKE 'universities.%'
    -- Science: full access
    OR p.code LIKE 'science.%'
    -- Reports: view + create + export
    OR p.code LIKE 'reports.%'
    -- Rating: view
    OR p.code LIKE 'rating.%'
    -- Classifiers: view + edit
    OR p.code LIKE 'classifiers.%'
    -- Dashboard
    OR p.code = 'dashboard.view'
    -- Menu navigation
    OR p.code = 'system.menu.view'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- MINISTRY_ADMIN: Core business + admin (old-hemis: Ministry)
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'MINISTRY_ADMIN'
  AND (
    p.category = 'CORE'
    OR p.code IN (
      'users.manage', 'users.view', 'users.create', 'users.edit', 'users.delete',
      'roles.manage', 'roles.view', 'roles.create', 'roles.edit', 'roles.delete',
      'permissions.manage', 'permissions.view',
      'system.menu.view', 'system.menus.manage',
      'system.translation.view', 'system.translation.manage',
      'system.users.view', 'system.view',
      'audit.view', 'settings.view', 'settings.edit'
    )
  )
ON CONFLICT DO NOTHING;

-- Ministry also gets all view permissions
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'MINISTRY_ADMIN' AND p.action = 'view'
ON CONFLICT DO NOTHING;

-- =====================================================
-- INSPECTOR: View-only across all modules (old-hemis: Inspeksiya)
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'INSPECTOR'
  AND p.action = 'view'
ON CONFLICT DO NOTHING;

-- Inspector also gets dashboard and menu
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'INSPECTOR'
  AND p.code IN ('dashboard.view', 'system.menu.view')
ON CONFLICT DO NOTHING;

-- =====================================================
-- VIEWER: Minimal view (old-hemis: Ministry_lite, vazirlikrole)
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'VIEWER'
  AND p.action = 'view'
  AND (
    p.code LIKE 'students.%'
    OR p.code LIKE 'teachers.%'
    OR p.code LIKE 'institutions.%'
    OR p.code LIKE 'science.%'
    OR p.code LIKE 'reports.%'
    OR p.code LIKE 'rating.%'
    OR p.code = 'dashboard.view'
    OR p.code = 'system.menu.view'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- REPORT_VIEWER: Reports + rating + institutions (old-hemis: ReportAdmin)
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'REPORT_VIEWER'
  AND (
    p.code LIKE 'reports.%'
    OR p.code LIKE 'rating.%'
    OR p.code LIKE 'institutions.%'
    OR p.code = 'dashboard.view'
    OR p.code = 'system.menu.view'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- EXTERNAL_API: Minimal B2B access (old-hemis: Student API, Xodim API, Hokimiyat)
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id, assigned_by)
SELECT r.id, p.id, 'system'
FROM roles r CROSS JOIN permissions p
WHERE r.code = 'EXTERNAL_API'
  AND p.code IN (
    'students.view', 'students.list.view',
    'teachers.view', 'teachers.list.view',
    'universities.view'
  )
ON CONFLICT DO NOTHING;

-- =====================================================
-- Verification
-- =====================================================
DO $$
DECLARE
    sa INTEGER; otm INTEGER; ma INTEGER; insp INTEGER; vw INTEGER; rv INTEGER; ext INTEGER;
BEGIN
    SELECT COUNT(*) INTO sa FROM role_permissions rp JOIN roles r ON rp.role_id = r.id WHERE r.code = 'SUPER_ADMIN';
    SELECT COUNT(*) INTO otm FROM role_permissions rp JOIN roles r ON rp.role_id = r.id WHERE r.code = 'OTM_API';
    SELECT COUNT(*) INTO ma FROM role_permissions rp JOIN roles r ON rp.role_id = r.id WHERE r.code = 'MINISTRY_ADMIN';
    SELECT COUNT(*) INTO insp FROM role_permissions rp JOIN roles r ON rp.role_id = r.id WHERE r.code = 'INSPECTOR';
    SELECT COUNT(*) INTO vw FROM role_permissions rp JOIN roles r ON rp.role_id = r.id WHERE r.code = 'VIEWER';
    SELECT COUNT(*) INTO rv FROM role_permissions rp JOIN roles r ON rp.role_id = r.id WHERE r.code = 'REPORT_VIEWER';
    SELECT COUNT(*) INTO ext FROM role_permissions rp JOIN roles r ON rp.role_id = r.id WHERE r.code = 'EXTERNAL_API';

    RAISE NOTICE 'S004: Role permissions — SUPER_ADMIN=%, OTM_API=%, MINISTRY_ADMIN=%, INSPECTOR=%, VIEWER=%, REPORT_VIEWER=%, EXTERNAL_API=%',
        sa, otm, ma, insp, vw, rv, ext;
END $$;
