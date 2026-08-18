-- =====================================================
-- S021: SEED TRANSLATIONS — bulk attach toast keys + login-title correction
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-18
-- Purpose:
--   1. Two NEW i18n keys for the bulk speciality->OTM attach result toast
--      (POST /api/v1/web/registry/speciality-attachments/bulk): how many rows were
--      created and how many forms were already attached (skipped).
--   2. CORRECT the login-page subtitle 'Higher Education Management Information System'
--      (seeded by S006, in prod → not editable): uz/oz/ru were wrong wording + Title Case
--      ("Oliy Ta'lim Boshqaruv ...") → proper name in sentence case
--      ("Oliy ta'limni boshqarish axborot tizimi"). An override via _seed_msg upsert.
--   NEW seed because S006/S010 are already applied in production (central_hemis) —
--   applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON from here.
-- Pattern: S006/S010 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent; corrects existing rows.
-- =====================================================

DO $$
BEGIN

-- Bulk-attach result toast (new keys)
--                category   key(en)                              uz                                                          oz                                                        ru
PERFORM _seed_msg('message', '{{n}} attached',                    '{{n}} ta biriktirildi',                                    '{{n}} та бириктирилди',                                  'Прикреплено: {{n}}');
PERFORM _seed_msg('message', '{{n}} already attached, skipped',   '{{n}} tasi allaqachon biriktirilgan, o''tkazib yuborildi',   '{{n}} таси аллақачон бириктирилган, ўтказиб юборилди',   '{{n}} уже прикреплено, пропущено');

-- Login-page subtitle — correct wording + sentence case (overrides the S006 value)
PERFORM _seed_msg('auth', 'Higher Education Management Information System', 'Oliy ta''limni boshqarish axborot tizimi', 'Олий таълимни бошқариш ахборот тизими', 'Информационная система управления высшим образованием');

END $$;
