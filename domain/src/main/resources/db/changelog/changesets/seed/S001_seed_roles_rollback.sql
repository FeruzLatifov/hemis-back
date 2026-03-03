-- =====================================================
-- Rollback S001: REMOVE SEEDED ROLES
-- =====================================================
-- Deletes ONLY the 5 roles created by S001 (by code).
-- =====================================================

DO $$
DECLARE
    roles_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'roles'
    ) INTO roles_exists;

    IF roles_exists THEN
        DELETE FROM roles WHERE code IN (
            'SUPER_ADMIN', 'MINISTRY_ADMIN', 'UNIVERSITY_ADMIN', 'VIEWER', 'REPORT_VIEWER'
        );
        RAISE NOTICE 'S001 Rollback: Deleted 5 seeded roles';
    ELSE
        RAISE NOTICE 'S001 Rollback: roles table does not exist, skipping';
    END IF;
END $$;
