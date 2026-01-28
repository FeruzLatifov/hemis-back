-- =====================================================
-- Rollback V006: DROP SYSTEM_MESSAGES TABLE
-- =====================================================
-- CASCADE will also drop: system_message_translations
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
    translations_count INTEGER := 0;
    translations_exists BOOLEAN;
BEGIN
    SELECT COUNT(*) INTO row_count FROM system_messages;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'system_message_translations'
    ) INTO translations_exists;

    IF translations_exists THEN
        SELECT COUNT(*) INTO translations_count FROM system_message_translations;
    END IF;

    -- GUARD: Block if more than 5000 messages (production i18n)
    IF row_count > 5000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: system_messages table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    IF translations_count > 0 THEN
        RAISE WARNING 'CASCADE will delete: % translations', translations_count;
    END IF;

    RAISE NOTICE 'V006 Rollback: Dropping system_messages table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS system_messages CASCADE;
