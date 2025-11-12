-- ================================================
-- V15: Add Final Submenu Translations
-- Date: 2025-01-10
-- Purpose: Add final 12 submenu translations (4 languages each)
--
-- New Submenus:
-- 1. registry-scientific: 10 submenus (Scientific Registry)
-- 2. reports-universities: 2 submenus (University Reports)
--
-- Total: 12 x 4 = 48 new translations
-- ================================================

-- ========================================
-- 1. REGISTRY > SCIENTIFIC (10 submenus)
-- ========================================

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 1.1 Doctorate Students
(gen_random_uuid(), 'menu', 'menu.registry.scientific.doctorate', 'Ilmiy tadqiqotchilar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.2 Dissertation Defense
(gen_random_uuid(), 'menu', 'menu.registry.scientific.dissertation', 'Dissertatsiya himoyasi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.3 Scientific Projects
(gen_random_uuid(), 'menu', 'menu.registry.scientific.project', 'Ilmiy loyihalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.4 Project Executors
(gen_random_uuid(), 'menu', 'menu.registry.scientific.executor', 'Loyiha ijrochilari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.5 Project Metadata
(gen_random_uuid(), 'menu', 'menu.registry.scientific.project_meta', 'Loyiha qiymati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.6 Scientific Publications
(gen_random_uuid(), 'menu', 'menu.registry.scientific.publication', 'Ilmiy nashrlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.7 Methodical Publications
(gen_random_uuid(), 'menu', 'menu.registry.scientific.methodical', 'Uslubiy nashrlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.8 Scientific Property
(gen_random_uuid(), 'menu', 'menu.registry.scientific.property', 'Ilmiy ishlanmalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.9 Publication Authors
(gen_random_uuid(), 'menu', 'menu.registry.scientific.author', 'Nashr mualliflari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.10 Research Activity
(gen_random_uuid(), 'menu', 'menu.registry.scientific.activity', 'Ilmiy faollik', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 2. REPORTS > UNIVERSITIES (2 submenus)
-- ========================================

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 2.1 Universities Count
(gen_random_uuid(), 'menu', 'menu.reports.universities.count', 'OTMlar umumiy soni', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.2 Faculty List
(gen_random_uuid(), 'menu', 'menu.reports.universities.faculty_list', 'Fakultetlar soni va ro''yxati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ================================================
-- TRANSLATIONS: Cyrillic (oz-UZ)
-- ================================================

INSERT INTO h_system_message_translation (message_id, language, translation)
SELECT m.id, 'oz-UZ',
  CASE m.message_key
    -- Registry > Scientific (10)
    WHEN 'menu.registry.scientific.doctorate' THEN 'Илмий тадқиқотчилар'
    WHEN 'menu.registry.scientific.dissertation' THEN 'Диссертация ҳимояси'
    WHEN 'menu.registry.scientific.project' THEN 'Илмий лойиҳалар'
    WHEN 'menu.registry.scientific.executor' THEN 'Лойиҳа ижрочилари'
    WHEN 'menu.registry.scientific.project_meta' THEN 'Лойиҳа қиймати'
    WHEN 'menu.registry.scientific.publication' THEN 'Илмий нашрлар'
    WHEN 'menu.registry.scientific.methodical' THEN 'Услубий нашрлар'
    WHEN 'menu.registry.scientific.property' THEN 'Илмий ишланмалар'
    WHEN 'menu.registry.scientific.author' THEN 'Нашр муаллифлари'
    WHEN 'menu.registry.scientific.activity' THEN 'Илмий фаоллик'

    -- Reports > Universities (2)
    WHEN 'menu.reports.universities.count' THEN 'ОТМлар умумий сони'
    WHEN 'menu.reports.universities.faculty_list' THEN 'Факультетлар сони ва рўйхати'

    ELSE m.message  -- Fallback
  END
FROM h_system_message m
WHERE m.category = 'menu'
  AND m.message_key IN (
    'menu.registry.scientific.doctorate', 'menu.registry.scientific.dissertation',
    'menu.registry.scientific.project', 'menu.registry.scientific.executor',
    'menu.registry.scientific.project_meta', 'menu.registry.scientific.publication',
    'menu.registry.scientific.methodical', 'menu.registry.scientific.property',
    'menu.registry.scientific.author', 'menu.registry.scientific.activity',
    'menu.reports.universities.count', 'menu.reports.universities.faculty_list'
  )
ON CONFLICT (message_id, language) DO UPDATE SET
  translation = EXCLUDED.translation,
  updated_at = CURRENT_TIMESTAMP;

-- ================================================
-- TRANSLATIONS: Russian (ru-RU)
-- ================================================

INSERT INTO h_system_message_translation (message_id, language, translation)
SELECT m.id, 'ru-RU',
  CASE m.message_key
    -- Registry > Scientific (10)
    WHEN 'menu.registry.scientific.doctorate' THEN 'Научные исследователи'
    WHEN 'menu.registry.scientific.dissertation' THEN 'Защита диссертаций'
    WHEN 'menu.registry.scientific.project' THEN 'Научные проекты'
    WHEN 'menu.registry.scientific.executor' THEN 'Исполнители проектов'
    WHEN 'menu.registry.scientific.project_meta' THEN 'Стоимость проекта'
    WHEN 'menu.registry.scientific.publication' THEN 'Научные публикации'
    WHEN 'menu.registry.scientific.methodical' THEN 'Методические публикации'
    WHEN 'menu.registry.scientific.property' THEN 'Научные разработки'
    WHEN 'menu.registry.scientific.author' THEN 'Авторы публикаций'
    WHEN 'menu.registry.scientific.activity' THEN 'Научная активность'

    -- Reports > Universities (2)
    WHEN 'menu.reports.universities.count' THEN 'Общее количество вузов'
    WHEN 'menu.reports.universities.faculty_list' THEN 'Количество и список факультетов'

    ELSE m.message  -- Fallback
  END
FROM h_system_message m
WHERE m.category = 'menu'
  AND m.message_key IN (
    'menu.registry.scientific.doctorate', 'menu.registry.scientific.dissertation',
    'menu.registry.scientific.project', 'menu.registry.scientific.executor',
    'menu.registry.scientific.project_meta', 'menu.registry.scientific.publication',
    'menu.registry.scientific.methodical', 'menu.registry.scientific.property',
    'menu.registry.scientific.author', 'menu.registry.scientific.activity',
    'menu.reports.universities.count', 'menu.reports.universities.faculty_list'
  )
ON CONFLICT (message_id, language) DO UPDATE SET
  translation = EXCLUDED.translation,
  updated_at = CURRENT_TIMESTAMP;

-- ================================================
-- TRANSLATIONS: English (en-US)
-- ================================================

INSERT INTO h_system_message_translation (message_id, language, translation)
SELECT m.id, 'en-US',
  CASE m.message_key
    -- Registry > Scientific (10)
    WHEN 'menu.registry.scientific.doctorate' THEN 'Doctoral Researchers'
    WHEN 'menu.registry.scientific.dissertation' THEN 'Dissertation Defense'
    WHEN 'menu.registry.scientific.project' THEN 'Scientific Projects'
    WHEN 'menu.registry.scientific.executor' THEN 'Project Executors'
    WHEN 'menu.registry.scientific.project_meta' THEN 'Project Cost'
    WHEN 'menu.registry.scientific.publication' THEN 'Scientific Publications'
    WHEN 'menu.registry.scientific.methodical' THEN 'Methodical Publications'
    WHEN 'menu.registry.scientific.property' THEN 'Research Developments'
    WHEN 'menu.registry.scientific.author' THEN 'Publication Authors'
    WHEN 'menu.registry.scientific.activity' THEN 'Research Activity'

    -- Reports > Universities (2)
    WHEN 'menu.reports.universities.count' THEN 'Total Number of Universities'
    WHEN 'menu.reports.universities.faculty_list' THEN 'Faculty Count and List'

    ELSE m.message  -- Fallback
  END
FROM h_system_message m
WHERE m.category = 'menu'
  AND m.message_key IN (
    'menu.registry.scientific.doctorate', 'menu.registry.scientific.dissertation',
    'menu.registry.scientific.project', 'menu.registry.scientific.executor',
    'menu.registry.scientific.project_meta', 'menu.registry.scientific.publication',
    'menu.registry.scientific.methodical', 'menu.registry.scientific.property',
    'menu.registry.scientific.author', 'menu.registry.scientific.activity',
    'menu.reports.universities.count', 'menu.reports.universities.faculty_list'
  )
ON CONFLICT (message_id, language) DO UPDATE SET
  translation = EXCLUDED.translation,
  updated_at = CURRENT_TIMESTAMP;
