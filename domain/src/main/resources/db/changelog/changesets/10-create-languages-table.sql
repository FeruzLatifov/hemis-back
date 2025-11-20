-- =====================================================
-- V10: Create Languages Table (Database-Driven i18n)
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20
-- Description: Create languages table for dynamic language management
--
-- Purpose:
-- - Replace hard-coded YAML language configuration
-- - Enable admin panel to add/remove languages without restart
-- - Database as single source of truth for supported languages
--
-- Features:
-- - Dynamic language management (no code changes!)
-- - Admin UI support (CRUD operations)
-- - Position-based sorting
-- - RTL support for Arabic/Hebrew/Persian
-- - Soft delete support (deleted_at)
-- - Audit trail (created_at, updated_at)
--
-- Table: languages
-- Columns: 12 total (id, code, name, native_name, iso_code, flags, position, timestamps)
-- Indexes: 3 (PK, unique code, active languages)
-- =====================================================

-- =====================================================
-- 1. Create languages table
-- =====================================================

CREATE TABLE IF NOT EXISTS public.languages (
    -- Primary key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Language identification
    code VARCHAR(10) NOT NULL UNIQUE,  -- e.g., "uz-UZ", "ru-RU", "en-US"
    name VARCHAR(100) NOT NULL,        -- Display name in English (e.g., "Uzbek")
    native_name VARCHAR(100) NOT NULL, -- Native name (e.g., "O'zbekcha")
    iso_code VARCHAR(10),              -- ISO 639-1 code (e.g., "uz", "ru")

    -- Status flags
    is_active BOOLEAN NOT NULL DEFAULT false,   -- Is language enabled?
    is_default BOOLEAN NOT NULL DEFAULT false,  -- Is this the default language?
    is_rtl BOOLEAN NOT NULL DEFAULT false,      -- Right-to-left script?

    -- Ordering
    position INTEGER DEFAULT 999,      -- Display order (lower = higher priority)

    -- Audit timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP               -- Soft delete support
);

-- =====================================================
-- 2. Create indexes
-- =====================================================

-- Index for active languages query (most common query)
CREATE INDEX IF NOT EXISTS idx_languages_active
    ON public.languages(is_active, position)
    WHERE deleted_at IS NULL;

-- Index for default language lookup
CREATE INDEX IF NOT EXISTS idx_languages_default
    ON public.languages(is_default)
    WHERE deleted_at IS NULL AND is_active = true;

-- Index for soft delete queries
CREATE INDEX IF NOT EXISTS idx_languages_deleted_at
    ON public.languages(deleted_at);

-- =====================================================
-- 3. Create trigger for updated_at
-- =====================================================

CREATE OR REPLACE FUNCTION update_languages_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_languages_updated_at
    BEFORE UPDATE ON public.languages
    FOR EACH ROW
    EXECUTE FUNCTION update_languages_updated_at();

-- =====================================================
-- 4. Add comments for documentation
-- =====================================================

COMMENT ON TABLE public.languages IS
'Database-driven language configuration - replaces YAML hard-coded languages';

COMMENT ON COLUMN public.languages.code IS
'Language code with region (ISO 639-1 + region), e.g., uz-UZ, ru-RU, en-US';

COMMENT ON COLUMN public.languages.name IS
'Display name in English for admin panel, e.g., Uzbek, Russian, English';

COMMENT ON COLUMN public.languages.native_name IS
'Native name for language switcher, e.g., O''zbekcha, Русский, English';

COMMENT ON COLUMN public.languages.iso_code IS
'ISO 639-1 language code (2 letters), e.g., uz, ru, en';

COMMENT ON COLUMN public.languages.is_active IS
'Whether this language is currently enabled (appears in language switcher)';

COMMENT ON COLUMN public.languages.is_default IS
'Whether this is the default fallback language (only one should be true)';

COMMENT ON COLUMN public.languages.is_rtl IS
'Right-to-left script support (true for Arabic, Hebrew, Persian)';

COMMENT ON COLUMN public.languages.position IS
'Sort order in language switcher (lower = higher priority)';

COMMENT ON COLUMN public.languages.deleted_at IS
'Soft delete timestamp - if not null, language is considered deleted';

-- =====================================================
-- Success message
-- =====================================================

DO $$
BEGIN
    RAISE NOTICE '✅ Languages table created successfully';
    RAISE NOTICE '   Table: public.languages';
    RAISE NOTICE '   Indexes: 3 (active, default, soft-delete)';
    RAISE NOTICE '   Triggers: 1 (updated_at auto-update)';
    RAISE NOTICE '   Next: Run changeset 11 to seed default languages';
END $$;
