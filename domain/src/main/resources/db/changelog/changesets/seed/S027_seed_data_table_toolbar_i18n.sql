-- =====================================================
-- S027: SEED TRANSLATIONS — reusable data-table toolbar
-- =====================================================
-- Author: hemis-team
-- Date: 2026-08-20
-- Purpose:
--   One NEW i18n key for the shared registry toolbar (DataTableToolbar): the accessible name of
--   the X button on an applied-filter chip. Interpolated with the filter name, so a screen
--   reader announces "Universitet filtrini olib tashlash" rather than a bare "X".
--   {{label}} is the i18next interpolation placeholder — it must survive verbatim in all four
--   languages, otherwise the filter name is dropped from the announcement.
--   Every other key the toolbar uses (Search / Filters / Clear / Refresh / Total) is already
--   seeded by S006/S010.
--   NEW seed because S006/S010 are already applied in production (central_hemis) —
--   applied changesets are never edited. system_message is the source of truth;
--   `sync:translations` regenerates the frontend JSON from here.
-- Pattern: S021/S022/S024/S026 (_seed_msg helper, persistent — defined in S006, not dropped).
-- Safety: ON CONFLICT (message_key) UPDATE inside _seed_msg — idempotent.
-- =====================================================

DO $$
BEGIN

--                category  key(en)                      uz                                       oz                                       ru
PERFORM _seed_msg('label', 'Remove {{label}} filter',   '{{label}} filtrini olib tashlash',      '{{label}} фильтрини олиб ташлаш',       'Удалить фильтр {{label}}');

END $$;
