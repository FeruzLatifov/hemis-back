-- =====================================================
-- V6: LANGUAGE CONFIGURATION
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Database-driven language management
--
-- Contents:
-- PART 1: Languages table schema
-- PART 2: Seed default languages (uz-UZ, oz-UZ, ru-RU, en-US)
--
-- Strategy: DATABASE-DRIVEN i18n
-- - Replace hard-coded YAML configuration
-- - Enable admin panel to add/remove languages
-- - No application restart required
-- - Single source of truth for supported languages
-- =====================================================

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 1: LANGUAGES TABLE SCHEMA
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

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

-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
-- PART 2: SEED DEFAULT LANGUAGES
-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

-- =====================================================
-- 1. Insert default languages
-- =====================================================

INSERT INTO public.languages (
    code,
    name,
    native_name,
    iso_code,
    is_active,
    is_default,
    is_rtl,
    position,
    created_at,
    updated_at
) VALUES
    -- Uzbek Latin (Primary/Default)
    (
        'uz-UZ',
        'Uzbek (Latin)',
        'O''zbekcha',
        'uz',
        true,  -- active
        true,  -- default
        false, -- not RTL
        1,     -- first position
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),

    -- Uzbek Cyrillic
    (
        'oz-UZ',
        'Uzbek (Cyrillic)',
        'Ўзбекча',
        'oz',
        true,  -- active
        false, -- not default
        false, -- not RTL
        2,     -- second position
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),

    -- Russian
    (
        'ru-RU',
        'Russian',
        'Русский',
        'ru',
        true,  -- active
        false, -- not default
        false, -- not RTL
        3,     -- third position
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),

    -- English (US)
    (
        'en-US',
        'English (US)',
        'English',
        'en',
        true,  -- active
        false, -- not default
        false, -- not RTL
        4,     -- fourth position
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (code) DO NOTHING;  -- Idempotent: skip if already exists

-- =====================================================
-- 2. Verify insertion
-- =====================================================

DO $$
DECLARE
    active_count INTEGER;
    default_count INTEGER;
    lang_record RECORD;
BEGIN
    -- Count active languages
    SELECT COUNT(*) INTO active_count
    FROM public.languages
    WHERE is_active = true AND deleted_at IS NULL;

    -- Count default languages
    SELECT COUNT(*) INTO default_count
    FROM public.languages
    WHERE is_default = true AND deleted_at IS NULL;

    -- Log results
    RAISE NOTICE '✅ Languages seeded successfully';
    RAISE NOTICE '   Active languages: % (expected: 4)', active_count;
    RAISE NOTICE '   Default language: % (expected: 1)', default_count;

    -- Validation: Ensure exactly one default language
    IF default_count != 1 THEN
        RAISE WARNING '⚠️  Expected 1 default language, found %', default_count;
    END IF;

    -- Display languages
    RAISE NOTICE '   Languages:';
    FOR lang_record IN (
        SELECT code, name, native_name, is_default
        FROM public.languages
        WHERE deleted_at IS NULL
        ORDER BY position
    ) LOOP
        RAISE NOTICE '     - % (%) % %',
            lang_record.code,
            lang_record.native_name,
            lang_record.name,
            CASE WHEN lang_record.is_default THEN '[DEFAULT]' ELSE '' END;
    END LOOP;
END $$;
