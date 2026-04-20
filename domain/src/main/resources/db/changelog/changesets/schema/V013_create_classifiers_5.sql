-- =====================================================
-- V018: CLASSIFIER TABLES — batch 5 (Infrastructure & Research)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-15
-- Purpose: 21 classifier tables for infrastructure/research domain.
--   Data copied from legacy hemishe_h_* tables (which remain for api-legacy).
--   Pattern: ReferenceEntity (code PK, name, is_active, audit 5 columns).
--   Exceptions: language_certificate (certificate_language_code).
-- =====================================================

-- 1. attandance_setting
CREATE TABLE attandance_setting
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO attandance_setting (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_attandance_setting
WHERE delete_ts IS NULL;

-- 2. auditorium_type
CREATE TABLE auditorium_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO auditorium_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_auditorium_type
WHERE delete_ts IS NULL;

-- 3. device_type
CREATE TABLE device_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO device_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_device_type
WHERE delete_ts IS NULL;

-- 4. diplom_blank_category
CREATE TABLE diplom_blank_category
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO diplom_blank_category (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_diplom_blank_category
WHERE delete_ts IS NULL;

-- 5. diplom_blank_generate_status
CREATE TABLE diplom_blank_generate_status
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO diplom_blank_generate_status (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_diplom_blank_generate_status
WHERE delete_ts IS NULL;

-- 6. diplom_blank_status
CREATE TABLE diplom_blank_status
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO diplom_blank_status (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_diplom_blank_status
WHERE delete_ts IS NULL;

-- 7. internship_form
CREATE TABLE internship_form
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO internship_form (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_internship_form
WHERE delete_ts IS NULL;

-- 8. internship_type
CREATE TABLE internship_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO internship_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_internship_type
WHERE delete_ts IS NULL;

-- 9. publication_database
CREATE TABLE publication_database
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO publication_database (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_publication_database
WHERE delete_ts IS NULL;

-- 10. publication_type
CREATE TABLE publication_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO publication_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_publication_type
WHERE delete_ts IS NULL;

-- 11. project_executor_type
CREATE TABLE project_executor_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO project_executor_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_project_executor_type
WHERE delete_ts IS NULL;

-- 12. project_locality
CREATE TABLE project_locality
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO project_locality (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_project_locality
WHERE delete_ts IS NULL;

-- 13. project_type
CREATE TABLE project_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO project_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_project_type
WHERE delete_ts IS NULL;

-- 14. resource_type
CREATE TABLE resource_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO resource_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_resource_type
WHERE delete_ts IS NULL;

-- 15. scholar_database
CREATE TABLE scholar_database
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO scholar_database (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_scholar_database
WHERE delete_ts IS NULL;

-- 16. scientific_project_type
CREATE TABLE scientific_project_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO scientific_project_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_scientific_project_type
WHERE delete_ts IS NULL;

-- 17. sport_type
CREATE TABLE sport_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO sport_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_sport_type
WHERE delete_ts IS NULL;

-- 18. teacher_achievement_type
CREATE TABLE teacher_achievement_type
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO teacher_achievement_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_teacher_achievement_type
WHERE delete_ts IS NULL;

-- 19. teacher_conduction_form
CREATE TABLE teacher_conduction_form
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO teacher_conduction_form (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_teacher_conduction_form
WHERE delete_ts IS NULL;

-- 20. workplace_compatibility
CREATE TABLE workplace_compatibility
(
    code       VARCHAR(20) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT NOW(),
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);
INSERT INTO workplace_compatibility (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_workplace_compatibility
WHERE delete_ts IS NULL;

-- 21. language_certificate (extra column: certificate_language_code)
CREATE TABLE language_certificate
(
    code                      VARCHAR(20) PRIMARY KEY,
    name                      VARCHAR(255) NOT NULL,
    name_ru                   VARCHAR(255),
    name_en                   VARCHAR(255),
    certificate_language_code VARCHAR(20),
    is_active                 BOOLEAN      NOT NULL DEFAULT true,
    sort_order                INTEGER               DEFAULT 0,
    version                   INTEGER               DEFAULT 1,
    created_at                TIMESTAMP             DEFAULT NOW(),
    created_by                VARCHAR(50),
    updated_at                TIMESTAMP,
    updated_by                VARCHAR(50)
);
INSERT INTO language_certificate (code, name, name_ru, name_en, certificate_language_code, is_active, version,
                                  created_at, created_by)
SELECT code,
       name,
       name_ru,
       name_en,
       _certificate_language,
       COALESCE(active, true),
       COALESCE(version, 1),
       COALESCE(create_ts, NOW()),
       'system:migration'
FROM hemishe_h_language_certificate
WHERE delete_ts IS NULL;
