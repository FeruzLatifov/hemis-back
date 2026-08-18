-- =====================================================
-- S021: SEED TRANSLATIONS — bulk speciality-attach toast keys
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-18
-- Purpose: Two i18n keys used by the bulk speciality->OTM attach result toast
--   (POST /api/v1/web/registry/speciality-attachments/bulk): how many rows were
--   created and how many forms were already attached (skipped).
--   Added as a NEW seed because S010 is already applied in production
--   (central_hemis) — applied changesets are never edited. system_message is the
--   source of truth; `sync:translations` regenerates the frontend JSON from here.
--   (One-off translation tweaks — e.g. fixing a label's wording — are done via the
--   Translations admin UI, not a migration; they land in system_message directly.)
-- Pattern: S006/S010 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Safety: ON CONFLICT UPDATE inside _seed_msg — idempotent, no duplicates.
-- =====================================================

DO $$
BEGIN

--                category   key(en)                              uz                                                          oz                                                        ru
PERFORM _seed_msg('message', '{{n}} attached',                    '{{n}} ta biriktirildi',                                    '{{n}} та бириктирилди',                                  'Прикреплено: {{n}}');
PERFORM _seed_msg('message', '{{n}} already attached, skipped',   '{{n}} tasi allaqachon biriktirilgan, o''tkazib yuborildi',   '{{n}} таси аллақачон бириктирилган, ўтказиб юборилди',   '{{n}} уже прикреплено, пропущено');

END $$;
