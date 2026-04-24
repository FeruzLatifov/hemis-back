-- =====================================================
-- Rollback M001: REMOVE MIGRATED USERS
-- =====================================================
-- Safe rollback - only deletes migrated data if tables exist
-- =====================================================

DO $$
DECLARE
    users_exists BOOLEAN;
    user_roles_exists BOOLEAN;
    oauth_client_exists BOOLEAN;
    oauth_client_role_exists BOOLEAN;
BEGIN
    -- Check if tables exist (may have been dropped by schema rollback)
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'users'
    ) INTO users_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'user_role'
    ) INTO user_roles_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'oauth_client'
    ) INTO oauth_client_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'oauth_client_role'
    ) INTO oauth_client_role_exists;

    -- Phase 2 dual-write: remove bindings + oauth_client rows we created.
    IF oauth_client_role_exists THEN
        DELETE FROM oauth_client_role WHERE granted_by = 'migration-phase2';
        RAISE NOTICE 'M001 Rollback: Deleted Phase 2 oauth_client_role bindings';
    END IF;

    IF oauth_client_exists THEN
        DELETE FROM oauth_client WHERE created_by = 'migration-phase2';
        RAISE NOTICE 'M001 Rollback: Deleted Phase 2 oauth_client rows';
    END IF;

    IF user_roles_exists THEN
        DELETE FROM user_role WHERE assigned_by = 'migration';
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
