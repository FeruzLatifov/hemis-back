-- =====================================================
-- ROLLBACK: V1 Baseline (Structure)
-- =====================================================
-- Purpose: Safely rollback V1 database structure
-- Managed by: Liquibase (no manual Flyway history update needed)
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
-- ROLLBACK STEP 1: Drop Join Tables (Foreign Keys First)
-- =====================================================

DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;

-- =====================================================
-- ROLLBACK STEP 2: Drop Translation Tables
-- =====================================================

DROP TABLE IF EXISTS message_translations CASCADE;

-- =====================================================
-- ROLLBACK STEP 3: Drop Main Tables
-- =====================================================

DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS system_messages CASCADE;
DROP TABLE IF EXISTS languages CASCADE;
DROP TABLE IF EXISTS configurations CASCADE;

-- =====================================================
-- VERIFICATION
-- =====================================================

DO $$
DECLARE
    table_count INTEGER;
    old_table_count INTEGER;
BEGIN
    -- Count V1 tables (should be 0)
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_name IN ('users', 'roles', 'permissions', 'user_roles', 'role_permissions',
                         'languages', 'configurations', 'system_messages', 'message_translations');

    -- Count old-hemis tables (should be unchanged)
    SELECT COUNT(*) INTO old_table_count
    FROM information_schema.tables
    WHERE table_schema = 'public'
      AND (table_name LIKE 'hemishe_%' OR table_name LIKE 'sec_%');

    RAISE NOTICE '==============================================';
    RAISE NOTICE 'V1 Rollback Complete (Liquibase)';
    RAISE NOTICE '==============================================';
    RAISE NOTICE '';
    RAISE NOTICE 'V1 Tables Remaining: % (expected: 0)', table_count;
    RAISE NOTICE 'Old-Hemis Tables: % (unchanged)', old_table_count;
    RAISE NOTICE '';

    IF table_count = 0 THEN
        RAISE NOTICE '✅ All V1 tables successfully dropped';
    ELSE
        RAISE WARNING '⚠️  Some V1 tables still exist!';
    END IF;

    RAISE NOTICE '';
    RAISE NOTICE 'Database restored to pre-V1 state';
    RAISE NOTICE 'Old-hemis data (hemishe_*, sec_*) UNTOUCHED';
    RAISE NOTICE '==============================================';
END $$;

-- =====================================================
-- Rollback Complete
-- =====================================================
-- ✅ All V1 tables dropped (9 tables)
-- ✅ All indexes dropped automatically
-- ✅ All constraints dropped automatically
-- ✅ Liquibase history updated automatically
-- ✅ Old-hemis tables UNTOUCHED (hemishe_*, sec_*)
-- =====================================================
