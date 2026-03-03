-- =====================================================
-- Rollback S004: REMOVE ROLE-PERMISSION MAPPINGS
-- =====================================================
-- Deletes role_permissions for the 5 system roles where assigned_by = 'system'.
-- In Liquibase reverse-order rollback, S003/S002 rollbacks come AFTER S004
-- rollback, so the permissions still exist at this point.
-- =====================================================

DO $$
DECLARE
    role_perms_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permissions'
    ) INTO role_perms_exists;

    IF role_perms_exists THEN
        DELETE FROM role_permissions WHERE role_id IN (
            SELECT id FROM roles WHERE code IN (
                'SUPER_ADMIN', 'MINISTRY_ADMIN', 'UNIVERSITY_ADMIN', 'VIEWER', 'REPORT_VIEWER'
            )
        ) AND assigned_by = 'system';
        RAISE NOTICE 'S004 Rollback: Deleted system role-permission mappings for 5 roles';
    ELSE
        RAISE NOTICE 'S004 Rollback: role_permissions table does not exist, skipping';
    END IF;
END $$;
