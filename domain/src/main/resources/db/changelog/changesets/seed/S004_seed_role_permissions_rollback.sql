-- =====================================================
-- Rollback S004: REMOVE ROLE-PERMISSION MAPPINGS
-- =====================================================
-- Deletes role_permissions for the 7 system roles where assigned_by = 'system'.
-- In Liquibase reverse-order rollback, S003/S002 rollbacks come AFTER S004
-- rollback, so the permissions still exist at this point.
-- =====================================================

DO $$
DECLARE
    role_perms_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permission'
    ) INTO role_perms_exists;

    IF role_perms_exists THEN
        DELETE FROM role_permission WHERE role_id IN (
            SELECT id FROM role WHERE code IN (
                'SUPER_ADMIN', 'OTM_API', 'MINISTRY_ADMIN', 'INSPECTOR', 'VIEWER', 'REPORT_VIEWER', 'EXTERNAL_API'
            )
        ) AND assigned_by = 'system';
        RAISE NOTICE 'S004 Rollback: Deleted system role-permission mappings for 7 roles';
    ELSE
        RAISE NOTICE 'S004 Rollback: role_permissions table does not exist, skipping';
    END IF;
END $$;
