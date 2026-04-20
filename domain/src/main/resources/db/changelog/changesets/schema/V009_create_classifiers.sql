-- =====================================================
-- V014: REFERENCE/CLASSIFIER TABLES (clean architecture)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-16
-- Purpose: Clean classifier tables with modern naming + audit.
--   Data copied from legacy hemishe_h_* tables (which remain for api-legacy).
--   Pattern: ReferenceEntity base (code PK, name, name_ru, name_en, is_active,
--            version, created_at/by, updated_at/by — NO deleted_at).
-- =====================================================

-- =====================================================
-- 1. gender
-- =====================================================
CREATE TABLE gender (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO gender (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_gender WHERE delete_ts IS NULL;

-- =====================================================
-- 2. citizenship
-- =====================================================
CREATE TABLE citizenship (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO citizenship (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_citizenship WHERE delete_ts IS NULL;

-- =====================================================
-- 3. nationality
-- =====================================================
CREATE TABLE nationality (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO nationality (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_nationality WHERE delete_ts IS NULL;

-- =====================================================
-- 4. academic_degree
-- =====================================================
CREATE TABLE academic_degree (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO academic_degree (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_academic_degree WHERE delete_ts IS NULL;

-- =====================================================
-- 5. academic_rank
-- =====================================================
CREATE TABLE academic_rank (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO academic_rank (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_academic_rank WHERE delete_ts IS NULL;

-- =====================================================
-- 6. ownership
-- =====================================================
CREATE TABLE ownership (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO ownership (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_ownership WHERE delete_ts IS NULL;

-- =====================================================
-- 7. university_type
-- =====================================================
CREATE TABLE university_type (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO university_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_university_type WHERE delete_ts IS NULL;

-- =====================================================
-- 8. university_belongs_to
-- =====================================================
CREATE TABLE university_belongs_to (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO university_belongs_to (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_university_belongs_to WHERE delete_ts IS NULL;

-- =====================================================
-- 9. contract_category
-- =====================================================
CREATE TABLE contract_category (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO contract_category (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_university_contract_category WHERE delete_ts IS NULL;

-- =====================================================
-- 10. hemis_version
-- =====================================================
CREATE TABLE hemis_version (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO hemis_version (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_hemis_version_type WHERE delete_ts IS NULL;

-- =====================================================
-- 11. employment_form
-- =====================================================
CREATE TABLE employment_form (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO employment_form (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_university_employee_form WHERE delete_ts IS NULL;

-- =====================================================
-- 12. employee_rate
-- =====================================================
CREATE TABLE employee_rate (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO employee_rate (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_university_employee_rate WHERE delete_ts IS NULL;

-- =====================================================
-- 13. soato (hierarchical: 4=region, 7=district)
-- =====================================================
CREATE TABLE soato (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    parent_code VARCHAR(20) REFERENCES soato(code),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

-- Insert parents (regions) first, then children (districts)
INSERT INTO soato (code, name, name_ru, parent_code, is_active, version, created_at, created_by)
SELECT code, name_uz, name_ru, NULL, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_soato WHERE delete_ts IS NULL AND parent_code IS NULL
ORDER BY code;

INSERT INTO soato (code, name, name_ru, parent_code, is_active, version, created_at, created_by)
SELECT code, name_uz, name_ru, parent_code, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_soato WHERE delete_ts IS NULL AND parent_code IS NOT NULL
ORDER BY code;

CREATE INDEX idx_soato_parent ON soato(parent_code) WHERE parent_code IS NOT NULL;

-- =====================================================
-- 14. terrain (mahalla/neighborhood — linked to soato)
-- =====================================================
CREATE TABLE terrain (
    code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    soato_code VARCHAR(20) REFERENCES soato(code),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

INSERT INTO terrain (code, name, name_ru, name_en, soato_code, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, _soato, COALESCE(active, true), COALESCE(version, 1),
       COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_terrain WHERE delete_ts IS NULL;

CREATE INDEX idx_terrain_soato ON terrain(soato_code) WHERE soato_code IS NOT NULL;
