-- =====================================================
-- Rollback S006: REMOVE ALL SEEDED TRANSLATIONS
-- =====================================================
-- Removes ALL translations seeded by S006 (consolidated)
-- This is safe because S006 is the ONLY i18n seed file
-- after consolidation of S007, S008, S011, S013, S014, S015, S017
-- Safe rollback - checks if tables exist first
-- =====================================================

DO $$
DECLARE
    translations_exists BOOLEAN;
    messages_exists BOOLEAN;
    _deleted_translations BIGINT := 0;
    _deleted_messages BIGINT := 0;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'system_message_translation'
    ) INTO translations_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'system_message'
    ) INTO messages_exists;

    IF translations_exists AND messages_exists THEN
        -- Delete all translations for seeded messages (all categories from S006)
        DELETE FROM system_message_translation WHERE message_id IN (
            SELECT id FROM system_message
            WHERE category IN (
                'action', 'status', 'label', 'message', 'validation',
                'table', 'pagination', 'confirm', 'auth', 'menu', 'error'
            )
        );
        GET DIAGNOSTICS _deleted_translations = ROW_COUNT;
        RAISE NOTICE 'S006 Rollback: Deleted % translation rows', _deleted_translations;
    ELSE
        RAISE NOTICE 'S006 Rollback: Tables do not exist, skipping translations';
    END IF;

    IF messages_exists THEN
        -- Delete all seeded messages
        DELETE FROM system_message
        WHERE category IN (
            'action', 'status', 'label', 'message', 'validation',
            'table', 'pagination', 'confirm', 'auth', 'menu', 'error'
        );
        GET DIAGNOSTICS _deleted_messages = ROW_COUNT;
        RAISE NOTICE 'S006 Rollback: Deleted % message rows', _deleted_messages;
    END IF;

    -- Clean up helper function if it exists (in case rollback runs mid-seed)
    DROP FUNCTION IF EXISTS _seed_msg(TEXT, TEXT, TEXT, TEXT, TEXT);
END $$;
