-- =====================================================
-- V1 ROLLBACK: Remove Complete Authentication System
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Rollback V1 auth system
--
-- WARNING: This will DELETE all auth system data!
-- - All role-permission mappings
-- - All user-role mappings
-- - All permissions
-- - All roles
-- - All users
-- - All i18n messages and translations
-- =====================================================

-- Drop role-permission mappings
DELETE FROM role_permissions;

-- Drop user-role mappings
DELETE FROM user_roles;

-- Drop permissions
DELETE FROM permissions;

-- Drop roles
DELETE FROM roles;

-- Drop users
DELETE FROM users;

-- Drop i18n translations
DELETE FROM system_message_translations;

-- Drop i18n messages
DELETE FROM system_messages;

-- Drop indexes
DROP INDEX IF EXISTS idx_user_roles_user_id;
DROP INDEX IF EXISTS idx_user_roles_role_id;
DROP INDEX IF EXISTS idx_role_permissions_role_id;
DROP INDEX IF EXISTS idx_role_permissions_permission_id;
DROP INDEX IF EXISTS idx_users_username_lowercase;
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_users_entity_code;
DROP INDEX IF EXISTS idx_users_deleted_at;
DROP INDEX IF EXISTS idx_users_active;
DROP INDEX IF EXISTS idx_users_user_type;
DROP INDEX IF EXISTS idx_roles_code;
DROP INDEX IF EXISTS idx_roles_active;
DROP INDEX IF EXISTS idx_roles_role_type;
DROP INDEX IF EXISTS idx_permissions_code;
DROP INDEX IF EXISTS idx_permissions_resource;
DROP INDEX IF EXISTS idx_permissions_category;
DROP INDEX IF EXISTS idx_permissions_resource_action;
DROP INDEX IF EXISTS idx_system_messages_category;
DROP INDEX IF EXISTS idx_system_messages_key;
DROP INDEX IF EXISTS idx_smt_message_id;
DROP INDEX IF EXISTS idx_smt_language;
DROP INDEX IF EXISTS idx_smt_message_language;

-- Drop tables (reverse order due to foreign keys)
DROP TABLE IF EXISTS system_message_translations;
DROP TABLE IF EXISTS system_messages;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ V1 auth system rolled back successfully';
    RAISE NOTICE '   All tables, indexes, and data removed';
END $$;
