-- =====================================================
-- V012: CREATE LANGUAGE_TRANSLATIONS TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: Language name translations (meta-translations)
-- =====================================================

CREATE TABLE language_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    language_id UUID NOT NULL REFERENCES languages(id) ON DELETE CASCADE,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Unique constraint per language-locale pair
    CONSTRAINT uq_language_translation UNIQUE (language_id, locale)
);

-- Comments
COMMENT ON TABLE language_translations IS 'How language names appear in different locales';
COMMENT ON COLUMN language_translations.locale IS 'The locale viewing this translation';

-- Indexes
CREATE INDEX idx_lt_language_id ON language_translations(language_id);
CREATE INDEX idx_lt_locale ON language_translations(locale);
