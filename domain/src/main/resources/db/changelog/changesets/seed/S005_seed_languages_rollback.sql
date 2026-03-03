-- =====================================================
-- Rollback S005: REMOVE SEEDED LANGUAGES
-- =====================================================
-- Deletes ONLY the 4 languages created by S005 (by code).
-- =====================================================

DO $$
DECLARE
    languages_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'languages'
    ) INTO languages_exists;

    IF languages_exists THEN
        DELETE FROM languages WHERE code IN ('uz-UZ', 'oz-UZ', 'ru-RU', 'en-US');
        RAISE NOTICE 'S005 Rollback: Deleted 4 seeded languages';
    ELSE
        RAISE NOTICE 'S005 Rollback: languages table does not exist, skipping';
    END IF;
END $$;
