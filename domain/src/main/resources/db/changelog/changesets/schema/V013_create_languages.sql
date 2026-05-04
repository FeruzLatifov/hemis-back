-- =====================================================
-- V013: language + configuration — system-wide settings
-- =====================================================
-- Author: hemis-team
-- Purpose:
--   language — supported UI languages (uz-UZ, ru-RU, en-US, ...)
--   configuration — key/value store for system settings incl. language toggles
-- Both follow AuditableEntity pattern (7 audit columns, soft delete).
-- =====================================================

CREATE TABLE language (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL,  -- Partial UNIQUE pastda
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100) NOT NULL,
    iso_code VARCHAR(2),
    position INTEGER DEFAULT 999,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_rtl BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

-- Only ONE language can be default
CREATE UNIQUE INDEX idx_language_single_default
ON language(is_default)
WHERE is_default = TRUE;

CREATE INDEX idx_language_active ON language(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_language_position ON language(position);
CREATE INDEX idx_language_deleted_at ON language(deleted_at) WHERE deleted_at IS NULL;

-- Partial UNIQUE: soft-deleted til code'ini qayta ishlatishga ruxsat
CREATE UNIQUE INDEX uq_language_code
    ON language(code)
    WHERE deleted_at IS NULL;

-- =====================================================
-- configuration — system-wide key/value store
-- =====================================================
CREATE TABLE configuration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    path VARCHAR(255) NOT NULL,  -- Partial UNIQUE pastda
    value TEXT,
    category VARCHAR(64),
    description TEXT,

    value_type VARCHAR(32) DEFAULT 'string',
    is_editable BOOLEAN NOT NULL DEFAULT TRUE,
    is_sensitive BOOLEAN NOT NULL DEFAULT FALSE,

    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

COMMENT ON TABLE configuration IS 'System-wide key/value configuration (path = unique key, value = text payload)';
COMMENT ON COLUMN configuration.path IS 'Dotted key, e.g. system.language.uz-UZ';
COMMENT ON COLUMN configuration.value_type IS 'Hint for UI rendering: boolean | number | string | password | json';

CREATE INDEX idx_configuration_category ON configuration(category) WHERE category IS NOT NULL;
CREATE INDEX idx_configuration_deleted_at ON configuration(deleted_at) WHERE deleted_at IS NULL;

-- Partial UNIQUE: soft-deleted config path'ini qayta ishlatishga ruxsat
CREATE UNIQUE INDEX uq_configuration_path
    ON configuration(path)
    WHERE deleted_at IS NULL;
