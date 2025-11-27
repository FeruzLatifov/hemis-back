-- =====================================================
-- Rollback V007: DROP SYSTEM_MESSAGE_TRANSLATIONS TABLE
-- =====================================================

DO $$
DECLARE
    row_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO row_count FROM system_message_translations;

    -- GUARD: Block if more than 20000 translations (production i18n)
    IF row_count > 20000 THEN
        RAISE EXCEPTION 'ROLLBACK BLOCKED: system_message_translations table has % rows. '
            'Production database detected. Manual intervention required.', row_count;
    END IF;

    RAISE NOTICE 'V007 Rollback: Dropping system_message_translations table (% rows)', row_count;
END $$;

DROP TABLE IF EXISTS system_message_translations CASCADE;
