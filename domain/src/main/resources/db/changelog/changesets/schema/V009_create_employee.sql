-- =====================================================
-- V009: HR MODULE — position classifiers + employee + employee_jobs
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-23
-- Purpose: Position classifiers + Central person registry + employment records (jobs)
-- Pattern: Banner SPRIDEN/NBRJOBS, PeopleSoft PERSONAL_DATA/JOB
-- Note: position_types + positions are NEW clean classifiers (not old hemishe_h_*)
-- =====================================================

-- =====================================================
-- POSITION_TYPES (lavozim turi — 14 ta guruh)
-- =====================================================
CREATE TABLE position_types (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE position_types IS 'Position type classifier: Leadership, Academic, Administrative, etc.';

-- =====================================================
-- POSITIONS (lavozim — type ga bog'langan)
-- =====================================================
CREATE TABLE positions (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    type_code VARCHAR(10) NOT NULL REFERENCES position_types(code),
    is_active BOOLEAN NOT NULL DEFAULT true,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE positions IS 'Position classifier: Rektor, Professor, Buxgalter, etc. Linked to position_type.';
CREATE INDEX idx_positions_type ON positions(type_code);

-- =====================================================
-- TABLE 1: employee (one person = one record, PINFL unique)
-- =====================================================
CREATE TABLE employee (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pinfl VARCHAR(14) NOT NULL UNIQUE,
    employee_id_number VARCHAR(50),       -- xodim shtat raqami
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    birth_date DATE,
    gender VARCHAR(2),
    citizenship VARCHAR(10),
    nationality VARCHAR(10),
    country VARCHAR(10),                  -- mamlakat kodi
    passport_series VARCHAR(10),
    passport_number VARCHAR(20),
    passport_date DATE,                   -- passport berilgan sana
    phone VARCHAR(50),
    email VARCHAR(255),
    address TEXT,
    province VARCHAR(20),                 -- viloyat kodi
    district VARCHAR(20),                 -- tuman kodi
    academic_degree VARCHAR(10),
    academic_rank VARCHAR(10),
    specialty VARCHAR(500),               -- mutaxassisligi
    tin VARCHAR(20),
    image JSONB,                          -- rasm
    year_of_enter INTEGER,                -- ishga kirgan yili

    -- Sync infrastructure
    source VARCHAR(50) DEFAULT 'hemis_sync',  -- hemis_sync, api_legal, manual, ministry, external_api
    source_university VARCHAR(10),             -- first university that created this record
    hemis_uid VARCHAR(255),                    -- original UUID from hemishe_e_teacher.id (for mapping)
    sort_order INTEGER DEFAULT 0,              -- display sort order

    -- Audit
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

-- Comments
COMMENT ON TABLE employee IS 'Central person registry — one record per PINFL, aggregated from 224 universities';
COMMENT ON COLUMN employee.pinfl IS 'Personal identification number (JSHSHIR) — unique identifier';
COMMENT ON COLUMN employee.source IS 'Data source: hemis_sync (from univer), api_legal (from legal entity API), manual';
COMMENT ON COLUMN employee.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN employee.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_employee_pinfl ON employee(pinfl);
CREATE INDEX idx_employee_name ON employee(last_name, first_name);
CREATE INDEX idx_employee_tin ON employee(tin) WHERE tin IS NOT NULL;
CREATE INDEX idx_employee_deleted ON employee(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_employee_passport ON employee(passport_series, passport_number) WHERE passport_number IS NOT NULL;

-- =====================================================
-- EMPLOYEE_JOBS (one person = many positions at many universities)
-- Pattern: PeopleSoft PS_JOB, Oracle PER_ALL_ASSIGNMENTS_F, Banner NBRJOBS
-- Direct successor of hemishe_e_employee_jobs (old CUBA table, stripped of prefix)
-- =====================================================
CREATE TABLE employee_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    employee_id_number VARCHAR(50),      -- xodim shtat raqami (univer dan)
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code),
    department_code VARCHAR(255),
    position_code VARCHAR(10) REFERENCES positions(code) ON DELETE RESTRICT,          -- position (new classifier)
    employee_type VARCHAR(10) REFERENCES position_types(code) ON DELETE RESTRICT,   -- position_type (new classifier)
    employment_form VARCHAR(10),
    employment_staff VARCHAR(10),
    employee_status VARCHAR(10),
    employee_rate VARCHAR(10),

    is_current BOOLEAN NOT NULL DEFAULT true,
    start_date DATE,
    end_date DATE,

    contract_number VARCHAR(100),
    contract_date DATE,
    decree_number VARCHAR(100),
    decree_date DATE,

    -- Sync infrastructure
    source VARCHAR(50) DEFAULT 'hemis_sync',
    source_uid VARCHAR(255),            -- original hemishe_e_employee_jobs.id
    hemis_uid VARCHAR(255),              -- original UUID from hemishe_e_employee_jobs._uid
    hash VARCHAR(64),                    -- sync uchun o'zgarish aniqlash (MD5/SHA256)
    sort_order INTEGER DEFAULT 0,

    -- Audit
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

-- Comments
COMMENT ON TABLE employee_jobs IS 'Employment records — one person can hold many jobs at many universities. PeopleSoft: PS_JOB. Banner: NBRJOBS. Old CUBA: hemishe_e_employee_jobs.';
COMMENT ON COLUMN employee_jobs.university_code IS 'Which university (like PeopleSoft SetID)';
COMMENT ON COLUMN employee_jobs.is_current IS 'Active assignment flag — false when person leaves or position changes';
COMMENT ON COLUMN employee_jobs.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN employee_jobs.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_ejobs_employee ON employee_jobs(employee_id);
CREATE INDEX idx_ejobs_university ON employee_jobs(university_code);
CREATE INDEX idx_ejobs_current ON employee_jobs(employee_id, is_current) WHERE is_current = true AND deleted_at IS NULL;
CREATE INDEX idx_ejobs_position ON employee_jobs(position_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_ejobs_deleted ON employee_jobs(deleted_at) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX idx_ejobs_source_uid ON employee_jobs(source_uid) WHERE source_uid IS NOT NULL;

-- =====================================================
-- DEFERRED FK: users.employee_id → employee(id)
-- =====================================================
-- V001 da employee_id ustuni yaratilgan, lekin employee jadvali hali yo'q edi.
-- Endi employee jadvali yaratildi — FK constraintni qo'shamiz.
ALTER TABLE users ADD CONSTRAINT fk_users_employee
    FOREIGN KEY (employee_id) REFERENCES employee(id)
    ON DELETE SET NULL;
