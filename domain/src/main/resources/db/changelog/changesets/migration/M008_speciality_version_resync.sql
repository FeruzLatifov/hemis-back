-- =====================================================
-- M008: FORCE UNIVER RE-SYNC — h_speciality.version = -1
-- =====================================================
-- Author: hemis-team
-- Purpose: Consuming OTM (Univer) tomonining so'roviga ko'ra — yagona mutaxassislik
--          klassifikatorining (h_speciality) BARCHA qatorlariga version = -1 qo'yiladi.
--          Maqsad: Univer versiya-asosli cache tekshiruvida markaziy versiyani o'zi saqlagan
--          qiymatdan FARQLI ko'rib, klassifikatorni to'liq qayta yuklab olsin (majburiy resync).
--
-- MUHIM (programmer bilan kelishilgan xulq):
--   * version = @Version (optimistic lock). Keyingi admin tahririda Hibernate -1 -> 0 qiladi.
--   * Klassifikator-darajali versiya = SUM(version); bu update'dan keyin u MANFIY bo'ladi
--     (kutilgan — ataylab "farqli" signal, doim uchraydigan qiymat emas).
--   * Univer buni faqat '!=' solishtiruvda resync sifatida qabul qiladi ('>' bilan YO'Q).
--   * Hozirgi jonli Univer kodi versiyani '|| true' bilan e'tiborsiz qoldiradi — signal Univer
--     versiyani honor qiladigan yangi kod chiqqandagina amal qiladi.
--   * specialityDistribution keshi (Redis L2, 24h) — signal darhol ko'rinishi uchun evict
--     qilinishi kerak (yangi pod L1 keshi bo'sh, lekin L2 saqlanadi).
--
-- Idempotent: sobit qiymatga UPDATE (takror ishlatilsa ham -1 qoladi).
-- Tartib: MIGRATION fazasi SEED'dan keyin ishlaydi -> S014/S017 h_speciality'ni seed qilgandan
--         so'ng -1 qo'yiladi (fresh rebuild'da ham to'g'ri).
-- runOnChange: true -> qiymat keyin o'zgartirilsa migration qayta qo'llanadi (iteratsiya uchun).
-- =====================================================

UPDATE h_speciality
   SET version    = -1,
       updated_at = CURRENT_TIMESTAMP,
       updated_by = 'system';
