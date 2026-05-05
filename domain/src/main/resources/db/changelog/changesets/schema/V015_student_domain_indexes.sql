-- =====================================================
-- V015: STUDENT DOMAIN PERFORMANCE INDEXES
-- =====================================================
-- Author: hemis-team
-- Date: 2026-05-05
-- Purpose: 1.15M Student qator bo'yicha hot-path query'lar uchun indekslar.
-- Source: Student domain audit (n-plus-one-detector + entity audit)
--   - Multi-tenant queries `WHERE _university = ?` sequential scan (224 OTM)
--   - Attendance.findByStudent — sequential scan
--   - PINFL master record race condition (no DB-level uniqueness on is_duplicate=true)
--
-- Self-contained: only CREATE INDEX (no DDL alter to hemishe_* tables, FROZEN).
-- Idempotent: IF NOT EXISTS + partial UNIQUE.
--
-- Depends on: legacy CUBA tables (FROZEN) — hemishe_e_student, hemishe_e_grade,
--             hemishe_e_attendance.
-- =====================================================

-- =====================================================
-- INDEX 1: Grade by university — multi-tenant filter index
-- =====================================================
-- Audit finding HIGH-5: Grade table has VARCHAR `_university` FK with NO index.
-- Query pattern: `WHERE _university = ? AND delete_ts IS NULL` — full table scan
-- on 1.15M qator + grade rows (5-30 grades per student → ~30M rows total).
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_grade_university_active
    ON hemishe_e_grade(_university)
    WHERE delete_ts IS NULL;

COMMENT ON INDEX idx_grade_university_active IS 'Multi-tenant filter for Grade — 224 OTM queries. Partial: active rows only.';

-- =====================================================
-- INDEX 2: Grade by student — child collection lookup
-- =====================================================
-- Query pattern: `WHERE _student = ? AND delete_ts IS NULL ORDER BY ...`
-- Used by GpaService, transcript generation, scholarship checks.
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_grade_student_active
    ON hemishe_e_grade(_student)
    WHERE delete_ts IS NULL;

COMMENT ON INDEX idx_grade_student_active IS 'Per-student grade lookup — GPA/transcript/scholarship.';

-- =====================================================
-- INDEX 3: Attendance by student — child collection lookup
-- =====================================================
-- Audit finding HIGH-10: Attendance has `_student` UUID with NO index.
-- Query pattern: "find attendance by student" = sequential scan.
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_attendance_student_active
    ON hemishe_e_attendance(_student)
    WHERE delete_ts IS NULL;

COMMENT ON INDEX idx_attendance_student_active IS 'Per-student attendance lookup.';

-- =====================================================
-- INDEX 4: Attendance by university+date — bulk reporting filter
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_attendance_uni_date
    ON hemishe_e_attendance(_university, attendance_date)
    WHERE delete_ts IS NULL;

COMMENT ON INDEX idx_attendance_uni_date IS 'University-wide attendance reports by date range.';

-- =====================================================
-- INDEX 5: Student PINFL master uniqueness — race condition fix
-- =====================================================
-- Audit finding HIGH-7: isDuplicate flag managed manually with NO DB constraint
-- enforcing "only one TRUE per PINFL". Concurrent inserts can create multiple
-- master records for same PINFL → findMasterByPinfl uses LIMIT 1 as workaround.
--
-- Partial UNIQUE: only one active master per PINFL.
-- Allows multiple is_duplicate=true rows if soft-deleted (delete_ts SET);
-- enforces uniqueness only on active masters.
-- =====================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_pinfl_master
    ON hemishe_e_student(pinfl)
    WHERE is_duplicate = true AND delete_ts IS NULL;

COMMENT ON INDEX uq_student_pinfl_master IS 'PINFL master record uniqueness — prevents duplicate master rows on concurrent enrollment.';

-- =====================================================
-- INDEX 6: Student deep paging — keyset support
-- =====================================================
-- Audit finding HIGH-10: findStudentsByUniversityPaginated uses OFFSET — slow on
-- deep pages (OFFSET 50000 = scans 50K rows first). Composite index supports
-- keyset paging: `WHERE _university = ? AND _student_status IN (...) ORDER BY create_ts DESC`.
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_student_university_status_createts
    ON hemishe_e_student(_university, _student_status, create_ts DESC)
    WHERE delete_ts IS NULL;

COMMENT ON INDEX idx_student_university_status_createts IS 'Multi-tenant + status + ORDER BY create_ts — keyset paging support.';
