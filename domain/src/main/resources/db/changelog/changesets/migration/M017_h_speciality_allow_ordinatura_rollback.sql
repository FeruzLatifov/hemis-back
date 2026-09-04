-- =====================================================
-- M017 ROLLBACK: narrow chk_h_speciality_edu_type back to the V018 pair ('11','12')
-- =====================================================
-- Liquibase unwinds in EXECUTION order, and S042 (the 69 ordinatura rows) runs immediately after
-- M017, so it unwinds immediately before this file and the table should already be free of
-- education_type = '13'. Should — not must: a curator may have created an ordinatura speciality
-- through the UI after the seed, and S042's rollback deliberately leaves those alone (it deletes by
-- explicit id). Re-adding the narrow CHECK would then fail 23514 with Postgres naming nothing but
-- the constraint, which is a confusing way to learn that real data is in the way.
--
-- So: count the survivors first and RAISE EXCEPTION with their codes. That turns an opaque
-- constraint violation into a sentence an operator can act on, and it refuses to be the step that
-- decides ministry data should disappear — deleting a curated speciality is a human decision.
-- =====================================================

DO $$
DECLARE
    leftover INTEGER;
    sample   TEXT;
BEGIN
    SELECT count(*) INTO leftover FROM h_speciality WHERE education_type = '13';
    IF leftover > 0 THEN
        SELECT string_agg(code || ' ' || name_uz, ', ' ORDER BY code) INTO sample
          FROM (SELECT code, name_uz FROM h_speciality WHERE education_type = '13' ORDER BY code LIMIT 5) t;
        RAISE EXCEPTION 'M017 rollback to''xtatildi: % ta ordinatura mutaxassisligi hali h_speciality''da (masalan: %). Avval S042 rollback''ini qo''llang yoki bu qatorlarni qo''lda ko''rib chiqing.',
            leftover, sample;
    END IF;
END $$;

SET LOCAL lock_timeout = '3s';

ALTER TABLE h_speciality DROP CONSTRAINT IF EXISTS chk_h_speciality_edu_type;

ALTER TABLE h_speciality
    ADD CONSTRAINT chk_h_speciality_edu_type
    CHECK (education_type IN ('11', '12'));

COMMENT ON CONSTRAINT chk_h_speciality_edu_type ON h_speciality IS
    'Education types this classifier admits: 11=Bakalavr, 12=Magistr.';
