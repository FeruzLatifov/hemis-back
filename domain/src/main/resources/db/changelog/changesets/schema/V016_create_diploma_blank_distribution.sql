-- =====================================================
-- V016: DIPLOMA BLANK DISTRIBUTION MODULE
-- =====================================================
-- Author: hemis-team
-- Date: 2026-07-02
-- Purpose: Ministry-managed central registry of diploma-blank serial-range
--          allocations to universities (OTM). One row = one contiguous
--          serial range (blank_start_number..blank_end_number) allocated
--          to a single OTM for a given education year/type + blank category.
-- Architecture: CENTRAL CRUD (ministry manages centrally; OTMs read via
--          existing legacy endpoints — NO fanout / outbox / webhook here).
-- Pattern: AuditableEntity (7 modern audit columns:
--          version, created_at/by, updated_at/by, deleted_at/by).
-- Classifiers resolved (raw-code fallback) via:
--          education_year        -> hemishe_h_education_year(code)
--          education_type        -> hemishe_h_education_type(code)
--          blank_category        -> hemishe_h_diplom_blank_category(code)
--          generate_status_code  -> hemishe_h_diplom_blank_generate_status(code)
-- =====================================================

CREATE TABLE IF NOT EXISTS diploma_blank_distribution (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- OTM (universitet) — ON DELETE RESTRICT (defense-in-depth; app-level soft-delete only)
    university_code VARCHAR(255) NOT NULL
        REFERENCES hemishe_e_university(code) ON DELETE RESTRICT,

    -- Classifier codes (raw code stored; name resolved via LEFT JOIN at read time)
    education_year VARCHAR(32),
    education_type VARCHAR(32),
    blank_category VARCHAR(32),

    -- Serial-range allocation
    blank_seria VARCHAR(32),
    blank_start_number INTEGER,
    blank_end_number INTEGER,

    -- Generation lifecycle status (classifier code)
    generate_status_code VARCHAR(32),

    distribution_date DATE,
    note TEXT,

    -- Data quality
    CONSTRAINT chk_dbd_range CHECK (
        blank_start_number IS NULL
        OR blank_end_number IS NULL
        OR blank_end_number >= blank_start_number
    ),
    CONSTRAINT chk_dbd_start_positive CHECK (
        blank_start_number IS NULL OR blank_start_number >= 0
    ),

    -- Audit (AuditableEntity — 7 columns)
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

COMMENT ON TABLE diploma_blank_distribution IS
    'Ministry-managed central registry of diploma-blank serial-range allocations to universities (OTM).
     One row = one contiguous serial range allocated to a single OTM. CENTRAL CRUD, no fanout.';
COMMENT ON COLUMN diploma_blank_distribution.blank_seria IS
    'Blank series (e.g. AB) for the allocated serial range';
COMMENT ON COLUMN diploma_blank_distribution.blank_start_number IS
    'Inclusive first serial number of the allocated range';
COMMENT ON COLUMN diploma_blank_distribution.blank_end_number IS
    'Inclusive last serial number of the allocated range (>= start)';

-- Indexes (query pattern: list filters + FK)
CREATE INDEX IF NOT EXISTS idx_dbd_university      ON diploma_blank_distribution(university_code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_dbd_education_year  ON diploma_blank_distribution(education_year)  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_dbd_blank_category  ON diploma_blank_distribution(blank_category)  WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_dbd_gen_status      ON diploma_blank_distribution(generate_status_code) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_dbd_dist_date       ON diploma_blank_distribution(distribution_date) WHERE deleted_at IS NULL;
