-- =====================================================
-- V003: HR CLASSIFIERS — h_position_type + h_position
-- =====================================================
-- Author: hemis-team
-- Date: 2026-04-23
-- Purpose: Position classifiers for HR domain.
--   Pattern: Banner SPRIDEN/NBRJOBS, PeopleSoft PERSONAL_DATA/JOB
--   Note: h_position_type + h_position are NEW clean classifiers (h_* prefiks: ADR-0006).
--         They feed employee_job (V004) and must exist before it.
-- =====================================================

-- =====================================================
-- h_position_type (lavozim turi — 14 ta guruh)
-- h_* prefiks: 224 OTM ekosistemi (hemis_NNN bazalari) konvensiyasi (ADR-0006)
-- =====================================================
CREATE TABLE h_position_type (
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

COMMENT ON TABLE h_position_type IS 'Position type classifier: Leadership, Academic, Administrative, etc.';

-- =====================================================
-- h_position (lavozim — type ga bog'langan)
-- =====================================================
CREATE TABLE h_position (
    code       VARCHAR(10) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    name_ru    VARCHAR(255),
    name_en    VARCHAR(255),
    type_code  VARCHAR(10)  NOT NULL REFERENCES h_position_type(code),
    is_active  BOOLEAN      NOT NULL DEFAULT true,
    sort_order INTEGER               DEFAULT 0,
    version    INTEGER               DEFAULT 1,
    created_at TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

COMMENT ON TABLE h_position IS 'Position classifier: Rektor, Professor, Buxgalter, etc. Linked to h_position_type.';
CREATE INDEX idx_h_position_type ON h_position(type_code);
