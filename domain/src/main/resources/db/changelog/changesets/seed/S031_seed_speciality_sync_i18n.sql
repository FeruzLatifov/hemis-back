-- =====================================================
-- S031: SEED TRANSLATIONS — Speciality "Sync to legacy" button
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-24
-- Purpose:
--   Two NEW i18n keys for the speciality classifier page's "Sinxronlash" button, which projects the
--   APPROVED rows of the unified h_speciality classifier down into the frozen legacy
--   hemishe_h_speciality_bachelor / _master tables (matched by UUID, idempotent) so the old-hemis +
--   Univer student-save path — which still resolves a student's speciality against those legacy
--   tables — can find a newly-curated speciality.
--     • 'Sync to legacy'                — the button label (uz "Sinxronlash").
--     • 'Copy approved specialities …'  — the confirm-dialog description (full English sentence as
--                                         the key, per the gettext model where en == message_key).
--   NEW seed because S006/S010 are already applied in production (central_hemis) — applied changesets
--   are never edited. system_message is the source of truth; `sync:translations` regenerates the
--   frontend JSON (en/oz/ru/uz) from here, so without this seed a future sync would drop these keys.
-- Pattern: S028 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                category   key(en)                                                                                      uz                                                                                                                                oz                                                                                                                                ru
PERFORM _seed_msg('action', 'Sync to legacy',                                                                            'Sinxronlash',                                                                                                                    'Синхронлаш',                                                                                                                     'Синхронизация');
PERFORM _seed_msg('label',  'Copy approved specialities to the old bachelor and master tables so students can be saved', 'Tasdiqlangan mutaxassisliklarni eski bakalavr va magistr jadvallariga ko''chiradi — shundan so''ng talabalarni ushbu mutaxassisliklar bilan saqlash mumkin bo''ladi', 'Тасдиқланган мутахассисликларни эски бакалавр ва магистр жадвалларига кўчиради — шундан сўнг талабаларни ушбу мутахассисликлар билан сақлаш мумкин бўлади', 'Копирует утверждённые специальности в старые таблицы бакалавра и магистра, чтобы можно было сохранять студентов с этими специальностями');

END $$;
