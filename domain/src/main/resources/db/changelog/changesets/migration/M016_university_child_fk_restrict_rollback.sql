-- =====================================================
-- Rollback M016: university_founder / university_profile FK -> ON DELETE CASCADE
-- =====================================================
-- V005:65 va V008:14 dagi ASLIY holatni tiklaydi.
--
-- ⚠️ OGOHLANTIRISH: rollback'dan keyin `DELETE FROM hemishe_e_university` (qo'lda psql,
--    yoki bu ikki jadvaldan boshqa bolasi yo'q YANGI OTM uchun) yana JIMGINA
--    ta'sischi va profil qatorlarini o'chirib yuboradi. Ilova qatlami himoyasi
--    (UniversityRepository dagi 11 ta hard-delete override) o'z joyida qoladi,
--    lekin DB darajasidagi chuqurlikda himoya yo'qoladi.
--
-- M016 bilan bir xil qidiruv mantig'i: nomga ko'r-ko'rona tayanmaymiz.
-- =====================================================

DO $$
DECLARE
    tgt      RECORD;
    existing RECORD;
    parent   OID := to_regclass('public.hemishe_e_university');
BEGIN
    IF parent IS NULL THEN
        RAISE EXCEPTION 'M016 rollback: public.hemishe_e_university topilmadi';
    END IF;

    FOR tgt IN
        SELECT *
        FROM (VALUES
            ('university_founder', 'university_founder_university_code_fkey'),
            ('university_profile', 'university_profile_university_code_fkey')
        ) AS t(child_table, canonical_name)
    LOOP
        IF to_regclass('public.' || tgt.child_table) IS NULL THEN
            RAISE EXCEPTION 'M016 rollback: public.% topilmadi', tgt.child_table;
        END IF;

        FOR existing IN
            SELECT c.conname
            FROM pg_constraint c
            WHERE c.contype   = 'f'
              AND c.conrelid  = to_regclass('public.' || tgt.child_table)
              AND c.confrelid = parent
              AND (SELECT array_agg(a.attname::text ORDER BY a.attname)
                   FROM unnest(c.conkey) AS k
                   JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = k)
                  = ARRAY['university_code']::text[]
        LOOP
            EXECUTE format('ALTER TABLE public.%I DROP CONSTRAINT %I',
                           tgt.child_table, existing.conname);
        END LOOP;

        EXECUTE format(
            'ALTER TABLE public.%I ADD CONSTRAINT %I '
            'FOREIGN KEY (university_code) REFERENCES public.hemishe_e_university(code) '
            'ON DELETE CASCADE',
            tgt.child_table, tgt.canonical_name);

        RAISE NOTICE 'M016 rollback: % -> ON DELETE CASCADE', tgt.canonical_name;
    END LOOP;
END $$;

COMMENT ON CONSTRAINT university_founder_university_code_fkey ON university_founder IS NULL;
COMMENT ON CONSTRAINT university_profile_university_code_fkey ON university_profile IS NULL;
