-- =====================================================
-- S022: SEED TRANSLATIONS — speciality re-placement (move) UI hints
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-19
-- Purpose:
--   Two NEW i18n keys for the speciality classifier edit dialog placement controls:
--     1. Shown when a row still has sub-directions and its hierarchy-level selector is
--        LOCKED (the children must be re-placed first). Mirrors the backend guard
--        SPECIALITY_HAS_CHILDREN_MOVE_FIRST (422).
--     2. A hint that re-placing a row drops it back to NEEDS_REVIEW ("Tasdiqlanmagan").
--   NEW seed because S006/S010 are already applied in production (central_hemis) —
--   applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON from here.
-- Pattern: S021 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                category  key(en)                                                                        uz                                                                                                    oz                                                                                        ru
PERFORM _seed_msg('label', 'Has sub-directions — move them to another level first to change this level', 'Ichki yo''nalishlari bor — bu darajani o''zgartirish uchun avval ularni boshqa darajaga ko''chiring', 'Ички йўналишлари бор — бу даражани ўзгартириш учун аввал уларни бошқа даражага кўчиринг', 'Есть поднаправления — сначала переместите их на другой уровень, чтобы изменить этот уровень');
-- 6-arg form (explicit en): the key keeps the old NEEDS_REVIEW wording, the visible text follows the
-- rename to "Not approved" / "Tasdiqlanmagan" (S025) so the hint and the status dropdown agree.
PERFORM _seed_msg('label', 'Moving to another place resets the status to Needs review', 'Moving to another place resets the status to "Not approved"', 'Boshqa joyga ko''chirilsa holat "Tasdiqlanmagan" ga o''tadi', 'Бошқа жойга кўчирилса ҳолат "Тасдиқланмаган" га ўтади', 'При перемещении в другое место статус меняется на «Не подтверждено»');

END $$;
