-- =====================================================
-- V017: DIPLOMA BLANK POOL
-- =====================================================
-- Author: hemis-team
-- Date: 2026-07-02
-- Purpose: Central pool table backing the pre-existing finance.DiplomaBlank
--          @Entity (hemishe_e_diploma_blank), which had NO create-migration
--          (the entity mapping existed but the table did not). Ministry-managed
--          central pool of individual diploma blank forms (series + number).
--          Read-only registry card (/institutions/diploma-blanks).
-- Pattern: BaseEntity (CUBA-style audit: version, create_ts/created_by,
--          update_ts/updated_by, delete_ts/deleted_by).
-- Architecture: additive CREATE TABLE only (no drops); populated later via
--          OTM blank-consumption sync or a future ministry blank-registration.
-- =====================================================

CREATE TABLE IF NOT EXISTS hemishe_e_diploma_blank (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    blank_code        VARCHAR(64) UNIQUE,
    series            VARCHAR(8),
    number            VARCHAR(16),
    _university       VARCHAR(64),
    _blank_type       VARCHAR(32),
    _status           VARCHAR(32),
    received_date     DATE,
    issued_date       DATE,
    academic_year     INTEGER,
    supplier          VARCHAR(256),
    batch_number      VARCHAR(64),
    status_reason     VARCHAR(512),
    security_features VARCHAR(1024),
    notes             VARCHAR(2048),

    -- Audit (BaseEntity — CUBA-style soft delete via delete_ts)
    version     INTEGER DEFAULT 1,
    create_ts   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50),
    update_ts   TIMESTAMP,
    updated_by  VARCHAR(50),
    delete_ts   TIMESTAMP,
    deleted_by  VARCHAR(50)
);

COMMENT ON TABLE hemishe_e_diploma_blank IS
    'Ministry-managed central pool of individual diploma blank forms (series+number). Backs finance.DiplomaBlank entity; read-only registry card.';

CREATE INDEX IF NOT EXISTS idx_edb_university ON hemishe_e_diploma_blank(_university) WHERE delete_ts IS NULL;
CREATE INDEX IF NOT EXISTS idx_edb_status     ON hemishe_e_diploma_blank(_status)     WHERE delete_ts IS NULL;
CREATE INDEX IF NOT EXISTS idx_edb_blank_code ON hemishe_e_diploma_blank(blank_code)  WHERE delete_ts IS NULL;
