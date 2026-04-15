-- =====================================================
-- Rollback V007: DROP LANGUAGES TABLE
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM languages;

    IF row_count > 20 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: languages table has % rows. Manual intervention required.', row_count;
    END IF;

    RAISE NOTICE 'V007 Rollback: Dropping languages table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS languages CASCADE;
