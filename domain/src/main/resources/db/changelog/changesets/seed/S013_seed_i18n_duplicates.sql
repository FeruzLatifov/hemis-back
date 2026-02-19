-- =====================================================
-- S013: SEED DUPLICATE DETECTION I18N TRANSLATIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2026-02-17
-- Purpose: Translations for student duplicates page
-- Strategy: IDEMPOTENT UPSERT using _seed_msg() helper
-- =====================================================

DO $$
BEGIN
    -- Page title and description
    PERFORM _seed_msg('menu', 'Duplicates', 'Dublikatlar', 'Дубликатлар', 'Дубликаты');
    PERFORM _seed_msg('label', 'Students with duplicate PINFL analysis', 'PINFL dublikatli talabalar tahlili', 'ПИНФЛ дубликатли талабалар таҳлили', 'Анализ студентов с дублирующимися ПИНФЛ');

    -- Stats cards
    PERFORM _seed_msg('label', 'Total duplicate PINFLs', 'Jami dublikat PINFLlar', 'Жами дубликат ПИНФЛлар', 'Всего дублирующихся ПИНФЛ');
    PERFORM _seed_msg('label', 'Cross university', 'Boshqa OTMda', 'Бошқа ОТМда', 'В другом ВУЗе');
    PERFORM _seed_msg('label', 'Same university', 'Bitta OTMda', 'Битта ОТМда', 'В одном ВУЗе');
    PERFORM _seed_msg('label', 'Multi level', 'Turli bosqich', 'Турли босқич', 'Разные уровни');
    PERFORM _seed_msg('status', 'Normal', 'Normal', 'Нормал', 'Нормально');

    -- Table and groups
    PERFORM _seed_msg('label', 'records', 'yozuv', 'ёзув', 'записей');
    PERFORM _seed_msg('label', 'universities', 'OTM', 'ОТМ', 'ВУЗов');
    PERFORM _seed_msg('message', '{{count}} groups found', '{{count}} guruh topildi', '{{count}} гуруҳ топилди', '{{count}} групп найдено');
    PERFORM _seed_msg('message', 'No duplicate records found', 'Dublikat yozuvlar topilmadi', 'Дубликат ёзувлар топилмади', 'Дублирующиеся записи не найдены');

    RAISE NOTICE 'S013: Duplicate detection i18n translations seeded (11 new keys)';
END $$;
