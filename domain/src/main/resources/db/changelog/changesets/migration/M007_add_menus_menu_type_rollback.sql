-- =====================================================
-- Rollback M007: Remove menu_type column and indexes
-- =====================================================

DROP INDEX IF EXISTS idx_menus_type_order;
DROP INDEX IF EXISTS idx_menus_parent_order_active;
ALTER TABLE menus DROP CONSTRAINT IF EXISTS chk_menus_menu_type;
ALTER TABLE menus DROP COLUMN IF EXISTS menu_type;
