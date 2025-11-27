-- =====================================================
-- Rollback V010: DROP LANGUAGE_TRANSLATIONS TABLE
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM language_translations;

    -- GUARD: Block if more than 1000 translations
    IF row_count > 1000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: language_translations table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    RAISE NOTICE 'V010 Rollback: Dropping language_translations table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS language_translations CASCADE;
