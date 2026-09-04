-- =====================================================
-- S037: SEED TRANSLATIONS — PERSON akkaunt logini endi PINFL EMAS, ism-familiya slug'i
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-26
-- Purpose:
--   PERSON akkauntda login 14 xonali PINFL bo'lishdan to'xtadi va ism + familiyadan hosil
--   qilinadigan lotin slug'ga aylandi (ISM FAMILIYA -> ism_familiya). Foydalanuvchi
--   yaratish formasi uchun 5 ta YANGI kalit:
--     • "Account credentials" — login/parol bloki sarlavhasi (avval PINFL bloki ichida edi).
--     • "The login is generated from the first and last name." — login maydoni ostidagi izoh.
--     • "First name and last name are required" — backend 400 xabari (LoginNameGenerator);
--       slug 3 belgidan qisqa bo'lganda, ya'ni ikkala ism ham bo'sh/yaroqsiz bo'lsa.
--     • "Regenerate login" — operator ismni o'zgartirgach loginni qayta taklif qilish tugmasi.
--     • "Ministry/university staff. The login is generated from the name; …" — PERSON akkaunt
--       turi kartochkasining tavsifi.
--   Jami: 5 ta YANGI kalit.
--
--   S010'dagi ikki kalit endi FAKT jihatdan noto'g'ri:
--     "The login is automatically set to the PINFL."
--     "Ministry/university staff. Login is the PINFL; details are fetched from the passport service."
--   Ular S010'da TEGILMAY qoladi (S010 qo'llangan va muzlatilgan — qo'llangan changeset
--   tahrirlanmaydi); frontend shunchaki ularga murojaat qilishni to'xtatadi.
--
--   Login, Password, Confirm password, Passwords do not match, Username already exists,
--   "Username must be 3-50 characters, …", Show/Hide password, Display name — allaqachon
--   seed qilingan, QAYTA yaratilmaydi (variant kalit yasash = ikki xil tarjima).
--
--   YANGI seed, chunki S006/S010/S032..S036 prodda (central_hemis) allaqachon qo'llangan.
--   system_message — yagona haqiqat manbai; `sync:translations` frontend JSON'larini
--   (en/oz/ru/uz) shu yerdan qayta yozadi, ya'ni seedsiz kalitlar keyingi sinxronda
--   JIMGINA yo'qoladi.
-- Pattern: S036 (5 argumentli _seed_msg helper — S006 da aniqlangan; en-US = kalitning o'zi).
-- Safety: _seed_msg ichida ON CONFLICT (message_key) DO UPDATE — idempotent.
-- =====================================================

DO $$
BEGIN

PERFORM _seed_msg('label',      'Account credentials', 'Hisob ma''lumotlari', 'Ҳисоб маълумотлари', 'Учётные данные');
PERFORM _seed_msg('label',      'The login is generated from the first and last name.', 'Login ism va familiyadan hosil qilinadi.', 'Логин исм ва фамилиядан ҳосил қилинади.', 'Логин формируется из имени и фамилии.');
PERFORM _seed_msg('validation', 'First name and last name are required', 'Ism va familiya majburiy', 'Исм ва фамилия мажбурий', 'Имя и фамилия обязательны');
PERFORM _seed_msg('label',      'Regenerate login', 'Loginni qayta hosil qilish', 'Логинни қайта ҳосил қилиш', 'Сформировать логин заново');
PERFORM _seed_msg('label',      'Ministry/university staff. The login is generated from the name; details are fetched from the passport service.', 'Vazirlik/universitet xodimi. Login ism-familiyadan hosil qilinadi; ma''lumotlar pasport xizmatidan olinadi.', 'Вазирлик/университет ходими. Логин исм-фамилиядан ҳосил қилинади; маълумотлар паспорт хизматидан олинади.', 'Сотрудник министерства/вуза. Логин формируется из имени и фамилии; данные загружаются из паспортной службы.');

END $$;
