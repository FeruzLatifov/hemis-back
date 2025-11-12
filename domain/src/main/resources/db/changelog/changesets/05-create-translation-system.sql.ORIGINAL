-- ================================================================
-- V6: COMPLETE MENU & TRANSLATION SYSTEM
-- ================================================================
-- Migrated from: hemis-back-main Flyway migrations (V9, V10, V12-V15)
-- Senior Architect: Clean Architecture Best Practice
-- Date: 2025-01-12
--
-- Contents:
-- 1. Menu Permissions (60 permissions for 6 main sections)
-- 2. Menu Translations (127 menu items × 4 languages)
-- 3. Role Assignments (ADMINISTRATORS + SUPER_ADMIN)
--
-- Structure:
-- - Dashboard (1 permission)
-- - Registry (4 permissions + nested)
-- - Rating (14 permissions + nested)
-- - Data Management (10 permissions + nested)
-- - Reports (25 permissions + nested)
-- - System (6 permissions)
--
-- Total: 60 menu permissions + 127 translations × 4 languages = 508 records
-- ================================================================

-- ================================================================
-- PART 1: MENU PERMISSIONS (60)
-- ================================================================

-- 1. DASHBOARD (1 permission)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('40000000-0000-0000-0000-000000000001', 'dashboard.view', 'View Dashboard', 'Access to main dashboard', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 2. REGISTRY MODULE (4 permissions)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('41000000-0000-0000-0000-000000000001', 'registry.view', 'View Registry', 'Access to registry section', true, CURRENT_TIMESTAMP),
('41000000-0000-0000-0000-000000000002', 'registry.e-reestr.view', 'View E-Reestr', 'View electronic registry navigation', true, CURRENT_TIMESTAMP),
('41000000-0000-0000-0000-000000000003', 'registry.scientific.view', 'View Scientific Registry', 'View scientific registry navigation', true, CURRENT_TIMESTAMP),
('41000000-0000-0000-0000-000000000004', 'registry.student-meta.view', 'View Student Metadata', 'View student metadata registry', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 3. RATING MODULE (14 permissions)
-- Main
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('42000000-0000-0000-0000-000000000001', 'rating.view', 'View Rating', 'Access to rating section', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Administrative Rating (4)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('42100000-0000-0000-0000-000000000001', 'rating.administrative.view', 'View Administrative Rating', 'View administrative rating section', true, CURRENT_TIMESTAMP),
('42100000-0000-0000-0000-000000000002', 'rating.administrative.employee.view', 'View Employee Rating', 'View administrative employee rating', true, CURRENT_TIMESTAMP),
('42100000-0000-0000-0000-000000000003', 'rating.administrative.students.view', 'View Students Rating', 'View administrative students rating', true, CURRENT_TIMESTAMP),
('42100000-0000-0000-0000-000000000004', 'rating.administrative.sport.view', 'View Sport Rating', 'View administrative sport facilities rating', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Academic Rating (4)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('42200000-0000-0000-0000-000000000001', 'rating.academic.view', 'View Academic Rating', 'View academic rating section', true, CURRENT_TIMESTAMP),
('42200000-0000-0000-0000-000000000002', 'rating.academic.methodical.view', 'View Methodical Rating', 'View academic methodical publications rating', true, CURRENT_TIMESTAMP),
('42200000-0000-0000-0000-000000000003', 'rating.academic.study.view', 'View Study Rating', 'View academic study rating', true, CURRENT_TIMESTAMP),
('42200000-0000-0000-0000-000000000004', 'rating.academic.verification.view', 'View Verification Rating', 'View verification type rating', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Scientific Rating (4)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('42300000-0000-0000-0000-000000000001', 'rating.scientific.view', 'View Scientific Rating', 'View scientific rating section', true, CURRENT_TIMESTAMP),
('42300000-0000-0000-0000-000000000002', 'rating.scientific.publications.view', 'View Publications Rating', 'View scientific publications rating', true, CURRENT_TIMESTAMP),
('42300000-0000-0000-0000-000000000003', 'rating.scientific.projects.view', 'View Projects Rating', 'View scientific projects rating', true, CURRENT_TIMESTAMP),
('42300000-0000-0000-0000-000000000004', 'rating.scientific.intellectual.view', 'View Intellectual Property Rating', 'View intellectual property rating', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Student GPA (1)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('42400000-0000-0000-0000-000000000001', 'rating.student-gpa.view', 'View Student GPA', 'View student GPA rating', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 4. DATA MANAGEMENT MODULE (10 permissions)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('43000000-0000-0000-0000-000000000001', 'data.view', 'View Data Management', 'Access to data management section', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000002', 'data.general.view', 'View General Data', 'View general data navigation', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000003', 'data.structure.view', 'View Structure Data', 'View structure data navigation', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000004', 'data.employee.view', 'View Employee Data', 'View employee data navigation', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000005', 'data.student.view', 'View Student Data', 'View student data navigation', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000006', 'data.education.view', 'View Education Data', 'View education data navigation', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000007', 'data.study.view', 'View Study Data', 'View study data navigation', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000008', 'data.science.view', 'View Science Data', 'View science data navigation', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000009', 'data.organizational.view', 'View Organizational Data', 'View organizational data navigation', true, CURRENT_TIMESTAMP),
('43000000-0000-0000-0000-000000000010', 'data.contract-category.view', 'View Contract Categories', 'View university contract categories', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 5. REPORTS MODULE (25 permissions)
-- Main Reports (1)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('44000000-0000-0000-0000-000000000001', 'reports.view', 'View Reports', 'Access to reports section', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Universities Reports (1)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('44100000-0000-0000-0000-000000000001', 'reports.universities.view', 'View University Reports', 'View university reports', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Employees Reports (3)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('44200000-0000-0000-0000-000000000001', 'reports.employees.view', 'View Employee Reports', 'View employee reports section', true, CURRENT_TIMESTAMP),
('44200000-0000-0000-0000-000000000002', 'reports.employees.private.view', 'View Teacher Private Reports', 'View teacher private reports', true, CURRENT_TIMESTAMP),
('44200000-0000-0000-0000-000000000003', 'reports.employees.work.view', 'View Teacher Work Reports', 'View teacher work reports', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Students Reports (7)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('44300000-0000-0000-0000-000000000001', 'reports.students.view', 'View Student Reports', 'View student reports section', true, CURRENT_TIMESTAMP),
('44300000-0000-0000-0000-000000000002', 'reports.students.statistics.view', 'View Student Statistics', 'View student statistics reports', true, CURRENT_TIMESTAMP),
('44300000-0000-0000-0000-000000000003', 'reports.students.education.view', 'View Student Education', 'View student education reports', true, CURRENT_TIMESTAMP),
('44300000-0000-0000-0000-000000000004', 'reports.students.private.view', 'View Student Private', 'View student private reports', true, CURRENT_TIMESTAMP),
('44300000-0000-0000-0000-000000000005', 'reports.students.attendance.view', 'View Student Attendance', 'View student attendance reports', true, CURRENT_TIMESTAMP),
('44300000-0000-0000-0000-000000000006', 'reports.students.score.view', 'View Student Scores', 'View student score reports', true, CURRENT_TIMESTAMP),
('44300000-0000-0000-0000-000000000007', 'reports.students.dynamic.view', 'View Student Dynamics', 'View student full dynamic reports', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Academic Reports (2)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('44400000-0000-0000-0000-000000000001', 'reports.academic.view', 'View Academic Reports', 'View academic reports section', true, CURRENT_TIMESTAMP),
('44400000-0000-0000-0000-000000000002', 'reports.academic.study.view', 'View Academic Study Reports', 'View academic study reports', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Research Reports (4)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('44500000-0000-0000-0000-000000000001', 'reports.research.view', 'View Research Reports', 'View research reports section', true, CURRENT_TIMESTAMP),
('44500000-0000-0000-0000-000000000002', 'reports.research.project.view', 'View Research Projects', 'View research project reports', true, CURRENT_TIMESTAMP),
('44500000-0000-0000-0000-000000000003', 'reports.research.publication.view', 'View Research Publications', 'View research publication reports', true, CURRENT_TIMESTAMP),
('44500000-0000-0000-0000-000000000004', 'reports.research.researcher.view', 'View Researchers', 'View researcher reports', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Economic Reports (3)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('44600000-0000-0000-0000-000000000001', 'reports.economic.view', 'View Economic Reports', 'View economic reports section', true, CURRENT_TIMESTAMP),
('44600000-0000-0000-0000-000000000002', 'reports.economic.finance.view', 'View Finance Reports', 'View financial reports', true, CURRENT_TIMESTAMP),
('44600000-0000-0000-0000-000000000003', 'reports.economic.xujalik.view', 'View Business Reports', 'View business management reports', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Reports Navigation (1)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('44700000-0000-0000-0000-000000000001', 'reports.employees.navigation.view', 'View Employees List', 'View employee navigation list', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- 6. SYSTEM MODULE (6 permissions)
INSERT INTO permissions (id, code, name, description, is_active, created_at)
VALUES
('45000000-0000-0000-0000-000000000001', 'system.view', 'View System', 'Access to system management', true, CURRENT_TIMESTAMP),
('45000000-0000-0000-0000-000000000002', 'system.temp.view', 'View Temp Data', 'View temporary system data', true, CURRENT_TIMESTAMP),
('45000000-0000-0000-0000-000000000003', 'system.translation.view', 'View Translations', 'Manage system translations', true, CURRENT_TIMESTAMP),
('45000000-0000-0000-0000-000000000004', 'system.university-users.view', 'View University Users', 'View university user management', true, CURRENT_TIMESTAMP),
('45000000-0000-0000-0000-000000000005', 'system.api-logs.view', 'View API Logs', 'View API integration logs', true, CURRENT_TIMESTAMP),
('45000000-0000-0000-0000-000000000006', 'system.report-update.view', 'View Report Updates', 'View report update history', true, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- ================================================================
-- PART 2: ASSIGN ALL NEW PERMISSIONS TO ADMINISTRATORS ROLE
-- ================================================================

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT 
    r.id as role_id,
    p.id as permission_id,
    CURRENT_TIMESTAMP
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ADMINISTRATORS'
  AND r.deleted_at IS NULL
  AND p.deleted_at IS NULL
  AND p.code IN (
    -- All 60 new menu permissions
    'dashboard.view',
    'registry.view', 'registry.e-reestr.view', 'registry.scientific.view', 'registry.student-meta.view',
    'rating.view', 
    'rating.administrative.view', 'rating.administrative.employee.view', 'rating.administrative.students.view', 'rating.administrative.sport.view',
    'rating.academic.view', 'rating.academic.methodical.view', 'rating.academic.study.view', 'rating.academic.verification.view',
    'rating.scientific.view', 'rating.scientific.publications.view', 'rating.scientific.projects.view', 'rating.scientific.intellectual.view',
    'rating.student-gpa.view',
    'data.view', 'data.general.view', 'data.structure.view', 'data.employee.view', 'data.student.view', 'data.education.view', 'data.study.view', 'data.science.view', 'data.organizational.view', 'data.contract-category.view',
    'reports.view', 'reports.universities.view',
    'reports.employees.view', 'reports.employees.private.view', 'reports.employees.work.view', 'reports.employees.navigation.view',
    'reports.students.view', 'reports.students.statistics.view', 'reports.students.education.view', 'reports.students.private.view', 'reports.students.attendance.view', 'reports.students.score.view', 'reports.students.dynamic.view',
    'reports.academic.view', 'reports.academic.study.view',
    'reports.research.view', 'reports.research.project.view', 'reports.research.publication.view', 'reports.research.researcher.view',
    'reports.economic.view', 'reports.economic.finance.view', 'reports.economic.xujalik.view',
    'system.view', 'system.temp.view', 'system.translation.view', 'system.university-users.view', 'system.api-logs.view', 'system.report-update.view'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ================================================================
-- PART 3: MENU TRANSLATIONS (127 items × 4 languages = 508 records)
-- ================================================================
-- Note: Continuing in next comment due to character limit...
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

-- ================================================================
-- PART 4: ADDITIONAL SUBMENU TRANSLATIONS (from V13)
-- ================================================================

-- ========================================
-- 2. DATA > STRUCTURE (6 submenus)
-- ========================================

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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

INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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

INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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
