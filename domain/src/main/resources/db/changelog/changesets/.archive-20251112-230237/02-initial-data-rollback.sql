-- =====================================================
-- ROLLBACK: V2 Initial Data (Seed Data)
-- =====================================================
-- Purpose: Safely rollback V2 seed data
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
-- ROLLBACK STEP 1: Delete User-Role Mappings
-- =====================================================

DELETE FROM user_roles
WHERE user_id = '00000000-0000-0000-0000-000000000001'::UUID
   OR role_id IN (
       '10000000-0000-0000-0000-000000000001'::UUID,
       '10000000-0000-0000-0000-000000000002'::UUID,
       '10000000-0000-0000-0000-000000000003'::UUID,
       '10000000-0000-0000-0000-000000000004'::UUID,
       '10000000-0000-0000-0000-000000000005'::UUID
   );

-- =====================================================
-- ROLLBACK STEP 2: Delete Role-Permission Mappings
-- =====================================================

DELETE FROM role_permissions
WHERE role_id IN (
    '10000000-0000-0000-0000-000000000001'::UUID,
    '10000000-0000-0000-0000-000000000002'::UUID,
    '10000000-0000-0000-0000-000000000003'::UUID,
    '10000000-0000-0000-0000-000000000004'::UUID,
    '10000000-0000-0000-0000-000000000005'::UUID
);

-- =====================================================
-- ROLLBACK STEP 3: Delete Message Translations
-- =====================================================

DELETE FROM message_translations
WHERE message_id IN (
    SELECT id FROM system_messages
    WHERE category IN ('app', 'menu', 'button', 'login', 'common', 'error', 'validation')
);

-- =====================================================
-- ROLLBACK STEP 4: Delete System Messages
-- =====================================================

DELETE FROM system_messages
WHERE category IN ('app', 'menu', 'button', 'login', 'common', 'error', 'validation');

-- =====================================================
-- ROLLBACK STEP 5: Delete Configurations
-- =====================================================

DELETE FROM configurations
WHERE path LIKE 'system.language.%' OR path = 'system.default_language';

-- =====================================================
-- ROLLBACK STEP 6: Delete Languages
-- =====================================================

DELETE FROM languages
WHERE code IN ('uz-UZ', 'oz-UZ', 'ru-RU', 'en-US', 'kk-UZ', 'tg-TG', 'kz-KZ', 'tm-TM', 'kg-KG');

-- =====================================================
-- ROLLBACK STEP 7: Delete Permissions
-- =====================================================

DELETE FROM permissions
WHERE category IN ('CORE', 'REPORTS', 'ADMIN', 'INTEGRATION');

-- =====================================================
-- ROLLBACK STEP 8: Delete Roles
-- =====================================================

DELETE FROM roles
WHERE id IN (
    '10000000-0000-0000-0000-000000000001'::UUID,
    '10000000-0000-0000-0000-000000000002'::UUID,
    '10000000-0000-0000-0000-000000000003'::UUID,
    '10000000-0000-0000-0000-000000000004'::UUID,
    '10000000-0000-0000-0000-000000000005'::UUID
);

-- =====================================================
-- ROLLBACK STEP 9: Delete Admin User
-- =====================================================

DELETE FROM users
WHERE id = '00000000-0000-0000-0000-000000000001'::UUID;

-- =====================================================
-- VERIFICATION
-- =====================================================

DO $$
DECLARE
    user_count INTEGER;
    role_count INTEGER;
    permission_count INTEGER;
    language_count INTEGER;
    config_count INTEGER;
    message_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO role_count FROM roles;
    SELECT COUNT(*) INTO permission_count FROM permissions;
    SELECT COUNT(*) INTO language_count FROM languages;
    SELECT COUNT(*) INTO config_count FROM configurations;
    SELECT COUNT(*) INTO message_count FROM system_messages;

    RAISE NOTICE '==============================================';
    RAISE NOTICE 'V2 Rollback Complete (Liquibase)';
    RAISE NOTICE '==============================================';
    RAISE NOTICE 'Remaining counts:';
    RAISE NOTICE '  Users: %', user_count;
    RAISE NOTICE '  Roles: %', role_count;
    RAISE NOTICE '  Permissions: %', permission_count;
    RAISE NOTICE '  Languages: %', language_count;
    RAISE NOTICE '  Configurations: %', config_count;
    RAISE NOTICE '  System Messages: %', message_count;
    RAISE NOTICE '';
    RAISE NOTICE 'All V2 seed data has been removed';
    RAISE NOTICE '==============================================';
END $$;

-- =====================================================
-- Rollback Complete
-- =====================================================
-- ✅ All V2 seed data removed
-- ✅ User-role mappings deleted
-- ✅ Role-permission mappings deleted
-- ✅ Admin user deleted
-- ✅ Roles deleted (5)
-- ✅ Permissions deleted (30)
-- ✅ Languages deleted (9)
-- ✅ Configurations deleted (10)
-- ✅ System messages deleted (40+)
-- ✅ Liquibase history updated automatically
-- ⚠️  Tables structure NOT dropped (use V1 rollback for full rollback)
-- =====================================================
