-- =====================================================
-- S005: SEED LANGUAGES
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Configure supported system languages
-- Strategy: IDEMPOTENT UPSERT (ON CONFLICT DO UPDATE)
-- =====================================================

-- First, ensure no duplicate defaults by resetting
UPDATE languages SET is_default = FALSE WHERE is_default = TRUE;

-- Uzbek (Latin) - DEFAULT
INSERT INTO languages (id, code, name, native_name, iso_code, position, is_active, is_rtl, is_default, created_by)
VALUES (
    gen_random_uuid(),
    'uz-UZ',
    'Uzbek (Latin)',
    'O''zbekcha',
    'uz',
    1,
    TRUE,
    FALSE,
    TRUE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    native_name = EXCLUDED.native_name,
    iso_code = EXCLUDED.iso_code,
    position = EXCLUDED.position,
    is_active = EXCLUDED.is_active,
    is_rtl = EXCLUDED.is_rtl,
    is_default = EXCLUDED.is_default,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Uzbek (Cyrillic)
INSERT INTO languages (id, code, name, native_name, iso_code, position, is_active, is_rtl, is_default, created_by)
VALUES (
    gen_random_uuid(),
    'oz-UZ',
    'Uzbek (Cyrillic)',
    'Ўзбекча',
    'uz',
    2,
    TRUE,
    FALSE,
    FALSE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    native_name = EXCLUDED.native_name,
    iso_code = EXCLUDED.iso_code,
    position = EXCLUDED.position,
    is_active = EXCLUDED.is_active,
    is_rtl = EXCLUDED.is_rtl,
    is_default = FALSE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Russian
INSERT INTO languages (id, code, name, native_name, iso_code, position, is_active, is_rtl, is_default, created_by)
VALUES (
    gen_random_uuid(),
    'ru-RU',
    'Russian',
    'Русский',
    'ru',
    3,
    TRUE,
    FALSE,
    FALSE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    native_name = EXCLUDED.native_name,
    iso_code = EXCLUDED.iso_code,
    position = EXCLUDED.position,
    is_active = EXCLUDED.is_active,
    is_rtl = EXCLUDED.is_rtl,
    is_default = FALSE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- English
INSERT INTO languages (id, code, name, native_name, iso_code, position, is_active, is_rtl, is_default, created_by)
VALUES (
    gen_random_uuid(),
    'en-US',
    'English',
    'English',
    'en',
    4,
    TRUE,
    FALSE,
    FALSE,
    'system'
)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    native_name = EXCLUDED.native_name,
    iso_code = EXCLUDED.iso_code,
    position = EXCLUDED.position,
    is_active = EXCLUDED.is_active,
    is_rtl = EXCLUDED.is_rtl,
    is_default = FALSE,
    updated_at = CURRENT_TIMESTAMP,
    updated_by = 'system';

-- Verification
DO $$
DECLARE
    lang_count INTEGER;
    default_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO lang_count FROM languages WHERE is_active = TRUE;
    SELECT COUNT(*) INTO default_count FROM languages WHERE is_default = TRUE AND deleted_at IS NULL;

    IF default_count != 1 THEN
        RAISE EXCEPTION 'S005 Failed: Expected exactly 1 default language, found %', default_count;
    END IF;

    RAISE NOTICE 'S005: % languages seeded (1 default)', lang_count;
END $$;
