-- =====================================================
-- M011: university_speciality_attachment — DROP soft delete (deleted_at / deleted_by)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-20
-- Purpose: An attachment has no dependants — nothing references it and it is re-created
--          in one click — so a soft-delete trail buys nothing here. In prod it only did
--          harm: fk_univ_spec_attach_spec is ON DELETE RESTRICT and the delete-guard
--          counted soft-deleted rows too, so a speciality was reported as "attached to
--          3 universities" while the attachment registry (which filters deleted_at IS NULL)
--          showed none of them. Business state already lives in `status`
--          (ACTIVE / SUSPENDED / REVOKED) — "revoked" is recorded there — so dropping the
--          soft-delete trail loses no history. Detaching becomes a HARD DELETE.
-- Scope:   Reverses the soft-delete half of V019: the two audit columns and the FOUR
--          partial (`WHERE deleted_at IS NULL`) indexes it created. The V021 index
--          idx_univ_spec_attach_form is already full-table — untouched. Index NAMES stay
--          the same, only the partial predicate goes away.
-- ⚠️ IRREVERSIBLE: step 2 deletes the soft-deleted rows for good. The count is reported
--          with RAISE NOTICE BEFORE the DELETE, so the deploy log keeps a record of how
--          much was dropped in prod (rollback restores structure only, never the rows).
-- Idempotent: the purge is skipped once deleted_at is gone; DROP INDEX IF EXISTS /
--          CREATE ... IF NOT EXISTS / DROP COLUMN IF EXISTS everywhere else.
-- =====================================================

-- 1-2) Report, then purge. The NOTICE comes first on purpose: it is the only record left
--      of what this migration removed.
DO $$
DECLARE
    _soft_deleted BIGINT := 0;
    _purged       BIGINT := 0;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name   = 'university_speciality_attachment'
          AND column_name  = 'deleted_at'
    ) THEN
        RAISE NOTICE 'M011: deleted_at is already gone — purge skipped (re-run)';
        RETURN;
    END IF;

    -- Dynamic SQL so the deleted_at reference is resolved only when the column exists
    -- (keeps the re-run path above free of any dependency on the dropped column).
    EXECUTE 'SELECT count(*) FROM university_speciality_attachment WHERE deleted_at IS NOT NULL'
        INTO _soft_deleted;
    RAISE NOTICE 'M011: % soft-deleted attachment row(s) found — deleting PERMANENTLY (not recoverable by rollback)',
        _soft_deleted;

    EXECUTE 'DELETE FROM university_speciality_attachment WHERE deleted_at IS NOT NULL';
    GET DIAGNOSTICS _purged = ROW_COUNT;
    RAISE NOTICE 'M011: purged % soft-deleted attachment row(s)', _purged;
END $$;

-- 3a) Guard BEFORE the unique index goes full-table. Step 2 removed every soft-deleted row,
--     so the live rows alone decide it — but a duplicate must fail loudly with the offending
--     keys, not as a bare "could not create unique index" from PostgreSQL.
DO $$
DECLARE
    _dup_groups BIGINT := 0;
    _dup_rows   BIGINT := 0;
    _sample     TEXT;
BEGIN
    SELECT count(*), coalesce(sum(d.c), 0)
      INTO _dup_groups, _dup_rows
    FROM (
        SELECT count(*) AS c
        FROM university_speciality_attachment
        GROUP BY university_code, speciality_id, education_form, edu_year
        HAVING count(*) > 1
    ) d;

    IF _dup_groups > 0 THEN
        SELECT string_agg(format('(%s / %s / form %s / %s) x%s',
                                 d.university_code, d.speciality_id, d.education_form, d.edu_year, d.c), '; ')
          INTO _sample
        FROM (
            SELECT university_code, speciality_id, education_form, edu_year, count(*) AS c
            FROM university_speciality_attachment
            GROUP BY university_code, speciality_id, education_form, edu_year
            HAVING count(*) > 1
            ORDER BY count(*) DESC
            LIMIT 5
        ) d;

        RAISE EXCEPTION 'M011: % duplicate (university_code, speciality_id, education_form, edu_year) group(s) covering % row(s) — uq_univ_spec_attach cannot become a full-table unique index. First 5: %',
            _dup_groups, _dup_rows, _sample
            USING HINT = 'Keep one row per group (the newest by updated_at/created_at), delete the rest, then re-run M011.';
    END IF;

    RAISE NOTICE 'M011: no duplicate attachment keys — uq_univ_spec_attach can drop its partial predicate';
END $$;

-- 3b) Recreate the four partial indexes without the predicate. They MUST be rebuilt here:
--     DROP COLUMN in step 4 would otherwise silently drop them (an index depending on
--     deleted_at goes away with the column), leaving the table with no uniqueness guard.
--     Names stay identical — nothing else in the codebase has to be touched.
DROP INDEX IF EXISTS uq_univ_spec_attach;
CREATE UNIQUE INDEX IF NOT EXISTS uq_univ_spec_attach
    ON university_speciality_attachment(university_code, speciality_id, education_form, edu_year);

DROP INDEX IF EXISTS idx_univ_spec_attach_univ;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_univ ON university_speciality_attachment(university_code);

DROP INDEX IF EXISTS idx_univ_spec_attach_spec;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_spec ON university_speciality_attachment(speciality_id);

DROP INDEX IF EXISTS idx_univ_spec_attach_year;
CREATE INDEX IF NOT EXISTS idx_univ_spec_attach_year ON university_speciality_attachment(edu_year);

-- 4) The soft-delete columns themselves.
ALTER TABLE university_speciality_attachment
    DROP COLUMN IF EXISTS deleted_at,
    DROP COLUMN IF EXISTS deleted_by;

-- 5) Table comment — V019 advertised the soft-delete trail; state the opposite now.
COMMENT ON TABLE university_speciality_attachment IS
    'Attach a unified-classifier speciality (h_speciality) to an OTM (university_code). Permission/role driven, tenant-scope fail-closed in the service layer. NO soft delete (M011): detaching is a HARD DELETE — nothing depends on an attachment and it is re-created on demand; the revocable business state lives in status (ACTIVE/SUSPENDED/REVOKED).';
