-- =====================================================
-- S015 ROLLBACK: Remove directions page i18n translations
-- =====================================================

DO $$
DECLARE
    _keys text[] := ARRAY[
        'Directions',
        'Speciality statistics by education type',
        'Total specialities',
        'With students',
        'Without students',
        'Total students in specialities',
        'Ordinatura',
        'Speciality code',
        'Speciality name',
        'Education type',
        'Total students',
        'Active students',
        'Graduated',
        'Expelled',
        'Search by speciality...',
        'Has students',
        'No students',
        '{{count}} specialities found'
    ];
    _key text;
    _msg_id uuid;
BEGIN
    FOREACH _key IN ARRAY _keys LOOP
        SELECT id INTO _msg_id FROM system_messages WHERE message_key = _key;
        IF _msg_id IS NOT NULL THEN
            DELETE FROM system_message_translations WHERE message_id = _msg_id;
            DELETE FROM system_messages WHERE id = _msg_id;
        END IF;
    END LOOP;
    RAISE NOTICE 'S015 ROLLBACK: Removed directions page i18n translations';
END $$;
