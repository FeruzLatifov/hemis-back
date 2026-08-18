-- =====================================================
-- Rollback S021: remove bulk speciality-attach toast keys
-- =====================================================
-- Deletes only the two keys S021 INTRODUCED (by explicit message_key list),
-- guarding for table existence (S006/S011 own the same categories).
-- NOTE: the login-title key ('Higher Education Management Information System') is
-- NOT removed here — it is owned by S006; S021 only corrected its translations. A
-- rollback intentionally keeps the corrected text (reverting would restore a typo).
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY['{{n}} attached', '{{n}} already attached, skipped'];
    _deleted_translations BIGINT := 0;
    _deleted_messages BIGINT := 0;
BEGIN
    IF to_regclass('public.system_message_translation') IS NOT NULL THEN
        DELETE FROM system_message_translation
        WHERE message_id IN (SELECT id FROM system_message WHERE message_key = ANY(_keys));
        GET DIAGNOSTICS _deleted_translations = ROW_COUNT;
    END IF;

    IF to_regclass('public.system_message') IS NOT NULL THEN
        DELETE FROM system_message WHERE message_key = ANY(_keys);
        GET DIAGNOSTICS _deleted_messages = ROW_COUNT;
    END IF;

    RAISE NOTICE 'S021 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
