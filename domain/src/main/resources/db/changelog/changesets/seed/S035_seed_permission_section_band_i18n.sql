-- =====================================================
-- S035: SEED TRANSLATIONS — "Section-wide" band in the role -> permissions editor
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-25
-- Purpose:
--   The permission tree is two levels deep (`classifiers` + `classifiers.speciality`, …) and
--   its resources flow into a 2-3 column grid, where the old `└` prefix could not express
--   parentage — a 12px indent says nothing once a child lands in another column. Each
--   resource is now a bordered card, and the ONE real distinction — the domain-wide resource
--   (path == domain id, e.g. `classifiers` = applies to every classifier section) — is lifted
--   into a full-width band above the section cards.
--     • Band title: "Section-wide" — the band cannot reuse the resource's own name, which
--       merely repeats the domain header ("Klassifikatorlar" inside the Klassifikatorlar card).
--
--   Second fix in the same editor: the `delete` chip claimed "irreversible" for EVERY resource,
--   which is false for most of them. Verified against the services: users / roles / universities /
--   diploma-blank-distribution / attached-specialities soft-delete (deleted_at | delete_ts) and can
--   be restored. Only institutions.speciality-attachments is physical (SpecialityAttachmentService
--   .delete -> repository.delete; an attachment is one click to recreate, M011). classifiers.speciality
--   WAS physical when this seed was written; M013 moved it to soft delete (deleted_at + @SQLRestriction,
--   restorable via POST /{id}/restore), so it now resolves to the restorable wording too. The frontend
--   picks the wording per resource, so a restorable delete needs its own line.
--     • "Deletes records (restorable)" — the soft-delete majority ("Deletes records (irreversible)"
--       stays from S032 for the two physical ones).
--   Total: 2 NEW keys.
--
--   NEW seed because S006/S010/S032/S033 are already applied in production (central_hemis) —
--   applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON (en/oz/ru/uz) from here, so without this
--   seed a future sync would drop the key and the band would show the raw English key.
-- Pattern: S033 (5-arg _seed_msg helper, persistent — defined in S006; en-US = key).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                 category   key(en)                            uz                                            oz                                             ru
PERFORM _seed_msg('label',  'Section-wide',                     'Butun bo''lim bo''yicha',                     'Бутун бўлим бўйича',                          'На весь раздел');
PERFORM _seed_msg('label',  'Deletes records (restorable)',     'Yozuvni o''chiradi (keyin tiklash mumkin)',   'Ёзувни ўчиради (кейин тиклаш мумкин)',        'Удаляет запись (можно восстановить)');

END $$;
