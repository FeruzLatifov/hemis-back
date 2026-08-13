# OTM ↔ speciality attachment ETL (2026-2027 → S018)

Generates `S018_seed_speciality_attachment_2026.sql` (+ rollback): the 2026-2027
ministry assignment of which speciality each OTM (university) is allowed to run,
loaded into `university_speciality_attachment` (V019).

```bash
python3 domain/etl/attachment/generate_s018.py   # rewrites S018 seed + rollback (deterministic)
```

## Inputs (tracked here)

| File | education_type | sheet |
|------|----------------|-------|
| `Bakalavr_2026-2027.xlsx`     | 11 (Bakalavr) | `Kunduzgi` |
| `Magistratura_2026-2027.xlsx` | 12 (Magistr)  | `Sheet1`   |

xlsx columns (0-based): `0 Num · 1 OTMID(university_code) · 2 Kateg · 3 Shifr(code) ·
4 name · 5 Yil · 6 Kunduzgi · 7 Kechki · 8 Masofa`. Rows whose `Shifr` is not an
8-digit code are OTM header rows and are skipped. `Kateg 1` = yo'nalish (L3),
`Kateg 2` = ichki yo'nalish (L4, "Yo'nalish: profil").

The classifier (`h_speciality`) is read from the checked-in seeds **S014 + S017**
(both — S017 holds the 2026 supplement). No DB connection is used.

## education_form fan-out

Each set education_form column becomes one attachment row. Codes are the ministry
classifier `hemishe_h_education_form` (source: `docs/old-klasifikatorlar/HEMIS_Klassifikator_Dump.json`):

| xlsx column | code | name |
|-------------|------|------|
| Kunduzgi | **11** | Kunduzgi (full-time) |
| Kechki   | **12** | Kechki (evening) |
| Masofa   | **16** | Masofaviy (distance) |

(13 Sirtqi, 14 Maxsus sirtqi, 15/17–23 second-higher/joint are not used by these sheets.)

## Resolution — NAME identity first (important)

Each plan row carries an official `Shifr` **and** a `name`. The classifier's identity
key is `(education_type, code, name)`, and a single `Shifr` can name **two different
specialities** in our historical union — e.g. `70720802` is BOTH
`Mashinasozlik ...avtomatlashtirilgan dastgohli komplekslari` AND
`Shaxta va yer osti muhandisligi`. A code-first match picks the wrong one; matching by
**name** picks the right node. Resolution tiers (see `resolve()`):

1. `1_EXACT` — code == Shifr AND name folds equal (4139 rows).
2. `2_NAME` — name folds equal, code differs (514). Trust the name.
3. `3_SPELL` — tight-normalized name equal (433): `yerosti↔yer osti`, `konstruksiya↔konstruktsiya`.
4. `4_PARENT` — the L4 profile is absent from the classifier (new sub-profiles like
   `Sport faoliyati: turon`, `Lingvistika: pushtu tili`) → attach to the L3 direction
   parent (62). Never a same-code sibling with a *different* profile.

Ambiguity tie-break (`pickbest`): code==Shifr > shared code-prefix (an L4 code is its
L3 parent's prefix + a variant) > matching `hierarchy_level` > 8-digit modern code >
lowest code. Result on the 2026-2027 data: **0 unresolved.**

## Output invariants

- `id = uuid5(ns, "university_code|speciality_id|education_form")` → stable across re-runs.
- `created_by = 'seed:S018-2026'` → the rollback deletes exactly these rows.
- `ON CONFLICT (university_code, speciality_id, education_form) WHERE deleted_at IS NULL
  DO NOTHING` → matches V019's partial unique index; safe on re-run and against a
  user-created colliding row.
- Deterministic: same inputs → byte-identical S018 (rows sorted, no timestamps).

Current output: **6565 rows, 98 OTM** (form 11=5079, 12=1036, 16=450).
