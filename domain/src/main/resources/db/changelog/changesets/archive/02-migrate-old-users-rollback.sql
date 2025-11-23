-- =====================================================
-- V2 ROLLBACK: Remove Migrated Old-hemis Users
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-20 (Optimized)
-- Purpose: Rollback V2 user migration
--
-- WARNING: This will DELETE all migrated users!
-- - All users migrated from sec_user
-- - All user-role mappings created during migration
-- =====================================================

-- Delete user-role mappings for migrated users
-- (Only delete mappings created during migration, not existing ones)
DELETE FROM user_roles ur
WHERE EXISTS (
    SELECT 1 FROM users u
    WHERE u.id = ur.user_id
      AND u.created_by IS NULL  -- Migration doesn't set created_by
);

-- Delete all migrated users
-- (Identify by lack of created_by - migration doesn't set it)
DELETE FROM users
WHERE created_by IS NULL;

-- Drop V2-specific indexes
DROP INDEX IF EXISTS idx_users_entity_code_v2;
DROP INDEX IF EXISTS idx_users_user_type_v2;
DROP INDEX IF EXISTS idx_users_enabled_v2;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✅ V2 user migration rolled back successfully';
    RAISE NOTICE '   All migrated users and their role mappings removed';
END $$;
