-- =====================================================
-- S028: SEED TRANSLATIONS — "Speciality years" label
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-20
-- Purpose:
--   One NEW i18n key for the attach dialog's speciality picker. Each search result carries the
--   speciality's own validity years (h_speciality_year) and used to label them "O'quv yili" —
--   the SAME wording as the attachment's academic year (university_speciality_attachment.edu_year)
--   selected two controls above it. A row reading "O'quv yili: 2024, 2026" under a form where the
--   admin had just picked "O'quv yili: 2026" looked like a contradiction.
--   The two are different facts: edu_year is WHEN the OTM runs this speciality, h_speciality_year
--   is WHICH editions of the speciality exist in the classifier. The picker now says
--   "Mutaxassislik yillari" so the distinction is visible.
--   NEW seed because S006/S010/S024/S026/S027 are already applied in production (central_hemis) —
--   applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON from here.
-- Pattern: S021/S022/S024/S026/S027 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                category  key(en)              uz                        oz                          ru
PERFORM _seed_msg('label', 'Speciality years', 'Mutaxassislik yillari', 'Мутахассислик йиллари', 'Годы специальности');

END $$;
