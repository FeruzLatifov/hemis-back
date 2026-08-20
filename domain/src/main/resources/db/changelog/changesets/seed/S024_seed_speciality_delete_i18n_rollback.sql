-- =====================================================
-- Rollback S024: remove speciality delete UI keys
-- =====================================================
-- Deletes only the five keys S024 INTRODUCED (by explicit message_key list),
-- guarding for table existence (S006/S010 own the same 'label' category).
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY[
        'Delete speciality',
        'This speciality has sub-directions. Delete them first, or move them under another parent.',
        'Only a speciality with the "Needs review" status can be deleted',
        'Move',
        'Speciality deleted'
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

    RAISE NOTICE 'S024 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
