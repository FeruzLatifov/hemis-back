-- =====================================================
-- V016: CLASSIFIER TABLES — batch 3 (Student & Academic)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-15
-- Purpose: 28 classifier tables for student/academic domain.
--   Data copied from legacy hemishe_h_* tables (which remain for api-legacy).
--   Pattern: ReferenceEntity (code PK, name, is_active, audit 5 columns).
--   Exceptions: certificate_language, score_type, student_achievement_type have extra columns.
-- =====================================================

-- 1. academic_mobile_type
CREATE TABLE academic_mobile_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO academic_mobile_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_academic_mobile_type WHERE delete_ts IS NULL;

-- 2. academic_reason
CREATE TABLE academic_reason (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO academic_reason (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_academic_reason WHERE delete_ts IS NULL;

-- 3. accomodation
CREATE TABLE accomodation (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO accomodation (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_accomodation WHERE delete_ts IS NULL;

-- 4. certificate_grade
CREATE TABLE certificate_grade (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO certificate_grade (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_certificate_grades WHERE delete_ts IS NULL;

-- 5. certificate_language (extra column: certificate_type_code)
CREATE TABLE certificate_language (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    certificate_type_code VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO certificate_language (code, name, name_ru, name_en, certificate_type_code, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, _certificate_type, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_certificate_language WHERE delete_ts IS NULL;

-- certificate_name
CREATE TABLE certificate_name (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO certificate_name (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_certificate_names WHERE delete_ts IS NULL;

-- certificate_subject
CREATE TABLE certificate_subject (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO certificate_subject (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_certificate_subjects WHERE delete_ts IS NULL;

-- 8. certificate_type
CREATE TABLE certificate_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO certificate_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_certificate_type WHERE delete_ts IS NULL;

-- 9. class_type
CREATE TABLE class_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO class_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_class_type WHERE delete_ts IS NULL;

-- 10. education_language
CREATE TABLE education_language (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO education_language (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_education_language WHERE delete_ts IS NULL;

-- 11. education_week_type
CREATE TABLE education_week_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO education_week_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_education_week_type WHERE delete_ts IS NULL;

-- 12. exam_finish
CREATE TABLE exam_finish (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO exam_finish (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_exam_finish WHERE delete_ts IS NULL;

-- 13. exam_type
CREATE TABLE exam_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO exam_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_exam_type WHERE delete_ts IS NULL;

-- 14. expel
CREATE TABLE expel (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO expel (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_expel WHERE delete_ts IS NULL;

-- 15. final_exam_type
CREATE TABLE final_exam_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO final_exam_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_final_exam_type WHERE delete_ts IS NULL;

-- 16. grade_system_type
CREATE TABLE grade_system_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO grade_system_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_grade_system_type WHERE delete_ts IS NULL;

-- 17. score_type (extra column: grade_system_code)
CREATE TABLE score_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    grade_system_code VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO score_type (code, name, name_ru, name_en, grade_system_code, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, _grade_system, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_score_type WHERE delete_ts IS NULL;

-- 18. semester
CREATE TABLE semester (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO semester (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_semester WHERE delete_ts IS NULL;

-- 19. semester_list
CREATE TABLE semester_list (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO semester_list (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_semester_list WHERE delete_ts IS NULL;

-- 20. student_achievement_type (extra column: parent_code)
CREATE TABLE student_achievement_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    parent_code VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO student_achievement_type (code, name, name_ru, name_en, parent_code, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, parent_code, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_student_achievement_type WHERE delete_ts IS NULL;

-- 21. student_living_status
CREATE TABLE student_living_status (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO student_living_status (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_student_living_status WHERE delete_ts IS NULL;

-- 22. student_room_mate_type
CREATE TABLE student_room_mate_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO student_room_mate_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_student_room_mate_type WHERE delete_ts IS NULL;

-- 23. student_social_type
CREATE TABLE student_social_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO student_social_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_student_social_type WHERE delete_ts IS NULL;

-- 24. student_type
CREATE TABLE student_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO student_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_student_type WHERE delete_ts IS NULL;

-- 25. study_schedule_type
CREATE TABLE study_schedule_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO study_schedule_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_study_schedule_type WHERE delete_ts IS NULL;

-- 26. subject_block
CREATE TABLE subject_block (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO subject_block (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_subject_block WHERE delete_ts IS NULL;

-- 27. subject_choose_type
CREATE TABLE subject_choose_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO subject_choose_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_subject_choose_type WHERE delete_ts IS NULL;

-- 28. subject_type
CREATE TABLE subject_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO subject_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_subject_type WHERE delete_ts IS NULL;

-- 29. absence_reason (dars qoldirish sabablari — old-hemis 2026-04-16 qo'shgan)
CREATE TABLE absence_reason (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO absence_reason (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_absence_reason WHERE delete_ts IS NULL;
