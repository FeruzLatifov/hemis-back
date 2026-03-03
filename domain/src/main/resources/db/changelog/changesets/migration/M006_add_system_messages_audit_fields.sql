-- =====================================================
-- M006: ADD MISSING AUDIT FIELDS TO SYSTEM_MESSAGES
-- =====================================================
-- Author: hemis-team
-- Date: 2025-02-24
-- Purpose: Add created_by and updated_by to system_messages
--
-- NOTE: These columns are now included in V006 directly.
-- IF NOT EXISTS ensures this is a safe no-op for fresh DBs.
-- Kept for backward compatibility with existing databases
-- that were created before V006 was updated.
-- =====================================================

ALTER TABLE system_messages ADD COLUMN IF NOT EXISTS created_by VARCHAR(50);
ALTER TABLE system_messages ADD COLUMN IF NOT EXISTS updated_by VARCHAR(50);
