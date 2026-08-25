-- =====================================================
-- S034: SEED TRANSLATIONS — create-user form, additional passport-detail labels
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-25
-- Purpose:
--   The "create user" form (Shaxs / PINFL passport autofill) now surfaces the passport-detail
--   fields that the gateway returns but the form previously kept hidden (only carried in the
--   submit payload). Three NEW field labels are shown read-only after a successful lookup:
--     • Birth place          (Tug'ilgan joy)
--     • Passport issue place  (Pasport berilgan joy)
--     • Expiry date           (Amal qilish muddati)
--   "Issued date" already exists (S010) and is reused; "Gender" values now map 1->Male/2->Female
--   on the frontend using existing "Male"/"Female" keys — no new key needed for that.
--
--   NEW seed because S006/S010 are already applied in production (central_hemis) — applied
--   changesets are not edited. system_message is the source of truth; `sync:translations`
--   regenerates the frontend JSON (en/oz/ru/uz) from here.
-- Pattern: S033 (5-arg _seed_msg helper, persistent — defined in S006; en-US = key).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                 category   key(en)                   uz                       oz                       ru
PERFORM _seed_msg('label',  'Birth place',            'Tug''ilgan joy',        'Туғилган жой',          'Место рождения');
PERFORM _seed_msg('label',  'Passport issue place',   'Pasport berilgan joy',  'Паспорт берилган жой',  'Место выдачи паспорта');
PERFORM _seed_msg('label',  'Expiry date',            'Amal qilish muddati',   'Амал қилиш муддати',    'Срок действия');

END $$;
