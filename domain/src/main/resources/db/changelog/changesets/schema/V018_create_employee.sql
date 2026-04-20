-- =====================================================
-- V009: HR MODULE — position classifiers + employee + employee_job
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-23
-- Purpose: Position classifiers + Central person registry + employment records (jobs)
-- Pattern: Banner SPRIDEN/NBRJOBS, PeopleSoft PERSONAL_DATA/JOB
-- Note: position_type + position are NEW clean classifiers (not old hemishe_h_*)
-- =====================================================

-- =====================================================
-- POSITION_TYPE (lavozim turi — 14 ta guruh)
-- =====================================================
CREATE TABLE position_type (
    code VARCHAR(10) PRIMARY KEY,
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

COMMENT ON TABLE position_type IS 'Position type classifier: Leadership, Academic, Administrative, etc.';

-- =====================================================
-- POSITION (lavozim — type ga bog'langan)
-- =====================================================
CREATE TABLE position (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    name_en VARCHAR(255),
    type_code VARCHAR(10) NOT NULL REFERENCES position_type(code),
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

COMMENT ON TABLE position IS 'Position classifier: Rektor, Professor, Buxgalter, etc. Linked to position_type.';
CREATE INDEX idx_position_type ON position(type_code);

-- =====================================================
-- TABLE 1: employee (one person = one record, PINFL unique)
-- =====================================================
CREATE TABLE employee (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pinfl VARCHAR(14) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    birth_date DATE,
    gender_code VARCHAR(2) REFERENCES gender(code),
    citizenship_code VARCHAR(10) REFERENCES citizenship(code),
    nationality_code VARCHAR(10) REFERENCES nationality(code),
    passport_series VARCHAR(10),
    passport_number VARCHAR(20),
    passport_date DATE,
    phone VARCHAR(50),
    email VARCHAR(255),
    address TEXT,
    -- Single hierarchical SOATO code: first 4 digits = region, 7 = district, 11 = neighborhood.
    -- Replaces legacy `province` + `district` pair — prefix derivation on the UI side.
    soato_code VARCHAR(20) REFERENCES soato(code),
    academic_degree_code VARCHAR(10) REFERENCES academic_degree(code),
    academic_rank_code VARCHAR(10) REFERENCES academic_rank(code),
    tin VARCHAR(20),

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
COMMENT ON COLUMN employee.soato_code IS 'Address SOATO (hierarchical): 4=region, 7=district, 11=neighborhood';
COMMENT ON COLUMN employee.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN employee.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_employee_pinfl ON employee(pinfl);
CREATE INDEX idx_employee_name ON employee(last_name, first_name);
CREATE INDEX idx_employee_tin ON employee(tin) WHERE tin IS NOT NULL;
CREATE INDEX idx_employee_deleted ON employee(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_employee_passport ON employee(passport_series, passport_number) WHERE passport_number IS NOT NULL;
CREATE INDEX idx_employee_soato ON employee(soato_code) WHERE soato_code IS NOT NULL;

-- =====================================================
-- EMPLOYEE_JOB (one person = many positions at many universities)
-- Pattern: PeopleSoft PS_JOB, Oracle PER_ALL_ASSIGNMENTS_F, Banner NBRJOBS
-- Direct successor of hemishe_e_employee_jobs (old CUBA table, stripped of prefix)
-- =====================================================
CREATE TABLE employee_job (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code),
    department_code VARCHAR(255),
    position_code VARCHAR(10) REFERENCES position(code) ON DELETE RESTRICT,
    employee_type_code VARCHAR(10) REFERENCES position_type(code) ON DELETE RESTRICT,
    employment_form_code VARCHAR(10),
    employee_rate_code VARCHAR(10),
    specialty VARCHAR(500),              -- job-level specialty (moved from `employee`)

    is_current BOOLEAN NOT NULL DEFAULT true,
    start_date DATE,
    end_date DATE,

    contract_number VARCHAR(100),
    contract_date DATE,
    decree_number VARCHAR(100),
    decree_date DATE,

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
COMMENT ON TABLE employee_job IS 'Employment records — one person can hold many jobs at many universities. PeopleSoft: PS_JOB. Banner: NBRJOBS.';
COMMENT ON COLUMN employee_job.university_code IS 'Which university (like PeopleSoft SetID)';
COMMENT ON COLUMN employee_job.is_current IS 'Active assignment flag — false when person leaves or position changes';
COMMENT ON COLUMN employee_job.specialty IS 'Specialty for this specific job (assignment-scoped, not person-scoped)';
COMMENT ON COLUMN employee_job.version IS 'Optimistic locking version (JPA @Version)';
COMMENT ON COLUMN employee_job.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_ejob_employee ON employee_job(employee_id);
CREATE INDEX idx_ejob_university ON employee_job(university_code);
CREATE INDEX idx_ejob_current ON employee_job(employee_id, is_current) WHERE is_current = true AND deleted_at IS NULL;
CREATE INDEX idx_ejob_position ON employee_job(position_code) WHERE deleted_at IS NULL;
CREATE INDEX idx_ejob_deleted ON employee_job(deleted_at) WHERE deleted_at IS NULL;

-- =====================================================
-- DEFERRED FKs: columns declared in earlier migrations,
--               constraints added here (employee created now)
-- =====================================================

-- users.employee_id (V001)
ALTER TABLE users ADD CONSTRAINT fk_users_employee
    FOREIGN KEY (employee_id) REFERENCES employee(id)
    ON DELETE SET NULL;

-- university_legal.{director,accountant}_employee_id (V014)
ALTER TABLE university_legal ADD CONSTRAINT fk_ul_director
    FOREIGN KEY (director_employee_id) REFERENCES employee(id)
    ON DELETE SET NULL;
ALTER TABLE university_legal ADD CONSTRAINT fk_ul_accountant
    FOREIGN KEY (accountant_employee_id) REFERENCES employee(id)
    ON DELETE SET NULL;

-- university_founder.employee_id (V015)
ALTER TABLE university_founder ADD CONSTRAINT fk_uf_employee
    FOREIGN KEY (employee_id) REFERENCES employee(id)
    ON DELETE SET NULL;
