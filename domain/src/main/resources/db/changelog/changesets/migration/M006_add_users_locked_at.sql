-- =====================================================
-- M006: ADD LOCKED_AT COLUMN TO USERS TABLE
-- =====================================================
-- Author: hemis-team
-- Date: 2026-03-03
-- Purpose: Auto-unlock feature — account auto-unlocks
--          after 15 minutes based on this timestamp
--
-- NOTE: This column is now included in V001 directly.
-- ADD COLUMN IF NOT EXISTS ensures safe no-op for fresh DBs.
-- Kept for backward compatibility with existing databases.
-- =====================================================

ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_at TIMESTAMP;

COMMENT ON COLUMN users.locked_at IS 'Timestamp when account was locked (auto-unlock after 15 min)';
