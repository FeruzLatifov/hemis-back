-- =====================================================
-- M012: oauth_client.client_id — UNIQUE -> PARTIAL UNIQUE (deleted_at IS NULL)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-21
--
-- MUAMMO:
--   V006'da `client_id VARCHAR(100) NOT NULL UNIQUE` — to'liq jadval bo'yicha unique.
--   O'chirish esa SOFT delete (OAuthClientAdminService.softDelete -> deleted_at + is_active=false),
--   entity'da `@SQLRestriction("deleted_at IS NULL")`. Natijada o'chirilgan qator `client_id` ni
--   ABADIY band qilib turadi: `existsByClientId` uni ko'rmaydi (restriction), shuning uchun
--   createClient() tekshiruvdan o'tadi va INSERT DB darajasida unique violation bilan yiqiladi —
--   foydalanuvchiga tushunarsiz 500. Prodda buni chetlab o'tish uchun eski hisobning client_id'si
--   qo'lda `jidu(yopilgan)` ga o'zgartirilgan (2026-08-21 holatida ro'yxatda ko'rinadi).
--
-- YECHIM:
--   Unique faqat TIRIK qatorlar orasida amal qilsin. Shunda o'chirilgan hisobning nomi qayta
--   ishlatiladi va `existsByClientId` (restriction bilan) DB qoidasi bilan AYNI narsani anglatadi.
--
-- XAVFSIZLIK:
--   Konstreynt nomi qattiq yozilmagan — pg_constraint'dan topiladi (V006 uni PostgreSQL'ga
--   nomlatgan, odatda oauth_client_client_id_key, lekin muhitlar orasida farq qilishi mumkin).
--   Avval tirik qatorlar orasida dublikat yo'qligi tekshiriladi: bo'lsa RAISE EXCEPTION —
--   migratsiya to'xtaydi, jimgina noto'g'ri holat yaratilmaydi.
--
-- ⚠️ M001 BILAN BOG'LIQLIK (kelajakda tuzoq):
--   M001_migrate_old_hemis_users:195 da `ON CONFLICT (client_id) DO NOTHING` bor va u changeset
--   `runOnChange: true`. PostgreSQL ustun ko'rsatilgan ON CONFLICT uchun MOS unique indeks talab
--   qiladi; PARTIAL indeks esa predikatsiz inference bilan TOPILMAYDI. Ya'ni M001 shu migratsiyadan
--   keyin qayta ishga tushsa, "no unique or exclusion constraint matching the ON CONFLICT
--   specification" bilan yiqiladi va butun deploy to'xtaydi.
--
--   M001 ATAYLAB tegilmadi: uni tahrirlash checksum'ni o'zgartiradi va prodda qayta ishga tushiradi,
--   u esa `users` jadvalidagi password/email/enabled ustunlarini `sec_user` dan QAYTA YOZADI
--   (2026-08-21 o'lchovi: 345 qatordan 3 tasining paroli farq qilardi). Feature uchun kerak
--   bo'lmagan bu xavfni deployga qo'shmadik.
--
--   TUZOQ QACHON ISHGA TUSHADI (2026-08-22 da lokal bazada AMALDA kuzatildi):
--     M001 `runOnChange: true` — u faqat CHECKSUM o'zgarganda qayta ishlaydi. Ya'ni xavf
--     "M001 tahrirlansa" degani bilan CHEKLANMAYDI. Checksum qayta hisoblanadigan HAR QANDAY
--     holat tuzoqni ochadi:
--       · M001 fayli tahrirlansa (hatto bitta izoh belgisi ham),
--       · `liquibase clearCheckSums` ishlatilsa,
--       · Liquibase checksum algoritmi versiyasi ko'tarilsa (md5sum oldidagi `9:` prefiksi).
--     Shunda M001 qayta ishlaydi va 195-qator ANIQ shu xato bilan yiqiladi:
--       "there is no unique or exclusion constraint matching the ON CONFLICT specification"
--
--   XAVFSIZLIK JIHATI: bu SOKIT emas — deploy BALAND OVOZ bilan to'xtaydi, migratsiya Job'i
--     Failed bo'ladi va prod eski image'da qoladi. Ma'lumot yo'qolmaydi, lekin deploy bloklanadi.
--
--   AGAR kimdir kelajakda M001'ni tahrirlasa — 195-qatorni shunga o'zgartirish YETARLI:
--       ON CONFLICT DO NOTHING;                              -- ustunsiz (har qanday konstreynt bilan ishlaydi)
--   yoki predikatni aniq ko'rsatish:
--       ON CONFLICT (client_id) WHERE deleted_at IS NULL DO NOTHING;
--
--
-- ⏱️ LOCK KUTISH CHEGARASI:
--   `ALTER TABLE ... DROP CONSTRAINT` ACCESS EXCLUSIVE lock oladi. PostgreSQL'da navbatda
--   turgan ACCESS EXCLUSIVE o'zidan KEYINGI barcha SELECT'larni ham bloklaydi — ya'ni
--   oauth_client'dan o'qiydigan HAR BIR machine-token so'rovi (224 OTM) to'xtaydi.
--   Yagona amaldagi chegara `statement_timeout=60000` edi (application-prod.yml) → eng yomon
--   holatda 60 sekundlik integratsiya uzilishi. `SET LOCAL lock_timeout` uni 3 sekundga
--   qisqartiradi: lock tez bo'shamasa migratsiya yiqiladi va deploy bekor bo'ladi —
--   224 OTM ni bloklab turgandan ko'ra shu afzal. SET LOCAL (SET emas) — tranzaksiya bilan
--   tugaydi, poolga sizib qolmaydi (PgBouncer search_path sabog'i).
--
-- 📋 AUDIT IZI (M012 dan keyingi xulq o'zgarishi):
--   Eski JWT yangi hisobga TEGISHLI BO'LMAYDI — token `sub` = oauth_client.id (UUID),
--   rollar tokenda muzlatilgan, `oauth_client_role` ham UUID FK. Imtiyoz merosi YO'Q.
--   LEKIN audit atributsiyasi UUID bo'yicha emas: AuditAspect CLIENT tokenlar uchun
--   user_id ni NULL qoldiradi va faqat username = client_id SATRINI yozadi. Ya'ni
--   o'chirilgan `otm301` va qayta yaratilgan `otm301` audit jurnalida ajralmaydi.
--   Nomni qayta ishlatishdan oldin shuni hisobga oling (eski token 24 soatgacha amal qiladi).
--
-- Eslatma: `idx_oauth_client_client_id` (oddiy indeks) ataylab qoldirildi — u deleted_at'siz
--   qidiruvlar uchun (masalan admin hisoboti). Yangi partial unique indeks tirik qatorlar
--   bo'yicha lookup'larni allaqachon qoplaydi.
-- =====================================================

-- Lock navbatida uzoq turmaymiz: 224 OTM ning token olishini bloklagandan ko'ra deploy yiqilsin.
SET LOCAL lock_timeout = '3s';

DO $$
DECLARE
    v_constraint text;
    v_dupes      integer;
    v_attnum     smallint;
BEGIN
    -- 1. Guard: tirik qatorlar orasida dublikat bo'lmasin (bo'lsa partial unique yaratilmaydi).
    SELECT count(*) INTO v_dupes
      FROM (SELECT client_id
              FROM oauth_client
             WHERE deleted_at IS NULL
             GROUP BY client_id
            HAVING count(*) > 1) d;

    IF v_dupes > 0 THEN
        RAISE EXCEPTION 'M012 TO''XTATILDI: oauth_client da % ta takrorlanuvchi tirik client_id bor — avval tozalang', v_dupes;
    END IF;

    -- 2. client_id ustunidagi mavjud UNIQUE konstreyntni topib tushiramiz.
    SELECT a.attnum INTO v_attnum
      FROM pg_attribute a
      JOIN pg_class c ON c.oid = a.attrelid
     WHERE c.relname = 'oauth_client' AND a.attname = 'client_id' AND a.attnum > 0;

    SELECT con.conname INTO v_constraint
      FROM pg_constraint con
      JOIN pg_class rel ON rel.oid = con.conrelid
     WHERE rel.relname = 'oauth_client'
       AND con.contype = 'u'
       AND con.conkey = ARRAY[v_attnum]::smallint[];

    IF v_constraint IS NOT NULL THEN
        EXECUTE format('ALTER TABLE oauth_client DROP CONSTRAINT %I', v_constraint);
        RAISE NOTICE 'M012: to''liq unique konstreynt tushirildi: %', v_constraint;
    ELSE
        RAISE NOTICE 'M012: client_id da to''liq unique konstreynt topilmadi (allaqachon tushirilgan bo''lishi mumkin)';
    END IF;
END $$;

-- 3. Unique endi faqat tirik qatorlar orasida.
CREATE UNIQUE INDEX IF NOT EXISTS uq_oauth_client_client_id_live
    ON oauth_client (client_id)
 WHERE deleted_at IS NULL;

COMMENT ON INDEX uq_oauth_client_client_id_live IS
    'client_id faqat TIRIK qatorlar orasida noyob — soft-delete qilingan hisobning nomi qayta ishlatilishi mumkin (M012)';
