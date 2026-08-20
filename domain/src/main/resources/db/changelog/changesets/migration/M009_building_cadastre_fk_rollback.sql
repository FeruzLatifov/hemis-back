-- Rollback M009: FK olib tashlash + cadastre JSONB ustunini tiklash.
-- ⚠️ MA'LUMOT: qaytarilgan `cadastre` ustuni BO'SH bo'ladi — M009 eski snapshot'ni
--    university_cadastre.raw'ga ko'chirgan, teskari ko'chirish avtomatik EMAS (bir tomonlama).
--    Prod'da rollback zarur bo'lsa, kerakli JSON'ni university_cadastre.raw'dan qo'lда tiklang.
-- M009 backfill'da yaratilgan PENDING/COMPLETE kadastr qatorlari qoldiriladi (zararsiz).

ALTER TABLE university_building DROP CONSTRAINT IF EXISTS fk_ub_cadastre;

ALTER TABLE university_building ADD COLUMN IF NOT EXISTS cadastre JSONB;
