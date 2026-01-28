-- =====================================================
-- Rollback S001: REMOVE SEEDED ROLES
-- =====================================================

DO $$
DECLARE
    roles_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'roles'
    ) INTO roles_exists;

    IF roles_exists THEN
        DELETE FROM roles WHERE created_by = 'system';
        RAISE NOTICE 'S001 Rollback: Deleted seeded roles';
    ELSE
        RAISE NOTICE 'S001 Rollback: roles table does not exist, skipping';
    END IF;
END $$;
