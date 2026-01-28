-- =====================================================
-- Rollback S004: REMOVE ROLE-PERMISSION MAPPINGS
-- =====================================================

DO $$
DECLARE
    role_perms_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permissions'
    ) INTO role_perms_exists;

    IF role_perms_exists THEN
        DELETE FROM role_permissions WHERE assigned_by = 'system';
        RAISE NOTICE 'S004 Rollback: Deleted role-permission mappings';
    ELSE
        RAISE NOTICE 'S004 Rollback: role_permissions table does not exist, skipping';
    END IF;
END $$;
