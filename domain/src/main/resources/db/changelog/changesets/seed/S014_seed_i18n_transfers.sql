-- =====================================================
-- S014: SEED TRANSFER DETECTION I18N TRANSLATIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2026-02-18
-- Purpose: Translations for transfer detection feature
-- Strategy: IDEMPOTENT UPSERT using _seed_msg() helper
-- =====================================================

DO $$
BEGIN
    -- Transfer category labels
    PERFORM _seed_msg('label', 'Internal transfer', 'Ichki ko''chirish', 'Ички кўчириш', 'Внутренний перевод');
    PERFORM _seed_msg('label', 'External transfer', 'Tashqi ko''chirish', 'Ташқи кўчириш', 'Внешний перевод');

    -- Speciality label for detail drawer
    PERFORM _seed_msg('label', 'Speciality', 'Mutaxassislik', 'Мутахассислик', 'Специальность');

    RAISE NOTICE 'S014: Transfer detection i18n translations seeded (3 new keys)';
END $$;
