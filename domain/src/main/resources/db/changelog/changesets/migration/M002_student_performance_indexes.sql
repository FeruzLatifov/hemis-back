-- ═══════════════════════════════════════════════════════════════════
-- M002: Performance indexes for hemishe_e_student (~3.4M rows)
--
-- Consolidated: filter, trigram search, pagination, prefix search.
-- All indexes are additive (CREATE IF NOT EXISTS), backward compatible.
-- ═══════════════════════════════════════════════════════════════════

-- pg_trgm extension for ILIKE substring search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

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
