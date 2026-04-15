-- =====================================================
-- V013: CADASTRE MODULE — university_cadastre
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-25
-- Purpose: Real estate objects from cadastre API (172.18.9.171/kadastr/)
-- Pattern: ARCHIBUS property table + Banner SLBBLDG
-- =====================================================

CREATE TABLE university_cadastre (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    university_code VARCHAR(255) NOT NULL REFERENCES hemishe_e_university(code),

    -- Cadastre identity
    cad_number VARCHAR(50) NOT NULL UNIQUE,
    cad_number_old VARCHAR(50),

    -- Location (flat columns for admin panel filter/sort)
    region_id INTEGER,
    region VARCHAR(255),
    district_id INTEGER,
    district VARCHAR(255),
    address TEXT,
    short_address VARCHAR(500),
    street VARCHAR(500),
    street_code VARCHAR(50),
    dom_num VARCHAR(50),
    neighborhood VARCHAR(255),
    neighborhood_id VARCHAR(50),

    -- Object classification
    tip VARCHAR(10),
    tip_text VARCHAR(255),
    vid VARCHAR(10),
    vid_text VARCHAR(255),

    -- Land area (sq meters)
    land_area NUMERIC(12,2) DEFAULT 0,
    land_area_i NUMERIC(12,2) DEFAULT 0,
    land_area_b NUMERIC(12,2) DEFAULT 0,
    land_area_f NUMERIC(12,2) DEFAULT 0,
    land_area_z NUMERIC(12,2) DEFAULT 0,
    land_area_d NUMERIC(12,2) DEFAULT 0,
    land_area_u NUMERIC(12,2) DEFAULT 0,

    -- Object area (sq meters)
    object_area NUMERIC(12,2) DEFAULT 0,
    object_area_l NUMERIC(12,2) DEFAULT 0,
    object_area_u NUMERIC(12,2) DEFAULT 0,

    -- Value
    cost BIGINT,

    -- Legal status
    eco_zone VARCHAR(10),
    ban_is BOOLEAN DEFAULT false,

    -- Land classification
    land_fund_type VARCHAR(50),
    land_use_type VARCHAR(50),
    land_fund_category VARCHAR(50),

    -- Nested data (JSONB)
    subjects JSONB,
    documents JSONB,
    documents_l JSONB,
    bans JSONB,

    -- API sync metadata
    data_source VARCHAR(20),
    api_raw_response JSONB,
    synced_at TIMESTAMP,

    -- Audit
    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50)
);

COMMENT ON TABLE university_cadastre IS 'University real estate objects from cadastre API (172.18.9.171/kadastr/)';
COMMENT ON COLUMN university_cadastre.cad_number IS 'Unique cadastre number, e.g. 10:10:02:03:03:5010';
COMMENT ON COLUMN university_cadastre.cost IS 'Cadastre value in UZS';
COMMENT ON COLUMN university_cadastre.version IS 'Optimistic locking version (JPA @Version)';
-- NOTE: No soft delete (deleted_at) by design.
-- Cadastre data is a synchronized snapshot from the external government API.
-- Records are updated in-place on each sync (upsert pattern via cad_number UNIQUE).
-- Historical state is tracked via api_raw_response + synced_at, not soft-delete rows.
-- If a cadastre object disappears from the API, the record stays as the last known state.

CREATE INDEX idx_ucadastre_university ON university_cadastre(university_code);
CREATE INDEX idx_ucadastre_region ON university_cadastre(region_id) WHERE region_id IS NOT NULL;
CREATE INDEX idx_ucadastre_district ON university_cadastre(district_id) WHERE district_id IS NOT NULL;
CREATE INDEX idx_ucadastre_ban ON university_cadastre(ban_is) WHERE ban_is = true;
