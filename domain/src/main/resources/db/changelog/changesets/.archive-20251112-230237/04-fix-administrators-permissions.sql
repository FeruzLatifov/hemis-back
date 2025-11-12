-- =====================================================
-- HEMIS Backend - Fix ADMINISTRATORS Role Permissions
-- =====================================================
-- Changeset: 04
-- Author: Senior Backend Team
-- Date: 2025-11-12
-- Purpose: Assign ALL permissions to ADMINISTRATORS role
--
-- Issue: ADMINISTRATORS role has 0 permissions (menu empty)
-- Solution: Assign all 30 permissions to ADMINISTRATORS
--
-- =====================================================

-- =====================================================
-- Step 1: Assign ALL Permissions to ADMINISTRATORS
-- =====================================================

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT
    r.id as role_id,
    p.id as permission_id,
    CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMINISTRATORS'
  AND r.deleted_at IS NULL
  AND p.deleted_at IS NULL
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- =====================================================
-- Step 2: Also assign to SUPER_ADMIN (if not already)
-- =====================================================

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT
    r.id as role_id,
    p.id as permission_id,
    CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND r.deleted_at IS NULL
  AND p.deleted_at IS NULL
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- =====================================================
-- Verification Query
-- =====================================================

DO $$
DECLARE
    admin_perm_count INTEGER;
    super_perm_count INTEGER;
    total_perms INTEGER;
BEGIN
    -- Count permissions
    SELECT COUNT(*) INTO total_perms FROM permissions WHERE deleted_at IS NULL;

    SELECT COUNT(DISTINCT rp.permission_id) INTO admin_perm_count
    FROM role_permissions rp
    JOIN roles r ON rp.role_id = r.id
    WHERE r.code = 'ADMINISTRATORS' AND r.deleted_at IS NULL;

    SELECT COUNT(DISTINCT rp.permission_id) INTO super_perm_count
    FROM role_permissions rp
    JOIN roles r ON rp.role_id = r.id
    WHERE r.code = 'SUPER_ADMIN' AND r.deleted_at IS NULL;

    RAISE NOTICE '========================================';
    RAISE NOTICE 'V4 Migration: Fix ADMINISTRATORS Permissions';
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
    RAISE NOTICE 'Total Permissions: %', total_perms;
    RAISE NOTICE 'ADMINISTRATORS: % permissions', admin_perm_count;
    RAISE NOTICE 'SUPER_ADMIN: % permissions', super_perm_count;
    RAISE NOTICE '';

    IF admin_perm_count = total_perms THEN
        RAISE NOTICE '✅ ADMINISTRATORS has ALL permissions';
    ELSE
        RAISE WARNING '⚠️  ADMINISTRATORS missing % permissions', (total_perms - admin_perm_count);
    END IF;

    RAISE NOTICE '========================================';
END $$;
