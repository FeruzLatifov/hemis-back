-- =====================================================
-- Rollback M001: REMOVE MIGRATED USERS
-- =====================================================
-- Safe rollback - only deletes migrated data if tables exist
-- =====================================================

DO $$
DECLARE
    users_exists BOOLEAN;
    user_roles_exists BOOLEAN;
BEGIN
    -- Check if tables exist (may have been dropped by schema rollback)
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'users'
    ) INTO users_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'user_roles'
    ) INTO user_roles_exists;

    IF user_roles_exists THEN
        DELETE FROM user_roles WHERE assigned_by = 'migration';
        RAISE NOTICE 'M001 Rollback: Deleted migrated user_roles';
    ELSE
        RAISE NOTICE 'M001 Rollback: user_roles table does not exist, skipping';
    END IF;

    IF users_exists THEN
        DELETE FROM users WHERE created_by = 'migration';
        RAISE NOTICE 'M001 Rollback: Deleted migrated users';
    ELSE
        RAISE NOTICE 'M001 Rollback: users table does not exist, skipping';
    END IF;
END $$;
