-- =====================================================
-- Menu Translation System (Using SystemMessage)
-- =====================================================
-- Using existing h_system_message and h_system_message_translation tables
-- for menu labels to maintain single translation system
--
-- Architecture:
-- 1. h_system_message - stores default Uzbek (uz-UZ) message
-- 2. h_system_message_translation - stores translations (ru-RU, en-US)
-- =====================================================

-- =====================================================
-- Menu Messages - Default (Uzbek - uz-UZ)
-- =====================================================

-- Dashboard
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES (gen_random_uuid(), 'menu', 'menu.dashboard', 'Bosh sahifa', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Registry Module (4 messages)
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
VALUES
(gen_random_uuid(), 'menu', 'menu.registry', 'Reestlar', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.registry.e_reestr', 'E-Reestr', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.registry.scientific', 'Ilmiy reestr', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'menu', 'menu.registry.student_meta', 'Talaba ma''lumotlari', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (message_key) DO NOTHING;

-- Rating Module (14 messages)
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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

INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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
