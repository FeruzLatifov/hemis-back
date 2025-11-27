-- =====================================================
-- Rollback S006: REMOVE MENU TRANSLATIONS
-- =====================================================
-- Safe rollback - checks if tables exist first
-- =====================================================

DO $$
DECLARE
    translations_exists BOOLEAN;
    messages_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'system_message_translations'
    ) INTO translations_exists;

    SELECT EXISTS (
        SELECT 1 FROM information_schema.tables WHERE table_name = 'system_messages'
    ) INTO messages_exists;

    IF translations_exists AND messages_exists THEN
        DELETE FROM system_message_translations WHERE message_id IN (
            SELECT id FROM system_messages WHERE category = 'menu'
        );
        RAISE NOTICE 'S006 Rollback: Deleted menu translations';
    ELSE
        RAISE NOTICE 'S006 Rollback: Tables do not exist, skipping';
    END IF;

    IF messages_exists THEN
        DELETE FROM system_messages WHERE category = 'menu';
        RAISE NOTICE 'S006 Rollback: Deleted menu messages';
    END IF;
END $$;
