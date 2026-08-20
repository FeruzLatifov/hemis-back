-- =====================================================
-- Rollback S025: restore the "Needs review" wording
-- =====================================================
-- The key is NOT deleted — 'Needs review' is owned by S006/S010. Rollback only
-- restores the four translations S025 overwrote (S010 wording, en-US = the key),
-- guarding for table existence like the S022/S024 rollbacks.
-- =====================================================

DO $$
BEGIN
    IF to_regclass('public.system_message') IS NOT NULL
       AND to_regclass('public.system_message_translation') IS NOT NULL THEN

        PERFORM _seed_msg('status', 'Needs review', 'Needs review', 'Ko''rib chiqish kerak', 'Кўриб чиқиш керак', 'Требует проверки');

        RAISE NOTICE 'S025 Rollback: restored "Needs review" translations';
    END IF;
END $$;
