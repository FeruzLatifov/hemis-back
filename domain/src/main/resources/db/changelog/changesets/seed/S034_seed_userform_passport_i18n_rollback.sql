-- =====================================================
-- Rollback S034: remove the create-user passport-detail i18n keys
-- =====================================================
DO $$
DECLARE
    _keys TEXT[] := ARRAY[
        'Birth place',
        'Passport issue place',
        'Expiry date'
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

    RAISE NOTICE 'S034 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
