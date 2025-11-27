-- =====================================================
-- Rollback M004: Clear synced university_id
-- =====================================================

DO $$
DECLARE
    users_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'users'
    ) INTO users_exists;

    IF users_exists THEN
        UPDATE users
        SET university_id = NULL,
            updated_at = CURRENT_TIMESTAMP,
            updated_by = 'rollback-m004'
        WHERE updated_by = 'migration-m004';
        RAISE NOTICE 'M004 Rollback: Cleared synced university_id';
    ELSE
        RAISE NOTICE 'M004 Rollback: users table does not exist, skipping';
    END IF;
END $$;
