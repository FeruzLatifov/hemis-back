-- =====================================================
-- V017: CLASSIFIER TABLES — batch 4 (Finance & Admin)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-15
-- Purpose: 20 classifier tables for finance/admin domain.
--   Data copied from legacy hemishe_h_* tables (which remain for api-legacy).
--   Pattern: ReferenceEntity (code PK, name, is_active, audit 5 columns).
--   Exceptions: grant_type (payment_form_code, grant_form), stipend_rate (category_code).
-- =====================================================

-- 1. contract_summa_type
CREATE TABLE contract_summa_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO contract_summa_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_contract_summa_type WHERE delete_ts IS NULL;

-- 2. contract_type
CREATE TABLE contract_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO contract_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_contract_type WHERE delete_ts IS NULL;


-- 3. contract_class (shartnoma sinfi: yillik/qayta o'qish/qisman)
CREATE TABLE contract_class (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO contract_class (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_contract_types WHERE delete_ts IS NULL;

-- 4. country
CREATE TABLE country (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO country (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_country WHERE delete_ts IS NULL;

-- 5. currency
CREATE TABLE currency (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO currency (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_currency WHERE delete_ts IS NULL;

-- 6. decree_type
CREATE TABLE decree_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO decree_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_decree_type WHERE delete_ts IS NULL;

-- 7. employee_age_range
CREATE TABLE employee_age_range (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO employee_age_range (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_employee_age_range WHERE delete_ts IS NULL;

-- 8. external_service_type
CREATE TABLE external_service_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO external_service_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_external_service_type WHERE delete_ts IS NULL;

-- 9. grant_type (extra columns: payment_form_code, grant_form)
CREATE TABLE grant_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    payment_form_code VARCHAR(20),
    grant_form VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO grant_type (code, name, name_ru, name_en, payment_form_code, grant_form, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, _payment_form, grant_form, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_grant_type WHERE delete_ts IS NULL;

-- 10. graduate_fields_type
CREATE TABLE graduate_fields_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO graduate_fields_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_graduate_fields_type WHERE delete_ts IS NULL;

-- 11. graduate_inactive_type
CREATE TABLE graduate_inactive_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO graduate_inactive_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_graduate_inactive_type WHERE delete_ts IS NULL;

-- 12. locality_type
CREATE TABLE locality_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO locality_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_locality_type WHERE delete_ts IS NULL;

-- 13. outside_activity
CREATE TABLE outside_activity (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO outside_activity (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_outside_activities WHERE delete_ts IS NULL;

-- 14. patient_type
CREATE TABLE patient_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO patient_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_patient_type WHERE delete_ts IS NULL;

-- 15. payment_form
CREATE TABLE payment_form (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO payment_form (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_payment_form WHERE delete_ts IS NULL;

-- 16. poverty_level
CREATE TABLE poverty_level (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO poverty_level (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_poverty_level WHERE delete_ts IS NULL;

-- 17. qualification
CREATE TABLE qualification (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO qualification (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_qualification WHERE delete_ts IS NULL;

-- 18. stipend_rate (extra column: category_code)
CREATE TABLE stipend_rate (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    category_code VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO stipend_rate (code, name, name_ru, name_en, category_code, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, _category, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_stipend_rate WHERE delete_ts IS NULL;

-- 19. stipend_rate_category
CREATE TABLE stipend_rate_category (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO stipend_rate_category (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_stipend_rate_category WHERE delete_ts IS NULL;

-- 20. scholarship_decree_type
CREATE TABLE scholarship_decree_type (
    code VARCHAR(20) PRIMARY KEY, name VARCHAR(255) NOT NULL, name_ru VARCHAR(255), name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true, sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1, created_at TIMESTAMP DEFAULT NOW(), created_by VARCHAR(50), updated_at TIMESTAMP, updated_by VARCHAR(50)
);
INSERT INTO scholarship_decree_type (code, name, name_ru, name_en, is_active, version, created_at, created_by)
SELECT code, name, name_ru, name_en, COALESCE(active, true), COALESCE(version, 1), COALESCE(create_ts, NOW()), 'system:migration'
FROM hemishe_h_scholarship_decree_type WHERE delete_ts IS NULL;
