-- =====================================================
-- S026: SEED TRANSLATIONS — speciality delete, OTM attachment blocker
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-20
-- Purpose:
--   Three NEW i18n keys for the fourth state of the speciality delete dialog — the row is
--   attached to universities (backend SPECIALITY_ATTACHED_TO_UNIVERSITY, 422). The dialog now
--   lists the OTMs (GET /api/v1/web/classifiers/speciality/{id}/attachments) instead of only
--   reporting a count, the way it already lists sub-directions:
--     1. The list heading, which doubles as the link to the attachments registry.
--     2. The guard message itself.
--     3. The footnote/tooltip for an OTM whose attachment count includes revoked rows:
--        university_speciality_attachment is soft-deleted (deleted_at), but the FK
--        fk_univ_spec_attach_spec is ON DELETE RESTRICT — a revoked row blocks the delete
--        exactly like a live one, which is why the list counts both.
--   NEW seed because S006/S010/S024 are already applied in production (central_hemis) —
--   applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON from here.
-- Pattern: S021/S022/S024 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                category  key(en)                                                                        uz                                                                                        oz                                                                                     ru
PERFORM _seed_msg('label', 'Attached to universities',                                                   'OTMlarga biriktirilgan',                                                                 'ОТМларга бириктирилган',                                                              'Закреплена за вузами');
PERFORM _seed_msg('label', 'This speciality is attached to universities. Remove those attachments first.', 'Bu mutaxassislik OTMlarga biriktirilgan. Avval o''sha biriktirishlarni bekor qiling.',   'Бу мутахассислик ОТМларга бириктирилган. Аввал ўша бириктиришларни бекор қилинг.',     'Эта специальность закреплена за вузами. Сначала снимите эти закрепления.');
PERFORM _seed_msg('label', 'Revoked attachments also block deletion',                                     'Bekor qilingan biriktirishlar ham o''chirishga to''sqinlik qiladi',                       'Бекор қилинган бириктиришлар ҳам ўчиришга тўсқинлик қилади',                           'Отменённые закрепления также препятствуют удалению');

END $$;
