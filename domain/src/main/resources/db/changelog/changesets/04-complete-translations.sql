-- =====================================================
-- Menu Translation System (Using SystemMessage)
-- =====================================================
-- Using existing system_messages and system_message_translations tables
-- for menu labels to maintain single translation system
--
-- Architecture:
-- 1. system_messages - stores default Uzbek (uz-UZ) message
-- 2. system_message_translations - stores translations (ru-RU, en-US)
-- =====================================================

-- =====================================================
-- Menu Messages - Default (Uzbek - uz-UZ)
-- =====================================================

-- Dashboard
INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES (gen_random_uuid(), 'menu', 'menu.dashboard', 'Bosh sahifa', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Registry Module (4 messages)
INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'menu', 'menu.registry', 'Reestlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr', 'E-Reestr', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.registry.scientific', 'Ilmiy reestr', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.registry.student_meta', 'Talaba ma''lumotlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Rating Module (14 messages)
INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'menu', 'menu.rating', 'Reyting', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.administrative', 'Administrativ reyting', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.administrative.employee', 'Xodimlar reytingi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.administrative.students', 'Talabalar reytingi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.administrative.sport', 'Sport reytingi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.academic', 'Akademik reyting', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.academic.methodical', 'Uslubiy nashrlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.academic.study', 'O''quv reytingi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.academic.verification', 'Tekshirish turi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.scientific', 'Ilmiy reyting', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.scientific.publications', 'Ilmiy nashrlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.scientific.projects', 'Ilmiy loyihalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.scientific.intellectual', 'Intellektual mulk', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.rating.student_gpa', 'Talaba GPA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Data Module (10 messages)
INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'menu', 'menu.data', 'Ma''lumotlar bazasi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.general', 'Umumiy ma''lumotlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.structure', 'Tuzilma', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.employee', 'Xodimlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.student', 'Talabalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.education', 'Ta''lim', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.study', 'O''qish jarayoni', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.science', 'Fan', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.organizational', 'Tashkiliy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.data.contract_category', 'Shartnoma turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Reports Module (25 messages)
INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'menu', 'menu.reports', 'Hisobotlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.universities', 'OTM hisobotlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.employees', 'Xodimlar hisobotlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.employees.navigation', 'Xodimlar ro''yxati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.employees.private', 'Xodimlar shaxsiy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.employees.work', 'Xodimlar ish', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.students', 'Talabalar hisobotlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.students.statistics', 'Talabalar statistikasi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.students.education', 'Talabalar ta''limi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.students.private', 'Talabalar shaxsiy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.students.attendance', 'Davomat', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.students.score', 'Baholar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.students.dynamic', 'Dinamika', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.academic', 'Akademik hisobotlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.academic.study', 'O''quv hisoboti', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.research', 'Tadqiqot hisobotlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.research.project', 'Tadqiqot loyihalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.research.publication', 'Tadqiqot nashlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.research.researcher', 'Tadqiqotchilar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.economic', 'Iqtisodiy hisobotlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.economic.finance', 'Moliya', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.reports.economic.xujalik', 'Xo''jalik', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- System Module (6 messages)
INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'menu', 'menu.system', 'Tizim', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.system.temp', 'Vaqtinchalik', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.system.translation', 'Tarjimalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.system.university_users', 'OTM foydalanuvchilari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.system.api_logs', 'API log''lar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.system.report_update', 'Hisobot yangilanishlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- =====================================================
-- Menu Translations - Russian (ru-RU)
-- =====================================================

INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'ru-RU',
  CASE m.message_key
    -- Dashboard
    WHEN 'menu.dashboard' THEN 'Главная'

    -- Registry
    WHEN 'menu.registry' THEN 'Реестры'
    WHEN 'menu.registry.e_reestr' THEN 'Э-Реестр'
    WHEN 'menu.registry.scientific' THEN 'Научный реестр'
    WHEN 'menu.registry.student_meta' THEN 'Данные студента'

    -- Rating
    WHEN 'menu.rating' THEN 'Рейтинг'
    WHEN 'menu.rating.administrative' THEN 'Административный рейтинг'
    WHEN 'menu.rating.administrative.employee' THEN 'Рейтинг сотрудников'
    WHEN 'menu.rating.administrative.students' THEN 'Рейтинг студентов'
    WHEN 'menu.rating.administrative.sport' THEN 'Спортивный рейтинг'
    WHEN 'menu.rating.academic' THEN 'Академический рейтинг'
    WHEN 'menu.rating.academic.methodical' THEN 'Методические публикации'
    WHEN 'menu.rating.academic.study' THEN 'Учебный рейтинг'
    WHEN 'menu.rating.academic.verification' THEN 'Тип проверки'
    WHEN 'menu.rating.scientific' THEN 'Научный рейтинг'
    WHEN 'menu.rating.scientific.publications' THEN 'Научные публикации'
    WHEN 'menu.rating.scientific.projects' THEN 'Научные проекты'
    WHEN 'menu.rating.scientific.intellectual' THEN 'Интеллектуальная собственность'
    WHEN 'menu.rating.student_gpa' THEN 'GPA студента'

    -- Data
    WHEN 'menu.data' THEN 'База данных'
    WHEN 'menu.data.general' THEN 'Общие данные'
    WHEN 'menu.data.structure' THEN 'Структура'
    WHEN 'menu.data.employee' THEN 'Сотрудники'
    WHEN 'menu.data.student' THEN 'Студенты'
    WHEN 'menu.data.education' THEN 'Образование'
    WHEN 'menu.data.study' THEN 'Учебный процесс'
    WHEN 'menu.data.science' THEN 'Наука'
    WHEN 'menu.data.organizational' THEN 'Организационный'
    WHEN 'menu.data.contract_category' THEN 'Типы договоров'

    -- Reports
    WHEN 'menu.reports' THEN 'Отчеты'
    WHEN 'menu.reports.universities' THEN 'Отчеты ВУЗов'
    WHEN 'menu.reports.employees' THEN 'Отчеты сотрудников'
    WHEN 'menu.reports.employees.navigation' THEN 'Список сотрудников'
    WHEN 'menu.reports.employees.private' THEN 'Личные данные сотрудников'
    WHEN 'menu.reports.employees.work' THEN 'Работа сотрудников'
    WHEN 'menu.reports.students' THEN 'Отчеты студентов'
    WHEN 'menu.reports.students.statistics' THEN 'Статистика студентов'
    WHEN 'menu.reports.students.education' THEN 'Образование студентов'
    WHEN 'menu.reports.students.private' THEN 'Личные данные студентов'
    WHEN 'menu.reports.students.attendance' THEN 'Посещаемость'
    WHEN 'menu.reports.students.score' THEN 'Оценки'
    WHEN 'menu.reports.students.dynamic' THEN 'Динамика'
    WHEN 'menu.reports.academic' THEN 'Академические отчеты'
    WHEN 'menu.reports.academic.study' THEN 'Учебный отчет'
    WHEN 'menu.reports.research' THEN 'Исследовательские отчеты'
    WHEN 'menu.reports.research.project' THEN 'Исследовательские проекты'
    WHEN 'menu.reports.research.publication' THEN 'Исследовательские публикации'
    WHEN 'menu.reports.research.researcher' THEN 'Исследователи'
    WHEN 'menu.reports.economic' THEN 'Экономические отчеты'
    WHEN 'menu.reports.economic.finance' THEN 'Финансы'
    WHEN 'menu.reports.economic.xujalik' THEN 'Хозяйство'

    -- System
    WHEN 'menu.system' THEN 'Система'
    WHEN 'menu.system.temp' THEN 'Временные'
    WHEN 'menu.system.translation' THEN 'Переводы'
    WHEN 'menu.system.university_users' THEN 'Пользователи ВУЗа'
    WHEN 'menu.system.api_logs' THEN 'API логи'
    WHEN 'menu.system.report_update' THEN 'Обновления отчетов'
    ELSE m.message  -- Fallback to default Uzbek message
  END
FROM system_messages m
WHERE m.category = 'menu'
  AND m.message_key LIKE 'menu.%'
  AND CASE m.message_key
    WHEN 'menu.dashboard' THEN TRUE
    WHEN 'menu.registry' THEN TRUE
    WHEN 'menu.registry.e_reestr' THEN TRUE
    WHEN 'menu.registry.scientific' THEN TRUE
    WHEN 'menu.registry.student_meta' THEN TRUE
    WHEN 'menu.rating' THEN TRUE
    WHEN 'menu.rating.administrative' THEN TRUE
    WHEN 'menu.rating.administrative.employee' THEN TRUE
    WHEN 'menu.rating.administrative.students' THEN TRUE
    WHEN 'menu.rating.administrative.sport' THEN TRUE
    WHEN 'menu.rating.academic' THEN TRUE
    WHEN 'menu.rating.academic.methodical' THEN TRUE
    WHEN 'menu.rating.academic.study' THEN TRUE
    WHEN 'menu.rating.academic.verification' THEN TRUE
    WHEN 'menu.rating.scientific' THEN TRUE
    WHEN 'menu.rating.scientific.publications' THEN TRUE
    WHEN 'menu.rating.scientific.projects' THEN TRUE
    WHEN 'menu.rating.scientific.intellectual' THEN TRUE
    WHEN 'menu.rating.student_gpa' THEN TRUE
    WHEN 'menu.data' THEN TRUE
    WHEN 'menu.data.general' THEN TRUE
    WHEN 'menu.data.structure' THEN TRUE
    WHEN 'menu.data.employee' THEN TRUE
    WHEN 'menu.data.student' THEN TRUE
    WHEN 'menu.data.education' THEN TRUE
    WHEN 'menu.data.study' THEN TRUE
    WHEN 'menu.data.science' THEN TRUE
    WHEN 'menu.data.organizational' THEN TRUE
    WHEN 'menu.data.contract_category' THEN TRUE
    WHEN 'menu.reports' THEN TRUE
    WHEN 'menu.reports.universities' THEN TRUE
    WHEN 'menu.reports.employees' THEN TRUE
    WHEN 'menu.reports.employees.navigation' THEN TRUE
    WHEN 'menu.reports.employees.private' THEN TRUE
    WHEN 'menu.reports.employees.work' THEN TRUE
    WHEN 'menu.reports.students' THEN TRUE
    WHEN 'menu.reports.students.statistics' THEN TRUE
    WHEN 'menu.reports.students.education' THEN TRUE
    WHEN 'menu.reports.students.private' THEN TRUE
    WHEN 'menu.reports.students.attendance' THEN TRUE
    WHEN 'menu.reports.students.score' THEN TRUE
    WHEN 'menu.reports.students.dynamic' THEN TRUE
    WHEN 'menu.reports.academic' THEN TRUE
    WHEN 'menu.reports.academic.study' THEN TRUE
    WHEN 'menu.reports.research' THEN TRUE
    WHEN 'menu.reports.research.project' THEN TRUE
    WHEN 'menu.reports.research.publication' THEN TRUE
    WHEN 'menu.reports.research.researcher' THEN TRUE
    WHEN 'menu.reports.economic' THEN TRUE
    WHEN 'menu.reports.economic.finance' THEN TRUE
    WHEN 'menu.reports.economic.xujalik' THEN TRUE
    WHEN 'menu.system' THEN TRUE
    WHEN 'menu.system.temp' THEN TRUE
    WHEN 'menu.system.translation' THEN TRUE
    WHEN 'menu.system.university_users' THEN TRUE
    WHEN 'menu.system.api_logs' THEN TRUE
    WHEN 'menu.system.report_update' THEN TRUE
    ELSE FALSE
  END = TRUE
ON CONFLICT (message_id, language) DO NOTHING;

-- =====================================================
-- Menu Translations - English (en-US)
-- =====================================================

INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'en-US',
  CASE m.message_key
    -- Dashboard
    WHEN 'menu.dashboard' THEN 'Dashboard'

    -- Registry
    WHEN 'menu.registry' THEN 'Registries'
    WHEN 'menu.registry.e_reestr' THEN 'E-Registry'
    WHEN 'menu.registry.scientific' THEN 'Scientific Registry'
    WHEN 'menu.registry.student_meta' THEN 'Student Data'

    -- Rating
    WHEN 'menu.rating' THEN 'Rating'
    WHEN 'menu.rating.administrative' THEN 'Administrative Rating'
    WHEN 'menu.rating.administrative.employee' THEN 'Employee Rating'
    WHEN 'menu.rating.administrative.students' THEN 'Student Rating'
    WHEN 'menu.rating.administrative.sport' THEN 'Sport Rating'
    WHEN 'menu.rating.academic' THEN 'Academic Rating'
    WHEN 'menu.rating.academic.methodical' THEN 'Methodical Publications'
    WHEN 'menu.rating.academic.study' THEN 'Study Rating'
    WHEN 'menu.rating.academic.verification' THEN 'Verification Type'
    WHEN 'menu.rating.scientific' THEN 'Scientific Rating'
    WHEN 'menu.rating.scientific.publications' THEN 'Scientific Publications'
    WHEN 'menu.rating.scientific.projects' THEN 'Scientific Projects'
    WHEN 'menu.rating.scientific.intellectual' THEN 'Intellectual Property'
    WHEN 'menu.rating.student_gpa' THEN 'Student GPA'

    -- Data
    WHEN 'menu.data' THEN 'Database'
    WHEN 'menu.data.general' THEN 'General Data'
    WHEN 'menu.data.structure' THEN 'Structure'
    WHEN 'menu.data.employee' THEN 'Employees'
    WHEN 'menu.data.student' THEN 'Students'
    WHEN 'menu.data.education' THEN 'Education'
    WHEN 'menu.data.study' THEN 'Study Process'
    WHEN 'menu.data.science' THEN 'Science'
    WHEN 'menu.data.organizational' THEN 'Organizational'
    WHEN 'menu.data.contract_category' THEN 'Contract Types'

    -- Reports
    WHEN 'menu.reports' THEN 'Reports'
    WHEN 'menu.reports.universities' THEN 'University Reports'
    WHEN 'menu.reports.employees' THEN 'Employee Reports'
    WHEN 'menu.reports.employees.navigation' THEN 'Employee List'
    WHEN 'menu.reports.employees.private' THEN 'Employee Personal'
    WHEN 'menu.reports.employees.work' THEN 'Employee Work'
    WHEN 'menu.reports.students' THEN 'Student Reports'
    WHEN 'menu.reports.students.statistics' THEN 'Student Statistics'
    WHEN 'menu.reports.students.education' THEN 'Student Education'
    WHEN 'menu.reports.students.private' THEN 'Student Personal'
    WHEN 'menu.reports.students.attendance' THEN 'Attendance'
    WHEN 'menu.reports.students.score' THEN 'Grades'
    WHEN 'menu.reports.students.dynamic' THEN 'Dynamics'
    WHEN 'menu.reports.academic' THEN 'Academic Reports'
    WHEN 'menu.reports.academic.study' THEN 'Study Report'
    WHEN 'menu.reports.research' THEN 'Research Reports'
    WHEN 'menu.reports.research.project' THEN 'Research Projects'
    WHEN 'menu.reports.research.publication' THEN 'Research Publications'
    WHEN 'menu.reports.research.researcher' THEN 'Researchers'
    WHEN 'menu.reports.economic' THEN 'Economic Reports'
    WHEN 'menu.reports.economic.finance' THEN 'Finance'
    WHEN 'menu.reports.economic.xujalik' THEN 'Economy'

    -- System
    WHEN 'menu.system' THEN 'System'
    WHEN 'menu.system.temp' THEN 'Temporary'
    WHEN 'menu.system.translation' THEN 'Translations'
    WHEN 'menu.system.university_users' THEN 'University Users'
    WHEN 'menu.system.api_logs' THEN 'API Logs'
    WHEN 'menu.system.report_update' THEN 'Report Updates'
    ELSE m.message  -- Fallback to default Uzbek message
  END
FROM system_messages m
WHERE m.category = 'menu'
  AND m.message_key LIKE 'menu.%'
  AND CASE m.message_key
    WHEN 'menu.dashboard' THEN TRUE
    WHEN 'menu.registry' THEN TRUE
    WHEN 'menu.registry.e_reestr' THEN TRUE
    WHEN 'menu.registry.scientific' THEN TRUE
    WHEN 'menu.registry.student_meta' THEN TRUE
    WHEN 'menu.rating' THEN TRUE
    WHEN 'menu.rating.administrative' THEN TRUE
    WHEN 'menu.rating.administrative.employee' THEN TRUE
    WHEN 'menu.rating.administrative.students' THEN TRUE
    WHEN 'menu.rating.administrative.sport' THEN TRUE
    WHEN 'menu.rating.academic' THEN TRUE
    WHEN 'menu.rating.academic.methodical' THEN TRUE
    WHEN 'menu.rating.academic.study' THEN TRUE
    WHEN 'menu.rating.academic.verification' THEN TRUE
    WHEN 'menu.rating.scientific' THEN TRUE
    WHEN 'menu.rating.scientific.publications' THEN TRUE
    WHEN 'menu.rating.scientific.projects' THEN TRUE
    WHEN 'menu.rating.scientific.intellectual' THEN TRUE
    WHEN 'menu.rating.student_gpa' THEN TRUE
    WHEN 'menu.data' THEN TRUE
    WHEN 'menu.data.general' THEN TRUE
    WHEN 'menu.data.structure' THEN TRUE
    WHEN 'menu.data.employee' THEN TRUE
    WHEN 'menu.data.student' THEN TRUE
    WHEN 'menu.data.education' THEN TRUE
    WHEN 'menu.data.study' THEN TRUE
    WHEN 'menu.data.science' THEN TRUE
    WHEN 'menu.data.organizational' THEN TRUE
    WHEN 'menu.data.contract_category' THEN TRUE
    WHEN 'menu.reports' THEN TRUE
    WHEN 'menu.reports.universities' THEN TRUE
    WHEN 'menu.reports.employees' THEN TRUE
    WHEN 'menu.reports.employees.navigation' THEN TRUE
    WHEN 'menu.reports.employees.private' THEN TRUE
    WHEN 'menu.reports.employees.work' THEN TRUE
    WHEN 'menu.reports.students' THEN TRUE
    WHEN 'menu.reports.students.statistics' THEN TRUE
    WHEN 'menu.reports.students.education' THEN TRUE
    WHEN 'menu.reports.students.private' THEN TRUE
    WHEN 'menu.reports.students.attendance' THEN TRUE
    WHEN 'menu.reports.students.score' THEN TRUE
    WHEN 'menu.reports.students.dynamic' THEN TRUE
    WHEN 'menu.reports.academic' THEN TRUE
    WHEN 'menu.reports.academic.study' THEN TRUE
    WHEN 'menu.reports.research' THEN TRUE
    WHEN 'menu.reports.research.project' THEN TRUE
    WHEN 'menu.reports.research.publication' THEN TRUE
    WHEN 'menu.reports.research.researcher' THEN TRUE
    WHEN 'menu.reports.economic' THEN TRUE
    WHEN 'menu.reports.economic.finance' THEN TRUE
    WHEN 'menu.reports.economic.xujalik' THEN TRUE
    WHEN 'menu.system' THEN TRUE
    WHEN 'menu.system.temp' THEN TRUE
    WHEN 'menu.system.translation' THEN TRUE
    WHEN 'menu.system.university_users' THEN TRUE
    WHEN 'menu.system.api_logs' THEN TRUE
    WHEN 'menu.system.report_update' THEN TRUE
    ELSE FALSE
  END = TRUE
ON CONFLICT (message_id, language) DO NOTHING;
-- =====================================================
-- V12: Add Cyrillic Uzbek Translations for Menus
-- oz-UZ (Uzbek Cyrillic) - 4th language support
--
-- Author: System Architect
-- Date: 2025-11-10
-- =====================================================

-- Insert Cyrillic Uzbek translations for existing menu items
INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'oz-UZ', 
  CASE m.message_key
    WHEN 'menu.dashboard' THEN 'Бош саҳифа'
    WHEN 'menu.registry' THEN 'Реестрлар'
    WHEN 'menu.rating' THEN 'Рейтинг'
    WHEN 'menu.data' THEN 'Маълумотлар базаси'
    WHEN 'menu.reports' THEN 'Ҳисоботлар'
    WHEN 'menu.system' THEN 'Тизим'
    WHEN 'menu.registry.e_reestr' THEN 'Е-Реестр'
    WHEN 'menu.registry.scientific' THEN 'Илмий реестр'
    WHEN 'menu.registry.student_meta' THEN 'Талаба маълумотлари'
    WHEN 'menu.rating.administrative' THEN 'Административ рейтинг'
    WHEN 'menu.rating.administrative.employee' THEN 'Ходимлар рейтинги'
    WHEN 'menu.rating.administrative.students' THEN 'Талабалар рейтинги'
    WHEN 'menu.rating.administrative.sport' THEN 'Спорт иншоотлари рейтинги'
    WHEN 'menu.rating.academic' THEN 'Академик рейтинг'
    WHEN 'menu.rating.academic.methodical' THEN 'Услубий нашрлар'
    WHEN 'menu.rating.academic.study' THEN 'Ўқув рейтинги'
    WHEN 'menu.rating.academic.verification' THEN 'Текшириш турлари'
    WHEN 'menu.rating.scientific' THEN 'Илмий рейтинг'
    WHEN 'menu.rating.scientific.publications' THEN 'Илмий нашрлар'
    WHEN 'menu.rating.scientific.projects' THEN 'Илмий лойиҳалар'
    WHEN 'menu.rating.scientific.intellectual' THEN 'Интеллектуал мулк'
    WHEN 'menu.rating.student_gpa' THEN 'Талаба GPA рейтинги'
    WHEN 'menu.data.general' THEN 'Умумий маълумотлар'
    WHEN 'menu.data.structure' THEN 'Тузилма'
    WHEN 'menu.data.employee' THEN 'Ходимлар'
    WHEN 'menu.data.student' THEN 'Талабалар'
    WHEN 'menu.data.education' THEN 'Таълим'
    WHEN 'menu.data.study' THEN 'Ўқув жараёни'
    WHEN 'menu.data.science' THEN 'Илмий фаолият'
    WHEN 'menu.data.organizational' THEN 'Ташкилий'
    WHEN 'menu.data.contract_category' THEN 'Шартнома турлари'
    WHEN 'menu.reports.universities' THEN 'ОТМ ҳисоботлари'
    WHEN 'menu.reports.employees' THEN 'Ходимлар ҳисоботлари'
    WHEN 'menu.reports.employees.navigation' THEN 'Ходимлар рўйхати'
    WHEN 'menu.reports.employees.private' THEN 'Ходимлар шахсий маълумотлари'
    WHEN 'menu.reports.employees.work' THEN 'Ходимлар иш фаолияти'
    WHEN 'menu.reports.students' THEN 'Талабалар ҳисоботлари'
    WHEN 'menu.reports.students.statistics' THEN 'Талабалар статистикаси'
    WHEN 'menu.reports.students.education' THEN 'Талабалар таълими'
    WHEN 'menu.reports.students.private' THEN 'Талабалар шахсий маълумотлари'
    WHEN 'menu.reports.students.attendance' THEN 'Талабалар давомати'
    WHEN 'menu.reports.students.score' THEN 'Талабалар баҳолари'
    WHEN 'menu.reports.students.dynamic' THEN 'Талабалар динамикаси'
    WHEN 'menu.reports.academic' THEN 'Академик ҳисоботлар'
    WHEN 'menu.reports.academic.study' THEN 'Ўқув ҳисоботи'
    WHEN 'menu.reports.research' THEN 'Илмий тадқиқот ҳисоботлари'
    WHEN 'menu.reports.research.project' THEN 'Тадқиқот лойиҳалари'
    WHEN 'menu.reports.research.publication' THEN 'Тадқиқот нашлари'
    WHEN 'menu.reports.research.researcher' THEN 'Тадқиқотчилар'
    WHEN 'menu.reports.economic' THEN 'Иқтисодий ҳисоботлар'
    WHEN 'menu.reports.economic.finance' THEN 'Молия ҳисоботи'
    WHEN 'menu.reports.economic.xujalik' THEN 'Хўжалик фаолияти'
    WHEN 'menu.system.temp' THEN 'Вақтинчалик'
    WHEN 'menu.system.translation' THEN 'Таржималар'
    WHEN 'menu.system.university_users' THEN 'ОТМ фойдаланувчилари'
    WHEN 'menu.system.api_logs' THEN 'API журнал'
    WHEN 'menu.system.report_update' THEN 'Ҳисоботларни янгилаш'
  END
FROM system_messages m
WHERE m.category = 'menu' 
  AND m.message_key IN (
    'menu.dashboard', 'menu.registry', 'menu.rating', 'menu.data', 'menu.reports', 'menu.system',
    'menu.registry.e_reestr', 'menu.registry.scientific', 'menu.registry.student_meta',
    'menu.rating.administrative', 'menu.rating.administrative.employee', 'menu.rating.administrative.students', 'menu.rating.administrative.sport',
    'menu.rating.academic', 'menu.rating.academic.methodical', 'menu.rating.academic.study', 'menu.rating.academic.verification',
    'menu.rating.scientific', 'menu.rating.scientific.publications', 'menu.rating.scientific.projects', 'menu.rating.scientific.intellectual',
    'menu.rating.student_gpa',
    'menu.data.general', 'menu.data.structure', 'menu.data.employee', 'menu.data.student', 'menu.data.education', 'menu.data.study', 'menu.data.science', 'menu.data.organizational', 'menu.data.contract_category',
    'menu.reports.universities', 'menu.reports.employees', 'menu.reports.employees.navigation', 'menu.reports.employees.private', 'menu.reports.employees.work',
    'menu.reports.students', 'menu.reports.students.statistics', 'menu.reports.students.education', 'menu.reports.students.private', 'menu.reports.students.attendance', 'menu.reports.students.score', 'menu.reports.students.dynamic',
    'menu.reports.academic', 'menu.reports.academic.study',
    'menu.reports.research', 'menu.reports.research.project', 'menu.reports.research.publication', 'menu.reports.research.researcher',
    'menu.reports.economic', 'menu.reports.economic.finance', 'menu.reports.economic.xujalik',
    'menu.system.temp', 'menu.system.translation', 'menu.system.university_users', 'menu.system.api_logs', 'menu.system.report_update'
  )
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;
-- ================================================
-- V13: Add Complete Menu Translations
-- Date: 2025-01-10
-- Purpose: Add 44 new submenu translations (4 languages each)
--
-- New Submenus:
-- 1. registry-e-reestr: 19 submenus (Muassasalar, Fakultetlar...)
-- 2. data-structure: 6 submenus
-- 3. data-education: 13 submenus
-- 4. data-general: 6 submenus
--
-- Total: 44 x 4 = 176 new translations
-- ================================================

-- ========================================
-- 1. REGISTRY > E-REESTR (19 submenus)
-- ========================================

-- Insert all 19 e-reestr submenus into system_messages
INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 1.1 Universities
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.university', 'Muassasalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.2 Faculties
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.faculty', 'Fakultetlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.3 Departments
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.cathedra', 'Kafedralar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.4 Teachers
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.teacher', 'O''qituvchilar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.5 Students
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.student', 'Talabalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.6 Diplomas
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.diploma', 'Diplomlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.7 Bachelor Specialities
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.speciality_bachelor', 'Yo''nalishlar (Bakalavr)', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.8 Master Specialities
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.speciality_master', 'Mutaxassisliklar (Magistr)', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.9 Doctoral Specialities
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.speciality_doctoral', 'Ixtisosliklar (Doktorantura)', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.10 Employee Jobs
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.employee_jobs', 'Xodimlar ish joylari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.11 Diploma Blank Distribution
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.diploma_blank_distribution', 'Diplom blankalarini taqsimlash', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.12 Diploma Blanks
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.diploma_blank', 'Diplom blankalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.13 Ordinatura Specialities
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.speciality_ordinatura', 'Ordinatura mutaxassisliklari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.14 University Specialities
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.university_speciality', 'OTM mutaxassisliklari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.15 Study Groups
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.university_group', 'O''quv guruhlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.16 Scholarships
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.student_scholarship', 'Stipendiya', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.17 Student Certificates
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.student_certificate', 'Talaba sertifikatlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.18 Employee Certificates
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.employee_certificate', 'O''qituvchi sertifikatlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.19 Students Lite
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr.student_lite', 'Talabalar Lite', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 2. DATA > STRUCTURE (6 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 2.1 University Types
(gen_random_uuid(), 'menu', 'menu.data.structure.university_type', 'OTM tashkiliy shakllari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.2 Ownership
(gen_random_uuid(), 'menu', 'menu.data.structure.ownership', 'OTM mulkchilik shakllari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.3 Department Types
(gen_random_uuid(), 'menu', 'menu.data.structure.department_type', 'OTM bo''linmalari turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.4 Locality Type
(gen_random_uuid(), 'menu', 'menu.data.structure.locality_type', 'Mahalliylik turi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.5 Activity Status
(gen_random_uuid(), 'menu', 'menu.data.structure.activity_status', 'OTM aktivlik statusi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.6 Belongs To
(gen_random_uuid(), 'menu', 'menu.data.structure.belongs_to', 'OTMning Vazirlikka tegishliligi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 3. DATA > EDUCATION (13 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 3.1 Education Types
(gen_random_uuid(), 'menu', 'menu.data.education.education_type', 'Ta''lim turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.2 Education Forms
(gen_random_uuid(), 'menu', 'menu.data.education.education_form', 'Ta''lim shakllari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.3 Education Languages
(gen_random_uuid(), 'menu', 'menu.data.education.education_language', 'Ta''lim tillari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.4 Grade System Types
(gen_random_uuid(), 'menu', 'menu.data.education.grade_system_type', 'Baholash tizimlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.5 Score Types
(gen_random_uuid(), 'menu', 'menu.data.education.score_type', 'Baho turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.6 Exam Types
(gen_random_uuid(), 'menu', 'menu.data.education.exam_type', 'Nazorat turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.7 Diploma Blank Categories
(gen_random_uuid(), 'menu', 'menu.data.education.diploma_blank_category', 'Diplom blanka kategoriyalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.8 Diploma Blank Statuses
(gen_random_uuid(), 'menu', 'menu.data.education.diploma_blank_status', 'Diplom blanka statuslari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.9 Diploma Generate Statuses
(gen_random_uuid(), 'menu', 'menu.data.education.diploma_blank_generate_status', 'Diplom shakllantirish statuslari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.10 Certificate Types
(gen_random_uuid(), 'menu', 'menu.data.education.certificate_type', 'Sertifikat toifasi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.11 Certificate Names
(gen_random_uuid(), 'menu', 'menu.data.education.certificate_names', 'Sertifikat turi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.12 Certificate Subjects
(gen_random_uuid(), 'menu', 'menu.data.education.certificate_subjects', 'Sertifikat fani', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.13 Certificate Grades
(gen_random_uuid(), 'menu', 'menu.data.education.certificate_grades', 'Sertifikat darajasi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 4. DATA > GENERAL (6 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 4.1 General (parent label fix)
(gen_random_uuid(), 'menu', 'menu.data.general', 'Umumiy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.2 Employees
(gen_random_uuid(), 'menu', 'menu.data.employee', 'Xodimlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.3 Students
(gen_random_uuid(), 'menu', 'menu.data.student', 'Talabalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.4 Study
(gen_random_uuid(), 'menu', 'menu.data.study', 'O''qitish', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.5 Science
(gen_random_uuid(), 'menu', 'menu.data.science', 'Ilmiy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.6 Organizational
(gen_random_uuid(), 'menu', 'menu.data.organizational', 'Tashkiliy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.7 Contract Category
(gen_random_uuid(), 'menu', 'menu.data.contract_category', 'Shartnoma kategoriyalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO UPDATE SET
  message = EXCLUDED.message,
  updated_at = CURRENT_TIMESTAMP;

-- ================================================
-- TRANSLATIONS: Cyrillic (oz-UZ)
-- ================================================

INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'oz-UZ',
  CASE m.message_key
    -- Registry > E-Reestr (19)
    WHEN 'menu.registry.e_reestr.university' THEN 'Муассасалар'
    WHEN 'menu.registry.e_reestr.faculty' THEN 'Факультетлар'
    WHEN 'menu.registry.e_reestr.cathedra' THEN 'Кафедралар'
    WHEN 'menu.registry.e_reestr.teacher' THEN 'Ўқитувчилар'
    WHEN 'menu.registry.e_reestr.student' THEN 'Талабалар'
    WHEN 'menu.registry.e_reestr.diploma' THEN 'Дипломлар'
    WHEN 'menu.registry.e_reestr.speciality_bachelor' THEN 'Йўналишлар (Бакалавр)'
    WHEN 'menu.registry.e_reestr.speciality_master' THEN 'Мутахассисликлар (Магистр)'
    WHEN 'menu.registry.e_reestr.speciality_doctoral' THEN 'Ихтисосликлар (Докторантура)'
    WHEN 'menu.registry.e_reestr.employee_jobs' THEN 'Ходимлар иш жойлари'
    WHEN 'menu.registry.e_reestr.diploma_blank_distribution' THEN 'Диплом бланкаларини тақсимлаш'
    WHEN 'menu.registry.e_reestr.diploma_blank' THEN 'Диплом бланкалар'
    WHEN 'menu.registry.e_reestr.speciality_ordinatura' THEN 'Ординатура мутахассисликлари'
    WHEN 'menu.registry.e_reestr.university_speciality' THEN 'ОТМ мутахассисликлари'
    WHEN 'menu.registry.e_reestr.university_group' THEN 'Ўқув гурухлари'
    WHEN 'menu.registry.e_reestr.student_scholarship' THEN 'Стипендия'
    WHEN 'menu.registry.e_reestr.student_certificate' THEN 'Талаба сертификатлари'
    WHEN 'menu.registry.e_reestr.employee_certificate' THEN 'Ўқитувчи сертификатлари'
    WHEN 'menu.registry.e_reestr.student_lite' THEN 'Талабалар Lite'

    -- Data > Structure (6)
    WHEN 'menu.data.structure.university_type' THEN 'ОТМ ташкилий шакллари'
    WHEN 'menu.data.structure.ownership' THEN 'ОТМ мулкчилик шакллари'
    WHEN 'menu.data.structure.department_type' THEN 'ОТМ бўлинмалари турлари'
    WHEN 'menu.data.structure.locality_type' THEN 'Маҳаллийлик тури'
    WHEN 'menu.data.structure.activity_status' THEN 'ОТМ активлик статуси'
    WHEN 'menu.data.structure.belongs_to' THEN 'ОТМнинг Вазирликка тегишлилиги'

    -- Data > Education (13)
    WHEN 'menu.data.education.education_type' THEN 'Таълим турлари'
    WHEN 'menu.data.education.education_form' THEN 'Таълим шакллари'
    WHEN 'menu.data.education.education_language' THEN 'Таълим тиллари'
    WHEN 'menu.data.education.grade_system_type' THEN 'Баҳолаш тизимлари'
    WHEN 'menu.data.education.score_type' THEN 'Баҳо турлари'
    WHEN 'menu.data.education.exam_type' THEN 'Назорат турлари'
    WHEN 'menu.data.education.diploma_blank_category' THEN 'Диплом бланка категориялари'
    WHEN 'menu.data.education.diploma_blank_status' THEN 'Диплом бланка статуслари'
    WHEN 'menu.data.education.diploma_blank_generate_status' THEN 'Диплом шакллантириш статуслари'
    WHEN 'menu.data.education.certificate_type' THEN 'Сертификат тоифаси'
    WHEN 'menu.data.education.certificate_names' THEN 'Сертификат тури'
    WHEN 'menu.data.education.certificate_subjects' THEN 'Сертификат фани'
    WHEN 'menu.data.education.certificate_grades' THEN 'Сертификат даражаси'

    -- Data > General (7)
    WHEN 'menu.data.general' THEN 'Умумий'
    WHEN 'menu.data.employee' THEN 'Ходимлар'
    WHEN 'menu.data.student' THEN 'Талабалар'
    WHEN 'menu.data.study' THEN 'Ўқитиш'
    WHEN 'menu.data.science' THEN 'Илмий'
    WHEN 'menu.data.organizational' THEN 'Ташкилий'
    WHEN 'menu.data.contract_category' THEN 'Шартнома категориялари'

    ELSE m.message  -- Fallback
  END
FROM system_messages m
WHERE m.category = 'menu'
  AND m.message_key IN (
    -- Registry > E-Reestr (19)
    'menu.registry.e_reestr.university', 'menu.registry.e_reestr.faculty', 'menu.registry.e_reestr.cathedra',
    'menu.registry.e_reestr.teacher', 'menu.registry.e_reestr.student', 'menu.registry.e_reestr.diploma',
    'menu.registry.e_reestr.speciality_bachelor', 'menu.registry.e_reestr.speciality_master',
    'menu.registry.e_reestr.speciality_doctoral', 'menu.registry.e_reestr.employee_jobs',
    'menu.registry.e_reestr.diploma_blank_distribution', 'menu.registry.e_reestr.diploma_blank',
    'menu.registry.e_reestr.speciality_ordinatura', 'menu.registry.e_reestr.university_speciality',
    'menu.registry.e_reestr.university_group', 'menu.registry.e_reestr.student_scholarship',
    'menu.registry.e_reestr.student_certificate', 'menu.registry.e_reestr.employee_certificate',
    'menu.registry.e_reestr.student_lite',
    -- Data > Structure (6)
    'menu.data.structure.university_type', 'menu.data.structure.ownership', 'menu.data.structure.department_type',
    'menu.data.structure.locality_type', 'menu.data.structure.activity_status', 'menu.data.structure.belongs_to',
    -- Data > Education (13)
    'menu.data.education.education_type', 'menu.data.education.education_form', 'menu.data.education.education_language',
    'menu.data.education.grade_system_type', 'menu.data.education.score_type', 'menu.data.education.exam_type',
    'menu.data.education.diploma_blank_category', 'menu.data.education.diploma_blank_status',
    'menu.data.education.diploma_blank_generate_status', 'menu.data.education.certificate_type',
    'menu.data.education.certificate_names', 'menu.data.education.certificate_subjects', 'menu.data.education.certificate_grades',
    -- Data > General (7)
    'menu.data.general', 'menu.data.employee', 'menu.data.student', 'menu.data.study',
    'menu.data.science', 'menu.data.organizational', 'menu.data.contract_category'
  )
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;

-- ================================================
-- TRANSLATIONS: Russian (ru-RU)
-- ================================================

INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'ru-RU',
  CASE m.message_key
    -- Registry > E-Reestr (19)
    WHEN 'menu.registry.e_reestr.university' THEN 'Университеты'
    WHEN 'menu.registry.e_reestr.faculty' THEN 'Факультеты'
    WHEN 'menu.registry.e_reestr.cathedra' THEN 'Кафедры'
    WHEN 'menu.registry.e_reestr.teacher' THEN 'Преподаватели'
    WHEN 'menu.registry.e_reestr.student' THEN 'Студенты'
    WHEN 'menu.registry.e_reestr.diploma' THEN 'Дипломы'
    WHEN 'menu.registry.e_reestr.speciality_bachelor' THEN 'Направления (Бакалавриат)'
    WHEN 'menu.registry.e_reestr.speciality_master' THEN 'Специальности (Магистратура)'
    WHEN 'menu.registry.e_reestr.speciality_doctoral' THEN 'Специальности (Докторантура)'
    WHEN 'menu.registry.e_reestr.employee_jobs' THEN 'Рабочие места сотрудников'
    WHEN 'menu.registry.e_reestr.diploma_blank_distribution' THEN 'Распределение бланков дипломов'
    WHEN 'menu.registry.e_reestr.diploma_blank' THEN 'Бланки дипломов'
    WHEN 'menu.registry.e_reestr.speciality_ordinatura' THEN 'Специальности ординатуры'
    WHEN 'menu.registry.e_reestr.university_speciality' THEN 'Специальности ВУЗа'
    WHEN 'menu.registry.e_reestr.university_group' THEN 'Учебные группы'
    WHEN 'menu.registry.e_reestr.student_scholarship' THEN 'Стипендия'
    WHEN 'menu.registry.e_reestr.student_certificate' THEN 'Сертификаты студентов'
    WHEN 'menu.registry.e_reestr.employee_certificate' THEN 'Сертификаты преподавателей'
    WHEN 'menu.registry.e_reestr.student_lite' THEN 'Студенты Lite'

    -- Data > Structure (6)
    WHEN 'menu.data.structure.university_type' THEN 'Организационные формы вузов'
    WHEN 'menu.data.structure.ownership' THEN 'Формы собственности вузов'
    WHEN 'menu.data.structure.department_type' THEN 'Типы подразделений вузов'
    WHEN 'menu.data.structure.locality_type' THEN 'Тип местности'
    WHEN 'menu.data.structure.activity_status' THEN 'Статус активности вуза'
    WHEN 'menu.data.structure.belongs_to' THEN 'Принадлежность вуза министерству'

    -- Data > Education (13)
    WHEN 'menu.data.education.education_type' THEN 'Типы образования'
    WHEN 'menu.data.education.education_form' THEN 'Формы образования'
    WHEN 'menu.data.education.education_language' THEN 'Языки обучения'
    WHEN 'menu.data.education.grade_system_type' THEN 'Системы оценивания'
    WHEN 'menu.data.education.score_type' THEN 'Типы оценок'
    WHEN 'menu.data.education.exam_type' THEN 'Типы контроля'
    WHEN 'menu.data.education.diploma_blank_category' THEN 'Категории бланков дипломов'
    WHEN 'menu.data.education.diploma_blank_status' THEN 'Статусы бланков дипломов'
    WHEN 'menu.data.education.diploma_blank_generate_status' THEN 'Статусы формирования дипломов'
    WHEN 'menu.data.education.certificate_type' THEN 'Категория сертификата'
    WHEN 'menu.data.education.certificate_names' THEN 'Тип сертификата'
    WHEN 'menu.data.education.certificate_subjects' THEN 'Предмет сертификата'
    WHEN 'menu.data.education.certificate_grades' THEN 'Уровень сертификата'

    -- Data > General (7)
    WHEN 'menu.data.general' THEN 'Общие'
    WHEN 'menu.data.employee' THEN 'Сотрудники'
    WHEN 'menu.data.student' THEN 'Студенты'
    WHEN 'menu.data.study' THEN 'Обучение'
    WHEN 'menu.data.science' THEN 'Наука'
    WHEN 'menu.data.organizational' THEN 'Организационные'
    WHEN 'menu.data.contract_category' THEN 'Категории договоров'

    ELSE m.message  -- Fallback
  END
FROM system_messages m
WHERE m.category = 'menu'
  AND m.message_key IN (
    -- Registry > E-Reestr (19)
    'menu.registry.e_reestr.university', 'menu.registry.e_reestr.faculty', 'menu.registry.e_reestr.cathedra',
    'menu.registry.e_reestr.teacher', 'menu.registry.e_reestr.student', 'menu.registry.e_reestr.diploma',
    'menu.registry.e_reestr.speciality_bachelor', 'menu.registry.e_reestr.speciality_master',
    'menu.registry.e_reestr.speciality_doctoral', 'menu.registry.e_reestr.employee_jobs',
    'menu.registry.e_reestr.diploma_blank_distribution', 'menu.registry.e_reestr.diploma_blank',
    'menu.registry.e_reestr.speciality_ordinatura', 'menu.registry.e_reestr.university_speciality',
    'menu.registry.e_reestr.university_group', 'menu.registry.e_reestr.student_scholarship',
    'menu.registry.e_reestr.student_certificate', 'menu.registry.e_reestr.employee_certificate',
    'menu.registry.e_reestr.student_lite',
    -- Data > Structure (6)
    'menu.data.structure.university_type', 'menu.data.structure.ownership', 'menu.data.structure.department_type',
    'menu.data.structure.locality_type', 'menu.data.structure.activity_status', 'menu.data.structure.belongs_to',
    -- Data > Education (13)
    'menu.data.education.education_type', 'menu.data.education.education_form', 'menu.data.education.education_language',
    'menu.data.education.grade_system_type', 'menu.data.education.score_type', 'menu.data.education.exam_type',
    'menu.data.education.diploma_blank_category', 'menu.data.education.diploma_blank_status',
    'menu.data.education.diploma_blank_generate_status', 'menu.data.education.certificate_type',
    'menu.data.education.certificate_names', 'menu.data.education.certificate_subjects', 'menu.data.education.certificate_grades',
    -- Data > General (7)
    'menu.data.general', 'menu.data.employee', 'menu.data.student', 'menu.data.study',
    'menu.data.science', 'menu.data.organizational', 'menu.data.contract_category'
  )
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;

-- ================================================
-- TRANSLATIONS: English (en-US)
-- ================================================

INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'en-US',
  CASE m.message_key
    -- Registry > E-Reestr (19)
    WHEN 'menu.registry.e_reestr.university' THEN 'Universities'
    WHEN 'menu.registry.e_reestr.faculty' THEN 'Faculties'
    WHEN 'menu.registry.e_reestr.cathedra' THEN 'Departments'
    WHEN 'menu.registry.e_reestr.teacher' THEN 'Teachers'
    WHEN 'menu.registry.e_reestr.student' THEN 'Students'
    WHEN 'menu.registry.e_reestr.diploma' THEN 'Diplomas'
    WHEN 'menu.registry.e_reestr.speciality_bachelor' THEN 'Bachelor Specialities'
    WHEN 'menu.registry.e_reestr.speciality_master' THEN 'Master Specialities'
    WHEN 'menu.registry.e_reestr.speciality_doctoral' THEN 'Doctoral Specialities'
    WHEN 'menu.registry.e_reestr.employee_jobs' THEN 'Employee Jobs'
    WHEN 'menu.registry.e_reestr.diploma_blank_distribution' THEN 'Diploma Blank Distribution'
    WHEN 'menu.registry.e_reestr.diploma_blank' THEN 'Diploma Blanks'
    WHEN 'menu.registry.e_reestr.speciality_ordinatura' THEN 'Ordinatura Specialities'
    WHEN 'menu.registry.e_reestr.university_speciality' THEN 'University Specialities'
    WHEN 'menu.registry.e_reestr.university_group' THEN 'Study Groups'
    WHEN 'menu.registry.e_reestr.student_scholarship' THEN 'Scholarship'
    WHEN 'menu.registry.e_reestr.student_certificate' THEN 'Student Certificates'
    WHEN 'menu.registry.e_reestr.employee_certificate' THEN 'Teacher Certificates'
    WHEN 'menu.registry.e_reestr.student_lite' THEN 'Students Lite'

    -- Data > Structure (6)
    WHEN 'menu.data.structure.university_type' THEN 'University Types'
    WHEN 'menu.data.structure.ownership' THEN 'Ownership Forms'
    WHEN 'menu.data.structure.department_type' THEN 'Department Types'
    WHEN 'menu.data.structure.locality_type' THEN 'Locality Type'
    WHEN 'menu.data.structure.activity_status' THEN 'Activity Status'
    WHEN 'menu.data.structure.belongs_to' THEN 'Ministry Affiliation'

    -- Data > Education (13)
    WHEN 'menu.data.education.education_type' THEN 'Education Types'
    WHEN 'menu.data.education.education_form' THEN 'Education Forms'
    WHEN 'menu.data.education.education_language' THEN 'Education Languages'
    WHEN 'menu.data.education.grade_system_type' THEN 'Grading Systems'
    WHEN 'menu.data.education.score_type' THEN 'Score Types'
    WHEN 'menu.data.education.exam_type' THEN 'Exam Types'
    WHEN 'menu.data.education.diploma_blank_category' THEN 'Diploma Blank Categories'
    WHEN 'menu.data.education.diploma_blank_status' THEN 'Diploma Blank Statuses'
    WHEN 'menu.data.education.diploma_blank_generate_status' THEN 'Diploma Generation Statuses'
    WHEN 'menu.data.education.certificate_type' THEN 'Certificate Category'
    WHEN 'menu.data.education.certificate_names' THEN 'Certificate Type'
    WHEN 'menu.data.education.certificate_subjects' THEN 'Certificate Subject'
    WHEN 'menu.data.education.certificate_grades' THEN 'Certificate Grade'

    -- Data > General (7)
    WHEN 'menu.data.general' THEN 'General'
    WHEN 'menu.data.employee' THEN 'Employees'
    WHEN 'menu.data.student' THEN 'Students'
    WHEN 'menu.data.study' THEN 'Study'
    WHEN 'menu.data.science' THEN 'Science'
    WHEN 'menu.data.organizational' THEN 'Organizational'
    WHEN 'menu.data.contract_category' THEN 'Contract Categories'

    ELSE m.message  -- Fallback
  END
FROM system_messages m
WHERE m.category = 'menu'
  AND m.message_key IN (
    -- Registry > E-Reestr (19)
    'menu.registry.e_reestr.university', 'menu.registry.e_reestr.faculty', 'menu.registry.e_reestr.cathedra',
    'menu.registry.e_reestr.teacher', 'menu.registry.e_reestr.student', 'menu.registry.e_reestr.diploma',
    'menu.registry.e_reestr.speciality_bachelor', 'menu.registry.e_reestr.speciality_master',
    'menu.registry.e_reestr.speciality_doctoral', 'menu.registry.e_reestr.employee_jobs',
    'menu.registry.e_reestr.diploma_blank_distribution', 'menu.registry.e_reestr.diploma_blank',
    'menu.registry.e_reestr.speciality_ordinatura', 'menu.registry.e_reestr.university_speciality',
    'menu.registry.e_reestr.university_group', 'menu.registry.e_reestr.student_scholarship',
    'menu.registry.e_reestr.student_certificate', 'menu.registry.e_reestr.employee_certificate',
    'menu.registry.e_reestr.student_lite',
    -- Data > Structure (6)
    'menu.data.structure.university_type', 'menu.data.structure.ownership', 'menu.data.structure.department_type',
    'menu.data.structure.locality_type', 'menu.data.structure.activity_status', 'menu.data.structure.belongs_to',
    -- Data > Education (13)
    'menu.data.education.education_type', 'menu.data.education.education_form', 'menu.data.education.education_language',
    'menu.data.education.grade_system_type', 'menu.data.education.score_type', 'menu.data.education.exam_type',
    'menu.data.education.diploma_blank_category', 'menu.data.education.diploma_blank_status',
    'menu.data.education.diploma_blank_generate_status', 'menu.data.education.certificate_type',
    'menu.data.education.certificate_names', 'menu.data.education.certificate_subjects', 'menu.data.education.certificate_grades',
    -- Data > General (7)
    'menu.data.general', 'menu.data.employee', 'menu.data.student', 'menu.data.study',
    'menu.data.science', 'menu.data.organizational', 'menu.data.contract_category'
  )
ON CONFLICT (message_id, language) DO UPDATE SET translation = EXCLUDED.translation;

-- END OF V13 MIGRATION
-- ================================================
-- V14: Add Data Submenu Translations
-- Date: 2025-01-10
-- Purpose: Add 71 new data submenu translations (4 languages each)
--
-- New Submenus:
-- 1. data-employee: 9 submenus
-- 2. data-student: 15 submenus
-- 3. data-study: 18 submenus
-- 4. data-science: 9 submenus
-- 5. data-organizational: 10 submenus
-- 6. data-general: 10 submenus
--
-- Total: 71 x 4 = 284 new translations
-- ================================================

-- ========================================
-- 1. DATA > EMPLOYEE (9 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 1.1 Employee Type
(gen_random_uuid(), 'menu', 'menu.data.employee.type', 'Xodimlar toifalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.2 Employee Status
(gen_random_uuid(), 'menu', 'menu.data.employee.status', 'O''qituvchi xolatlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.3 Employee Rate
(gen_random_uuid(), 'menu', 'menu.data.employee.rate', 'Mehnat stavkalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.4 Employee Form
(gen_random_uuid(), 'menu', 'menu.data.employee.form', 'Mehnat shakllari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.5 Position Type
(gen_random_uuid(), 'menu', 'menu.data.employee.position_type', 'Lavozim turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.6 Qualification
(gen_random_uuid(), 'menu', 'menu.data.employee.qualification', 'Malaka oshirish joylari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.7 Achievement
(gen_random_uuid(), 'menu', 'menu.data.employee.achievement', 'O''qituvchi yutuqlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.8 Academic Degree
(gen_random_uuid(), 'menu', 'menu.data.employee.academic_degree', 'Ilmiy darajalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 1.9 Academic Rank
(gen_random_uuid(), 'menu', 'menu.data.employee.academic_rank', 'Ilmiy unvonlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 2. DATA > STUDENT (15 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 2.1 Student Status
(gen_random_uuid(), 'menu', 'menu.data.student.status', 'Talaba holatlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.2 Student Achievement
(gen_random_uuid(), 'menu', 'menu.data.student.achievement', 'Talaba yutuqlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.3 Expel Reasons
(gen_random_uuid(), 'menu', 'menu.data.student.expel', 'Chetlatish sabablari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.4 Accommodation
(gen_random_uuid(), 'menu', 'menu.data.student.accommodation', 'Yashash joyi turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.5 Doctoral Type
(gen_random_uuid(), 'menu', 'menu.data.student.doctoral_type', 'Doktorant toifalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.6 Social Type
(gen_random_uuid(), 'menu', 'menu.data.student.social_type', 'Ijtimoiy toifalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.7 Academic Leave Reasons
(gen_random_uuid(), 'menu', 'menu.data.student.academic_reason', 'Talabalarga akademik ta''til berish sabablari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.8 Doctoral Student Status
(gen_random_uuid(), 'menu', 'menu.data.student.doctoral_status', 'Doktorantura talaba holatlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.9 Graduate Fields
(gen_random_uuid(), 'menu', 'menu.data.student.graduate_fields', 'Bitiruvchilar faoliyat sohalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.10 Graduate Inactive
(gen_random_uuid(), 'menu', 'menu.data.student.graduate_inactive', 'Ishga joylashmaganlik sabablari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.11 Student Type
(gen_random_uuid(), 'menu', 'menu.data.student.student_type', 'Talaba toifasi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.12 Living Status
(gen_random_uuid(), 'menu', 'menu.data.student.living_status', 'Talaba yashash joyi statusi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.13 Roommate Type
(gen_random_uuid(), 'menu', 'menu.data.student.roommate_type', 'Birgalikda yashashdiganlar toifasi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.14 Workplace Compatibility
(gen_random_uuid(), 'menu', 'menu.data.student.workplace_compatibility', 'Yo''nalishga mosligi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.15 Academic Mobile Type
(gen_random_uuid(), 'menu', 'menu.data.student.academic_mobile', 'Akademik mobillik turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 3. DATA > STUDY (18 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 3.1 Education Year
(gen_random_uuid(), 'menu', 'menu.data.study.year', 'O''quv yillari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.2 Course
(gen_random_uuid(), 'menu', 'menu.data.study.course', 'O''quv kurslari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.3 Semester
(gen_random_uuid(), 'menu', 'menu.data.study.semester', 'Semestr turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.4 Education Week Type
(gen_random_uuid(), 'menu', 'menu.data.study.week_type', 'O''quv grafik haftalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.5 Subject Block
(gen_random_uuid(), 'menu', 'menu.data.study.subject_block', 'Fanlar bloklari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.6 Subject Type
(gen_random_uuid(), 'menu', 'menu.data.study.subject_type', 'Fanlar toifalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.7 Class Type
(gen_random_uuid(), 'menu', 'menu.data.study.class_type', 'Mashg''ulot turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.8 Exam Finish
(gen_random_uuid(), 'menu', 'menu.data.study.exam_finish', 'Fanning yakuniy nazorat shakli', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.9 Final Exam Type
(gen_random_uuid(), 'menu', 'menu.data.study.final_exam_type', 'Qaydnoma turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.10 Semester List
(gen_random_uuid(), 'menu', 'menu.data.study.semester_list', 'Semestrlar ro''yxati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.11 Decree Type
(gen_random_uuid(), 'menu', 'menu.data.study.decree_type', 'Buyruq turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.12 Sport Type
(gen_random_uuid(), 'menu', 'menu.data.study.sport_type', 'Sport turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.13 Attendance Setting
(gen_random_uuid(), 'menu', 'menu.data.study.attendance_setting', 'Davomat chegaralari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.14 Teacher Conduction Form
(gen_random_uuid(), 'menu', 'menu.data.study.teacher_conduction', 'Dars olib borish shakllari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.15 Internship Form
(gen_random_uuid(), 'menu', 'menu.data.study.internship_form', 'Stajirovka shakllari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.16 Internship Type
(gen_random_uuid(), 'menu', 'menu.data.study.internship_type', 'Stajirovka turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.17 Resource Type
(gen_random_uuid(), 'menu', 'menu.data.study.resource_type', 'Resurs turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 3.18 Outside Activities
(gen_random_uuid(), 'menu', 'menu.data.study.outside_activities', 'Auditoriyadan tashqari mashg''ulotlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 4. DATA > SCIENCE (9 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 4.1 Project Type
(gen_random_uuid(), 'menu', 'menu.data.science.project_type', 'Ilmiy loyihalar turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.2 Project Locality
(gen_random_uuid(), 'menu', 'menu.data.science.project_locality', 'Ilmiy loyiha maqomi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.3 Currency
(gen_random_uuid(), 'menu', 'menu.data.science.currency', 'Valyuta turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.4 Executor Type
(gen_random_uuid(), 'menu', 'menu.data.science.executor_type', 'Ijrochilar turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.5 Publication Type
(gen_random_uuid(), 'menu', 'menu.data.science.publication_type', 'Ilmiy nashr turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.6 Methodical Publication Type
(gen_random_uuid(), 'menu', 'menu.data.science.methodical_type', 'Uslubiy nashr turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.7 Patient Type (Intellectual Property)
(gen_random_uuid(), 'menu', 'menu.data.science.patient_type', 'Intellektural mulklar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.8 Publication Database
(gen_random_uuid(), 'menu', 'menu.data.science.publication_database', 'Ilmiy nashr bazalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 4.9 Scholar Database
(gen_random_uuid(), 'menu', 'menu.data.science.scholar_database', 'Tadqiqotchilar ba''zalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 5. DATA > ORGANIZATIONAL (10 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 5.1 Payment Form
(gen_random_uuid(), 'menu', 'menu.data.organizational.payment_form', 'To''lov turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.2 Stipend Rate
(gen_random_uuid(), 'menu', 'menu.data.organizational.stipend_rate', 'Stipendiya turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.3 Stipend Rate Category
(gen_random_uuid(), 'menu', 'menu.data.organizational.stipend_category', 'Stipendiya turlari kategoriyalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.4 Scholarship Decree Type
(gen_random_uuid(), 'menu', 'menu.data.organizational.scholarship_decree', 'Stipendiya buyrug`i turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.5 Contract Type
(gen_random_uuid(), 'menu', 'menu.data.organizational.contract_type', 'Shartnoma turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.6 Contract Summa Type
(gen_random_uuid(), 'menu', 'menu.data.organizational.contract_summa', 'Shartnoma qiymati turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.7 Contract Category
(gen_random_uuid(), 'menu', 'menu.data.organizational.contract_category', 'Shartnoma toifalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.8 Auditorium Type
(gen_random_uuid(), 'menu', 'menu.data.organizational.auditorium_type', 'Auditoriya turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.9 Device Type
(gen_random_uuid(), 'menu', 'menu.data.organizational.device_type', 'AKT qurilmalari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 5.10 Grant Type
(gen_random_uuid(), 'menu', 'menu.data.organizational.grant_type', 'Grant turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ========================================
-- 6. DATA > GENERAL (10 submenus)
-- ========================================

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 6.1 Country
(gen_random_uuid(), 'menu', 'menu.data.general.country', 'Davlatlar ro''yxati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.2 SOATO (Regions)
(gen_random_uuid(), 'menu', 'menu.data.general.soato', 'Viloyat va tumanlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.3 Nationality
(gen_random_uuid(), 'menu', 'menu.data.general.nationality', 'Millatlar ro''yxati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.4 Citizenship
(gen_random_uuid(), 'menu', 'menu.data.general.citizenship', 'Fuqarolik holati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.5 Gender
(gen_random_uuid(), 'menu', 'menu.data.general.gender', 'Jins turlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.6 Bachelor Specialty
(gen_random_uuid(), 'menu', 'menu.data.general.bachelor_specialty', 'BSc ta''lim yo''nalishlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.7 Master Specialty
(gen_random_uuid(), 'menu', 'menu.data.general.master_specialty', 'MSc mutaxassisliklari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.8 Doctoral Specialty
(gen_random_uuid(), 'menu', 'menu.data.general.doctoral_specialty', 'PhD va DSc ixtisosliklari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.9 Terrain (Neighborhoods)
(gen_random_uuid(), 'menu', 'menu.data.general.terrain', 'Mahallalar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 6.10 Poverty Level
(gen_random_uuid(), 'menu', 'menu.data.general.poverty_level', 'Kambag''allik darajasi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ================================================
-- TRANSLATIONS: Cyrillic (oz-UZ)
-- ================================================

INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'oz-UZ',
  CASE m.message_key
    -- Data > Employee (9)
    WHEN 'menu.data.employee.type' THEN 'Ходимлар тоифалари'
    WHEN 'menu.data.employee.status' THEN 'Ўқитувчи холатлари'
    WHEN 'menu.data.employee.rate' THEN 'Меҳнат ставкалари'
    WHEN 'menu.data.employee.form' THEN 'Меҳнат шакллари'
    WHEN 'menu.data.employee.position_type' THEN 'Лавозим турлари'
    WHEN 'menu.data.employee.qualification' THEN 'Малака оширишжойлари'
    WHEN 'menu.data.employee.achievement' THEN 'Ўқитувчи ютуқлари'
    WHEN 'menu.data.employee.academic_degree' THEN 'Илмий даражалар'
    WHEN 'menu.data.employee.academic_rank' THEN 'Илмий унвонлар'

    -- Data > Student (15)
    WHEN 'menu.data.student.status' THEN 'Талаба ҳолатлари'
    WHEN 'menu.data.student.achievement' THEN 'Талаба ютуқлари'
    WHEN 'menu.data.student.expel' THEN 'Четлатиш сабаблари'
    WHEN 'menu.data.student.accommodation' THEN 'Яшаш жойи турлари'
    WHEN 'menu.data.student.doctoral_type' THEN 'Докторант тоифалари'
    WHEN 'menu.data.student.social_type' THEN 'Ижтимоий тоифалар'
    WHEN 'menu.data.student.academic_reason' THEN 'Талабаларга академик таътил бериш сабаблари'
    WHEN 'menu.data.student.doctoral_status' THEN 'Докторантура талаба ҳолатлари'
    WHEN 'menu.data.student.graduate_fields' THEN 'Битирувчилар фаолият соҳалари'
    WHEN 'menu.data.student.graduate_inactive' THEN 'Ишга жойлашмаганлик сабаблари'
    WHEN 'menu.data.student.student_type' THEN 'Талаба тоифаси'
    WHEN 'menu.data.student.living_status' THEN 'Талаба яшаш жойи статуси'
    WHEN 'menu.data.student.roommate_type' THEN 'Биргаликда яшашдиганлар тоифаси'
    WHEN 'menu.data.student.workplace_compatibility' THEN 'Йўналишга мослиги'
    WHEN 'menu.data.student.academic_mobile' THEN 'Академик мобиллик турлари'

    -- Data > Study (18)
    WHEN 'menu.data.study.year' THEN 'Ўқув йиллари'
    WHEN 'menu.data.study.course' THEN 'Ўқув курслари'
    WHEN 'menu.data.study.semester' THEN 'Семестр турлари'
    WHEN 'menu.data.study.week_type' THEN 'Ўқув график ҳафталари'
    WHEN 'menu.data.study.subject_block' THEN 'Фанлар блоклари'
    WHEN 'menu.data.study.subject_type' THEN 'Фанлар тоифалари'
    WHEN 'menu.data.study.class_type' THEN 'Машғулот турлари'
    WHEN 'menu.data.study.exam_finish' THEN 'Фаннинг якуний назорат шакли'
    WHEN 'menu.data.study.final_exam_type' THEN 'Қайднома турлари'
    WHEN 'menu.data.study.semester_list' THEN 'Семестрлар рўйхати'
    WHEN 'menu.data.study.decree_type' THEN 'Буйруқ турлари'
    WHEN 'menu.data.study.sport_type' THEN 'Спорт турлари'
    WHEN 'menu.data.study.attendance_setting' THEN 'Давомат чегаралари'
    WHEN 'menu.data.study.teacher_conduction' THEN 'Дарс олиб бориш шакллари'
    WHEN 'menu.data.study.internship_form' THEN 'Стажировка шакллари'
    WHEN 'menu.data.study.internship_type' THEN 'Стажировка турлари'
    WHEN 'menu.data.study.resource_type' THEN 'Ресурс турлари'
    WHEN 'menu.data.study.outside_activities' THEN 'Аудиториядан ташқари машғулотлар'

    -- Data > Science (9)
    WHEN 'menu.data.science.project_type' THEN 'Илмий лойиҳалар турлари'
    WHEN 'menu.data.science.project_locality' THEN 'Илмий лойиҳа мақоми'
    WHEN 'menu.data.science.currency' THEN 'Валюта турлари'
    WHEN 'menu.data.science.executor_type' THEN 'Ижрочилар турлари'
    WHEN 'menu.data.science.publication_type' THEN 'Илмий нашр турлари'
    WHEN 'menu.data.science.methodical_type' THEN 'Услубий нашр турлари'
    WHEN 'menu.data.science.patient_type' THEN 'Интеллектурал мулклар'
    WHEN 'menu.data.science.publication_database' THEN 'Илмий нашр базалари'
    WHEN 'menu.data.science.scholar_database' THEN 'Тадқиқотчилар базалари'

    -- Data > Organizational (10)
    WHEN 'menu.data.organizational.payment_form' THEN 'Тўлов турлари'
    WHEN 'menu.data.organizational.stipend_rate' THEN 'Стипендия турлари'
    WHEN 'menu.data.organizational.stipend_category' THEN 'Стипендия турлари категориялари'
    WHEN 'menu.data.organizational.scholarship_decree' THEN 'Стипендия буйруғи турлари'
    WHEN 'menu.data.organizational.contract_type' THEN 'Шартнома турлари'
    WHEN 'menu.data.organizational.contract_summa' THEN 'Шартнома қиймати турлари'
    WHEN 'menu.data.organizational.contract_category' THEN 'Шартнома тоифалари'
    WHEN 'menu.data.organizational.auditorium_type' THEN 'Аудитория турлари'
    WHEN 'menu.data.organizational.device_type' THEN 'АКТ қурилмалари'
    WHEN 'menu.data.organizational.grant_type' THEN 'Грант турлари'

    -- Data > General (10)
    WHEN 'menu.data.general.country' THEN 'Давлатлар рўйхати'
    WHEN 'menu.data.general.soato' THEN 'Вилоят ва туманлар'
    WHEN 'menu.data.general.nationality' THEN 'Миллатлар рўйхати'
    WHEN 'menu.data.general.citizenship' THEN 'Фуқаролик ҳолати'
    WHEN 'menu.data.general.gender' THEN 'Жинс турлари'
    WHEN 'menu.data.general.bachelor_specialty' THEN 'BSc таълим йўналишлари'
    WHEN 'menu.data.general.master_specialty' THEN 'MSc мутахассисликлари'
    WHEN 'menu.data.general.doctoral_specialty' THEN 'PhD ва DSc ихтисосликлари'
    WHEN 'menu.data.general.terrain' THEN 'Маҳаллалар'
    WHEN 'menu.data.general.poverty_level' THEN 'Камбағаллик даражаси'

    ELSE m.message  -- Fallback
  END
FROM system_messages m
WHERE m.category = 'menu'
  AND m.message_key IN (
    -- Employee
    'menu.data.employee.type', 'menu.data.employee.status', 'menu.data.employee.rate',
    'menu.data.employee.form', 'menu.data.employee.position_type', 'menu.data.employee.qualification',
    'menu.data.employee.achievement', 'menu.data.employee.academic_degree', 'menu.data.employee.academic_rank',
    -- Student
    'menu.data.student.status', 'menu.data.student.achievement', 'menu.data.student.expel',
    'menu.data.student.accommodation', 'menu.data.student.doctoral_type', 'menu.data.student.social_type',
    'menu.data.student.academic_reason', 'menu.data.student.doctoral_status', 'menu.data.student.graduate_fields',
    'menu.data.student.graduate_inactive', 'menu.data.student.student_type', 'menu.data.student.living_status',
    'menu.data.student.roommate_type', 'menu.data.student.workplace_compatibility', 'menu.data.student.academic_mobile',
    -- Study
    'menu.data.study.year', 'menu.data.study.course', 'menu.data.study.semester',
    'menu.data.study.week_type', 'menu.data.study.subject_block', 'menu.data.study.subject_type',
    'menu.data.study.class_type', 'menu.data.study.exam_finish', 'menu.data.study.final_exam_type',
    'menu.data.study.semester_list', 'menu.data.study.decree_type', 'menu.data.study.sport_type',
    'menu.data.study.attendance_setting', 'menu.data.study.teacher_conduction', 'menu.data.study.internship_form',
    'menu.data.study.internship_type', 'menu.data.study.resource_type', 'menu.data.study.outside_activities',
    -- Science
    'menu.data.science.project_type', 'menu.data.science.project_locality', 'menu.data.science.currency',
    'menu.data.science.executor_type', 'menu.data.science.publication_type', 'menu.data.science.methodical_type',
    'menu.data.science.patient_type', 'menu.data.science.publication_database', 'menu.data.science.scholar_database',
    -- Organizational
    'menu.data.organizational.payment_form', 'menu.data.organizational.stipend_rate', 'menu.data.organizational.stipend_category',
    'menu.data.organizational.scholarship_decree', 'menu.data.organizational.contract_type', 'menu.data.organizational.contract_summa',
    'menu.data.organizational.contract_category', 'menu.data.organizational.auditorium_type', 'menu.data.organizational.device_type',
    'menu.data.organizational.grant_type',
    -- General
    'menu.data.general.country', 'menu.data.general.soato', 'menu.data.general.nationality',
    'menu.data.general.citizenship', 'menu.data.general.gender', 'menu.data.general.bachelor_specialty',
    'menu.data.general.master_specialty', 'menu.data.general.doctoral_specialty', 'menu.data.general.terrain',
    'menu.data.general.poverty_level'
  )
ON CONFLICT (message_id, language) DO UPDATE SET
  translation = EXCLUDED.translation,
  updated_at = CURRENT_TIMESTAMP;

-- ================================================
-- TRANSLATIONS: Russian (ru-RU)
-- ================================================

INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'ru-RU',
  CASE m.message_key
    -- Data > Employee (9)
    WHEN 'menu.data.employee.type' THEN 'Категории сотрудников'
    WHEN 'menu.data.employee.status' THEN 'Статусы преподавателей'
    WHEN 'menu.data.employee.rate' THEN 'Трудовые ставки'
    WHEN 'menu.data.employee.form' THEN 'Формы труда'
    WHEN 'menu.data.employee.position_type' THEN 'Типы должностей'
    WHEN 'menu.data.employee.qualification' THEN 'Места повышения квалификации'
    WHEN 'menu.data.employee.achievement' THEN 'Достижения преподавателей'
    WHEN 'menu.data.employee.academic_degree' THEN 'Ученые степени'
    WHEN 'menu.data.employee.academic_rank' THEN 'Ученые звания'

    -- Data > Student (15)
    WHEN 'menu.data.student.status' THEN 'Статусы студентов'
    WHEN 'menu.data.student.achievement' THEN 'Достижения студентов'
    WHEN 'menu.data.student.expel' THEN 'Причины отчисления'
    WHEN 'menu.data.student.accommodation' THEN 'Типы проживания'
    WHEN 'menu.data.student.doctoral_type' THEN 'Категории докторантов'
    WHEN 'menu.data.student.social_type' THEN 'Социальные категории'
    WHEN 'menu.data.student.academic_reason' THEN 'Причины предоставления академического отпуска'
    WHEN 'menu.data.student.doctoral_status' THEN 'Статусы докторантов'
    WHEN 'menu.data.student.graduate_fields' THEN 'Области деятельности выпускников'
    WHEN 'menu.data.student.graduate_inactive' THEN 'Причины не трудоустройства'
    WHEN 'menu.data.student.student_type' THEN 'Категории студентов'
    WHEN 'menu.data.student.living_status' THEN 'Статус места проживания студента'
    WHEN 'menu.data.student.roommate_type' THEN 'Категории совместно проживающих'
    WHEN 'menu.data.student.workplace_compatibility' THEN 'Соответствие направлению'
    WHEN 'menu.data.student.academic_mobile' THEN 'Типы академической мобильности'

    -- Data > Study (18)
    WHEN 'menu.data.study.year' THEN 'Учебные годы'
    WHEN 'menu.data.study.course' THEN 'Учебные курсы'
    WHEN 'menu.data.study.semester' THEN 'Типы семестров'
    WHEN 'menu.data.study.week_type' THEN 'Учебные недели графика'
    WHEN 'menu.data.study.subject_block' THEN 'Блоки предметов'
    WHEN 'menu.data.study.subject_type' THEN 'Категории предметов'
    WHEN 'menu.data.study.class_type' THEN 'Типы занятий'
    WHEN 'menu.data.study.exam_finish' THEN 'Формы итогового контроля предмета'
    WHEN 'menu.data.study.final_exam_type' THEN 'Типы ведомостей'
    WHEN 'menu.data.study.semester_list' THEN 'Список семестров'
    WHEN 'menu.data.study.decree_type' THEN 'Типы приказов'
    WHEN 'menu.data.study.sport_type' THEN 'Виды спорта'
    WHEN 'menu.data.study.attendance_setting' THEN 'Настройки посещаемости'
    WHEN 'menu.data.study.teacher_conduction' THEN 'Формы проведения занятий'
    WHEN 'menu.data.study.internship_form' THEN 'Формы стажировки'
    WHEN 'menu.data.study.internship_type' THEN 'Типы стажировки'
    WHEN 'menu.data.study.resource_type' THEN 'Типы ресурсов'
    WHEN 'menu.data.study.outside_activities' THEN 'Внеаудиторные занятия'

    -- Data > Science (9)
    WHEN 'menu.data.science.project_type' THEN 'Типы научных проектов'
    WHEN 'menu.data.science.project_locality' THEN 'Статус научного проекта'
    WHEN 'menu.data.science.currency' THEN 'Типы валют'
    WHEN 'menu.data.science.executor_type' THEN 'Типы исполнителей'
    WHEN 'menu.data.science.publication_type' THEN 'Типы научных публикаций'
    WHEN 'menu.data.science.methodical_type' THEN 'Типы методических публикаций'
    WHEN 'menu.data.science.patient_type' THEN 'Интеллектуальная собственность'
    WHEN 'menu.data.science.publication_database' THEN 'Базы научных публикаций'
    WHEN 'menu.data.science.scholar_database' THEN 'Базы исследователей'

    -- Data > Organizational (10)
    WHEN 'menu.data.organizational.payment_form' THEN 'Формы оплаты'
    WHEN 'menu.data.organizational.stipend_rate' THEN 'Виды стипендий'
    WHEN 'menu.data.organizational.stipend_category' THEN 'Категории видов стипендий'
    WHEN 'menu.data.organizational.scholarship_decree' THEN 'Типы приказов о стипендиях'
    WHEN 'menu.data.organizational.contract_type' THEN 'Типы договоров'
    WHEN 'menu.data.organizational.contract_summa' THEN 'Типы стоимости договора'
    WHEN 'menu.data.organizational.contract_category' THEN 'Категории договоров'
    WHEN 'menu.data.organizational.auditorium_type' THEN 'Типы аудиторий'
    WHEN 'menu.data.organizational.device_type' THEN 'ИКТ устройства'
    WHEN 'menu.data.organizational.grant_type' THEN 'Типы грантов'

    -- Data > General (10)
    WHEN 'menu.data.general.country' THEN 'Список стран'
    WHEN 'menu.data.general.soato' THEN 'Области и районы'
    WHEN 'menu.data.general.nationality' THEN 'Список национальностей'
    WHEN 'menu.data.general.citizenship' THEN 'Гражданство'
    WHEN 'menu.data.general.gender' THEN 'Типы пола'
    WHEN 'menu.data.general.bachelor_specialty' THEN 'Направления образования BSc'
    WHEN 'menu.data.general.master_specialty' THEN 'Специальности MSc'
    WHEN 'menu.data.general.doctoral_specialty' THEN 'Специальности PhD и DSc'
    WHEN 'menu.data.general.terrain' THEN 'Махалли'
    WHEN 'menu.data.general.poverty_level' THEN 'Уровень бедности'

    ELSE m.message  -- Fallback
  END
FROM system_messages m
WHERE m.category = 'menu'
  AND m.message_key IN (
    -- Employee
    'menu.data.employee.type', 'menu.data.employee.status', 'menu.data.employee.rate',
    'menu.data.employee.form', 'menu.data.employee.position_type', 'menu.data.employee.qualification',
    'menu.data.employee.achievement', 'menu.data.employee.academic_degree', 'menu.data.employee.academic_rank',
    -- Student
    'menu.data.student.status', 'menu.data.student.achievement', 'menu.data.student.expel',
    'menu.data.student.accommodation', 'menu.data.student.doctoral_type', 'menu.data.student.social_type',
    'menu.data.student.academic_reason', 'menu.data.student.doctoral_status', 'menu.data.student.graduate_fields',
    'menu.data.student.graduate_inactive', 'menu.data.student.student_type', 'menu.data.student.living_status',
    'menu.data.student.roommate_type', 'menu.data.student.workplace_compatibility', 'menu.data.student.academic_mobile',
    -- Study
    'menu.data.study.year', 'menu.data.study.course', 'menu.data.study.semester',
    'menu.data.study.week_type', 'menu.data.study.subject_block', 'menu.data.study.subject_type',
    'menu.data.study.class_type', 'menu.data.study.exam_finish', 'menu.data.study.final_exam_type',
    'menu.data.study.semester_list', 'menu.data.study.decree_type', 'menu.data.study.sport_type',
    'menu.data.study.attendance_setting', 'menu.data.study.teacher_conduction', 'menu.data.study.internship_form',
    'menu.data.study.internship_type', 'menu.data.study.resource_type', 'menu.data.study.outside_activities',
    -- Science
    'menu.data.science.project_type', 'menu.data.science.project_locality', 'menu.data.science.currency',
    'menu.data.science.executor_type', 'menu.data.science.publication_type', 'menu.data.science.methodical_type',
    'menu.data.science.patient_type', 'menu.data.science.publication_database', 'menu.data.science.scholar_database',
    -- Organizational
    'menu.data.organizational.payment_form', 'menu.data.organizational.stipend_rate', 'menu.data.organizational.stipend_category',
    'menu.data.organizational.scholarship_decree', 'menu.data.organizational.contract_type', 'menu.data.organizational.contract_summa',
    'menu.data.organizational.contract_category', 'menu.data.organizational.auditorium_type', 'menu.data.organizational.device_type',
    'menu.data.organizational.grant_type',
    -- General
    'menu.data.general.country', 'menu.data.general.soato', 'menu.data.general.nationality',
    'menu.data.general.citizenship', 'menu.data.general.gender', 'menu.data.general.bachelor_specialty',
    'menu.data.general.master_specialty', 'menu.data.general.doctoral_specialty', 'menu.data.general.terrain',
    'menu.data.general.poverty_level'
  )
ON CONFLICT (message_id, language) DO UPDATE SET
  translation = EXCLUDED.translation,
  updated_at = CURRENT_TIMESTAMP;

-- ================================================
-- TRANSLATIONS: English (en-US)
-- ================================================

INSERT INTO system_message_translations (message_id, language, translation)
SELECT m.id, 'en-US',
  CASE m.message_key
    -- Data > Employee (9)
    WHEN 'menu.data.employee.type' THEN 'Employee Categories'
    WHEN 'menu.data.employee.status' THEN 'Teacher Statuses'
    WHEN 'menu.data.employee.rate' THEN 'Employment Rates'
    WHEN 'menu.data.employee.form' THEN 'Employment Forms'
    WHEN 'menu.data.employee.position_type' THEN 'Position Types'
    WHEN 'menu.data.employee.qualification' THEN 'Training Locations'
    WHEN 'menu.data.employee.achievement' THEN 'Teacher Achievements'
    WHEN 'menu.data.employee.academic_degree' THEN 'Academic Degrees'
    WHEN 'menu.data.employee.academic_rank' THEN 'Academic Ranks'

    -- Data > Student (15)
    WHEN 'menu.data.student.status' THEN 'Student Statuses'
    WHEN 'menu.data.student.achievement' THEN 'Student Achievements'
    WHEN 'menu.data.student.expel' THEN 'Expulsion Reasons'
    WHEN 'menu.data.student.accommodation' THEN 'Accommodation Types'
    WHEN 'menu.data.student.doctoral_type' THEN 'Doctoral Student Categories'
    WHEN 'menu.data.student.social_type' THEN 'Social Categories'
    WHEN 'menu.data.student.academic_reason' THEN 'Academic Leave Reasons'
    WHEN 'menu.data.student.doctoral_status' THEN 'Doctoral Student Statuses'
    WHEN 'menu.data.student.graduate_fields' THEN 'Graduate Activity Fields'
    WHEN 'menu.data.student.graduate_inactive' THEN 'Unemployment Reasons'
    WHEN 'menu.data.student.student_type' THEN 'Student Categories'
    WHEN 'menu.data.student.living_status' THEN 'Student Living Status'
    WHEN 'menu.data.student.roommate_type' THEN 'Roommate Categories'
    WHEN 'menu.data.student.workplace_compatibility' THEN 'Workplace Compatibility'
    WHEN 'menu.data.student.academic_mobile' THEN 'Academic Mobility Types'

    -- Data > Study (18)
    WHEN 'menu.data.study.year' THEN 'Academic Years'
    WHEN 'menu.data.study.course' THEN 'Academic Courses'
    WHEN 'menu.data.study.semester' THEN 'Semester Types'
    WHEN 'menu.data.study.week_type' THEN 'Academic Calendar Weeks'
    WHEN 'menu.data.study.subject_block' THEN 'Subject Blocks'
    WHEN 'menu.data.study.subject_type' THEN 'Subject Categories'
    WHEN 'menu.data.study.class_type' THEN 'Class Types'
    WHEN 'menu.data.study.exam_finish' THEN 'Final Assessment Forms'
    WHEN 'menu.data.study.final_exam_type' THEN 'Grade Sheet Types'
    WHEN 'menu.data.study.semester_list' THEN 'Semester List'
    WHEN 'menu.data.study.decree_type' THEN 'Decree Types'
    WHEN 'menu.data.study.sport_type' THEN 'Sport Types'
    WHEN 'menu.data.study.attendance_setting' THEN 'Attendance Settings'
    WHEN 'menu.data.study.teacher_conduction' THEN 'Teaching Conduction Forms'
    WHEN 'menu.data.study.internship_form' THEN 'Internship Forms'
    WHEN 'menu.data.study.internship_type' THEN 'Internship Types'
    WHEN 'menu.data.study.resource_type' THEN 'Resource Types'
    WHEN 'menu.data.study.outside_activities' THEN 'Extracurricular Activities'

    -- Data > Science (9)
    WHEN 'menu.data.science.project_type' THEN 'Scientific Project Types'
    WHEN 'menu.data.science.project_locality' THEN 'Project Locality Status'
    WHEN 'menu.data.science.currency' THEN 'Currency Types'
    WHEN 'menu.data.science.executor_type' THEN 'Executor Types'
    WHEN 'menu.data.science.publication_type' THEN 'Scientific Publication Types'
    WHEN 'menu.data.science.methodical_type' THEN 'Methodical Publication Types'
    WHEN 'menu.data.science.patient_type' THEN 'Intellectual Property'
    WHEN 'menu.data.science.publication_database' THEN 'Publication Databases'
    WHEN 'menu.data.science.scholar_database' THEN 'Scholar Databases'

    -- Data > Organizational (10)
    WHEN 'menu.data.organizational.payment_form' THEN 'Payment Forms'
    WHEN 'menu.data.organizational.stipend_rate' THEN 'Stipend Types'
    WHEN 'menu.data.organizational.stipend_category' THEN 'Stipend Rate Categories'
    WHEN 'menu.data.organizational.scholarship_decree' THEN 'Scholarship Decree Types'
    WHEN 'menu.data.organizational.contract_type' THEN 'Contract Types'
    WHEN 'menu.data.organizational.contract_summa' THEN 'Contract Amount Types'
    WHEN 'menu.data.organizational.contract_category' THEN 'Contract Categories'
    WHEN 'menu.data.organizational.auditorium_type' THEN 'Auditorium Types'
    WHEN 'menu.data.organizational.device_type' THEN 'ICT Devices'
    WHEN 'menu.data.organizational.grant_type' THEN 'Grant Types'

    -- Data > General (10)
    WHEN 'menu.data.general.country' THEN 'Country List'
    WHEN 'menu.data.general.soato' THEN 'Regions and Districts'
    WHEN 'menu.data.general.nationality' THEN 'Nationality List'
    WHEN 'menu.data.general.citizenship' THEN 'Citizenship Status'
    WHEN 'menu.data.general.gender' THEN 'Gender Types'
    WHEN 'menu.data.general.bachelor_specialty' THEN 'BSc Education Directions'
    WHEN 'menu.data.general.master_specialty' THEN 'MSc Specialties'
    WHEN 'menu.data.general.doctoral_specialty' THEN 'PhD and DSc Specialties'
    WHEN 'menu.data.general.terrain' THEN 'Neighborhoods'
    WHEN 'menu.data.general.poverty_level' THEN 'Poverty Level'

    ELSE m.message  -- Fallback
  END
FROM system_messages m
WHERE m.category = 'menu'
  AND m.message_key IN (
    -- Employee
    'menu.data.employee.type', 'menu.data.employee.status', 'menu.data.employee.rate',
    'menu.data.employee.form', 'menu.data.employee.position_type', 'menu.data.employee.qualification',
    'menu.data.employee.achievement', 'menu.data.employee.academic_degree', 'menu.data.employee.academic_rank',
    -- Student
    'menu.data.student.status', 'menu.data.student.achievement', 'menu.data.student.expel',
    'menu.data.student.accommodation', 'menu.data.student.doctoral_type', 'menu.data.student.social_type',
    'menu.data.student.academic_reason', 'menu.data.student.doctoral_status', 'menu.data.student.graduate_fields',
    'menu.data.student.graduate_inactive', 'menu.data.student.student_type', 'menu.data.student.living_status',
    'menu.data.student.roommate_type', 'menu.data.student.workplace_compatibility', 'menu.data.student.academic_mobile',
    -- Study
    'menu.data.study.year', 'menu.data.study.course', 'menu.data.study.semester',
    'menu.data.study.week_type', 'menu.data.study.subject_block', 'menu.data.study.subject_type',
    'menu.data.study.class_type', 'menu.data.study.exam_finish', 'menu.data.study.final_exam_type',
    'menu.data.study.semester_list', 'menu.data.study.decree_type', 'menu.data.study.sport_type',
    'menu.data.study.attendance_setting', 'menu.data.study.teacher_conduction', 'menu.data.study.internship_form',
    'menu.data.study.internship_type', 'menu.data.study.resource_type', 'menu.data.study.outside_activities',
    -- Science
    'menu.data.science.project_type', 'menu.data.science.project_locality', 'menu.data.science.currency',
    'menu.data.science.executor_type', 'menu.data.science.publication_type', 'menu.data.science.methodical_type',
    'menu.data.science.patient_type', 'menu.data.science.publication_database', 'menu.data.science.scholar_database',
    -- Organizational
    'menu.data.organizational.payment_form', 'menu.data.organizational.stipend_rate', 'menu.data.organizational.stipend_category',
    'menu.data.organizational.scholarship_decree', 'menu.data.organizational.contract_type', 'menu.data.organizational.contract_summa',
    'menu.data.organizational.contract_category', 'menu.data.organizational.auditorium_type', 'menu.data.organizational.device_type',
    'menu.data.organizational.grant_type',
    -- General
    'menu.data.general.country', 'menu.data.general.soato', 'menu.data.general.nationality',
    'menu.data.general.citizenship', 'menu.data.general.gender', 'menu.data.general.bachelor_specialty',
    'menu.data.general.master_specialty', 'menu.data.general.doctoral_specialty', 'menu.data.general.terrain',
    'menu.data.general.poverty_level'
  )
ON CONFLICT (message_id, language) DO UPDATE SET
  translation = EXCLUDED.translation,
  updated_at = CURRENT_TIMESTAMP;
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

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO system_messages (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
-- 2.1 Universities Count
(gen_random_uuid(), 'menu', 'menu.reports.universities.count', 'OTMlar umumiy soni', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- 2.2 Faculty List
(gen_random_uuid(), 'menu', 'menu.reports.universities.faculty_list', 'Fakultetlar soni va ro''yxati', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- ================================================
-- TRANSLATIONS: Cyrillic (oz-UZ)
-- ================================================

INSERT INTO system_message_translations (message_id, language, translation)
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
FROM system_messages m
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

INSERT INTO system_message_translations (message_id, language, translation)
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
FROM system_messages m
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

INSERT INTO system_message_translations (message_id, language, translation)
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
FROM system_messages m
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
