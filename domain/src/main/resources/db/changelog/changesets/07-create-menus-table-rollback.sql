-- =====================================================
-- ROLLBACK: Drop menus table
-- =====================================================
-- Migration: 07 - Rollback create menus table
-- Author: System
-- Date: 2025-11-16
-- Description: Rollback script for menus table creation
-- =====================================================

-- Drop trigger first
DROP TRIGGER IF EXISTS trigger_menus_updated_at ON menus;

-- Drop function
DROP FUNCTION IF EXISTS update_menus_updated_at();

-- Drop indexes (CASCADE will drop them with table, but explicit for clarity)
DROP INDEX IF EXISTS idx_menus_parent_id;
DROP INDEX IF EXISTS idx_menus_active;
DROP INDEX IF EXISTS idx_menus_order;
DROP INDEX IF EXISTS idx_menus_deleted_at;
DROP INDEX IF EXISTS idx_menus_permission;

-- Drop table (CASCADE will remove foreign key constraints)
DROP TABLE IF EXISTS menus CASCADE;
