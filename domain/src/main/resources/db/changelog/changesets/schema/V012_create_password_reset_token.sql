-- V012: Password reset tokens for forgot-password flow
CREATE TABLE password_reset_token (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used       BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_prt_token ON password_reset_token(token);
CREATE INDEX idx_prt_user_expires ON password_reset_token(user_id, expires_at);
CREATE INDEX idx_prt_user_created ON password_reset_token(user_id, created_at);
