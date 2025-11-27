-- =====================================================
-- Rollback S002: REMOVE CORE PERMISSIONS
-- =====================================================

DO $$
DECLARE
    permissions_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'permissions'
    ) INTO permissions_exists;

    IF permissions_exists THEN
        DELETE FROM permissions WHERE category = 'CORE' AND created_by = 'system';
        RAISE NOTICE 'S002 Rollback: Deleted core permissions';
    ELSE
        RAISE NOTICE 'S002 Rollback: permissions table does not exist, skipping';
    END IF;
END $$;
