-- =====================================================
-- S044 ROLLBACK: restore the original (misspelled) Magistr 310000 label
-- =====================================================
-- A rollback restores the state the changeset found, not the state anyone would prefer — so this puts
-- the space back. Same guards mirrored: only act if the row still carries the corrected spelling this
-- changeset wrote (so a later hand-rename is not clobbered), and only if the old identity key is free.
-- version is bumped again rather than decremented: the OTMs need to know the label moved back, and
-- @Version counts edits, it does not track a value.
-- =====================================================

DO $$
DECLARE
    target_id CONSTANT UUID := 'ed932734-6429-451f-be51-f126c0bbb08a';
    old_name  CONSTANT TEXT := 'Ijtimoiy va xulq atvorga mansub fanlar';
    new_name  CONSTANT TEXT := 'Ijtimoiy va xulq-atvorga mansub fanlar';
    found     INTEGER;
    taken     INTEGER;
BEGIN
    SELECT count(*) INTO found
      FROM h_speciality
     WHERE id = target_id AND name_uz = new_name AND deleted_at IS NULL;

    IF found = 0 THEN
        RAISE NOTICE 'S044 rollback: 310000 (Magistr) bu changeset yozgan holatda emas — oʻtkazib yuborildi';
        RETURN;
    END IF;

    SELECT count(*) INTO taken
      FROM h_speciality
     WHERE education_type = '12' AND code = '310000'
       AND name_search = h_speciality_fold(old_name)
       AND deleted_at IS NULL AND id <> target_id;

    IF taken > 0 THEN
        RAISE NOTICE 'S044 rollback: eski identity band — oʻtkazib yuborildi';
        RETURN;
    END IF;

    UPDATE h_speciality
       SET name_uz    = old_name,
           version    = version + 1,
           updated_at = CURRENT_TIMESTAMP,
           updated_by = 'system'
     WHERE id = target_id;
END $$;
