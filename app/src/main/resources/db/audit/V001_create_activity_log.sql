-- =====================================================
-- Activity Log — CRUD operatsiyalar audit jadvali
-- =====================================================

CREATE TABLE IF NOT EXISTS activity_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- Kim
    user_id         UUID,
    username        VARCHAR(255),
    user_ip         VARCHAR(45),
    user_agent      VARCHAR(512),
    session_id      VARCHAR(128),
    -- Nima qildi
    action          VARCHAR(20) NOT NULL,
    entity_type     VARCHAR(255),
    entity_id       VARCHAR(255),
    entity_name     VARCHAR(500),
    -- Qanday ma'lumot
    old_value       JSONB,
    new_value       JSONB,
    changed_fields  TEXT[],
    -- Kontekst
    request_id      VARCHAR(64),
    endpoint        VARCHAR(500),
    description     TEXT,
    -- Vaqt
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_activity_user ON activity_log (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_entity ON activity_log (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_activity_action ON activity_log (action, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_request ON activity_log (request_id);
CREATE INDEX IF NOT EXISTS idx_activity_created ON activity_log (created_at DESC);

-- Immutability: audit loglar o'zgartirilmasligi va o'chirilmasligi kerak
REVOKE UPDATE, DELETE ON activity_log FROM PUBLIC;
