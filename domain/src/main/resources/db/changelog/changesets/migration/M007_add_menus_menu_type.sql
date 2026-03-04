-- =====================================================
-- M007: ADD MENU_TYPE COLUMN TO MENUS TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-03
-- Purpose: Frontend uses menuType to separate main menus
--          from system menus (shown below separator)
--          instead of hardcoded icon/permission detection
--
-- NOTE: This column is now included in V008 directly.
-- ADD COLUMN IF NOT EXISTS ensures safe no-op for fresh DBs.
-- Kept for backward compatibility with existing databases.
-- =====================================================

-- Add menu_type column (default 'main' for all existing menus)
ALTER TABLE menus ADD COLUMN IF NOT EXISTS menu_type VARCHAR(20) NOT NULL DEFAULT 'main';

-- Add CHECK constraint (idempotent with DO block)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_menus_menu_type'
    ) THEN
        ALTER TABLE menus ADD CONSTRAINT chk_menus_menu_type CHECK (menu_type IN ('main', 'system'));
    END IF;
END $$;

-- Set 'system' for the System root menu
UPDATE menus SET menu_type = 'system' WHERE code = 'system' AND menu_type = 'main';

COMMENT ON COLUMN menus.menu_type IS 'Menu type: main (regular) or system (shown below separator)';

-- =====================================================
-- COVERING INDEXES FOR PERFORMANCE
-- =====================================================
-- Most common query: active children by parent, ordered
CREATE INDEX IF NOT EXISTS idx_menus_parent_order_active
    ON menus(parent_id, order_number)
    WHERE deleted_at IS NULL AND is_active = true;

-- Menu type filtering (system vs main separation)
CREATE INDEX IF NOT EXISTS idx_menus_type_order
    ON menus(menu_type, order_number)
    WHERE deleted_at IS NULL AND is_active = true;
