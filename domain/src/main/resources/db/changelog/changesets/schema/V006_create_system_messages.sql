-- =====================================================
-- V006: i18n MODULE — system_messages + system_message_translations
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: i18n system messages and translations
-- =====================================================

CREATE TABLE system_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(100) NOT NULL,
    message_key VARCHAR(255) NOT NULL UNIQUE,
    message TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

-- Comments
COMMENT ON TABLE system_messages IS 'i18n message keys and default values';
COMMENT ON COLUMN system_messages.message_key IS 'Unique key for translation lookup';
COMMENT ON COLUMN system_messages.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_system_messages_category ON system_messages(category);
-- message_key UNIQUE constraint already creates a B-tree index
CREATE INDEX idx_system_messages_deleted_at ON system_messages(deleted_at) WHERE deleted_at IS NULL;

-- =====================================================
-- SYSTEM_MESSAGE_TRANSLATIONS (tarjimalar)
-- =====================================================
CREATE TABLE system_message_translations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES system_messages(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,
    translation TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uq_message_language UNIQUE (message_id, language)
);

COMMENT ON TABLE system_message_translations IS 'Translations for system messages by language';

CREATE INDEX idx_smt_message_id ON system_message_translations(message_id);
CREATE INDEX idx_smt_language ON system_message_translations(language);
