-- =====================================================
-- Rollback M006: Remove locked_at column from users table
-- =====================================================

ALTER TABLE users DROP COLUMN IF EXISTS locked_at;
