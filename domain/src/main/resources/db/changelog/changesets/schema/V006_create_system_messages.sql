-- =====================================================
-- V006: CREATE SYSTEM_MESSAGES TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-23
-- Purpose: i18n system messages (keys and default values)
-- =====================================================

CREATE TABLE system_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(100) NOT NULL,
    message_key VARCHAR(255) NOT NULL UNIQUE,
    message TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

-- Comments
COMMENT ON TABLE system_messages IS 'i18n message keys and default values';
COMMENT ON COLUMN system_messages.message_key IS 'Unique key for translation lookup';
COMMENT ON COLUMN system_messages.deleted_at IS 'Soft delete timestamp (null = active)';

-- Indexes
CREATE INDEX idx_system_messages_category ON system_messages(category);
CREATE INDEX idx_system_messages_key ON system_messages(message_key);
CREATE INDEX idx_system_messages_deleted_at ON system_messages(deleted_at) WHERE deleted_at IS NULL;
