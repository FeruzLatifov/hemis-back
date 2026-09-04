-- =====================================================
-- S043: SEED TRANSLATIONS — Ordinatura: the third education type's name + the page subtitle
-- =====================================================
-- Author: hemis-team
-- Date: 2026-09-04
-- Purpose:
--   One key. Until M017/S042 the speciality classifier admitted exactly two education types, and
--   every screen that had to name one wrote it as a two-way ternary — `code === '11' ? t('Bachelor')
--   : t('Master')` — on the page header, the create dialog, the edit dialog and both detail views.
--   With Ordinatura ('13') in the table those ternaries would have labelled all 69 residency
--   specialities "Magistr". They now go through one helper (`educationTypeLabelKey` in
--   speciality.api.ts) which resolves '13' to this key.
--
--   The label is a FALLBACK, not the primary source: every one of those screens prefers the name
--   h_education_type returns for the code (V022 seeds Ordinatura / Ординатура / Residency there),
--   and only reaches for t() while those options are still loading. It is seeded anyway because the
--   backend's .xlsx export asks for it directly — SpecialityClassifierController.buildLabels()
--   calls i18nService.getMessage("Residency", lang) to title the third worksheet — and an unseeded
--   key there would print the literal word "Residency" into a Russian export.
--
--   Values match V022's h_education_type row exactly, so the fallback and the classifier cannot
--   disagree about what to call the same thing.
--
--   The second key replaces the page subtitle. /classifiers/speciality announced itself as the
--   "Unified bachelor and master speciality classifier" — a sentence that stopped being true the
--   moment Ordinatura landed, and that would go stale again at the next type. The new wording names
--   no types at all, so it survives them. The old key is NOT deleted: S010 owns it and an applied
--   changeset keeps its rows; nothing renders it any more.
--
--   NEW seed: S006/S009/S010/S032..S041 are applied in production (central_hemis) and applied
--   changesets are never edited. system_message is the single source of truth — `sync:translations`
--   rewrites the frontend JSONs (en/oz/ru/uz) from it, so an unseeded key disappears silently at
--   the next sync. Run that sync after applying.
-- Pattern: S040/S041 (5-argument _seed_msg helper defined in S006; en-US = the key itself).
-- Safety: _seed_msg does ON CONFLICT (message_key) DO UPDATE — idempotent, runOnChange.
-- =====================================================

DO $$
BEGIN

-- ── The third education type, beside S006's 'Bachelor' and 'Master' ──
PERFORM _seed_msg('label', 'Residency', 'Ordinatura', 'Ординатура', 'Ординатура');

-- ── Page subtitle that names no education type, so it cannot go stale again ──
PERFORM _seed_msg('label', 'Unified speciality classifier',
                  'Mutaxassisliklar yagona klassifikatori',
                  'Мутахассисликлар ягона классификатори',
                  'Единый классификатор специальностей');

END $$;
