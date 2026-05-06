-- ═══════════════════════════════════════════════════════════════════
-- M002: Performance & search indexes for legacy hemishe_e_* tables
--
-- Scope: tables that exist in OUR database (`test1_hemis` per .env) —
-- inspected via information_schema. Tables that belong to per-OTM Univer
-- (Yii2 PHP) databases (hemis_NNN) are NOT indexed here, and their Java
-- entities have been removed (Grade, Attendance for example).
--
-- Indexed tables in this migration:
--   - hemishe_e_student          (~1.15M rows)
--   - hemishe_e_student_diploma  (substring search via trigram)
--   - hemishe_e_student_meta     (race-fix UNIQUE)
--
-- Index-only DDL (no CREATE TABLE in our migrations) — `hemishe_e_*`
-- schema is FROZEN per domain/CLAUDE.md.
--
-- Idempotent: every index uses IF NOT EXISTS / partial UNIQUE.
-- Defensive: each table group guarded by information_schema check;
-- partial dev dumps that lack a target table skip silently.
-- ═══════════════════════════════════════════════════════════════════

-- pg_trgm extension for ILIKE substring search + GIN trigram indexes
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ═══════════ Student (~1.15M rows) ═══════════
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = 'public' AND table_name = 'hemishe_e_student') THEN
        RAISE NOTICE 'M002: hemishe_e_student not present, skipping student indexes';
        RETURN;
    END IF;

    -- ── Filter indexes ───────────────────────────────────────────
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

    -- ── B-tree search indexes ────────────────────────────────────
    CREATE INDEX IF NOT EXISTS idx_student_code              ON hemishe_e_student (code);
    CREATE INDEX IF NOT EXISTS idx_student_pinfl             ON hemishe_e_student (pinfl);
    CREATE INDEX IF NOT EXISTS idx_student_lastname          ON hemishe_e_student (lastname);

    -- ── GIN trigram indexes (ILIKE '%pattern%') ─────────────────
    CREATE INDEX IF NOT EXISTS idx_student_lastname_trgm     ON hemishe_e_student USING gin (lastname gin_trgm_ops);
    CREATE INDEX IF NOT EXISTS idx_student_firstname_trgm    ON hemishe_e_student USING gin (firstname gin_trgm_ops);
    CREATE INDEX IF NOT EXISTS idx_student_code_trgm         ON hemishe_e_student USING gin (code gin_trgm_ops);
    CREATE INDEX IF NOT EXISTS idx_student_pinfl_trgm        ON hemishe_e_student USING gin (pinfl gin_trgm_ops);

    -- ── Prefix search indexes (LIKE 'value%') ────────────────────
    CREATE INDEX IF NOT EXISTS idx_student_code_prefix       ON hemishe_e_student (code text_pattern_ops)  WHERE delete_ts IS NULL;
    CREATE INDEX IF NOT EXISTS idx_student_pinfl_prefix      ON hemishe_e_student (pinfl text_pattern_ops) WHERE delete_ts IS NULL;

    -- ── Composite / covering indexes ─────────────────────────────
    CREATE INDEX IF NOT EXISTS idx_student_univ_status_delts ON hemishe_e_student ("_university", "_student_status", delete_ts);
    CREATE INDEX IF NOT EXISTS idx_student_delts_code        ON hemishe_e_student (delete_ts, code DESC);
    CREATE INDEX IF NOT EXISTS idx_student_active_code_desc  ON hemishe_e_student (code DESC) WHERE delete_ts IS NULL;

    -- ── Keyset paging composite (multi-tenant + status + create_ts) ──
    CREATE INDEX IF NOT EXISTS idx_student_university_status_createts
        ON hemishe_e_student ("_university", "_student_status", create_ts DESC)
        WHERE delete_ts IS NULL;
    COMMENT ON INDEX idx_student_university_status_createts IS 'Multi-tenant + status + ORDER BY create_ts — keyset paging support.';
END $$;

-- ── PINFL master uniqueness (race condition fix) ─────────────────
-- DEFENSIVE GUARD: pre-check existing data — if active masters already
-- collide, SKIP the index with WARNING (data NOT modified — legacy CUBA
-- `hemishe_*` schema is FROZEN per domain/CLAUDE.md).
DO $$
DECLARE
    dup_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = 'public' AND table_name = 'hemishe_e_student') THEN
        RAISE NOTICE 'M002: hemishe_e_student not present, skipping uq_student_pinfl_master';
        RETURN;
    END IF;

    SELECT COUNT(*) INTO dup_count
    FROM (SELECT pinfl FROM hemishe_e_student
          WHERE is_duplicate = true AND delete_ts IS NULL
          GROUP BY pinfl HAVING COUNT(*) > 1) dups;

    IF dup_count > 0 THEN
        RAISE WARNING 'M002: % PINFL(s) have multiple active master records — uq_student_pinfl_master skipped. Cleanup required (mark stale duplicates as is_duplicate=false). Re-run migration after cleanup.', dup_count;
    ELSE
        CREATE UNIQUE INDEX IF NOT EXISTS uq_student_pinfl_master
            ON hemishe_e_student (pinfl)
            WHERE is_duplicate = true AND delete_ts IS NULL;
        COMMENT ON INDEX uq_student_pinfl_master IS 'PINFL master record uniqueness — prevents duplicate master rows on concurrent enrollment.';
    END IF;
END $$;

-- ═══════════ StudentDiploma — substring search ═══════════
-- findByDiplomaNumberContainingIgnoreCase uses LOWER(diploma_number) LIKE '%X%'
-- (leading wildcard = full table scan). Trigram GIN: ~50ms vs ~5s on 1M+ rows.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = 'public' AND table_name = 'hemishe_e_student_diploma') THEN
        RAISE NOTICE 'M002: hemishe_e_student_diploma not present, skipping diploma indexes';
        RETURN;
    END IF;

    CREATE INDEX IF NOT EXISTS idx_diploma_number_trgm
        ON hemishe_e_student_diploma
        USING GIN (LOWER(diploma_number) gin_trgm_ops);
    COMMENT ON INDEX idx_diploma_number_trgm IS 'pg_trgm GIN — substring search on diploma_number (LIKE %X%).';
END $$;

-- ═══════════ StudentMeta — race condition fix ═══════════
-- StudentMetaService.create generates max(u_id)+1 then INSERT — two concurrent
-- transactions can read same max → both insert with same u_id. Partial UNIQUE
-- allows reuse after soft-delete (transfer scenarios).
--
-- DEFENSIVE GUARD: same FROZEN-schema policy as PINFL master above.
DO $$
DECLARE
    dup_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_schema = 'public' AND table_name = 'hemishe_e_student_meta') THEN
        RAISE NOTICE 'M002: hemishe_e_student_meta not present, skipping uq_student_meta_uid_university_active';
        RETURN;
    END IF;

    SELECT COUNT(*) INTO dup_count
    FROM (SELECT u_id, "_university" FROM hemishe_e_student_meta
          WHERE delete_ts IS NULL
          GROUP BY u_id, "_university" HAVING COUNT(*) > 1) dups;

    IF dup_count > 0 THEN
        RAISE WARNING 'M002: % (u_id, _university) pair(s) have duplicate active rows — uq_student_meta_uid_university_active skipped. Cleanup required.', dup_count;
    ELSE
        CREATE UNIQUE INDEX IF NOT EXISTS uq_student_meta_uid_university_active
            ON hemishe_e_student_meta (u_id, "_university")
            WHERE delete_ts IS NULL;
        COMMENT ON INDEX uq_student_meta_uid_university_active IS 'Per-(u_id, university) uniqueness — prevents concurrent-insert duplicate.';
    END IF;
END $$;
