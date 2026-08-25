-- =====================================================
-- Rollback S033: remove the role-template i18n keys
-- =====================================================
-- Deletes only the keys S033 INTRODUCED (by explicit message_key list), guarding
-- for table existence (S006/S010/S032 own the same 'label' category).
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY[
        'Operator',
        'Approver',
        'Role template',
        'View, create & edit records',
        'View, edit & approve records',
        'Compact',
        'Detailed',
        'Custom selection',
        'Advanced: fine-tune permissions'
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

    RAISE NOTICE 'S033 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
