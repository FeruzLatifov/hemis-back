-- =====================================================
-- S045: SEED TRANSLATIONS — read-only identity block on the user edit page
-- =====================================================
-- Author: hemis-team
-- Date: 2026-09-04
-- Purpose:
--   Two keys for a section that had no UI at all until now. Creating a user resolves the person
--   from the GUVD passport gateway and stores PINFL, passport, birth date and birth place on the
--   `users` row; EDITING one rendered none of them — UserFormPage put the whole person section
--   behind `!isEdit`, so an administrator could not see WHICH person an existing account belongs
--   to. The data was already travelling all the way to the browser (UserAdminResponse carries it,
--   the UserAdmin type declares it); only the markup was missing.
--
--     • 'Identity' — the section heading. Deliberately not "Passport data": the block also carries
--       PINFL and birth place, and what it actually answers is "who is this account".
--
--     • 'Identity data is set when the account is created and cannot be changed' — the note under
--       the fields. It states a real guarantee rather than a UI convention: users.pinfl is mapped
--       `updatable = false`, so Hibernate omits the column from every UPDATE it generates and no
--       request field, setter or mapper can change it. A wrong PINFL is corrected by deleting the
--       account and creating the right one, which leaves a trail — an UPDATE would silently
--       re-point an existing account, with its roles and its audit history, at a different person.
--
--   The other four labels the block renders — PINFL, 'Passport (series + number)', 'Birth date',
--   'Birth place' — are already seeded (S037/S006) and reused verbatim from the create form.
--
--   NEW seed: S006/S009/S010/S032..S044 are applied in production and applied changesets are never
--   edited. system_message is the single source of truth — `sync:translations` rewrites the
--   frontend JSONs from it, so an unseeded key disappears silently at the next sync.
-- Pattern: S040/S041/S043 (5-argument _seed_msg helper defined in S006; en-US = the key itself).
-- Safety: _seed_msg does ON CONFLICT (message_key) DO UPDATE — idempotent, runOnChange.
-- =====================================================

DO $$
BEGIN

PERFORM _seed_msg('label', 'Identity', 'Shaxs maʼlumotlari', 'Шахс маълумотлари', 'Личные данные');

PERFORM _seed_msg('label', 'Identity data is set when the account is created and cannot be changed',
                  'Shaxs maʼlumotlari hisob yaratilganda belgilanadi va oʻzgartirilmaydi',
                  'Шахс маълумотлари ҳисоб яратилганда белгиланади ва ўзгартирилмайди',
                  'Личные данные задаются при создании учётной записи и не изменяются');

END $$;
