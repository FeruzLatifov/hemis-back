-- =====================================================
-- M013 ROLLBACK: partial unique -> to'liq UNIQUE + soft-delete ustunlarini DROP
-- =====================================================
-- DIQQAT 1 — TIRILTIRISH: deleted_at tushirilgach "o'chirilgan" belgisi yo'qoladi va
--   soft-delete qilingan qatorlar yana ro'yxatlarda, daraxtda va OTM distribution'da
--   PAYDO BO'LADI. Ma'lumot yo'qolmaydi, lekin admin o'chirgan qator qaytadi.
-- DIQQAT 2 — ATAYLAB YIQILISHI MUMKIN: M013 dan keyin o'chirilgan qatorning identity'si
--   qayta ishlatilgan bo'lishi mumkin (aynan shu buning maqsadi edi). To'liq UNIQUE'ni
--   tiklash uchun qatorlardan birini YO'Q QILISH kerak — buni jimgina qilmaymiz.
-- DIQQAT 3 — LOCK: bu fayl ACCESS EXCLUSIVE ni UCH marta oladi (ADD CONSTRAINT yangi
--   unique indeks quradi, so'ng ikkita DROP COLUMN). lock_timeout SIZ statement_timeout=60s
--   yagona chegara bo'lib qolardi — ya'ni incident paytidagi rollback 224 OTM klassifikator
--   pull'ini 60 sekundgacha to'xtatib qo'yishi mumkin edi.
-- =====================================================

SET LOCAL lock_timeout = '3s';

DO $$
DECLARE
    v_dupes integer;
    v_row   record;
BEGIN
    SELECT count(*) INTO v_dupes
      FROM (SELECT education_type, code, name_search
              FROM h_speciality
             GROUP BY education_type, code, name_search
            HAVING count(*) > 1) d;

    IF v_dupes > 0 THEN
        RAISE NOTICE 'M013 rollback: % ta takrorlanuvchi identity topildi —', v_dupes;
        FOR v_row IN
            SELECT education_type, code, name_search, count(*) AS n,
                   count(*) FILTER (WHERE deleted_at IS NULL) AS tirik
              FROM h_speciality
             GROUP BY education_type, code, name_search
            HAVING count(*) > 1
             ORDER BY education_type, code
        LOOP
            RAISE NOTICE '   edu=% code=% name_search=% jami=% tirik=%',
                v_row.education_type, v_row.code, v_row.name_search, v_row.n, v_row.tirik;
        END LOOP;
        RAISE EXCEPTION 'M013 rollback TO''XTATILDI: yuqoridagi qatorlar to''liq UNIQUE''ni buzadi. Ortiqcha (odatda soft-delete qilingan) qatorlarni qo''lda hal qiling, so''ng rollbackni takrorlang.';
    END IF;
END $$;

DROP INDEX IF EXISTS uq_h_speciality_identity_live;
DROP INDEX IF EXISTS idx_h_speciality_deleted;

-- IF EXISTS mudofaa uchun: qo'lda tuzatilgan muhitda konstreynt allaqachon turgan bo'lsa
-- 42P07 bermasin.
ALTER TABLE h_speciality DROP CONSTRAINT IF EXISTS uq_h_speciality_identity;
ALTER TABLE h_speciality
    ADD CONSTRAINT uq_h_speciality_identity
        UNIQUE NULLS NOT DISTINCT (education_type, code, name_search);

-- Ustunlar OXIRIDA: DROP COLUMN o'ziga bog'liq indekslarni JIMGINA olib tashlaydi
-- (M011:93-96 sabog'i) — ikkala indeks yuqorida ANIQ DROP qilingan.
ALTER TABLE h_speciality DROP COLUMN IF EXISTS deleted_at;
ALTER TABLE h_speciality DROP COLUMN IF EXISTS deleted_by;
