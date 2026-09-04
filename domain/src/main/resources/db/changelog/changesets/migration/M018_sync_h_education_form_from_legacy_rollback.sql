-- =====================================================
-- M018 ROLLBACK: drop the education forms this changeset imported
-- =====================================================
-- Precise by construction: M018 stamps created_by = 'legacy-sync-M018' on every row it inserts, so
-- the rollback can name exactly what it introduced instead of guessing from the legacy table (which
-- may itself have moved on since).
--
-- Two things it deliberately does NOT undo:
--   • the name/active refresh of pre-existing rows. Those columns are CUBA-authoritative per V021, so
--     the value M018 wrote is the ministry's current one — putting back a stale name would be
--     restoring an error, not a state. version stays where it is; @Version counts edits.
--   • a row that something already references. university_speciality_attachment.education_form is
--     ON DELETE RESTRICT: if an OTM has attached a speciality under a newly imported form, deleting
--     it would fail 23503 mid-rollback. Those are left standing and named in a NOTICE — a live
--     attachment outranks a tidy rollback.
-- =====================================================

DO $$
DECLARE
    blocked TEXT;
BEGIN
    SELECT string_agg(DISTINCT m.code, ', ' ORDER BY m.code) INTO blocked
      FROM h_education_form m
      JOIN university_speciality_attachment a ON a.education_form = m.code
     WHERE m.created_by = 'legacy-sync-M018';

    IF blocked IS NOT NULL THEN
        RAISE NOTICE 'M018 rollback: % shakli OTM biriktirmalarida ishlatilyapti — oʻchirilmadi', blocked;
    END IF;
END $$;

DELETE FROM h_education_form m
 WHERE m.created_by = 'legacy-sync-M018'
   AND NOT EXISTS (SELECT 1 FROM university_speciality_attachment a WHERE a.education_form = m.code);
