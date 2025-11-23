-- =====================================================
-- V5 ROLLBACK: Remove Complete Menu System
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Rollback V5 menu system
--
-- WARNING: This will DELETE entire menu system!
-- - All menu audit logs
-- - All menu data
-- - Menu table and indexes
-- - Audit logs table
-- =====================================================

-- Drop audit logs table (no foreign keys)
DROP TABLE IF EXISTS menu_audit_logs CASCADE;

-- Drop menu table (includes all menu data)
DROP TABLE IF EXISTS menus CASCADE;

-- Drop triggers and functions
DROP TRIGGER IF EXISTS trigger_menus_updated_at ON menus;
DROP FUNCTION IF EXISTS update_menus_updated_at();

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ V5 menu system rolled back successfully';
    RAISE NOTICE '   Menu table, audit logs, and all data removed';
END $$;
