-- =====================================================
-- Login Log — Autentifikatsiya hodisalari jadvali
-- =====================================================

CREATE TABLE IF NOT EXISTS login_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    username        VARCHAR(255) NOT NULL,
    user_ip         VARCHAR(45),
    user_agent      VARCHAR(512),
    -- Hodisa
    event_type      VARCHAR(20) NOT NULL,
    failure_reason  VARCHAR(255),
    -- Vaqt
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_login_user ON login_log (username, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_event ON login_log (event_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_ip ON login_log (user_ip, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_login_created ON login_log (created_at DESC);

-- Immutability: audit loglar o'zgartirilmasligi va o'chirilmasligi kerak
REVOKE UPDATE, DELETE ON login_log FROM PUBLIC;
