-- =====================================================
-- V12: Add Cyrillic Uzbek Translations for Menus
-- oz-UZ (Uzbek Cyrillic) - 4th language support
--
-- Author: System Architect
-- Date: 2025-11-10
-- =====================================================

-- Insert Cyrillic Uzbek translations for existing menu items
INSERT INTO h_system_message_translation (message_id, language, translation)
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
FROM h_system_message m
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
