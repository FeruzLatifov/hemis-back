-- =====================================================
-- Rollback V009: DROP LANGUAGES TABLE
-- =====================================================
-- CASCADE will also drop: language_translations
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
    lang_trans_count INTEGER := 0;
    lang_trans_exists BOOLEAN;
BEGIN
    SELECT COUNT(*) INTO row_count FROM languages;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'language_translations'
    ) INTO lang_trans_exists;

    IF lang_trans_exists THEN
        SELECT COUNT(*) INTO lang_trans_count FROM language_translations;
    END IF;

    -- GUARD: Block if more than 20 languages (unlikely but safe)
    IF row_count > 20 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: languages table has % rows. '
            'Unexpected data. Manual intervention required.', row_count;
    END IF;

    IF lang_trans_count > 0 THEN
        RAISE WARNING 'CASCADE will delete: % language_translations', lang_trans_count;
    END IF;

    RAISE NOTICE 'V009 Rollback: Dropping languages table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS languages CASCADE;
