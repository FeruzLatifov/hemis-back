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

-- Insert all 19 e-reestr submenus into h_system_message
INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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
