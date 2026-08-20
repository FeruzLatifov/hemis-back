-- M009: university_building.cadastre JSONB duplikatini olib tashlash + cad_number FK.
-- Sabab: kadastr fakti endi FAQAT university_cadastre'da (V025) — cad_number bo'yicha BIR MARTA.
--        university_building.cadastre JSONB (V010'dan, university_cadastre'dan OLDIN) — takror.
-- Model: bino cad_number bilan university_cadastre'ga bog'lanadi (FK), fakt nusxasi saqlanmaydi.
-- V010 prod-live → in-place edit YO'Q, alohida additive M-migratsiya.

-- 1) Bo'sh string cad_number → NULL. FK faqat NULL'ni istisno qiladi, "" ni EMAS — V010 col 13
--    Excel bulk-import bo'sh katakni '' qilib qo'yishi mumkin; aks holda (4) ADD CONSTRAINT '' da yiqiladi.
UPDATE university_building SET cad_number = NULL WHERE cad_number = '';

-- 2) FK darrov bajarilishi uchun mavjud bino cad_number'lariga university_cadastre qatori yaratamiz.
--    MA'LUMOT SAQLASH: bino eski cadastre JSONB snapshot'i bo'lsa — uni `raw`ga ko'chirib COMPLETE
--    qilamiz ((3) DROP COLUMN'da yo'qolmasin); aks holda PENDING placeholder (retry scheduler to'ldiradi).
--    DISTINCT ON — bir cad_number ko'p binoда bo'lsa, snapshot'li qatorni afzal ko'radi.
INSERT INTO university_cadastre (cad_number, fetch_status, raw, fetch_error)
SELECT DISTINCT ON (b.cad_number)
       b.cad_number,
       CASE WHEN b.cadastre IS NOT NULL THEN 'COMPLETE' ELSE 'PENDING' END,
       b.cadastre,
       CASE WHEN b.cadastre IS NOT NULL THEN NULL ELSE 'auto-created for building FK (M009 backfill)' END
FROM university_building b
WHERE b.cad_number IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM university_cadastre c WHERE c.cad_number = b.cad_number)
ORDER BY b.cad_number, (b.cadastre IS NOT NULL) DESC
ON CONFLICT (cad_number) DO NOTHING;

-- 3) Redundant kadastr snapshot ustunini o'chirish (endi university_cadastre.raw'da saqlangan).
ALTER TABLE university_building DROP COLUMN IF EXISTS cadastre;

-- 4) FK: university_building.cad_number -> university_cadastre.cad_number (idempotent — re-run xavfsiz).
--    Nullable (bino cad_number'siz ham yashaydi); kadastr o'chsa cad_number NULL bo'ladi.
ALTER TABLE university_building DROP CONSTRAINT IF EXISTS fk_ub_cadastre;
ALTER TABLE university_building
    ADD CONSTRAINT fk_ub_cadastre FOREIGN KEY (cad_number)
    REFERENCES university_cadastre(cad_number) ON DELETE SET NULL;
