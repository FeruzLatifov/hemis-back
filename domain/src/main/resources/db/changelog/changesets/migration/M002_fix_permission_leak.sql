-- =====================================================
-- M002: FIX PERMISSION LEAK - SQL Operator Precedence Bug
-- =====================================================
-- Author: hemis-team
-- Date: 2025-11-26
-- Purpose: Remove incorrectly assigned system.* permissions from non-admin roles
-- Root Cause: S003b_seed_permissions_menu.sql had missing parentheses in WHERE clause
--             causing OR conditions to apply to ALL roles instead of just VIEWER
-- Affected Roles: VIEWER, REPORT_VIEWER, UNIVERSITY_ADMIN
-- =====================================================

-- Step 1: Remove system.* permissions from VIEWER role
-- VIEWER should only have view permissions for registry, rating, data, reports
DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE code = 'VIEWER')
  AND permission_id IN (
    SELECT id FROM permissions WHERE code LIKE 'system.%'
  );

-- Step 2: Remove system.* permissions from REPORT_VIEWER role
-- REPORT_VIEWER should only have reports.* permissions
DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE code = 'REPORT_VIEWER')
  AND permission_id IN (
    SELECT id FROM permissions WHERE code LIKE 'system.%'
  );

-- Step 3: Remove system.translation.manage from UNIVERSITY_ADMIN
-- UNIVERSITY_ADMIN can view system pages but should not manage translations
DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE code = 'UNIVERSITY_ADMIN')
  AND permission_id IN (
    SELECT id FROM permissions WHERE code = 'system.translation.manage'
  );

-- Step 4: Verification
DO $$
DECLARE
    viewer_system_count INTEGER;
    report_viewer_system_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO viewer_system_count
    FROM role_permissions rp
    JOIN roles r ON rp.role_id = r.id
    JOIN permissions p ON rp.permission_id = p.id
    WHERE r.code = 'VIEWER' AND p.code LIKE 'system.%';

    SELECT COUNT(*) INTO report_viewer_system_count
    FROM role_permissions rp
    JOIN roles r ON rp.role_id = r.id
    JOIN permissions p ON rp.permission_id = p.id
    WHERE r.code = 'REPORT_VIEWER' AND p.code LIKE 'system.%';

    IF viewer_system_count > 0 THEN
        RAISE EXCEPTION 'M002 FAILED: VIEWER still has % system permissions', viewer_system_count;
    END IF;

    IF report_viewer_system_count > 0 THEN
        RAISE EXCEPTION 'M002 FAILED: REPORT_VIEWER still has % system permissions', report_viewer_system_count;
    END IF;

    RAISE NOTICE 'M002: Permission leak fixed successfully';
END $$;
