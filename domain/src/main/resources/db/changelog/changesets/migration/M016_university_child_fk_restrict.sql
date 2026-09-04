-- =====================================================
-- M016: hemishe_e_university child FKs — ON DELETE CASCADE -> RESTRICT
-- =====================================================
-- Author: hemis-team
-- Date: 2026-09-04
--
-- MUAMMO:
--   "Universitet hech qachon JISMONAN o'chirilmaydi" qoidasi shu paytgacha faqat
--   niyat edi. hemishe_e_university ga 95 ta FK ishora qiladi:
--     * NO ACTION 85 (eski CUBA) + RESTRICT 6 (yangi schema) = DELETE ni BLOKLAYDI
--     * SET NULL  2  -> users.university_id, university_lifecycle.successor_code
--     * CASCADE   2  -> university_founder, university_profile
--   Ya'ni bolasi bo'lgan OTM bugun ham o'chmaydi (273 OTM dan 0 tasi bolasiz), lekin
--   YANGI yaratilgan, hali hech qayerda ishlatilmagan OTM uchun oyna ochiq qoladi:
--   sun'iy sinovda bolasi faqat university_profile + 1 users bo'lgan qatorga
--   `DELETE FROM hemishe_e_university WHERE code = ...` -> DELETE 1 o'tib ketdi,
--   profil JIMGINA yo'q bo'ldi, foydalanuvchining university_id NULL bo'ldi.
--
-- YECHIM (CHUQURLIKDA HIMOYA — defence in depth):
--   Hard delete endi KOD darajasida taqiqlangan: UniversityRepository dagi 11 ta
--   meros olingan hard-delete metodi (CrudRepository 5 + JpaRepository 4 +
--   JpaSpecificationExecutor ning 2 ta bulk delete(...Specification) overload'i)
--   UnsupportedOperationException tashlaydi. Lekin bu faqat ILOVA yo'lini yopadi.
--   Qo'lda psql, kelajakdagi native @Query, yoki ETL skripti uchun bu ikki teshik
--   ochiq turishi kerak emas — DB o'zi ham "yo'q" desin.
--   Yagona to'g'ri o'chirish yo'li: UniversityRegistryService.deleteUniversity(code)
--   (delete_ts stamp) + restoreUniversity(code).
--
-- NEGA IKKI SET NULL GA TEGILMAYDI (ataylab):
--   * users.university_id -> SET NULL TO'G'RI xulq. OTM qatori yo'qolsa ham
--     foydalanuvchi o'chmasligi kerak; uni RESTRICT qilish "OTM ni o'chirish uchun
--     avval xodimlarni o'chir" degan xavfli yo'lni ochadi.
--   * university_lifecycle.successor_code -> vorislik zanjiri (qaysi OTM qaysi OTM ga
--     qo'shildi). Zanjir UZILISHI kerak, BLOKLAMASLIGI: yo'qolgan vorisga ishora
--     qilgan tarixiy qator saqlanib, successor_code NULL bo'lgani ma'noli.
--   Ikkalasi ham "jimgina ma'lumot yo'qotish" emas, ataylab tanlangan semantika.
--
-- NEGA pg_constraint QIDIRUVI KERAK (M012 bilan AYNI sabab, M013 dan farq):
--   V005:65 va V008:14 FK ni INLINE va NOMSIZ e'lon qilgan
--   (`... REFERENCES hemishe_e_university(code) ON DELETE CASCADE`), ya'ni nomni
--   PostgreSQL bergan. M013 da konstreynt V018 da ANIQ nomlangani uchun qidiruv
--   kerak emas edi; bu yerda esa generatsiya qilingan nomga ko'r-ko'rona
--   tayanmasdan, (jadval, university_code -> hemishe_e_university) juftligiga mos
--   HAR QANDAY FK topib o'chiriladi va kanonik nom bilan qayta qo'yiladi.
--
-- IDEMPOTENT: mos FK topilsa o'chiriladi (nomi qanday bo'lishidan qat'i nazar),
--   keyin kanonik nom bilan RESTRICT qilib qo'shiladi. Qayta ishga tushirish
--   xavfsiz va natija bir xil (konvergent).
--
-- YETIM QATOR YO'Q: university_founder 0, university_profile 0 (test2_hemis) —
--   CASCADE FK allaqachon kuchda bo'lgani uchun yetim qator PAYDO BO'LA OLMAYDI,
--   shuning uchun ADD CONSTRAINT validatsiyasi toza o'tadi.
--
-- CONCURRENTLY EMAS: ikkala jadval ham kichik; DROP+ADD bitta tranzaksiyada,
--   ACCESS EXCLUSIVE lock qisqa.
--
-- LOCK: FK ni qayta qo'yish IKKALA tomonda ham lock oladi — bola jadvalda
--   ACCESS EXCLUSIVE, OTA jadval `hemishe_e_university` da esa
--   SHARE ROW EXCLUSIVE. Ota jadval — 224 OTM ning har bir so'rovi tegadigan
--   registr, ya'ni lock_timeout'siz migratsiya uzun o'quvchi ortida NAVBATGA
--   turadi va o'zi ham butun navbatni bloklaydi. `lock_timeout` bilan u tez
--   yiqiladi va deploy qayta uriniladi — trafikni ushlab turmaydi.
--   (M013 va M015 da ham aynan shu qator bor.)
-- =====================================================

SET LOCAL lock_timeout = '3s';

DO $$
DECLARE
    tgt      RECORD;
    existing RECORD;
    parent   OID := to_regclass('public.hemishe_e_university');
BEGIN
    IF parent IS NULL THEN
        RAISE EXCEPTION 'M016: public.hemishe_e_university topilmadi';
    END IF;

    FOR tgt IN
        SELECT *
        FROM (VALUES
            ('university_founder', 'university_founder_university_code_fkey'),
            ('university_profile', 'university_profile_university_code_fkey')
        ) AS t(child_table, canonical_name)
    LOOP
        IF to_regclass('public.' || tgt.child_table) IS NULL THEN
            RAISE EXCEPTION 'M016: public.% topilmadi', tgt.child_table;
        END IF;

        -- (child_table.university_code -> hemishe_e_university) ga mos HAR QANDAY FK,
        -- nomi qanday bo'lishidan qat'i nazar.
        FOR existing IN
            SELECT c.conname, c.confdeltype
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
            RAISE NOTICE 'M016: % o''chirildi (%.% edi: confdeltype=%)',
                         existing.conname, 'public', tgt.child_table, existing.confdeltype;
        END LOOP;

        EXECUTE format(
            'ALTER TABLE public.%I ADD CONSTRAINT %I '
            'FOREIGN KEY (university_code) REFERENCES public.hemishe_e_university(code) '
            'ON DELETE RESTRICT',
            tgt.child_table, tgt.canonical_name);

        RAISE NOTICE 'M016: % -> ON DELETE RESTRICT', tgt.canonical_name;
    END LOOP;
END $$;

COMMENT ON CONSTRAINT university_founder_university_code_fkey ON university_founder IS
    'ON DELETE RESTRICT (M016) — universitet faqat SOFT delete qilinadi (delete_ts). CASCADE edi: yangi OTM jimgina ta''sischilari bilan birga yo''q bo''lardi.';
COMMENT ON CONSTRAINT university_profile_university_code_fkey ON university_profile IS
    'ON DELETE RESTRICT (M016) — universitet faqat SOFT delete qilinadi (delete_ts). CASCADE edi: yangi OTM jimgina profili bilan birga yo''q bo''lardi.';
