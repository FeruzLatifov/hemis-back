-- =====================================================
-- V007: CREATE SYSTEM_MESSAGE_TRANSLATIONS TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: i18n translations for system messages
-- =====================================================

CREATE TABLE system_message_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES system_messages(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,
    translation TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    -- Unique constraint per message-language pair
    CONSTRAINT uq_message_language UNIQUE (message_id, language)
);

-- Comments
COMMENT ON TABLE system_message_translations IS 'Translations for system messages by language';

-- Indexes
CREATE INDEX idx_smt_message_id ON system_message_translations(message_id);
CREATE INDEX idx_smt_language ON system_message_translations(language);
CREATE INDEX idx_smt_message_language ON system_message_translations(message_id, language);
