-- =====================================================
-- S041: SEED TRANSLATIONS — recycle-bin button label, restore permission, unknown-action fallback
-- =====================================================
-- Author: hemis-team
-- Date: 2026-09-04
-- Purpose:
--   Three keys the code already asks i18next for but no seed ever created, plus the one the new
--   `universities.restore` permission needs in the role → permissions editor.
--
--     • 'Deleted items' — the recycle-bin BUTTON on /institutions/universities and
--       /classifiers/speciality. Both buttons currently render S039's 'Deleted', which is the
--       audit-log ACTION chip: in ru that value is 'Удалено', a short neuter passive participle
--       ("it was deleted"). Stamped on a log row it is exactly right; on a button that means
--       "show me the deleted ones" it reads as a status, not a destination. uz/oz carry both
--       roles with one word ("O'chirilgan"), so the collision is Russian-only — but a button and
--       a log chip are two different jobs and get two keys. 'Deleted' stays with S039 (the audit
--       chip keeps rendering it); UniversitiesToolbar.tsx and SpecialityClassifierPage.tsx move
--       to this one.
--
--     • 'Restores a deleted record' — the "what it grants" microcopy for the `restore` action
--       chip in the role → permissions editor (ACTION_META in permissions.meta.ts). The new
--       `universities.restore` permission introduces `action = 'restore'`; every one of the ten
--       existing actions has its grant line seeded in S032, and this is the eleventh. The chip
--       LABEL reuses 'Restore', already seeded by S036 for the recycle-bin button — one word, one
--       key, no duplicate.
--
--     • 'Grants permission' — UNKNOWN_ACTION_META.grantKey (permissions.meta.ts). When the
--       backend adds an action before ACTION_META knows it, `actionMetaOf` falls through to that
--       fallback and the editor renders t('Grants permission') in the chip tooltip, the aria-label
--       and the detailed view. The key existed in NO json and NO seed, so i18next returned the key
--       itself and those three places read English in uz, oz and ru. `check:i18n` cannot see it —
--       it is a value in a metadata object, not a t('literal') call site.
--
--   NEW seed: S006/S009/S010/S032..S039 are applied in production (central_hemis) and applied
--   changesets are never edited. system_message is the single source of truth — `sync:translations`
--   rewrites the frontend JSONs (en/oz/ru/uz) from it, so an unseeded key disappears silently at
--   the next sync.
-- Pattern: S040 (5-argument _seed_msg helper defined in S006; en-US = the key itself).
-- Safety: _seed_msg does ON CONFLICT (message_key) DO UPDATE — idempotent, runOnChange.
-- =====================================================

DO $$
BEGIN

-- ── The recycle-bin button (S039's 'Deleted' stays the audit chip) ──
PERFORM _seed_msg('label',  'Deleted items',              'O''chirilgan',                    'Ўчирилган',                       'Удалённые');

-- ── Grant microcopy for the `restore` action chip — the eleventh, next to S032's ten ──
PERFORM _seed_msg('label',  'Restores a deleted record',  'O''chirilgan yozuvni qaytaradi',  'Ўчирилган ёзувни қайтаради',      'Восстанавливает удалённую запись');

-- ── Fallback microcopy for an action ACTION_META does not know yet ──
PERFORM _seed_msg('label',  'Grants permission',          'Ruxsat beradi',                   'Рухсат беради',                   'Предоставляет право');

END $$;
