-- =====================================================
-- V011: CREATE LANGUAGES TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Supported languages configuration
-- =====================================================

CREATE TABLE languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100) NOT NULL,
    iso_code VARCHAR(2),
    position INTEGER DEFAULT 999,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_rtl BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),

    -- Versioning
    version INTEGER DEFAULT 1
);

-- Comments
COMMENT ON TABLE languages IS 'Supported system languages';
COMMENT ON COLUMN languages.code IS 'ISO language code (e.g., uz-UZ, ru-RU)';
COMMENT ON COLUMN languages.iso_code IS 'ISO 639-1 code (2 letters)';
COMMENT ON COLUMN languages.position IS 'Display order (lower = first)';
COMMENT ON COLUMN languages.is_rtl IS 'Right-to-left flag (Arabic, Hebrew, etc.)';
COMMENT ON COLUMN languages.is_default IS 'Only ONE language can be default';

-- CRITICAL: Business constraint - only ONE default language
CREATE UNIQUE INDEX idx_languages_single_default
ON languages(is_default)
WHERE is_default = TRUE AND deleted_at IS NULL;

-- Other indexes
CREATE INDEX idx_languages_code ON languages(code);
CREATE INDEX idx_languages_active ON languages(is_active) WHERE is_active = TRUE;
CREATE INDEX idx_languages_position ON languages(position);
CREATE INDEX idx_languages_deleted_at ON languages(deleted_at) WHERE deleted_at IS NULL;
