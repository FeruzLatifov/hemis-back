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

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message (id, category, message_key, message, is_active, created_at, updated_at)
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

INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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

INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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

INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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
