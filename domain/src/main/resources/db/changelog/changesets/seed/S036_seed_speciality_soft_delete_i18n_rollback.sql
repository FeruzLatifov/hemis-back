-- =====================================================
-- Rollback S036: mutaxassislik soft-delete/tiklash i18n kalitlarini olib tashlash
-- =====================================================
-- Faqat S036 KIRITGAN kalitlar (aniq ro'yxat bo'yicha), jadval mavjudligini tekshirib
-- (S006/S010/S032..S035 ayni 'label' kategoriyasiga egalik qiladi).
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY[
        'Deleted specialities',
        'Restore',
        'Speciality restored',
        'Nothing deleted yet',
        'Deleted at',
        'Deleted by',
        'The speciality is hidden everywhere and can be restored later',
        'A live speciality already uses this code and name',
        'The parent speciality is deleted — restore the parent first'
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

    RAISE NOTICE 'S036 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
