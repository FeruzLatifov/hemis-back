-- =====================================================
-- S039: SEED TRANSLATIONS — record history dialog (audit trail)
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-29 (extended 2026-08-30 — audit vocabulary)
-- Purpose:
--   The "Tarix" dialog (EntityHistoryDialog) answers two different questions and needs a subtitle
--   that says which one is on screen:
--     • per record  → the fields that changed ("Changed fields", already seeded in S010).
--     • per owner   → who attached, edited or detached WHICH speciality, and when. Calling that
--       "Changed fields" was wrong: an attach or a detach changes no field, it creates or removes a
--       whole row, and that is exactly what the OTM-scoped list shows.
--
--   The dialog also showed the log RAW — `UPDATE`, `reviewStatus`, `NEEDS_REVIEW`, `distributable
--   true→false`. Storing raw facts is right (a record of what happened must not depend on today's
--   wording); showing them is not — nobody outside the code knows what NEEDS_REVIEW is. The
--   frontend now resolves every audit action, field name and enum value through one dictionary
--   (components/audit/audit-labels.ts), shared by the history dialog, the ministry journal and its
--   detail drawer, and these are the words it needs.
--
--   Keys:
--     • "Who did what, and when", "Attached, edited and detached specialities" — the two subtitles.
--     • Audit actions, past tense: "Updated", "Deleted", "Restored", "Viewed", "Exported",
--       "Imported" ("Created" is already seeded in S010).
--     • Field names the audit log emits that no dictionary covered yet:
--       "Distributed to the OTMs" (HSpeciality.isDistributable — the derived answer to "do the 230
--       OTMs see this row", which is what an approval actually changes) and
--       "Name (Uzbek Cyrillic)" (name_oz; the uz/ru/en variants are already seeded).
--     • "Expand" / "Collapse" — used by SpecialityTree since it was written and never seeded (the
--       frontend carried them in scripts/i18n-known-missing.json); the history dialog's collapsed
--       timeline needs the same two words, so they are seeded here and the baseline is pruned.
--     • "Client ID" / "Last used" — same class of drift, found by running the sync: they existed
--       ONLY in the frontend JSONs, so the first sync after this changeset would have deleted the
--       OAuth-clients table's column headers. Seeded verbatim from those JSONs.
--
--   NEW seed: S006/S010/S032..S037 are applied in production (central_hemis) and applied
--   changesets are never edited. system_message is the single source of truth — `sync:translations`
--   rewrites the frontend JSONs (en/oz/ru/uz) from it, so an unseeded key disappears silently at
--   the next sync.
-- Pattern: S037 (5-argument _seed_msg helper defined in S006; en-US = the key itself).
-- Safety: _seed_msg does ON CONFLICT (message_key) DO UPDATE — idempotent, runOnChange.
-- =====================================================

DO $$
BEGIN

-- ── The two history-dialog subtitles ──
PERFORM _seed_msg('label', 'Who did what, and when', 'Kim, nima qildi va qachon', 'Ким, нима қилди ва қачон', 'Кто, что сделал и когда');
PERFORM _seed_msg('label', 'Attached, edited and detached specialities', 'Biriktirilgan, tahrirlangan va o''chirilgan mutaxassisliklar', 'Бириктирилган, таҳрирланган ва ўчирилган мутахассисликлар', 'Прикреплённые, изменённые и удалённые специальности');

-- ── Audit actions (the chip on every log entry). "Created" is S010. ──
PERFORM _seed_msg('label', 'Updated',  'O''zgartirilgan',   'Ўзгартирилган',     'Изменено');
PERFORM _seed_msg('label', 'Deleted',  'O''chirilgan',      'Ўчирилган',         'Удалено');
PERFORM _seed_msg('label', 'Restored', 'Tiklangan',         'Тикланган',         'Восстановлено');
PERFORM _seed_msg('label', 'Viewed',   'Ko''rilgan',        'Кўрилган',          'Просмотрено');
PERFORM _seed_msg('label', 'Exported', 'Eksport qilingan',  'Экспорт қилинган',  'Экспортировано');
PERFORM _seed_msg('label', 'Imported', 'Import qilingan',   'Импорт қилинган',   'Импортировано');

-- ── Field names the history table shows ──
PERFORM _seed_msg('label', 'Name (Uzbek Cyrillic)', 'Nomi (kirilcha)', 'Номи (кириллча)', 'Название (кириллица)');

-- 'Distributed to the OTMs' was seeded by an earlier revision of THIS changeset and is now unused:
-- `distributable` is a derived getter (APPROVED + code + active + not deleted), each input already a
-- row of the same diff table, and the word claimed a delivery that has not happened — the Univer
-- side has no `h_speciality` table, so ApplyHemisEventJob skips the pushed classifier and acks it.
-- An unreleased key that nothing uses is dropped rather than left as drift (this seed owns it).
DELETE FROM system_message WHERE message_key = 'Distributed to the OTMs';

-- ── Two keys the frontend JSONs carried but NO seed ever created ──
--   Surfaced by running `sync:translations` for the keys above: the sync REWRITES the JSONs from
--   system_message, so a key that lives only in the JSON silently disappears the first time anyone
--   syncs — and 'Client ID' / 'Last used' are the OAuth-clients table's own column headers. Seeded
--   with exactly the wording the JSONs carried, so nothing on that page changes.
PERFORM _seed_msg('table', 'Client ID', 'Client ID', 'Client ID', 'Client ID');
PERFORM _seed_msg('table', 'Last used', 'Oxirgi ishlatilgan', 'Охирги ишлатилган', 'Последнее использование');

-- ── The edit dialog now says what saving an APPROVED row does ──
--   Any change to distributed content (code, names, education type, years) returns the row to
--   NEEDS_REVIEW and withdraws it from the OTMs, whoever is editing — an approval is an approval OF
--   CONTENT, so the content may not change under it silently.
PERFORM _seed_msg('label', 'Any edit returns an approved speciality to Needs review and withdraws it from the OTMs', 'Har qanday tahrir tasdiqlangan mutaxassislikni Tasdiqlanmagan holatiga qaytaradi va OTMlardan chaqirib oladi', 'Ҳар қандай таҳрир тасдиқланган мутахассисликни Тасдиқланмаган ҳолатига қайтаради ва ОТМлардан чақириб олади', 'Любое изменение возвращает утверждённую специальность в статус «Не утверждено» и отзывает её из вузов');

-- ── Expand / collapse (SpecialityTree + the collapsed history timeline) ──
PERFORM _seed_msg('label', 'Expand',   'Ochish',  'Очиш',  'Развернуть');
PERFORM _seed_msg('label', 'Collapse', 'Yig''ish', 'Йиғиш', 'Свернуть');

END $$;
