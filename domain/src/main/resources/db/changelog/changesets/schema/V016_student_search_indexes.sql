-- =====================================================
-- V016: STUDENT SEARCH INDEXES — pg_trgm + StudentMeta race fix
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-05
-- Purpose:
--   1. pg_trgm GIN index for diploma_number LIKE '%term%' (audit MEDIUM-26).
--      LOWER(diploma_number) LIKE '%X%' = sequential scan on potentially 1M+ rows.
--   2. Partial UNIQUE for student_meta (u_id, _university) — race condition
--      (audit HIGH-6: concurrent insert after soft-delete can reuse same uId).
--
-- Self-contained: only CREATE INDEX (no DDL alter to FROZEN hemishe_* tables).
-- Idempotent: IF NOT EXISTS + CREATE EXTENSION IF NOT EXISTS.
--
-- Depends on: legacy CUBA tables — hemishe_e_student_diploma, hemishe_e_student_meta.
-- =====================================================

-- =====================================================
-- EXTENSION 1: pg_trgm — trigram-based fuzzy/substring search
-- =====================================================
-- Required for GIN index on LOWER(diploma_number) gin_trgm_ops.
-- Idempotent — IF NOT EXISTS allows re-run on already-installed extension.
-- =====================================================
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =====================================================
-- INDEX 1: Diploma number trigram GIN — substring search optimization
-- =====================================================
-- Audit finding MEDIUM-26: findByDiplomaNumberContainingIgnoreCase uses
-- `LOWER(diploma_number) LIKE '%term%'` with leading wildcard — full table scan.
-- pg_trgm GIN index allows planner to use trigrams for substring match: ~50ms
-- vs ~5s on 1M+ row table.
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_diploma_number_trgm
    ON hemishe_e_student_diploma
    USING GIN (LOWER(diploma_number) gin_trgm_ops);

COMMENT ON INDEX idx_diploma_number_trgm IS 'pg_trgm GIN — substring search on diploma_number (LIKE %X%).';

-- =====================================================
-- INDEX 2: StudentMeta partial UNIQUE — race condition fix
-- =====================================================
-- Audit finding HIGH-6: StudentMetaService.create generates max(u_id)+1 then
-- INSERT — two concurrent transactions can read same max → both insert with
-- same u_id. Old comment: "unique constraint (u_id, _university) includes
-- soft-deleted records" — but no actual partial unique was created.
--
-- Partial UNIQUE: only one active (u_id, _university) tuple at a time.
-- Allows soft-deleted u_id to be reused after delete_ts SET (transfer scenarios).
-- =====================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_meta_uid_university_active
    ON hemishe_e_student_meta(u_id, _university)
    WHERE delete_ts IS NULL;

COMMENT ON INDEX uq_student_meta_uid_university_active IS 'Per-(u_id, university) uniqueness — prevents concurrent-insert duplicate.';
