-- =====================================================
-- V003: HR CLASSIFIERS — position_type + position
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-23
-- Purpose: Position classifiers for HR domain.
--   Pattern: Banner SPRIDEN/NBRJOBS, PeopleSoft PERSONAL_DATA/JOB
--   Note: position_type + position are NEW clean classifiers (not old hemishe_h_*).
--         They feed employee_job (V004) and must exist before it.
-- =====================================================

-- =====================================================
-- POSITION_TYPE (lavozim turi — 14 ta guruh)
-- =====================================================
CREATE TABLE position_type (
    code       VARCHAR(10) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

COMMENT ON TABLE position_type IS 'Position type classifier: Leadership, Academic, Administrative, etc.';

-- =====================================================
-- POSITION (lavozim — type ga bog'langan)
-- =====================================================
CREATE TABLE position (
    code       VARCHAR(10) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    type_code  VARCHAR(10)  NOT NULL REFERENCES position_type(code),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

COMMENT ON TABLE position IS 'Position classifier: Rektor, Professor, Buxgalter, etc. Linked to position_type.';
CREATE INDEX idx_position_type ON position(type_code);
