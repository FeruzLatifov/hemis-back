-- =====================================================
-- Rollback M004: Clear synced fields (not recommended)
-- =====================================================
-- Note: This rollback clears ALL synced fields.
-- Only use for testing or if M004 caused data issues.
-- =====================================================

DO $$
DECLARE
    users_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'users'
    ) INTO users_exists;

    IF NOT users_exists THEN
        RAISE NOTICE 'M004 Rollback: users table does not exist, skipping';
        RETURN;
    END IF;

    -- Note: We don't clear university_id, updated_at, etc.
    -- because we can't distinguish M004 updates from other updates.
    -- This is intentional - rolling back would cause data loss.

    RAISE NOTICE 'M004 Rollback: No action taken to preserve data integrity';
    RAISE NOTICE 'If you need to revert, manually restore from backup';
END $$;
