-- ═══════════════════════════════════════════════════════════════════
-- M002: Performance & search indexes for legacy hemishe_e_* (FROZEN tables)
--
-- Consolidated index migration — covers Student, Grade, Attendance,
-- StudentDiploma, StudentMeta. All targets are CUBA Platform legacy tables
-- whose schema we DO NOT own (no CREATE TABLE in our migrations); index-only
-- DDL is the allowed mutation per domain/CLAUDE.md.
--
-- Idempotent: every index uses IF NOT EXISTS / partial UNIQUE.
-- All operations additive — backward compatible, no data shape change.
-- ═══════════════════════════════════════════════════════════════════

-- pg_trgm extension for ILIKE substring search + GIN trigram indexes
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ═══════════ Student (~3.4M rows) ═══════════
-- ── Filter indexes ───────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_student_delete_ts         ON hemishe_e_student (delete_ts);
CREATE INDEX IF NOT EXISTS idx_student_university        ON hemishe_e_student ("_university");
CREATE INDEX IF NOT EXISTS idx_student_status            ON hemishe_e_student ("_student_status");
CREATE INDEX IF NOT EXISTS idx_student_payment_form      ON hemishe_e_student ("_payment_form");
CREATE INDEX IF NOT EXISTS idx_student_course            ON hemishe_e_student ("_course");
CREATE INDEX IF NOT EXISTS idx_student_faculty           ON hemishe_e_student ("_faculty");
CREATE INDEX IF NOT EXISTS idx_student_education_type    ON hemishe_e_student ("_education_type");
CREATE INDEX IF NOT EXISTS idx_student_education_form    ON hemishe_e_student ("_education_form");
CREATE INDEX IF NOT EXISTS idx_student_education_year    ON hemishe_e_student ("_education_year");
CREATE INDEX IF NOT EXISTS idx_student_gender            ON hemishe_e_student ("_gender");

-- ── B-tree search indexes ────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_student_code              ON hemishe_e_student (code);
CREATE INDEX IF NOT EXISTS idx_student_pinfl             ON hemishe_e_student (pinfl);
CREATE INDEX IF NOT EXISTS idx_student_lastname          ON hemishe_e_student (lastname);

-- ── GIN trigram indexes (ILIKE '%pattern%') ──────────────────────
CREATE INDEX IF NOT EXISTS idx_student_lastname_trgm     ON hemishe_e_student USING gin (lastname gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_student_firstname_trgm    ON hemishe_e_student USING gin (firstname gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_student_code_trgm         ON hemishe_e_student USING gin (code gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_student_pinfl_trgm        ON hemishe_e_student USING gin (pinfl gin_trgm_ops);

-- ── Prefix search indexes (LIKE 'value%') ────────────────────────
CREATE INDEX IF NOT EXISTS idx_student_code_prefix       ON hemishe_e_student (code text_pattern_ops)  WHERE delete_ts IS NULL;
CREATE INDEX IF NOT EXISTS idx_student_pinfl_prefix      ON hemishe_e_student (pinfl text_pattern_ops) WHERE delete_ts IS NULL;

-- ── Composite / covering indexes ─────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_student_univ_status_delts ON hemishe_e_student ("_university", "_student_status", delete_ts);
CREATE INDEX IF NOT EXISTS idx_student_delts_code        ON hemishe_e_student (delete_ts, code DESC);
CREATE INDEX IF NOT EXISTS idx_student_active_code_desc  ON hemishe_e_student (code DESC) WHERE delete_ts IS NULL;

-- ── Keyset paging composite (multi-tenant + status + create_ts) ──
-- findStudentsByUniversityPaginated avoids OFFSET (slow on deep pages).
CREATE INDEX IF NOT EXISTS idx_student_university_status_createts
    ON hemishe_e_student ("_university", "_student_status", create_ts DESC)
    WHERE delete_ts IS NULL;
COMMENT ON INDEX idx_student_university_status_createts IS 'Multi-tenant + status + ORDER BY create_ts — keyset paging support.';

-- ── PINFL master uniqueness (race condition fix) ─────────────────
-- isDuplicate flag managed manually with no DB constraint enforcing
-- "only one TRUE per PINFL". Concurrent inserts could create multiple
-- master rows for same PINFL. Partial UNIQUE allows reuse after soft-delete.
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_pinfl_master
    ON hemishe_e_student (pinfl)
    WHERE is_duplicate = true AND delete_ts IS NULL;
COMMENT ON INDEX uq_student_pinfl_master IS 'PINFL master record uniqueness — prevents duplicate master rows on concurrent enrollment.';

-- ═══════════ Grade (~30M rows = 5-30 grades per student) ═══════════
CREATE INDEX IF NOT EXISTS idx_grade_university_active
    ON hemishe_e_grade ("_university")
    WHERE delete_ts IS NULL;
COMMENT ON INDEX idx_grade_university_active IS 'Multi-tenant filter for Grade — 224 OTM queries. Partial: active rows only.';

CREATE INDEX IF NOT EXISTS idx_grade_student_active
    ON hemishe_e_grade ("_student")
    WHERE delete_ts IS NULL;
COMMENT ON INDEX idx_grade_student_active IS 'Per-student grade lookup — GPA/transcript/scholarship.';

-- ═══════════ Attendance ═══════════
CREATE INDEX IF NOT EXISTS idx_attendance_student_active
    ON hemishe_e_attendance ("_student")
    WHERE delete_ts IS NULL;
COMMENT ON INDEX idx_attendance_student_active IS 'Per-student attendance lookup.';

CREATE INDEX IF NOT EXISTS idx_attendance_uni_date
    ON hemishe_e_attendance ("_university", attendance_date)
    WHERE delete_ts IS NULL;
COMMENT ON INDEX idx_attendance_uni_date IS 'University-wide attendance reports by date range.';

-- ═══════════ StudentDiploma — substring search ═══════════
-- findByDiplomaNumberContainingIgnoreCase uses LOWER(diploma_number) LIKE '%X%'
-- (leading wildcard = full table scan). Trigram GIN: ~50ms vs ~5s on 1M+ rows.
CREATE INDEX IF NOT EXISTS idx_diploma_number_trgm
    ON hemishe_e_student_diploma
    USING GIN (LOWER(diploma_number) gin_trgm_ops);
COMMENT ON INDEX idx_diploma_number_trgm IS 'pg_trgm GIN — substring search on diploma_number (LIKE %X%).';

-- ═══════════ StudentMeta — race condition fix ═══════════
-- StudentMetaService.create generates max(u_id)+1 then INSERT — two concurrent
-- transactions can read same max → both insert with same u_id. Partial UNIQUE
-- allows reuse after soft-delete (transfer scenarios).
CREATE UNIQUE INDEX IF NOT EXISTS uq_student_meta_uid_university_active
    ON hemishe_e_student_meta (u_id, "_university")
    WHERE delete_ts IS NULL;
COMMENT ON INDEX uq_student_meta_uid_university_active IS 'Per-(u_id, university) uniqueness — prevents concurrent-insert duplicate.';
