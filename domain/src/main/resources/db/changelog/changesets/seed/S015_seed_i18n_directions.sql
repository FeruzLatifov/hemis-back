-- =====================================================
-- S015: SEED DIRECTIONS PAGE I18N TRANSLATIONS
-- =====================================================
-- Author: hemis-team
-- Date: 2026-02-19
-- Purpose: Translations for student directions (specialities) page
-- Strategy: IDEMPOTENT UPSERT using _seed_msg() helper
-- =====================================================

DO $$
BEGIN
    -- Page title and description
    PERFORM _seed_msg('menu', 'Directions', 'Yo''nalishlar', 'Йўналишлар', 'Направления');
    PERFORM _seed_msg('label', 'Speciality statistics by education type', 'Ta''lim turi bo''yicha mutaxassislik statistikasi', 'Таълим тури бўйича мутахассислик статистикаси', 'Статистика специальностей по типу образования');

    -- Summary cards
    PERFORM _seed_msg('label', 'Total specialities', 'Jami mutaxassisliklar', 'Жами мутахассисликлар', 'Всего специальностей');
    PERFORM _seed_msg('label', 'With students', 'Talabasi bor', 'Талабаси бор', 'Со студентами');
    PERFORM _seed_msg('label', 'Without students', 'Talabasi yo''q', 'Талабаси йўқ', 'Без студентов');
    PERFORM _seed_msg('label', 'Total students in specialities', 'Mutaxassisliklardagi jami talabalar', 'Мутахассисликлардаги жами талабалар', 'Всего студентов в специальностях');

    -- Education types
    PERFORM _seed_msg('label', 'Ordinatura', 'Ordinatura', 'Ординатура', 'Ординатура');

    -- Table columns
    PERFORM _seed_msg('table', 'Speciality code', 'Mutaxassislik kodi', 'Мутахассислик коди', 'Код специальности');
    PERFORM _seed_msg('table', 'Speciality name', 'Mutaxassislik nomi', 'Мутахассислик номи', 'Название специальности');
    PERFORM _seed_msg('table', 'Education type', 'Ta''lim turi', 'Таълим тури', 'Тип образования');
    PERFORM _seed_msg('table', 'Total students', 'Jami talabalar', 'Жами талабалар', 'Всего студентов');
    PERFORM _seed_msg('table', 'Active students', 'Faol talabalar', 'Фаол талабалар', 'Активные студенты');
    PERFORM _seed_msg('label', 'Graduated', 'Bitirganlar', 'Битирганлар', 'Выпускники');
    PERFORM _seed_msg('label', 'Expelled', 'Chetlashganlar', 'Четлашганлар', 'Отчисленные');

    -- Filters
    PERFORM _seed_msg('label', 'Search by speciality...', 'Mutaxassislik bo''yicha qidirish...', 'Мутахассислик бўйича қидириш...', 'Поиск по специальности...');
    PERFORM _seed_msg('label', 'Has students', 'Talabasi bor', 'Талабаси бор', 'Есть студенты');
    PERFORM _seed_msg('label', 'No students', 'Talabasi yo''q', 'Талабаси йўқ', 'Без студентов');

    -- Results
    PERFORM _seed_msg('message', '{{count}} specialities found', '{{count}} mutaxassislik topildi', '{{count}} мутахассислик топилди', '{{count}} специальностей найдено');

    RAISE NOTICE 'S015: Directions page i18n translations seeded (17 new keys)';
END $$;
