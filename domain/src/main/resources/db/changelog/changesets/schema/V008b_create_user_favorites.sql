-- =====================================================
-- V008b: CREATE USER_FAVORITES TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20
-- Purpose: User favorite menu items for quick access
-- Note: Extracted from V008 for granularity (1 file = 1 table)
-- =====================================================

CREATE TABLE IF NOT EXISTS user_favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    menu_code VARCHAR(100) NOT NULL,
    order_number INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_favorites UNIQUE (user_id, menu_code)
);

CREATE INDEX IF NOT EXISTS idx_user_favorites_user ON user_favorites(user_id);

COMMENT ON TABLE user_favorites IS 'User favorite menu items for quick access';
COMMENT ON COLUMN user_favorites.menu_code IS 'Menu code reference (not FK for flexibility)';
