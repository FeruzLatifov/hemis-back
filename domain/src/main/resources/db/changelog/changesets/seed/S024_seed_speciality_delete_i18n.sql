-- =====================================================
-- S024: SEED TRANSLATIONS — speciality delete UI
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-20
-- Purpose:
--   Five NEW i18n keys for deleting a speciality classifier row from the classifier card:
--     1. The confirm-dialog title.
--     2. The children guard message — mirrors the backend SPECIALITY_HAS_CHILDREN_DELETE_FIRST (422).
--     3. The status guard message — mirrors SPECIALITY_DELETE_APPROVED_FORBIDDEN (422):
--        only a "Needs review" (NEEDS_REVIEW) row may be deleted.
--     4. "Move" — the action offered next to Delete when the row still has sub-directions.
--     5. The success toast.
--   NEW seed because S006/S010 are already applied in production (central_hemis) —
--   applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON from here.
-- Pattern: S021/S022 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                category  key(en)                                                                          uz                                                                                              oz                                                                                        ru
PERFORM _seed_msg('label', 'Delete speciality',                                                            'Mutaxassislikni o''chirish',                                                                   'Мутахассисликни ўчириш',                                                                 'Удалить специальность');
PERFORM _seed_msg('label', 'This speciality has sub-directions. Delete them first, or move them under another parent.', 'Bu mutaxassislikda ichki yo''nalishlar bor. Avval ularni o''chiring yoki boshqa otaga ko''chiring.', 'Бу мутахассисликда ички йўналишлар бор. Аввал уларни ўчиринг ёки бошқа отага кўчиринг.', 'У этой специальности есть поднаправления. Сначала удалите их или переместите к другому родителю.');
-- 6-arg form (explicit en): the key keeps the old NEEDS_REVIEW wording, the visible text is "Not approved" (S025).
PERFORM _seed_msg('label', 'Only a speciality with the "Needs review" status can be deleted',               'Only a speciality with the "Not approved" status can be deleted', 'Faqat "Tasdiqlanmagan" holatidagi mutaxassislikni o''chirish mumkin', 'Фақат "Тасдиқланмаган" ҳолатидаги мутахассисликни ўчириш мумкин', 'Удалить можно только специальность со статусом «Не подтверждено»');
PERFORM _seed_msg('label', 'Move',                                                                         'Ko''chirish',                                                                                  'Кўчириш',                                                                                'Переместить');
PERFORM _seed_msg('label', 'Speciality deleted',                                                           'Mutaxassislik o''chirildi',                                                                    'Мутахассислик ўчирилди',                                                                 'Специальность удалена');

END $$;
