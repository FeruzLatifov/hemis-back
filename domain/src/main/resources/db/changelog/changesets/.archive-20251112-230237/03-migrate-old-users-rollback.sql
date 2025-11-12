-- =====================================================
-- ROLLBACK: V3 User Migration
-- =====================================================
-- Purpose: Safely rollback V3 user migration
-- Removes migrated users, roles, and relationships
-- Preserves default V2 seed data (admin user, default roles)
-- =====================================================

-- Safety Check: Confirm database
DO $$
BEGIN
    IF current_database() != 'test_hemis' THEN
        RAISE EXCEPTION 'SAFETY CHECK FAILED: This script can only run on test_hemis database. Current: %', current_database();
    END IF;

    RAISE NOTICE '✅ Safety check passed: Running on test_hemis database';
END $$;

-- =====================================================
-- ROLLBACK STEP 1: Delete Migrated User-Role Mappings
-- =====================================================
-- Delete mappings for migrated users (excluding default admin)
-- =====================================================

DELETE FROM user_roles
WHERE user_id IN (
    SELECT id FROM users
    WHERE id NOT IN ('00000000-0000-0000-0000-000000000001')  -- Keep default admin mappings
);

-- =====================================================
-- ROLLBACK STEP 2: Delete Migrated Users
-- =====================================================
-- Delete all migrated users (keep default admin from V2)
-- =====================================================

DELETE FROM users
WHERE id NOT IN ('00000000-0000-0000-0000-000000000001');  -- Keep default admin user

-- =====================================================
-- ROLLBACK STEP 3: Delete Migrated Roles
-- =====================================================
-- Delete migrated roles (keep V2 default roles)
-- =====================================================

DELETE FROM roles
WHERE id NOT IN (
    '10000000-0000-0000-0000-000000000001'::UUID,  -- SUPER_ADMIN
    '10000000-0000-0000-0000-000000000002'::UUID,  -- MINISTRY_ADMIN
    '10000000-0000-0000-0000-000000000003'::UUID,  -- UNIVERSITY_ADMIN
    '10000000-0000-0000-0000-000000000004'::UUID,  -- VIEWER
    '10000000-0000-0000-0000-000000000005'::UUID   -- REPORT_VIEWER
);

-- =====================================================
-- VERIFICATION
-- =====================================================

DO $$
DECLARE
    user_count INTEGER;
    role_count INTEGER;
    mapping_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO role_count FROM roles;
    SELECT COUNT(*) INTO mapping_count FROM user_roles;

    RAISE NOTICE '==============================================';
    RAISE NOTICE 'V3 Rollback Complete';
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Remaining counts (should match V2 state):';
    RAISE NOTICE '  Users: % (expected: 1)', user_count;
    RAISE NOTICE '  Roles: % (expected: 5)', role_count;
    RAISE NOTICE '  User-Role Mappings: % (expected: 5)', mapping_count;
    RAISE NOTICE '';

    IF user_count = 1 AND role_count = 5 THEN
        RAISE NOTICE '✅ Database restored to V2 state (default data only)';
    ELSE
        RAISE WARNING '⚠️  Database state does not match V2';
    END IF;

    RAISE NOTICE '';
    RAISE NOTICE 'All V3 migrated users removed';
    RAISE NOTICE 'Default admin user and roles preserved';
    RAISE NOTICE '==============================================';
END $$;

-- =====================================================
-- Rollback Complete
-- =====================================================
-- ✅ Migrated users deleted
-- ✅ Migrated roles deleted
-- ✅ User-role mappings deleted
-- ✅ Default V2 data preserved (1 admin, 5 roles)
-- ✅ Old-hemis data UNTOUCHED (sec_user, sec_role, sec_user_role)
-- =====================================================
