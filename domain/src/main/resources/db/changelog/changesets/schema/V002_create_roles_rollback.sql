-- =====================================================
-- Rollback V002: DROP ROLES TABLE
-- =====================================================
-- WARNING: This is a DESTRUCTIVE operation!
-- CASCADE will also drop: user_roles, role_permissions
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
    user_roles_count INTEGER := 0;
    role_perms_count INTEGER := 0;
    user_roles_exists BOOLEAN;
    role_perms_exists BOOLEAN;
BEGIN
    SELECT COUNT(*) INTO row_count FROM roles;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'user_roles'
    ) INTO user_roles_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permissions'
    ) INTO role_perms_exists;

    IF user_roles_exists THEN
        SELECT COUNT(*) INTO user_roles_count FROM user_roles;
    END IF;

    IF role_perms_exists THEN
        SELECT COUNT(*) INTO role_perms_count FROM role_permissions;
    END IF;

    -- GUARD: Block if more than 50 roles (production has custom roles)
    IF row_count > 50 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: roles table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    IF user_roles_count > 0 OR role_perms_count > 0 THEN
        RAISE WARNING 'CASCADE will delete: % user_roles, % role_permissions',
            user_roles_count, role_perms_count;
    END IF;

    RAISE NOTICE 'V002 Rollback: Dropping roles table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS roles CASCADE;
