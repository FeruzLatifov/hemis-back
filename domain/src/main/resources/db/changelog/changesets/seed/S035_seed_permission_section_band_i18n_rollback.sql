-- =====================================================
-- Rollback S035: remove the "Section-wide" band + restorable-delete i18n keys
-- =====================================================
-- Deletes only the keys S035 INTRODUCED (by explicit message_key list), guarding for table
-- existence (S006/S010/S032/S033 own the same 'label' category). 'Deletes records (irreversible)'
-- belongs to S032 and is deliberately NOT touched here.
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY['Section-wide', 'Deletes records (restorable)'];
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

    RAISE NOTICE 'S035 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
