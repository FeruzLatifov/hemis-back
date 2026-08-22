-- =====================================================
-- Rollback S029: remove the OAuth secret-rotation i18n keys
-- =====================================================
-- Deletes only the keys S029 INTRODUCED (by explicit message_key list),
-- guarding for table existence (S006/S010 own the same 'label' category).
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY[
        'Rotate secret',
        'Generate automatically',
        'Enter manually',
        'New secret',
        'At least {{n}} characters',
        'Repeat new secret',
        'Secrets do not match',
        'Weak',
        'Fair',
        'Strong',
        'Estimated entropy: {{bits}} bits',
        'Must not contain the Client ID',
        'Too many repeated characters',
        'Avoid sequences like abcd or 1234',
        'Avoid guessable words like admin, password or test',
        'Not complex enough — make it longer or more random',
        'Mix letters, digits and symbols',
        'New secret — copy now',
        'Secret changed',
        'Already-issued tokens stay valid for up to 24 hours. Rotating or disabling only blocks new tokens.'
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

    RAISE NOTICE 'S029 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
