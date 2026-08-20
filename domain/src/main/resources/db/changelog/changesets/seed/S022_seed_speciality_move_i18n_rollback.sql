-- =====================================================
-- Rollback S022: remove speciality re-placement (move) UI hint keys
-- =====================================================
-- Deletes only the two keys S022 INTRODUCED (by explicit message_key list),
-- guarding for table existence (S006/S010 own the same 'label' category).
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY[
        'Has sub-directions — move them to another level first to change this level',
        'Moving to another place resets the status to Needs review'
    ];
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

    RAISE NOTICE 'S022 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
