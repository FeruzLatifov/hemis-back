-- =====================================================
-- V11: Seed Languages Data (Initial 4 Languages)
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20
-- Description: Seed initial language data
--
-- Languages:
-- 1. uz-UZ - Uzbek (Latin) - Primary/Default
-- 2. oz-UZ - Uzbek (Cyrillic)
-- 3. ru-RU - Russian
-- 4. en-US - English (US)
--
-- All languages enabled by default for backward compatibility
-- =====================================================

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
