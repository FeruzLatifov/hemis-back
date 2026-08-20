-- =====================================================
-- S025: SEED TRANSLATIONS — review status NEEDS_REVIEW display rename
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-20
-- Purpose:
--   Rename only the VISIBLE text of the speciality review_status NEEDS_REVIEW
--   state: "Needs review" -> "Not approved" / "Tasdiqlanmagan".
--   The i18n KEY stays 'Needs review' ON PURPOSE — it is referenced by the
--   frontend call sites and by the already-applied S010/S022/S024 seeds; renaming
--   the key would touch all of them for no gain. Only the four translations move.
--   6-arg _seed_msg overload is required here: the English UI text must change too,
--   and the 5-arg form would keep en-US pinned to the message_key.
--   NOT to be confused with `active = false` ("Faol emas") — that is a separate
--   column. This key labels `review_status` (APPROVED / NEEDS_REVIEW).
--   NEW seed because S006/S010 are already applied in production (central_hemis) —
--   applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON from here.
-- Pattern: S021/S022/S024 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Category stays 'status' (the category S010 gave this key): _seed_msg overwrites
-- category on conflict, so passing 'label' here would silently re-categorize it.
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                 category  key(en)          en              uz                oz                 ru
PERFORM _seed_msg('status', 'Needs review', 'Not approved', 'Tasdiqlanmagan', 'Тасдиқланмаган', 'Не подтверждено');

-- The field label follows the values it describes: a column headed "review status" above cells
-- reading "Tasdiqlangan / Tasdiqlanmagan" was the old vocabulary. Category 'label' — the one S010
-- gave this key (_seed_msg overwrites category on conflict, so it must match).
PERFORM _seed_msg('label', 'Review status', 'Approval status', 'Tasdiqlash holati', 'Тасдиқлаш ҳолати', 'Статус подтверждения');

END $$;
