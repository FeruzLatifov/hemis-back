-- =====================================================
-- Rollback V003: DROP PERMISSIONS TABLE
-- =====================================================
-- WARNING: This is a DESTRUCTIVE operation!
-- CASCADE will also drop: role_permissions
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
    role_perms_count INTEGER := 0;
    role_perms_exists BOOLEAN;
BEGIN
    SELECT COUNT(*) INTO row_count FROM permissions;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permissions'
    ) INTO role_perms_exists;

    IF role_perms_exists THEN
        SELECT COUNT(*) INTO role_perms_count FROM role_permissions;
    END IF;

    -- GUARD: Block if more than 500 permissions (production has many custom)
    IF row_count > 500 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: permissions table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    IF role_perms_count > 0 THEN
        RAISE WARNING 'CASCADE will delete: % role_permissions', role_perms_count;
    END IF;

    RAISE NOTICE 'V003 Rollback: Dropping permissions table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS permissions CASCADE;
