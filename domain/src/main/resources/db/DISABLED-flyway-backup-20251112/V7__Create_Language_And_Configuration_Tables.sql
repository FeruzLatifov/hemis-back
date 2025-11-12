-- ========================================
-- V7: Language Management & System Configuration
-- ========================================
--
-- Purpose: Implement UNIVER pattern for language management
--
-- Tables:
--   1. h_language - Language classifier (9 languages)
--   2. h_system_configuration - System-wide settings
--
-- Features:
--   - 9 language support with active/inactive toggle
--   - System configuration key-value store
--   - 4 default active languages (uz-UZ, oz-UZ, ru-RU, en-US)
--   - 5 additional languages (kk-UZ, tg-TG, kz-KZ, tm-TM, kg-KG)
--
-- ========================================

-- ========================================
-- TABLE 1: h_language
-- ========================================

CREATE TABLE IF NOT EXISTS h_language (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    native_name VARCHAR(100) NOT NULL,
    iso_code VARCHAR(2),
    position INTEGER DEFAULT 999,
    is_active BOOLEAN NOT NULL DEFAULT false,
    is_rtl BOOLEAN DEFAULT false,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_language_code_format CHECK (code ~ '^[a-z]{2}-[A-Z]{2}$')
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_language_code ON h_language(code);
CREATE INDEX IF NOT EXISTS idx_language_active ON h_language(is_active);
CREATE INDEX IF NOT EXISTS idx_language_position ON h_language(position);

COMMENT ON TABLE h_language IS 'System languages - UNIVER pattern';
COMMENT ON COLUMN h_language.code IS 'Language code (e.g., uz-UZ, ru-RU)';
COMMENT ON COLUMN h_language.name IS 'Display name in English';
COMMENT ON COLUMN h_language.native_name IS 'Native name (in own script)';
COMMENT ON COLUMN h_language.iso_code IS 'ISO 639-1 code (2 letters)';
COMMENT ON COLUMN h_language.position IS 'Display order (lower = first)';
COMMENT ON COLUMN h_language.is_active IS 'Language available in UI';
COMMENT ON COLUMN h_language.is_rtl IS 'Right-to-left language';
COMMENT ON COLUMN h_language.is_default IS 'System default (cannot disable)';

-- ========================================
-- TABLE 2: h_system_configuration
-- ========================================

CREATE TABLE IF NOT EXISTS h_system_configuration (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path VARCHAR(255) NOT NULL UNIQUE,
    value TEXT,
    category VARCHAR(64),
    description TEXT,
    value_type VARCHAR(32) DEFAULT 'string',
    is_editable BOOLEAN DEFAULT true,
    is_sensitive BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT chk_config_path_format CHECK (path ~ '^[a-z0-9_.]+$')
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_sys_config_path ON h_system_configuration(path);
CREATE INDEX IF NOT EXISTS idx_sys_config_category ON h_system_configuration(category);

COMMENT ON TABLE h_system_configuration IS 'System configuration key-value store - UNIVER pattern';
COMMENT ON COLUMN h_system_configuration.path IS 'Configuration path (e.g., system.language.uz_uz)';
COMMENT ON COLUMN h_system_configuration.value IS 'Configuration value (as string)';
COMMENT ON COLUMN h_system_configuration.category IS 'Configuration category';
COMMENT ON COLUMN h_system_configuration.description IS 'Human-readable description';
COMMENT ON COLUMN h_system_configuration.value_type IS 'Data type: boolean, number, string, password, json';
COMMENT ON COLUMN h_system_configuration.is_editable IS 'Can be edited through UI';
COMMENT ON COLUMN h_system_configuration.is_sensitive IS 'Sensitive value (masked in UI)';

-- ========================================
-- DATA: Insert Languages (9 languages)
-- ========================================

-- STEP 1: Default Active Languages (4)
INSERT INTO h_language (id, code, name, native_name, iso_code, position, is_active, is_rtl, is_default) VALUES
(gen_random_uuid(), 'uz-UZ', 'Uzbek (Latin)', 'O''zbekcha', 'uz', 1, TRUE, FALSE, TRUE),
(gen_random_uuid(), 'oz-UZ', 'Uzbek (Cyrillic)', 'Ўзбекча', 'uz', 2, TRUE, FALSE, TRUE),
(gen_random_uuid(), 'ru-RU', 'Russian', 'Русский', 'ru', 3, TRUE, FALSE, TRUE),
(gen_random_uuid(), 'en-US', 'English', 'English', 'en', 4, TRUE, FALSE, FALSE);

-- STEP 2: Additional Inactive Languages (5)
INSERT INTO h_language (id, code, name, native_name, iso_code, position, is_active, is_rtl, is_default) VALUES
(gen_random_uuid(), 'kk-UZ', 'Karakalpak', 'Қарақалпақша', 'kk', 5, FALSE, FALSE, FALSE),
(gen_random_uuid(), 'tg-TG', 'Tajik', 'Тоҷикӣ', 'tg', 6, FALSE, FALSE, FALSE),
(gen_random_uuid(), 'kz-KZ', 'Kazakh', 'Қазақша', 'kz', 7, FALSE, FALSE, FALSE),
(gen_random_uuid(), 'tm-TM', 'Turkmen', 'Türkmençe', 'tm', 8, FALSE, FALSE, FALSE),
(gen_random_uuid(), 'kg-KG', 'Kyrgyz', 'Кыргызча', 'kg', 9, FALSE, FALSE, FALSE);

-- ========================================
-- DATA: Language Configuration Toggles
-- ========================================

-- STEP 3: Insert Language Toggle Configs
INSERT INTO h_system_configuration (id, path, value, category, description, value_type, is_editable) VALUES
(gen_random_uuid(), 'system.language.uz_uz', 'true', 'language', 'Enable Uzbek (Latin) language', 'boolean', FALSE),
(gen_random_uuid(), 'system.language.oz_uz', 'true', 'language', 'Enable Uzbek (Cyrillic) language', 'boolean', FALSE),
(gen_random_uuid(), 'system.language.ru_ru', 'true', 'language', 'Enable Russian language', 'boolean', FALSE),
(gen_random_uuid(), 'system.language.en_us', 'true', 'language', 'Enable English language', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.kk_uz', 'false', 'language', 'Enable Karakalpak language', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.tg_tg', 'false', 'language', 'Enable Tajik language', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.kz_kz', 'false', 'language', 'Enable Kazakh language', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.tm_tm', 'false', 'language', 'Enable Turkmen language', 'boolean', TRUE),
(gen_random_uuid(), 'system.language.kg_kg', 'false', 'language', 'Enable Kyrgyz language', 'boolean', TRUE);

-- STEP 4: Insert System Default Language Config
INSERT INTO h_system_configuration (id, path, value, category, description, value_type, is_editable) VALUES
(gen_random_uuid(), 'system.default_language', 'uz-UZ', 'system', 'Default system language', 'string', TRUE);

-- ========================================
-- VERIFICATION
-- ========================================

-- Verify language table
SELECT
    '✅ Language Table' as status,
    COUNT(*) as total_languages,
    SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as active_languages,
    SUM(CASE WHEN is_default = true THEN 1 ELSE 0 END) as default_languages
FROM h_language;

-- Verify configuration table
SELECT
    '✅ Configuration Table' as status,
    COUNT(*) as total_configs,
    SUM(CASE WHEN category = 'language' THEN 1 ELSE 0 END) as language_configs
FROM h_system_configuration;

-- Show all languages with their status
SELECT
    code,
    name,
    native_name,
    position,
    is_active,
    is_default
FROM h_language
ORDER BY position ASC;
