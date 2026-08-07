# Speciality classifier — seed ETL (source of truth)

This directory is the **versioned source of truth** for the unified speciality classifier seeds
(`h_speciality` + `h_speciality_year`, V018). The generated Liquibase seeds are **machine-written — never
hand-edit them**; edit the source here and re-run the ETL.

> Previously this pipeline lived only under `startup/docs/mutaxasisliklar/` (outside any git repo) — a
> single-disk durability risk (audit HIGH). It now lives in the repo alongside the seeds it produces.
> This `domain/etl/speciality/` copy is **canonical**; the `docs/mutaxasisliklar/` copies are superseded.

## Files

| File | Role |
|------|------|
| `etl_speciality.py` | Bulk ETL → `S014_seed_h_speciality.sql` + `S015_seed_h_speciality_year.sql` |
| `etl_speciality_kushimcha_2026.py` | 2026 supplementary ETL → `S017_seed_h_speciality_2026.sql` |
| `tahlil_kushimcha_2026.py` | Analysis helper (kushimcha existence report; does not write seeds) |
| `2_Bakalavr.xlsx`, `3_Magistr.xlsx` | Bulk source (5367 APPROVED rows) — **tracked** |
| `Бакалавр-кушимча-2026.xlsx`, `Магистр-кушимча-2026.xlsx` | 2026 supplementary source (66 leaves) — **tracked** |
| `live_new_bachelor.csv`, `live_new_master.csv` | 53 live-DB-new NEEDS_REVIEW rows — **tracked** |
| `etl_speciality*.csv`, `*_REVIEW.csv` | Derived intermediates — **gitignored** (regenerated) |

## Regenerate (one command each, run from THIS directory)

```bash
cd domain/etl/speciality
python3 etl_speciality.py              # -> S014 + S015 (+ etl_speciality*.csv intermediates)
python3 etl_speciality_kushimcha_2026.py   # -> S017 (reads etl_speciality.csv from the step above)
```

`SEED_DIR` is computed relative to the script (`../../src/main/resources/db/changelog/changesets/seed`),
so the scripts work from anywhere in the repo. Inputs (xlsx / CSV) are read from the current directory,
so run from here. Requires `python3` + `openpyxl` (`pip install openpyxl`).

## What the ETL guarantees (self-checks — a failure aborts generation, not the migration)

- **Identity**: no `(education_level, code, name_search)` appears twice across S014 **and** S017 — so the seed
  loads clean under `uq_h_speciality_identity` on a fresh DB. `etl_speciality.py` **consolidates** the 28
  year-versioned `(edu, code, name)` twins into one row carrying the union of years (children re-pointed).
- **Fold parity**: the Python `fold()` is byte-identical to the SQL `h_speciality_fold()` (V018, no NFKD) and
  the Java `foldSearch()`. `name_search` is a **GENERATED** column — the ETL does **not** emit it.
- **Parents resolve**: every S017 `parent_id` exists as an id in S014; 0 dangling / 0 self-reference.
- **Kushimcha identity rule** (ministry, user-confirmed): a leaf whose `(edu, code, name)` doesn't already
  exist → new `uuid5` row + 2026; an exact match with 2026 → NO-OP; exact match without 2026 → LINK the year.
  Nothing is retired. `name_oz` = the original Cyrillic (no longer discarded). See ADR-0014.

## Related

- Schema: `domain/src/main/resources/db/changelog/changesets/schema/V018_create_h_speciality.sql`
- Seeds: `.../changesets/seed/S014_…`, `S015_…`, `S016_…`, `S017_…`
- Decision record: [`docs/adr/0014-speciality-name-columns-and-identity.md`](../../../docs/adr/0014-speciality-name-columns-and-identity.md)
