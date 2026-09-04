-- =====================================================
-- Rollback S037: PERSON login (ism-familiya slug) i18n kalitlarini olib tashlash
-- =====================================================
-- Faqat S037 KIRITGAN kalitlar (aniq ro'yxat bo'yicha), jadval mavjudligini tekshirib
-- (S006/S010/S032..S036 ayni 'label'/'validation' kategoriyalariga egalik qiladi).
-- =====================================================

DO $$
DECLARE
    _keys TEXT[] := ARRAY[
        'Account credentials',
        'The login is generated from the first and last name.',
        'First name and last name are required',
        'Regenerate login',
        'Ministry/university staff. The login is generated from the name; details are fetched from the passport service.'
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

    RAISE NOTICE 'S037 Rollback: deleted % translation rows, % message rows',
        _deleted_translations, _deleted_messages;
END $$;
