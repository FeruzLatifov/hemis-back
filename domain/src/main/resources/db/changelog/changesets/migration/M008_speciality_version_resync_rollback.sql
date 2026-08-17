-- =====================================================
-- M008 ROLLBACK: h_speciality.version -> 1 (schema default)
-- =====================================================
-- DIQQAT: asl per-qator versiyalar (v1/v2/...) QAYTARIB BO'LMAYDI — ular -1 bilan ustiga yozildi.
-- Bu rollback faqat ustunni sog'lom, musbat default holatga (1) qaytaradi, shunda SUM(version)
-- manfiy bo'lib qolmaydi. Aniq oldingi qiymatlar kerak bo'lsa — S014/S017 seed'dan qayta tiklang.
-- =====================================================

UPDATE h_speciality
   SET version    = 1,
       updated_at = CURRENT_TIMESTAMP,
       updated_by = 'system';
