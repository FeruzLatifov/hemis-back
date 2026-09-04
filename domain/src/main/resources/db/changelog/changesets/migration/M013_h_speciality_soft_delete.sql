-- =====================================================
-- M013: h_speciality — SOFT DELETE (deleted_at/deleted_by)
--        + uq_h_speciality_identity: to'liq UNIQUE -> PARTIAL UNIQUE
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-25
--
-- MUAMMO:
--   HSpecialityService.delete() qatorni JISMONAN o'chirardi (repository.delete).
--   Klassifikator qatori esa hech qachon yo'qolmasligi kerak: uni 224 OTM ham,
--   eski talaba-saqlash jadvallari ham UUID bo'yicha ko'rsatadi.
--
-- YECHIM:
--   deleted_at/deleted_by (AuditableEntity nomlanishi) + entity'da
--   @SQLRestriction("deleted_at IS NULL"). Qator qoladi, hamma joydan yo'qoladi.
--
-- NEGA IDENTITY UNIQUE PARTIAL BO'LISHI SHART (M012 bilan AYNI sabab):
--   uq_h_speciality_identity butun jadval bo'yicha. Soft-delete qilingan qator
--   (education_type, code, name_search) ni ABADIY band qilib turadi, name_search esa
--   GENERATED (V018:95) — bo'shatib bo'lmaydi. "Xato o'chirdim, qaytadan qo'shaman"
--   degan birinchi harakat tushunarsiz 500 (23505) berardi. M012 da oauth_client.client_id
--   AYNAN shu sababdan prodda qo'lda boshqa nomga o'tkazilgan edi.
--
-- NEGA preCondition YO'Q (M011/M012 shakli):
--   Bu fayl allaqachon to'liq idempotent (ADD COLUMN IF NOT EXISTS x2,
--   DROP CONSTRAINT IF EXISTS, CREATE UNIQUE INDEX IF NOT EXISTS). columnExists
--   preCondition + onFail MARK_RAN esa IKKALA yarmini birga gate qiladi: deleted_at
--   qandaydir sababdan mavjud bo'lsa (qo'lda qo'shilgan ustun, RAISE bilan to'xtagan
--   rollback, qayta yaratilgan muhit) butun changeset JIMGINA "ran" deb belgilanadi va
--   uq_h_speciality_identity TO'LIQ UNIQUE bo'lib qolardi — ya'ni M012 hodisasi qaytadi,
--   lekin bu safar Liquibase "muvaffaqiyat" deb hisobot beradi. Ustiga-ustak, Liquibase
--   MARK_RAN changeset'ni ham rollback qiladi va ADD CONSTRAINT 42P07 bilan yiqilardi.
--
-- NEGA pg_constraint QIDIRUVI KERAK EMAS (M012 dan farq):
--   V018 konstreyntni ANIQ nomlagan (uq_h_speciality_identity, V018:125-126);
--   M012 da esa V006 inline UNIQUE ishlatgani uchun nomni PostgreSQL bergan edi.
--
-- NULLS NOT DISTINCT SAQLANADI:
--   code NULLABLE va ~15 ta kodsiz NEEDS_REVIEW qator (education_type, name) noyobligiga
--   AYNAN shu clause orqali bo'ysunadi (V018:84, :123-124). Uni yangi indeksda yozmaslik —
--   xato emas, JIMGINA integrity regressiyasi.
--
-- ON CONFLICT INFERENCE (M001/M012 sinfidagi tuzoq) — bu yerda YO'Q:
--   Partial indeksni predikatsiz `ON CONFLICT (education_type, code, name_search)` TOPMAYDI.
--   Tekshirildi: S014/S017 `ON CONFLICT (id)`, S015/S017 `ON CONFLICT (speciality_id, year)` —
--   hech biri identity kalitiga inference qilmaydi. Kelajakda identity bo'yicha ON CONFLICT
--   yozilsa `WHERE deleted_at IS NULL` predikatini ANIQ ko'rsatish SHART.
--
-- M011 BILAN ZIDDIYAT EMAS (batafsil: ADR-0014, M013 bandi):
--   M011 university_speciality_attachment dan soft delete ni OLIB TASHLAGAN — biriktirma
--   bir bosishda qayta yaratiladi va yashirin qator o'chirish guard'ini bloklab turardi.
--   Mutaxassislik boshqa: unga 224 OTM va eski talaba qatorlari UUID bilan bog'langan.
--
-- DIQQAT — GUARD'LARDA DB TAYANCHI QOLMADI:
--   fk_h_speciality_parent va fk_univ_spec_attach_spec (ikkalasi ON DELETE RESTRICT)
--   UPDATE'ga qarshi ISHLAMAYDI. Soft delete — non-key UPDATE (FOR NO KEY UPDATE), u
--   bola/biriktirma INSERT'i oladigan FOR KEY SHARE bilan TO'QNASHMAYDI. Ya'ni bir vaqtda
--   "A o'chiryapti / B bola qo'shyapti" holati endi DB darajasida seriyalanmaydi.
--   Qoldiq ATAYLAB qabul qilindi (kodbazada 0 ta pessimistic lock bor; biriktirma uchun
--   3 aktyorli poyga kerak, chunki biriktirish APPROVED, o'chirish esa NEEDS_REVIEW talab
--   qiladi) — ketma-ket (poygasiz) yo'l SPECIALITY_RESTORE_PARENT_DELETED bilan yopilgan.
--
-- LOCK: ALTER TABLE ACCESS EXCLUSIVE oladi; navbatdagi ACCESS EXCLUSIVE undan keyingi
--   HAR BIR SELECT ni ham bloklaydi (klassifikator pull = 224 OTM). SET LOCAL (SET emas —
--   PgBouncer pool sabog'i) lock_timeout bilan tez yiqilamiz.
--
-- CONCURRENTLY EMAS: h_speciality ~5.4k qator (S014). CONCURRENTLY mandati 1M+ hemishe_*
--   jadvallariga tegishli; bu yerda u DROP va CREATE ni ikki tranzaksiyaga ajratib,
--   unique UMUMAN amal qilmaydigan oyna ochib qo'yardi.
--
-- INDEKSLAR: V018 ning 5 ta mavjud indeksi ATAYLAB to'liq (partial qilinmadi) — ~5.4k
--   qatorda `WHERE deleted_at IS NULL` ~100% qatorga mos keladi, planner undan foyda
--   ko'rmaydi. Yangi idx_h_speciality_deleted esa TESKARI yo'nalishda (IS NOT NULL) —
--   kichik tomon, listDeleted() aynan shuni skanerlaydi (uy qoidasi: V001:33, V002:38).
-- =====================================================

SET LOCAL lock_timeout = '3s';

-- 1. Soft-delete ustunlari (AuditableEntity: deleted_at / deleted_by).
ALTER TABLE h_speciality ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE h_speciality ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(50);

COMMENT ON COLUMN h_speciality.deleted_at IS
    'Soft delete: NULL = tirik. Entity''da @SQLRestriction(''deleted_at IS NULL'') barcha JPQL o''qishlarini filtrlaydi. Native SQL FILTRLANMAYDI — qo''lda predikat qo''ying (M013)';
COMMENT ON COLUMN h_speciality.deleted_by IS
    'Kim o''chirdi (username). softDelete() uni o''rnatmaydi — service qatlami yozadi (M013)';

-- 2. O'chirilganlar ro'yxati uchun indeks (kichik tomon: IS NOT NULL).
CREATE INDEX IF NOT EXISTS idx_h_speciality_deleted
    ON h_speciality (deleted_at)
 WHERE deleted_at IS NOT NULL;

-- 3. Identity UNIQUE -> faqat TIRIK qatorlar orasidagi PARTIAL unique.
DO $$
DECLARE
    v_dupes integer;
BEGIN
    SELECT count(*) INTO v_dupes
      FROM (SELECT education_type, code, name_search
              FROM h_speciality
             WHERE deleted_at IS NULL
             GROUP BY education_type, code, name_search
            HAVING count(*) > 1) d;

    IF v_dupes > 0 THEN
        RAISE EXCEPTION 'M013 TO''XTATILDI: h_speciality da % ta takrorlanuvchi TIRIK (education_type, code, name_search) bor — avval konsolidatsiya qiling', v_dupes;
    END IF;
END $$;

ALTER TABLE h_speciality DROP CONSTRAINT IF EXISTS uq_h_speciality_identity;

CREATE UNIQUE INDEX IF NOT EXISTS uq_h_speciality_identity_live
    ON h_speciality (education_type, code, name_search)
    NULLS NOT DISTINCT
 WHERE deleted_at IS NULL;

COMMENT ON INDEX uq_h_speciality_identity_live IS
    '(education_type, code, name_search) faqat TIRIK qatorlar orasida noyob — soft-delete qilingan mutaxassislikni ayni nom/kod bilan qayta yaratish mumkin (M013). NULLS NOT DISTINCT: ~15 kodsiz NEEDS_REVIEW qator (education_type, name) bo''yicha noyob qoladi.';
