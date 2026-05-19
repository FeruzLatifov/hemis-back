-- =====================================================
-- V015 ROLLBACK: EMPLOYEE SYNC INFRASTRUCTURE
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-08
-- Reverses V015_create_employee_sync_infrastructure.sql
--
-- Drop order: dependent jadvallar avval (FK references)
-- =====================================================

-- 1. Drop outbox event (multi-domain — but no FK from anywhere)
DROP TABLE IF EXISTS outbox_event;

-- 3. Drop indexes on employee_job (before column drop)
DROP INDEX IF EXISTS uq_ejob_univer_source;
DROP INDEX IF EXISTS idx_ejob_synced;

-- 4. Drop columns on employee_job
ALTER TABLE employee_job DROP COLUMN IF EXISTS source_uid;
ALTER TABLE employee_job DROP COLUMN IF EXISTS content_hash;
ALTER TABLE employee_job DROP COLUMN IF EXISTS synced_at;

-- 5. Drop indexes on employee (before column drop)
DROP INDEX IF EXISTS idx_employee_synced;

-- 6. Drop columns on employee
ALTER TABLE employee DROP COLUMN IF EXISTS synced_at;
