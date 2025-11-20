-- ================================================
-- Rollback: V9 - Drop Menu Audit Logs Table
-- Description: Rollback menu audit logs table creation
-- ================================================

-- Drop indexes first
DROP INDEX IF EXISTS idx_menu_audit_new_value_gin;
DROP INDEX IF EXISTS idx_menu_audit_old_value_gin;
DROP INDEX IF EXISTS idx_menu_audit_changed_at;
DROP INDEX IF EXISTS idx_menu_audit_changed_by;
DROP INDEX IF EXISTS idx_menu_audit_action;
DROP INDEX IF EXISTS idx_menu_audit_menu_id;

-- Drop table
DROP TABLE IF EXISTS menu_audit_logs;
