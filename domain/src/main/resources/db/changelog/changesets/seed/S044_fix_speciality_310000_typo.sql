-- =====================================================
-- S044: spelling fix — Magistr 310000 "xulq atvorga" → "xulq-atvorga"
-- =====================================================
-- Author: hemis-team
-- Date: 2026-09-04
--
-- SCOPE RULE (ministry, explicit): the L1/L2 rows of this classifier are CATEGORY LABELS — the
-- ministry duplicates the same 18 + 59 of them under every education type — and a spelling error in
-- one of those labels may be corrected. The L3/L4 rows are the official speciality names themselves
-- and are NEVER touched: they stay byte-for-byte as the source xlsx delivered them. This changeset
-- edits an L2 row and nothing else.
--
-- Comparing the Bakalavr and Magistr L1/L2 sets row by row, they agree on 76 of 77 and disagree on
-- exactly one character:
--
--     '11'  310000  'Ijtimoiy va xulq-atvorga mansub fanlar'    (0x2d, hyphen)
--     '12'  310000  'Ijtimoiy va xulq atvorga mansub fanlar'    (0x20, space)   ← ed932734
--
-- "Xulq-atvor" is one hyphenated word in Uzbek, so the Bakalavr spelling is correct and the Magistr
-- row carries the typo. It is not one this project introduced: S014 imported it verbatim from the
-- ministry's 3_Magistr.xlsx (S014:179), which is what a faithful import should do — but the
-- consequence is that the same category reads differently depending on which tab you open, and it
-- reads differently again in the .xlsx export and in whatever the 224 OTMs are shown.
--
-- A full scan of all 154 L1/L2 rows found no other spelling defect: no straight/curly apostrophe
-- where the project standard is ʻ (U+02BB) / ʼ (U+02BC), no doubled or edge whitespace, and no other
-- Bakalavr↔Magistr divergence. This is the only one.
--
-- Scope: ONE row, ONE column. The 34 children of ed932734 are untouched (only their parent's own
-- label changes), as are its code, its id, its years, its parent and the Bakalavr twin.
--
-- version = version + 1 on purpose. This row sits at the table's -1 baseline like 5511 others; the
-- bump moves it to 0 and, with it, the SUM(version) snapshot Univer polls to decide whether the
-- classifier changed. A label correction IS a change the OTMs need to pick up, so the signal has to
-- move — leaving version alone would ship a silent edit.
--
-- Guards, both checked before the write, and either one turns this into a reported no-op:
--   • the row must still carry the typo — so a re-run does nothing, and a curator who has since
--     renamed the row by hand is never overwritten;
--   • the corrected identity must be free — uq_h_speciality_identity_live is UNIQUE on
--     (education_type, code, name_search) among live rows and name_search is GENERATED from name_uz,
--     so this UPDATE moves the row onto a NEW identity key. If another live '12'/310000 row already
--     holds the hyphenated spelling, this reports and skips instead of failing 23505 mid-migration.
--
-- ⚠ The source 3_Magistr.xlsx still contains the typo and etl_speciality.py has no override map, so a
--   re-run of that ETL would regenerate S014 with the old spelling. Re-apply this changeset after any
--   such re-run (same convention as the MANUAL CORRECTIONS blocks in the S015/S017 headers).
-- =====================================================

DO $$
DECLARE
    target_id CONSTANT UUID := 'ed932734-6429-451f-be51-f126c0bbb08a';
    old_name  CONSTANT TEXT := 'Ijtimoiy va xulq atvorga mansub fanlar';
    new_name  CONSTANT TEXT := 'Ijtimoiy va xulq-atvorga mansub fanlar';
    found     INTEGER;
    taken     INTEGER;
    lvl       INTEGER;
BEGIN
    SELECT count(*), max(hierarchy_level) INTO found, lvl
      FROM h_speciality
     WHERE id = target_id AND name_uz = old_name AND deleted_at IS NULL;

    IF found = 0 THEN
        RAISE NOTICE 'S044: 310000 (Magistr) allaqachon tuzatilgan yoki qoʻlda oʻzgartirilgan — oʻtkazib yuborildi';
        RETURN;
    END IF;

    -- Belt and braces for the scope rule above: this changeset must never touch an L3/L4 row, even
    -- if the id above is ever mistyped or the row is later re-levelled.
    IF lvl IS DISTINCT FROM 2 THEN
        RAISE EXCEPTION 'S044: nishon qator L2 emas (hierarchy_level=%) — mutaxassislik nomlari tuzatilmaydi', lvl;
    END IF;

    SELECT count(*) INTO taken
      FROM h_speciality
     WHERE education_type = '12'
       AND code = '310000'
       AND name_search = h_speciality_fold(new_name)
       AND deleted_at IS NULL
       AND id <> target_id;

    IF taken > 0 THEN
        RAISE NOTICE 'S044: (12, 310000, %) identity band — tuzatish oʻtkazib yuborildi', new_name;
        RETURN;
    END IF;

    UPDATE h_speciality
       SET name_uz    = new_name,
           version    = version + 1,
           updated_at = CURRENT_TIMESTAMP,
           updated_by = 'system'
     WHERE id = target_id;

    RAISE NOTICE 'S044: 310000 (Magistr, L2) "%" -> "%"', old_name, new_name;
END $$;
