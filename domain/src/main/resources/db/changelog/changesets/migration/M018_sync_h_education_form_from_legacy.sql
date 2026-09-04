-- =====================================================
-- M018: re-sync h_education_form from the frozen hemishe_h_education_form
-- =====================================================
-- Author: hemis-team
-- Date: 2026-09-04
--
-- V021 seeded h_education_form 1:1 from the frozen CUBA classifier and declared the split of
-- ownership that still holds: the ministry owns `code`, the uz `name` and `active`; we own
-- `name_ru`/`name_en` and `sort_order`. (V021's header justifies that split by saying the CUBA source
-- has no translations — it does have them; what is true is that V021 chose its own, shorter wording
-- for the 13 known codes, which is why ours are not overwritten. See step 1.) That seed ran ONCE.
-- Anything the ministry has added to hemishe_h_education_form since is invisible to us — the
-- classifier silently stops one row short, and because university_speciality_attachment.education_form
-- FKs into our table with RESTRICT, a form we never imported cannot be picked at all.
--
-- This changeset closes that gap against WHATEVER the legacy table holds on the database it runs on.
-- It carries no hard-coded row list on purpose: the local snapshot is already in sync (13 of 13), so
-- a literal list would encode a diff that is only true here and would do nothing where it matters.
--
-- What it does, in order:
--   1. INSERT every live numeric legacy code we do not have, taking name_ru/name_en FROM THE LEGACY
--      ROW when it has them. V021's header states the CUBA source is uz-only; that is not true —
--      hemishe_h_education_form does carry name_ru/name_en (e.g. 'Очное обучение' / 'Full-time
--      education' for code 11). V021 wrote its own shorter wording for the 13 known codes
--      ('Дневная' / 'Full-time') and those stay ours, but for a code nobody has ever translated the
--      ministry's own text beats a NULL. Where the legacy row leaves them blank they stay NULL and
--      the NOTICE below names the code so it can be filled in.
--   2. UPDATE the uz name / active flag of existing rows where the legacy value differs — CUBA is
--      authoritative for those two columns per V021. RU/EN and sort_order are NOT touched, precisely
--      because ours were curated to differ (see step 1): overwriting them would undo V021 on every run.
--   3. REPORT rows we hold that the legacy table no longer lists live. It does NOT delete or
--      deactivate them: the FK is ON DELETE RESTRICT and an OTM attachment may already point at one,
--      so withdrawing a form is a decision with consequences and belongs to a human, not to a
--      migration that happens to notice.
--
-- Idempotent: INSERT ... ON CONFLICT DO NOTHING plus a DISTINCT-guarded UPDATE, so a second run
-- changes nothing and reports zeroes.
--
-- Scope: only `code ~ '^[0-9]+$'` and `delete_ts IS NULL` — the same two filters V021 used, so this
-- and the original seed can never disagree about what "the legacy set" means.
--
-- created_by = 'legacy-sync-M018' on inserted rows: it is what makes the rollback precise (it can
-- delete exactly the rows this changeset introduced and nothing else) and it tells anyone reading
-- the table later where a row with empty RU/EN came from.
--
-- ⚠ ONE-OFF. It runs once per database and catches up whatever has accumulated. It does NOT keep the
--   two tables in step going forward — the next ministry addition needs another pass. If that turns
--   out to be a recurring need, the durable fix is a scheduled re-sync or a button on the classifier
--   screen, not a migration per addition.
--
-- The same drift is possible in h_education_type (V022) and h_education_year (V018), which use this
-- exact pattern. Both are in sync on the snapshot checked here and are deliberately left alone.
--
-- Caches: `classifierEducationForm` and `legacyClassifierMaps` hold this data with a TTL. A new form
-- appears after eviction or expiry, not instantly — clear them (or restart) if it must be immediate.
-- =====================================================

SET LOCAL lock_timeout = '3s';

DO $$
DECLARE
    inserted_codes TEXT;
    inserted_n     INTEGER := 0;
    updated_n      INTEGER := 0;
    orphan_codes   TEXT;
BEGIN
    -- ── 1. New codes ────────────────────────────────────────────────────────────────────────
    WITH ins AS (
        INSERT INTO h_education_form (code, name, name_ru, name_en, is_active, sort_order, created_by)
        SELECT l.code,
               COALESCE(NULLIF(l.name, ''), l.code),
               NULLIF(l.name_ru, ''),          -- ministry's own translation beats a NULL for a new code
               NULLIF(l.name_en, ''),
               COALESCE(l.active, true),
               l.code::int,
               'legacy-sync-M018'
        FROM hemishe_h_education_form l
        WHERE l.code ~ '^[0-9]+$'
          AND l.delete_ts IS NULL
          AND NOT EXISTS (SELECT 1 FROM h_education_form m WHERE m.code = l.code)
        ON CONFLICT (code) DO NOTHING
        RETURNING code, name, name_ru
    )
    SELECT count(*),
           string_agg(code || ' ' || name || CASE WHEN name_ru IS NULL THEN ' [RU/EN BOʻSH]' ELSE '' END,
                      ', ' ORDER BY code)
      INTO inserted_n, inserted_codes FROM ins;

    -- ── 2. uz name / active drift on rows we already hold ───────────────────────────────────
    WITH upd AS (
        UPDATE h_education_form m
           SET name       = COALESCE(NULLIF(l.name, ''), l.code),
               is_active  = COALESCE(l.active, true),
               version    = m.version + 1,
               updated_at = CURRENT_TIMESTAMP,
               updated_by = 'legacy-sync-M018'
          FROM hemishe_h_education_form l
         WHERE l.code = m.code
           AND l.code ~ '^[0-9]+$'
           AND l.delete_ts IS NULL
           AND (m.name IS DISTINCT FROM COALESCE(NULLIF(l.name, ''), l.code)
                OR m.is_active IS DISTINCT FROM COALESCE(l.active, true))
        RETURNING m.code
    )
    SELECT count(*) INTO updated_n FROM upd;

    -- ── 3. Ours-only rows — reported, never removed ─────────────────────────────────────────
    SELECT string_agg(m.code || ' (' || m.name || ')', ', ' ORDER BY m.code)
      INTO orphan_codes
      FROM h_education_form m
     WHERE NOT EXISTS (
        SELECT 1 FROM hemishe_h_education_form l
         WHERE l.code = m.code AND l.delete_ts IS NULL AND l.code ~ '^[0-9]+$');

    RAISE NOTICE 'M018: h_education_form sinxronlandi — % ta yangi, % ta yangilandi', inserted_n, updated_n;
    IF inserted_n > 0 THEN
        RAISE NOTICE 'M018: YANGI shakllar: %', inserted_codes;
    END IF;
    IF orphan_codes IS NOT NULL THEN
        RAISE NOTICE 'M018: eski jadvalda YOʻQ, lekin bizda bor (oʻchirilmadi — FK RESTRICT, qoʻlda koʻrib chiqing): %', orphan_codes;
    END IF;
END $$;
