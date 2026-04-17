-- =====================================================
-- Rollback S001: REMOVE SEEDED ROLES
-- =====================================================
-- Deletes ONLY the 7 roles created by S001 (by code).
-- =====================================================

DO $$
DECLARE
    roles_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'role'
    ) INTO roles_exists;

    IF roles_exists THEN
        DELETE FROM role WHERE code IN (
            'SUPER_ADMIN', 'OTM_API', 'MINISTRY_ADMIN', 'INSPECTOR', 'VIEWER', 'REPORT_VIEWER', 'EXTERNAL_API'
        );
        RAISE NOTICE 'S001 Rollback: Deleted 7 seeded roles';
    ELSE
        RAISE NOTICE 'S001 Rollback: roles table does not exist, skipping';
    END IF;
END $$;
