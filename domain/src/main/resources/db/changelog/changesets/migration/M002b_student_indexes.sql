-- ═══════════════════════════════════════════════════════════════════
-- M002b: hemishe_e_student indexes — zero-downtime build
--
-- All CREATE INDEX CONCURRENTLY (online build, no ACCESS EXCLUSIVE
-- LOCK). 1.15M+ row table in prod — per-index build ~30s-5min depending
-- on column cardinality and total size. Total ~30-60min wall clock, but
-- writes/reads continue throughout.
--
-- Liquibase splitStatements: true ⇒ each ";" runs in its own session.
-- runInTransaction: false ⇒ CONCURRENTLY's "cannot run in tx block"
-- constraint is satisfied.
--
-- Defensive (FROZEN schema): preCondition in master.yaml verifies
-- table presence before running this changeset.
--
-- IF NOT EXISTS ⇒ idempotent; re-run after partial failure is safe
-- (PostgreSQL marks broken CONCURRENTLY index INVALID — DROP & re-create).
-- ═══════════════════════════════════════════════════════════════════

-- ── Filter indexes (10) ─────────────────────────────────────────────
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_delete_ts         ON hemishe_e_student (delete_ts);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_university        ON hemishe_e_student ("_university");
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_status            ON hemishe_e_student ("_student_status");
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_payment_form      ON hemishe_e_student ("_payment_form");
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_course            ON hemishe_e_student ("_course");
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_faculty           ON hemishe_e_student ("_faculty");
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_education_type    ON hemishe_e_student ("_education_type");
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_education_form    ON hemishe_e_student ("_education_form");
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_education_year    ON hemishe_e_student ("_education_year");
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_gender            ON hemishe_e_student ("_gender");

-- ── B-tree search indexes (3) ───────────────────────────────────────
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_code              ON hemishe_e_student (code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_pinfl             ON hemishe_e_student (pinfl);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_lastname          ON hemishe_e_student (lastname);

-- ── GIN trigram (ILIKE '%pattern%') (4) ─────────────────────────────
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_lastname_trgm     ON hemishe_e_student USING gin (lastname  gin_trgm_ops);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_firstname_trgm    ON hemishe_e_student USING gin (firstname gin_trgm_ops);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_code_trgm         ON hemishe_e_student USING gin (code      gin_trgm_ops);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_pinfl_trgm        ON hemishe_e_student USING gin (pinfl     gin_trgm_ops);

-- ── Prefix search (LIKE 'value%') (2) ───────────────────────────────
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_code_prefix       ON hemishe_e_student (code  text_pattern_ops) WHERE delete_ts IS NULL;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_pinfl_prefix      ON hemishe_e_student (pinfl text_pattern_ops) WHERE delete_ts IS NULL;

-- ── Composite / covering (3) ────────────────────────────────────────
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_univ_status_delts ON hemishe_e_student ("_university", "_student_status", delete_ts);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_delts_code        ON hemishe_e_student (delete_ts, code DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_active_code_desc  ON hemishe_e_student (code DESC) WHERE delete_ts IS NULL;

-- ── Keyset paging composite (1, with comment) ───────────────────────
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_university_status_createts
    ON hemishe_e_student ("_university", "_student_status", create_ts DESC)
    WHERE delete_ts IS NULL;
COMMENT ON INDEX idx_student_university_status_createts IS 'Multi-tenant + status + ORDER BY create_ts — keyset paging support.';

-- ── Duplicate-analysis covering index (1) ───────────────────────────
-- M003 mv_student_duplicates + duplicate-analysis CTE uchun covering index.
-- Bu yerda (M002b) turadi, chunki 1.15M-row hemishe_e_student index CONCURRENTLY bo'lishi
-- SHART (bloklovchi lock'siz). Feature MV/menu esa M003_student_duplicates'da qoladi.
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_student_dup_analysis
    ON hemishe_e_student (pinfl, "_student_status", "_university", "_education_type",
                          "_speciality_bachelor", "_speciality_master", "_speciality_ordinatura")
    WHERE delete_ts IS NULL AND pinfl IS NOT NULL AND pinfl != '';
