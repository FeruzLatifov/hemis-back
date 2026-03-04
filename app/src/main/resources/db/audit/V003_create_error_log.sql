-- =====================================================
-- Error Log — Xatolar audit jadvali
-- =====================================================

CREATE TABLE IF NOT EXISTS error_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    username        VARCHAR(255),
    user_ip         VARCHAR(45),
    -- Xato
    error_type      VARCHAR(255),
    error_message   TEXT,
    stack_trace     TEXT,
    -- Kontekst
    endpoint        VARCHAR(500),
    request_id      VARCHAR(64),
    request_body    JSONB,
    -- Vaqt
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_error_type ON error_log (error_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_error_user ON error_log (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_error_created ON error_log (created_at DESC);

-- Immutability: audit loglar o'zgartirilmasligi va o'chirilmasligi kerak
REVOKE UPDATE, DELETE ON error_log FROM PUBLIC;
