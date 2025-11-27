-- =====================================================
-- Rollback S005: REMOVE SEEDED LANGUAGES
-- =====================================================

DO $$
DECLARE
    languages_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'languages'
    ) INTO languages_exists;

    IF languages_exists THEN
        DELETE FROM languages WHERE created_by = 'system';
        RAISE NOTICE 'S005 Rollback: Deleted seeded languages';
    ELSE
        RAISE NOTICE 'S005 Rollback: languages table does not exist, skipping';
    END IF;
END $$;
