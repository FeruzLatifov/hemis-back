-- =====================================================
-- V015: CLASSIFIER TABLES — batch 2 (Entity+Repository)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-16
-- Purpose: 16 classifier tables that have existing JPA entities + repositories.
--   Data copied from legacy hemishe_h_* tables (which remain for api-legacy).
--   Pattern: ReferenceEntity (code PK, name, is_active, audit 5 columns).
-- =====================================================

-- 1. admission_type
CREATE TABLE admission_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO admission_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_admission_type WHERE delete_ts IS NULL;

-- 2. doctoral_student_status
CREATE TABLE doctoral_student_status (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO doctoral_student_status (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_doctoral_student_status WHERE delete_ts IS NULL;

-- 3. doctoral_student_type
CREATE TABLE doctoral_student_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO doctoral_student_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_doctoral_student_type WHERE delete_ts IS NULL;

-- 4. education_form
CREATE TABLE education_form (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO education_form (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_education_form WHERE delete_ts IS NULL;

-- 5. education_type
CREATE TABLE education_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO education_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_education_type WHERE delete_ts IS NULL;

-- 6. education_year
CREATE TABLE education_year (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO education_year (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_education_year WHERE delete_ts IS NULL;

-- 7. course
CREATE TABLE course (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO course (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_course WHERE delete_ts IS NULL;

-- 8. university_department_type
CREATE TABLE university_department_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO university_department_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_university_department_type WHERE delete_ts IS NULL;

-- 9. methodical_publication_type
CREATE TABLE methodical_publication_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO methodical_publication_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_methodical_publication_type WHERE delete_ts IS NULL;

-- 10. publication_locality
CREATE TABLE publication_locality (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO publication_locality (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_publication_locality WHERE delete_ts IS NULL;

-- 11. student_status_type
CREATE TABLE student_status_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO student_status_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_student_status_type WHERE delete_ts IS NULL;

-- 12. teacher_position_type
CREATE TABLE teacher_position_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO teacher_position_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_teacher_position_type WHERE delete_ts IS NULL;

-- 13. transfer_type
CREATE TABLE transfer_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO transfer_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_transfer_type WHERE delete_ts IS NULL;

-- 14. university_employee_status_type
CREATE TABLE university_employee_status_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO university_employee_status_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_university_employee_status_type WHERE delete_ts IS NULL;

-- 15. university_employee_type
CREATE TABLE university_employee_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO university_employee_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_university_employee_type WHERE delete_ts IS NULL;

-- 16. verification_type
CREATE TABLE verification_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO verification_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_verification_type WHERE delete_ts IS NULL;
