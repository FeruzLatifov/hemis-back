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
--     2. A hint that re-placing a row drops it back to "Needs review" (NEEDS_REVIEW).
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
PERFORM _seed_msg('label', 'Moving to another place resets the status to Needs review',                  'Boshqa joyga ko''chirilsa holat qayta ko''rib chiqishga o''tadi',                                     'Бошқа жойга кўчирилса ҳолат қайта кўриб чиқишга ўтади',                                   'При перемещении в другое место статус меняется на «требует проверки»');

END $$;
