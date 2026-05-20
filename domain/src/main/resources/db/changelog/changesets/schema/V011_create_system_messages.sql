-- V011: Create system_message and system_message_translation tables

CREATE TABLE system_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category VARCHAR(100) NOT NULL,
    message_key VARCHAR(255) NOT NULL,  -- Partial UNIQUE pastda (soft-delete uyg'unligi)
    message TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    version INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

CREATE INDEX idx_system_message_category ON system_message(category);
CREATE INDEX idx_system_message_deleted_at ON system_message(deleted_at) WHERE deleted_at IS NULL;

-- Partial UNIQUE: soft-deleted xabar key'ini qayta ishlatishga ruxsat
CREATE UNIQUE INDEX uq_system_message_key
    ON system_message(message_key)
    WHERE deleted_at IS NULL;

CREATE TABLE system_message_translation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES system_message(id) ON DELETE CASCADE,
    language VARCHAR(10) NOT NULL,
    translation TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP,
    updated_by VARCHAR(50),

    CONSTRAINT uq_message_language UNIQUE (message_id, language)
);

CREATE INDEX idx_system_message_translation_message_id ON system_message_translation(message_id);
CREATE INDEX idx_system_message_translation_language ON system_message_translation(language);
