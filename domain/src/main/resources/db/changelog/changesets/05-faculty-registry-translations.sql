-- liquibase formatted sql

-- changeset ai-assistant:5-faculty-registry-translations-1
-- comment: Add Faculty Registry translations (uz-UZ, oz-UZ, ru-RU, en-US)

-- ========================================
-- 1. MENU TRANSLATIONS
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'menu', 'menu.registry.faculty', 'Fakultet', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO UPDATE SET
    message = EXCLUDED.message,
    updated_at = CURRENT_TIMESTAMP;

-- Menu translations (oz-UZ, ru-RU, en-US)
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
-- 2. TABLE COLUMN TRANSLATIONS (uz-UZ base)
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'table', 'table.actions', 'Amallar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.universityName', 'OTM nomi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.universityCode', 'OTM kodi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.code', 'Kod', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.nameUz', 'Nomi (o''zbekcha)', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.nameRu', 'Nomi (ruscha)', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.status', 'Holati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'table', 'table.faculty.facultyCount', 'Fakultetlar soni', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO UPDATE SET message = EXCLUDED.message, updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 3. CYRILLIC (oz-UZ) TRANSLATIONS
-- ========================================
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT
    sm.id,
    'oz-UZ',
    CASE sm.message_key
        WHEN 'table.actions' THEN 'Амаллар'
        WHEN 'table.faculty.universityName' THEN 'ОТМ номи'
        WHEN 'table.faculty.universityCode' THEN 'ОТМ коди'
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
    'table.actions', 'table.faculty.universityName', 'table.faculty.universityCode',
    'table.faculty.code', 'table.faculty.nameUz', 'table.faculty.nameRu', 
    'table.faculty.status', 'table.faculty.facultyCount'
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
        WHEN 'table.actions' THEN 'Действия'
        WHEN 'table.faculty.universityName' THEN 'Название ВУЗа'
        WHEN 'table.faculty.universityCode' THEN 'Код ВУЗа'
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
    'table.actions', 'table.faculty.universityName', 'table.faculty.universityCode',
    'table.faculty.code', 'table.faculty.nameUz', 'table.faculty.nameRu',
    'table.faculty.status', 'table.faculty.facultyCount'
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
        WHEN 'table.actions' THEN 'Actions'
        WHEN 'table.faculty.universityName' THEN 'University Name'
        WHEN 'table.faculty.universityCode' THEN 'University Code'
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
    'table.actions', 'table.faculty.universityName', 'table.faculty.universityCode',
    'table.faculty.code', 'table.faculty.nameUz', 'table.faculty.nameRu',
    'table.faculty.status', 'table.faculty.facultyCount'
)
ON CONFLICT (message_id, language) DO UPDATE SET
    translation = EXCLUDED.translation,
    updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 6. ACTIONS TRANSLATIONS
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'actions', 'actions.view', 'Ko''rish', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'actions', 'actions.close', 'Yopish', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO UPDATE SET message = EXCLUDED.message, updated_at = CURRENT_TIMESTAMP;

-- Actions translations (all languages)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, lang.code,
    CASE
        WHEN sm.message_key = 'actions.view' AND lang.code = 'oz-UZ' THEN 'Кўриш'
        WHEN sm.message_key = 'actions.view' AND lang.code = 'ru-RU' THEN 'Просмотр'
        WHEN sm.message_key = 'actions.view' AND lang.code = 'en-US' THEN 'View'
        WHEN sm.message_key = 'actions.close' AND lang.code = 'oz-UZ' THEN 'Ёпиш'
        WHEN sm.message_key = 'actions.close' AND lang.code = 'ru-RU' THEN 'Закрыть'
        WHEN sm.message_key = 'actions.close' AND lang.code = 'en-US' THEN 'Close'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
CROSS JOIN (SELECT 'oz-UZ' as code UNION ALL SELECT 'ru-RU' UNION ALL SELECT 'en-US') lang
WHERE sm.message_key IN ('actions.view', 'actions.close')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

-- ========================================
-- 7. DETAILS DRAWER TRANSLATIONS
-- ========================================
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'details', 'details.basicInfo', 'Asosiy ma''lumotlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'details', 'details.auditInfo', 'Audit ma''lumotlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'details', 'details.shortName', 'Qisqa nomi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'details', 'details.facultyType', 'Fakultet turi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'details', 'details.createdAt', 'Yaratilgan', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'details', 'details.createdBy', 'Yaratuvchi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'details', 'details.updatedAt', 'Yangilangan', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'details', 'details.updatedBy', 'Yangilovchi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO UPDATE SET message = EXCLUDED.message, updated_at = CURRENT_TIMESTAMP;

-- Details translations (oz-UZ)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, 'oz-UZ',
    CASE sm.message_key
        WHEN 'details.basicInfo' THEN 'Асосий маълумотлар'
        WHEN 'details.auditInfo' THEN 'Аудит маълумотлари'
        WHEN 'details.shortName' THEN 'Қисқа номи'
        WHEN 'details.facultyType' THEN 'Факультет тури'
        WHEN 'details.createdAt' THEN 'Яратилган'
        WHEN 'details.createdBy' THEN 'Яратувчи'
        WHEN 'details.updatedAt' THEN 'Янгиланган'
        WHEN 'details.updatedBy' THEN 'Янгиловчи'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN ('details.basicInfo', 'details.auditInfo', 'details.shortName', 'details.facultyType',
    'details.createdAt', 'details.createdBy', 'details.updatedAt', 'details.updatedBy')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

-- Details translations (ru-RU)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, 'ru-RU',
    CASE sm.message_key
        WHEN 'details.basicInfo' THEN 'Основная информация'
        WHEN 'details.auditInfo' THEN 'Аудит'
        WHEN 'details.shortName' THEN 'Краткое название'
        WHEN 'details.facultyType' THEN 'Тип факультета'
        WHEN 'details.createdAt' THEN 'Создано'
        WHEN 'details.createdBy' THEN 'Создал'
        WHEN 'details.updatedAt' THEN 'Обновлено'
        WHEN 'details.updatedBy' THEN 'Обновил'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN ('details.basicInfo', 'details.auditInfo', 'details.shortName', 'details.facultyType',
    'details.createdAt', 'details.createdBy', 'details.updatedAt', 'details.updatedBy')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

-- Details translations (en-US)
INSERT INTO h_system_message_translation (message_id, language, translation, created_at, updated_at)
SELECT sm.id, 'en-US',
    CASE sm.message_key
        WHEN 'details.basicInfo' THEN 'Basic Information'
        WHEN 'details.auditInfo' THEN 'Audit Information'
        WHEN 'details.shortName' THEN 'Short Name'
        WHEN 'details.facultyType' THEN 'Faculty Type'
        WHEN 'details.createdAt' THEN 'Created At'
        WHEN 'details.createdBy' THEN 'Created By'
        WHEN 'details.updatedAt' THEN 'Updated At'
        WHEN 'details.updatedBy' THEN 'Updated By'
    END, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM h_system_message sm
WHERE sm.message_key IN ('details.basicInfo', 'details.auditInfo', 'details.shortName', 'details.facultyType',
    'details.createdAt', 'details.createdBy', 'details.updatedAt', 'details.updatedBy')
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation, updated_at = CURRENT_TIMESTAMP;

