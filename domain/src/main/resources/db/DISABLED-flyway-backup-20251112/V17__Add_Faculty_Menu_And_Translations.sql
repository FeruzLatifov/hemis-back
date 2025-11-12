-- ========================================
-- V17: Add Faculty Registry Menu & Translations
-- ========================================
-- Author: AI Assistant
-- Date: 2025-11-11
-- Purpose: Add i18n translations for Faculty Registry module
-- Categories: menu.*, table.faculty.*, filters.*, actions.*, empty.*, pagination.*, errors.*
-- ========================================

-- ========================================
-- 1. MENU TRANSLATIONS
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'menu', 'menu.registry.faculty', 'Fakultet', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO UPDATE SET
    message = EXCLUDED.message,
    updated_at = CURRENT_TIMESTAMP;

-- Menu translations
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT 
    sm.id,
    lang.code,
    CASE 
        WHEN lang.code = 'oz-UZ' THEN 'Факультет'
        WHEN lang.code = 'ru-RU' THEN 'Факультет'
        WHEN lang.code = 'en-US' THEN 'Faculty'
        ELSE sm.message
    END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM h_system_message sm
CROSS JOIN (
    SELECT 'oz-UZ' as code
    UNION ALL SELECT 'ru-RU'
    UNION ALL SELECT 'en-US'
) lang
WHERE sm.message_key = 'menu.registry.faculty'
ON CONFLICT (message_id, language) DO UPDATE SET
    translation = EXCLUDED.translation,
    updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 2. TABLE COLUMN TRANSLATIONS (uz-UZ)
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'table', 'table.faculty.universityName', 'OTM nomi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.code', 'Kod', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.nameUz', 'Nomi (o''zbekcha)', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.nameRu', 'Nomi (ruscha)', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.status', 'Holati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.facultyCount', 'Fakultetlar soni', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 3. CYRILLIC (oz-UZ) TRANSLATIONS
-- ========================================
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT
    sm.id,
    'oz-UZ',
    CASE sm.message_key
        WHEN 'table.faculty.universityName' THEN 'ОТМ номи'
        WHEN 'table.faculty.code' THEN 'Код'
        WHEN 'table.faculty.nameUz' THEN 'Номи (ўзбекча)'
        WHEN 'table.faculty.nameRu' THEN 'Номи (русча)'
        WHEN 'table.faculty.status' THEN 'Ҳолати'
        WHEN 'table.faculty.facultyCount' THEN 'Факультетлар сони'
    END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN (
    'table.faculty.universityName', 'table.faculty.code', 'table.faculty.nameUz',
    'table.faculty.nameRu', 'table.faculty.status', 'table.faculty.facultyCount'
)
ON CONFLICT (message_id, language) DO UPDATE SET
    translation = EXCLUDED.translation,
    updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 4. RUSSIAN (ru-RU) TRANSLATIONS
-- ========================================
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT
    sm.id,
    'ru-RU',
    CASE sm.message_key
        WHEN 'table.faculty.universityName' THEN 'Название ВУЗа'
        WHEN 'table.faculty.code' THEN 'Код'
        WHEN 'table.faculty.nameUz' THEN 'Название (узбекский)'
        WHEN 'table.faculty.nameRu' THEN 'Название (русский)'
        WHEN 'table.faculty.status' THEN 'Статус'
        WHEN 'table.faculty.facultyCount' THEN 'Количество факультетов'
    END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN (
    'table.faculty.universityName', 'table.faculty.code', 'table.faculty.nameUz',
    'table.faculty.nameRu', 'table.faculty.status', 'table.faculty.facultyCount'
)
ON CONFLICT (message_id, language) DO UPDATE SET
    translation = EXCLUDED.translation,
    updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 5. ENGLISH (en-US) TRANSLATIONS
-- ========================================
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT
    sm.id,
    'en-US',
    CASE sm.message_key
        WHEN 'table.faculty.universityName' THEN 'University Name'
        WHEN 'table.faculty.code' THEN 'Code'
        WHEN 'table.faculty.nameUz' THEN 'Name (Uzbek)'
        WHEN 'table.faculty.nameRu' THEN 'Name (Russian)'
        WHEN 'table.faculty.status' THEN 'Status'
        WHEN 'table.faculty.facultyCount' THEN 'Faculty Count'
    END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN (
    'table.faculty.universityName', 'table.faculty.code', 'table.faculty.nameUz',
    'table.faculty.nameRu', 'table.faculty.status', 'table.faculty.facultyCount'
)
ON CONFLICT (message_id, language) DO UPDATE SET
    translation = EXCLUDED.translation,
    updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 6. FILTER TRANSLATIONS (uz-UZ)
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'filters', 'filters.title', 'Filtrlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'filters', 'filters.searchPlaceholder', 'Kod, nom yoki INN bo''yicha qidirish...', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'filters', 'filters.status', 'Holat', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'filters', 'filters.statusActive', 'Faol', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'filters', 'filters.statusInactive', 'Nofaol', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Filter translations (oz-UZ)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, 'oz-UZ',
    CASE sm.message_key
        WHEN 'filters.title' THEN 'Филтрлар'
        WHEN 'filters.searchPlaceholder' THEN 'Код, ном ёки ИНН бўйича қидириш...'
        WHEN 'filters.status' THEN 'Ҳолат'
        WHEN 'filters.statusActive' THEN 'Фаол'
        WHEN 'filters.statusInactive' THEN 'Нофаол'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN ('filters.title', 'filters.searchPlaceholder', 'filters.status', 'filters.statusActive', 'filters.statusInactive')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

-- Filter translations (ru-RU)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, 'ru-RU',
    CASE sm.message_key
        WHEN 'filters.title' THEN 'Фильтры'
        WHEN 'filters.searchPlaceholder' THEN 'Поиск по коду, названию или ИНН...'
        WHEN 'filters.status' THEN 'Статус'
        WHEN 'filters.statusActive' THEN 'Активный'
        WHEN 'filters.statusInactive' THEN 'Неактивный'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN ('filters.title', 'filters.searchPlaceholder', 'filters.status', 'filters.statusActive', 'filters.statusInactive')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

-- Filter translations (en-US)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, 'en-US',
    CASE sm.message_key
        WHEN 'filters.title' THEN 'Filters'
        WHEN 'filters.searchPlaceholder' THEN 'Search by code, name or TIN...'
        WHEN 'filters.status' THEN 'Status'
        WHEN 'filters.statusActive' THEN 'Active'
        WHEN 'filters.statusInactive' THEN 'Inactive'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN ('filters.title', 'filters.searchPlaceholder', 'filters.status', 'filters.statusActive', 'filters.statusInactive')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 7. ACTION TRANSLATIONS
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'actions', 'actions.refresh', 'Yangilash', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'actions', 'actions.export', 'Export', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'actions', 'actions.exportExcel', 'Excel yuklab olish', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'columns', 'columns.title', 'Ustunlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Actions (oz-UZ, ru-RU, en-US)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, lang.code,
    CASE
        WHEN sm.message_key = 'actions.refresh' AND lang.code = 'oz-UZ' THEN 'Янгилаш'
        WHEN sm.message_key = 'actions.refresh' AND lang.code = 'ru-RU' THEN 'Обновить'
        WHEN sm.message_key = 'actions.refresh' AND lang.code = 'en-US' THEN 'Refresh'
        WHEN sm.message_key = 'actions.export' AND lang.code = 'oz-UZ' THEN 'Экспорт'
        WHEN sm.message_key = 'actions.export' AND lang.code = 'ru-RU' THEN 'Экспорт'
        WHEN sm.message_key = 'actions.export' AND lang.code = 'en-US' THEN 'Export'
        WHEN sm.message_key = 'actions.exportExcel' AND lang.code = 'oz-UZ' THEN 'Excel юклаб олиш'
        WHEN sm.message_key = 'actions.exportExcel' AND lang.code = 'ru-RU' THEN 'Скачать Excel'
        WHEN sm.message_key = 'actions.exportExcel' AND lang.code = 'en-US' THEN 'Download Excel'
        WHEN sm.message_key = 'columns.title' AND lang.code = 'oz-UZ' THEN 'Устунлар'
        WHEN sm.message_key = 'columns.title' AND lang.code = 'ru-RU' THEN 'Колонки'
        WHEN sm.message_key = 'columns.title' AND lang.code = 'en-US' THEN 'Columns'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
CROSS JOIN (SELECT 'oz-UZ' as code UNION ALL SELECT 'ru-RU' UNION ALL SELECT 'en-US') lang
WHERE sm.message_key IN ('actions.refresh', 'actions.export', 'actions.exportExcel', 'columns.title')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 8. EMPTY STATE & ERROR TRANSLATIONS
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'empty', 'empty.noData', 'Ma''lumotlar topilmadi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'empty', 'empty.noFaculties', 'Fakultetlar topilmadi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'errors', 'errors.loadFailed', 'Ma''lumotlarni yuklashda xatolik', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'errors', 'errors.exportFailed', 'Eksport qilishda xatolik', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'pagination', 'pagination.rowsPerPage', 'Sahifadagi qatorlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'pagination', 'pagination.of', 'dan', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Empty/Error/Pagination translations (all languages)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, lang.code,
    CASE
        WHEN sm.message_key = 'empty.noData' AND lang.code = 'oz-UZ' THEN 'Маълумотлар топилмади'
        WHEN sm.message_key = 'empty.noData' AND lang.code = 'ru-RU' THEN 'Данные не найдены'
        WHEN sm.message_key = 'empty.noData' AND lang.code = 'en-US' THEN 'No data found'
        WHEN sm.message_key = 'empty.noFaculties' AND lang.code = 'oz-UZ' THEN 'Факультетлар топилмади'
        WHEN sm.message_key = 'empty.noFaculties' AND lang.code = 'ru-RU' THEN 'Факультеты не найдены'
        WHEN sm.message_key = 'empty.noFaculties' AND lang.code = 'en-US' THEN 'No faculties found'
        WHEN sm.message_key = 'errors.loadFailed' AND lang.code = 'oz-UZ' THEN 'Маълумотларни юклашда хатолик'
        WHEN sm.message_key = 'errors.loadFailed' AND lang.code = 'ru-RU' THEN 'Ошибка загрузки данных'
        WHEN sm.message_key = 'errors.loadFailed' AND lang.code = 'en-US' THEN 'Failed to load data'
        WHEN sm.message_key = 'errors.exportFailed' AND lang.code = 'oz-UZ' THEN 'Экспорт қилишда хатолик'
        WHEN sm.message_key = 'errors.exportFailed' AND lang.code = 'ru-RU' THEN 'Ошибка экспорта'
        WHEN sm.message_key = 'errors.exportFailed' AND lang.code = 'en-US' THEN 'Export failed'
        WHEN sm.message_key = 'pagination.rowsPerPage' AND lang.code = 'oz-UZ' THEN 'Саҳифадаги қаторлар'
        WHEN sm.message_key = 'pagination.rowsPerPage' AND lang.code = 'ru-RU' THEN 'Строк на странице'
        WHEN sm.message_key = 'pagination.rowsPerPage' AND lang.code = 'en-US' THEN 'Rows per page'
        WHEN sm.message_key = 'pagination.of' AND lang.code = 'oz-UZ' THEN 'дан'
        WHEN sm.message_key = 'pagination.of' AND lang.code = 'ru-RU' THEN 'из'
        WHEN sm.message_key = 'pagination.of' AND lang.code = 'en-US' THEN 'of'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
CROSS JOIN (SELECT 'oz-UZ' as code UNION ALL SELECT 'ru-RU' UNION ALL SELECT 'en-US') lang
WHERE sm.message_key IN ('empty.noData', 'empty.noFaculties', 'errors.loadFailed', 'errors.exportFailed', 'pagination.rowsPerPage', 'pagination.of')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- Migration Complete
-- ========================================
-- Menu item updated: /registry/faculty
-- Translations added: menu.*, table.faculty.*, filters.*, actions.*, empty.*, errors.*, pagination.*
-- Languages: uz-UZ (default), oz-UZ (cyrillic), ru-RU, en-US
-- ========================================

