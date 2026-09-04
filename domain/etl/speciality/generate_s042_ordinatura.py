#!/usr/bin/env python3
"""S042 generator — Ordinatura (residency) specialities into the unified h_speciality classifier.

Source
------
``hemishe_h_speciality_ordinatura.xlsx`` — a dump of the frozen CUBA classifier table of the same
name (69 live rows, all ``delete_ts IS NULL``, all ``active``, all 8-digit ``8091*`` codes).

The live legacy table actually holds **70** rows; the 70th (``7b2f1cec…`` / code ``17.00.09`` /
"Raqs sanʼati") is a stray that belongs to no ordinatura branch — its ``_parent`` points outside the
table and its code uses the scientific-degree ``NN.NN.NN`` shape, not the 8-digit classifier shape.
The ministry's xlsx deliberately omits it, so this ETL imports the 69 and never the 70th.

Decisions encoded here (ministry-confirmed)
-------------------------------------------
* ``education_type = '13'`` (Ordinatura in ``h_education_type``). V022 already seeds that code and
  anticipated this exact import; M017 widens the V018 CHECK to admit it.
* ``hierarchy_level = 3`` for all 69, under a **two-node ancestor chain cloned for
  education_type '13'**: ``900000 SOGʻLIQNI SAQLASH VA IJTIMOIY TAʼMINOT`` (L1) →
  ``910000 Sogʻliqni saqlash`` (L2) → the 69 leaves.

  The obvious-looking parent is 960be177 — the Bakalavr ``910000``, which 18 of the source rows
  already carry as their ``_parent`` — and in the LEGACY schema it is the only possible one:
  ``hemishe_h_speciality_ordinatura._parent`` has an FK onto ``hemishe_h_speciality_bachelor(id)``
  and CUBA types the field as ``HSpecialityBachelor``. (Someone put an ordinatura row under the
  MASTER ``910000`` once, on 2022-11-02, and soft-deleted it the same day — the row is still there
  with its ``delete_ts``.)

  The unified classifier is a different shape: every education type keeps its own complete copy of
  the tree — Bakalavr and Magistr each have 18 L1 + 59 L2 rows with identical codes and different
  ids — and ``getTree`` filters by education type, surfacing any node whose parent is outside the
  filtered set as a root. Pointing the leaves at a '11' parent therefore rendered the Ordinatura
  view as 69 flat rows with no hierarchy at all. Cloning the two categories for '13' is what the
  table's own convention already does twice, and it also restores the invariant that held for all
  5603 pre-existing rows: a child's education_type always equals its parent's.
* Year **2023** for every row — the legacy table has an empty ``_year`` column, and years are
  mandatory in this classifier (see the S015 header), so the edition year is supplied here.
* ``review_status = 'APPROVED'`` — an official ministry classifier, exactly like S014/S017.
* ``id`` = the legacy UUID, verbatim. LegacySpecialitySyncService bridges h_speciality back into the
  frozen ``hemishe_h_speciality_*`` tables **by UUID**, so reusing the legacy id keeps that bridge and
  any existing student reference pointing at one row instead of forking into two.
* ``is_checked`` = the source value (35 of the 69 are ``true``), unlike the S014/S017 rows which are
  born ``false``: these are copied verbatim from a table CUBA and Univer already serve, so flipping
  the flag would make the two copies disagree for no reason.
* ``version = -1`` (ministry instruction) — which is not an oddity but this table's own baseline:
  5512 of the 5603 pre-existing rows carry -1, including all four ancestor twins. The higher values
  are the handful of rows that have since been edited through the UI (60 at 1, 41 at 2, 11 at 3, …),
  because Hibernate's ``@Version`` increments from wherever a row starts. Inserting at ``1`` would
  have made every imported row look like it had already been curated once. A negative seed is fine
  for optimistic locking: the first edit writes 0, then 1, 2 …

Text normalization
------------------
* Latin apostrophes are folded to the project-standard modifier letters — ``o'``/``o‘`` → ``oʻ``
  (U+02BB), ``g'``/``g‘`` → ``gʻ`` — matching S014/S017 ("Yuz-jagʻ jarrohligi", "choʻl"). This is
  cosmetic for identity: the DB's ``h_speciality_fold()`` maps every apostrophe variant to a space,
  so ``name_search`` is unchanged either way.
* Whitespace is collapsed (one source ``name_en`` carries a double space).
* ONE editorial correction, flagged in the seed header: row ``19d8acc7`` (80910725 Parodontologiya)
  carries the English word "Periodontology" in ``name_ru`` and nothing in ``name_en``. English text
  in the Russian column would render as English to a Russian reader, so it is moved to ``name_en``
  and ``name_ru`` left NULL. Nothing is lost — the RU name was never Russian to begin with.

``name_oz`` is NULL for all 69: the source has a Russian column and no Uzbek-Cyrillic one.

Run
---
    python3 domain/etl/speciality/generate_s042_ordinatura.py

Rewrites ``changesets/seed/S042_seed_h_speciality_ordinatura.sql`` (+ its rollback) in place.
"""

from __future__ import annotations

import re
import sys
import unicodedata
import uuid
from pathlib import Path

try:
    import openpyxl
except ImportError:  # pragma: no cover - operator-facing
    sys.exit("openpyxl kerak:  pip install openpyxl")

HERE = Path(__file__).resolve().parent
XLSX = HERE / "hemishe_h_speciality_ordinatura.xlsx"
SEED_DIR = HERE.parent.parent / "src/main/resources/db/changelog/changesets/seed"
OUT_SQL = SEED_DIR / "S042_seed_h_speciality_ordinatura.sql"
OUT_ROLLBACK = SEED_DIR / "S042_seed_h_speciality_ordinatura_rollback.sql"

EDUCATION_TYPE = "13"          # h_education_type.code — Ordinatura
VERSION = -1                   # the table's own baseline: 5512 of 5603 existing rows carry -1
HIERARCHY_LEVEL = 3
YEAR = 2023

# uuid5 namespace + key shape reused verbatim from etl_speciality_kushimcha_2026.py (S017), so every
# generated speciality id in this project comes out of the same recipe and a re-run never forks.
NS = uuid.UUID("6f9619ff-8b86-d011-b42d-00cf4fc964ff")


def gen_id(code: str) -> str:
    return str(uuid.uuid5(NS, f"hemis-spec-ordinatura|{EDUCATION_TYPE}|{code}"))


# The two ancestor categories, cloned for education_type '13'. Names, review status and audit flags
# are copied verbatim from their Bakalavr/Magistr twins (a3796ec1/960be177 and ae3b0823/1f1187c0) —
# all four carry NULL in every non-uz name column, so these do too.
ANCESTOR_L1_CODE = "900000"
ANCESTOR_L1_NAME = "SOGʻLIQNI SAQLASH VA IJTIMOIY TAʼMINOT"
ANCESTOR_L2_CODE = "910000"
ANCESTOR_L2_NAME = "Sogʻliqni saqlash"

ANCESTOR_L1_ID = gen_id(ANCESTOR_L1_CODE)
ANCESTOR_L2_ID = gen_id(ANCESTOR_L2_CODE)

# The Bakalavr 910000 the ministry's own legacy rows point at. Recorded, not used as a parent: the
# legacy table's _parent FK targets hemishe_h_speciality_bachelor(id) and CUBA types the field as
# HSpecialityBachelor, so 960be177 is the only value THAT schema accepts. In the unified classifier
# the tree is filtered by education type, so a '11' parent would leave every ordinatura row hanging
# as a root — hence the '13' clone above. Anything that ever syncs ordinatura back into the legacy
# table must translate ANCESTOR_L2_ID → this id.
LEGACY_BACHELOR_PARENT_ID = "960be177-4e20-4a3c-b381-a1d816370e3f"

# The stray legacy row the ministry xlsx omits — asserted here so a future re-export that
# accidentally includes it fails loudly instead of importing a dance speciality as a residency.
EXCLUDED_LEGACY_ID = "7b2f1cec-430c-cd8d-fa2e-f81d2080c782"

# Row 19d8acc7: English text sitting in the Russian column (see module docstring).
RU_TO_EN_FIX = {"19d8acc7-6250-adfd-6e39-0c1cc2e9966b": "Periodontology"}

TURNED_COMMA = "ʻ"   # ʻ — oʻ / gʻ
APOSTROPHE = "ʼ"     # ʼ — taʼminlash


def normalize(text: object | None) -> str | None:
    """Collapse whitespace and fold apostrophe variants to the project-standard letters."""
    if text is None:
        return None
    s = unicodedata.normalize("NFC", str(text))
    s = re.sub(r"\s+", " ", s).strip()
    if not s:
        return None
    # o'/o‘/o’ -> oʻ and g'/g‘/g’ -> gʻ (both cases); any remaining straight/curly quote is the
    # glottal-stop apostrophe (taʼminlash), which takes U+02BC.
    s = re.sub(r"([oOgG])['‘’ʼ]", lambda m: m.group(1) + TURNED_COMMA, s)
    s = re.sub(r"['‘’]", APOSTROPHE, s)
    return s


def sql_str(value: str | None) -> str:
    return "NULL" if value is None else "'" + value.replace("'", "''") + "'"


def load_rows() -> list[dict]:
    wb = openpyxl.load_workbook(XLSX, data_only=True)
    ws = wb.worksheets[0]
    raw = list(ws.iter_rows(values_only=True))
    header = [str(h) for h in raw[0]]
    idx = {name: i for i, name in enumerate(header)}

    rows: list[dict] = []
    for r in raw[1:]:
        if not r[idx["id"]]:
            continue
        row_id = str(r[idx["id"]]).strip()
        if row_id == EXCLUDED_LEGACY_ID:
            sys.exit(f"XATO: chetlashtirilgan legacy qator xlsx'ga qaytib kelibdi: {row_id}")
        if r[idx["delete_ts"]]:
            continue  # CUBA soft delete — never import a deleted classifier row
        name_uz = normalize(r[idx["name"]])
        if not name_uz:
            sys.exit(f"XATO: name bo'sh — id={row_id}")
        name_ru = normalize(r[idx["name_ru"]])
        name_en = normalize(r[idx["name_en"]])
        if row_id in RU_TO_EN_FIX:
            assert name_ru == RU_TO_EN_FIX[row_id], f"kutilmagan name_ru: {name_ru!r}"
            name_ru, name_en = None, RU_TO_EN_FIX[row_id]
        rows.append({
            "id": row_id,
            "code": normalize(r[idx["code"]]),
            "name_uz": name_uz,
            "name_ru": name_ru,
            "name_en": name_en,
            "is_checked": bool(r[idx["is_checked"]]),
        })
    return rows


def fold(text: str) -> str:
    """Byte-identical to the DB's h_speciality_fold() and the Java foldSearch()."""
    s = text
    for ch in "'’ʻʼ‘`":
        s = s.replace(ch, " ")
    return re.sub(r"\s+", " ", s.lower()).strip()


def check_identity(rows: list[dict]) -> None:
    """The DB's uq_h_speciality_identity is (education_type, code, name_search) — verify it here
    so a violation is a readable ETL error, not a 23505 in the middle of a migration."""
    seen: dict[tuple[str | None, str], str] = {}
    for row in rows:
        key = (row["code"], fold(row["name_uz"]))
        if key in seen:
            sys.exit(f"XATO: identity takrorlanishi {key} — {seen[key]} va {row['id']}")
        seen[key] = row["id"]
    ids = [r["id"] for r in rows]
    if len(set(ids)) != len(ids):
        sys.exit("XATO: id takrorlanishi")


HEADER = f"""-- =====================================================
-- S042: Ordinatura specialities (h_speciality + h_speciality_year)
-- =====================================================
-- Source: docs/klassifikator/hemishe_h_speciality_ordinatura.xlsx — the ministry's export of the
--         frozen CUBA table `hemishe_h_speciality_ordinatura` (69 live rows, all 8-digit 8091*).
--         Generated by domain/etl/speciality/generate_s042_ordinatura.py — DO NOT hand-edit,
--         re-run the ETL.
-- Scope:  the classifier stops being bachelor+master only. education_type = '13' (Ordinatura),
--         already seeded by V022 and admitted by the CHECK that M017 widens immediately before
--         this changeset — M017 MUST stay ordered above S042 or every INSERT below fails 23514.
-- Rows:   69, not the 70 the live legacy table holds. The 70th ({EXCLUDED_LEGACY_ID[:8]}…,
--         code 17.00.09 'Raqs sanʼati') is a stray: its _parent points outside the ordinatura
--         table and its code uses the scientific-degree NN.NN.NN shape rather than the 8-digit
--         classifier shape. The ministry's xlsx omits it and so does this seed.
-- Tree:   71 rows, not 69 — the two ancestor categories are cloned for education_type '13':
--             900000 SOGʻLIQNI SAQLASH VA IJTIMOIY TAʼMINOT   (L1, parent NULL)
--               └─ 910000 Sogʻliqni saqlash                   (L2)
--                    └─ the 69 imported specialities          (L3)
--         Every education type in this table already keeps its OWN complete copy of the tree:
--         Bakalavr and Magistr each have 18 L1 + 59 L2 rows carrying identical codes and different
--         ids (900000 = a3796ec1 for '11' and ae3b0823 for '12'; 910000 = 960be177 and 1f1187c0).
--         This is the third copy of the one branch ordinatura needs, with names, review status and
--         audit flags taken verbatim from those twins.
--         Why not simply reuse 960be177, the Bakalavr 910000 that 18 of the source rows already
--         name as their _parent? Because in the LEGACY schema that is the only value possible —
--         hemishe_h_speciality_ordinatura._parent has an FK onto hemishe_h_speciality_bachelor(id)
--         and CUBA types the field as HSpecialityBachelor — while in the unified classifier
--         getTree() filters by education type and surfaces a node whose parent is outside the
--         filtered set AS A ROOT. A '11' parent therefore rendered the Ordinatura view as 69 flat
--         rows with no hierarchy at all. Cloning also restores the invariant that held across all
--         5603 pre-existing rows: a child's education_type always equals its parent's.
--         Anything that ever syncs ordinatura back into the legacy table must translate this L2 id
--         to 960be177 — LegacySpecialitySyncService covers '11'/'12' only and does not touch these.
-- Year:   {YEAR} for all 71, ancestors included. The legacy _year column is empty and this
--         classifier requires years (S015 header), so the edition year is supplied here; every one
--         of the 36 L1 and 118 L2 rows already in the table carries at least one year, so the two
--         new categories must too. {YEAR} rather than the twins' 2021 because that is when this
--         branch enters the classifier — and the year filter prunes on descendants anyway (a branch
--         survives iff it has a surviving child). The S015 parent-year backfill only touches
--         hierarchy_level 4, so it never reaches any of these rows.
-- id:     the legacy UUID verbatim. LegacySpecialitySyncService bridges h_speciality back into the
--         frozen hemishe_h_speciality_* tables BY UUID, so reusing the legacy id keeps that bridge
--         and any existing reference pointing at one row instead of forking it into two.
-- is_checked: the source value (35 of the 69 are true) — unlike S014/S017 rows, which are born
--         false. These are copied verbatim from a table CUBA and Univer already serve; flipping
--         the flag would make the two copies disagree for no reason.
-- ROW VERSION: -1 (ministry instruction), which is this table's own baseline rather than an
--         oddity — 5512 of the 5603 pre-existing rows carry -1, including all four ancestor twins.
--         The higher values are the handful of rows edited through the UI since (60 at 1, 41 at 2,
--         11 at 3, ...), because @Version increments from wherever a row starts. Inserting at 1
--         would have made every imported row look as if it had already been curated once. A
--         negative seed is fine for optimistic locking: the first edit writes 0, then 1, 2, ...
-- review_status APPROVED (official ministry classifier, like S014/S017) => distributable. A seed
--         INSERT does not push: only HSpecialityService.update() emits an outbox event.
-- name_uz: apostrophes folded to the project-standard letters (oʻ/gʻ = U+02BB, ʼ = U+02BC), as in
--         S014/S017. Cosmetic for identity — h_speciality_fold() maps every variant to a space.
-- name_oz: NULL for all 69 — the source carries a Russian column and no Uzbek-Cyrillic one.
-- CORRECTION (documented, one row): 19d8acc7 (80910725 Parodontologiya) carried the ENGLISH word
--         'Periodontology' in name_ru with name_en empty. English text in the Russian column
--         renders as English to a Russian reader, so it is moved to name_en and name_ru left NULL.
--         Nothing is lost — that value was never Russian.
-- Duplicated codes are legal and intentional: 80910714 and 80910715 each name two different
--         specialities in the source. uq_h_speciality_identity is (education_type, code,
--         name_search), so both members of each pair are distinct rows.
-- Idempotent: ON CONFLICT (id) DO NOTHING never overwrites a later FE edit; the year rows use
--         ON CONFLICT (speciality_id, year) DO NOTHING.
-- Additive: hemishe_h_speciality_ordinatura and the 175/175 legacy contract are NOT touched.
-- =====================================================

"""


def emit(rows: list[dict]) -> None:
    lines = [HEADER]
    lines.append(
        "INSERT INTO h_speciality "
        "(id,code,name_uz,name_oz,name_ru,name_en,education_type,review_status,"
        "parent_id,hierarchy_level,active,is_checked,version) VALUES\n"
    )
    def row_sql(row_id, code, name_uz, name_ru, name_en, parent, level, checked):
        return (
            "  ({id},{code},{uz},NULL,{ru},{en},'{edu}','APPROVED',{parent},{lvl},true,{checked},{version})"
        ).format(
            id=sql_str(row_id), code=sql_str(code), uz=sql_str(name_uz),
            ru=sql_str(name_ru), en=sql_str(name_en), edu=EDUCATION_TYPE,
            parent=sql_str(parent), lvl=level,
            checked="true" if checked else "false", version=VERSION,
        )

    values = [
        "  -- Ancestor categories, cloned for education_type '13' (see the Tree note above)",
        row_sql(ANCESTOR_L1_ID, ANCESTOR_L1_CODE, ANCESTOR_L1_NAME, None, None, None, 1, False),
        row_sql(ANCESTOR_L2_ID, ANCESTOR_L2_CODE, ANCESTOR_L2_NAME, None, None, ANCESTOR_L1_ID, 2, False),
        "  -- The 69 imported specialities",
    ]
    for row in sorted(rows, key=lambda r: (r["code"] or "", r["name_uz"])):
        values.append(row_sql(row["id"], row["code"], row["name_uz"], row["name_ru"],
                              row["name_en"], ANCESTOR_L2_ID, HIERARCHY_LEVEL, row["is_checked"]))
    # A comment line must not be followed by a comma, and the row before it must still carry one.
    body = ""
    for i, line in enumerate(values):
        is_comment = line.lstrip().startswith("--")
        nxt_real = next((v for v in values[i + 1:] if not v.lstrip().startswith("--")), None)
        body += line + ("" if is_comment or nxt_real is None else ",") + "\n"
    lines.append(body.rstrip("\n"))
    lines.append("\nON CONFLICT (id) DO NOTHING;\n\n")

    lines.append(
        f"-- Edition year {YEAR} for all {len(rows) + 2} rows, the two ancestor categories included — every L1 and\n"
        "-- L2 row already in this table carries at least one year, so these must too. Scoped to this\n"
        f"-- seed's own ids rather than \"every education_type = '{EDUCATION_TYPE}' row\", so a re-run never\n"
        f"-- stamps {YEAR} onto an ordinatura speciality a curator later added with a different year.\n"
    )
    lines.append("INSERT INTO h_speciality_year (speciality_id, year) VALUES\n")
    year_values = [f"  ('{ANCESTOR_L1_ID}',{YEAR})", f"  ('{ANCESTOR_L2_ID}',{YEAR})"] + [
        f"  ('{row['id']}',{YEAR})"
        for row in sorted(rows, key=lambda r: (r["code"] or "", r["name_uz"]))
    ]
    lines.append(",\n".join(year_values))
    lines.append("\nON CONFLICT (speciality_id, year) DO NOTHING;\n\n")

    total = len(rows) + 2
    lines.append(f"""-- Verification: report, never fail. A shortfall means rows pre-existed under a different id
-- (an FE-created duplicate) — worth a look, but not a reason to abort a migration. The orphan
-- check is the one that matters: it is what was wrong before the ancestors existed, and a
-- non-zero count means the Ordinatura view has gone back to rendering a flat list.
DO $$
DECLARE
    spec_count   INTEGER;
    year_count   INTEGER;
    orphan_count INTEGER;
BEGIN
    SELECT count(*) INTO spec_count FROM h_speciality WHERE education_type = '{EDUCATION_TYPE}';
    SELECT count(*) INTO year_count
      FROM h_speciality_year y
      JOIN h_speciality s ON s.id = y.speciality_id
     WHERE s.education_type = '{EDUCATION_TYPE}' AND y.year = {YEAR};
    SELECT count(*) INTO orphan_count
      FROM h_speciality c
      LEFT JOIN h_speciality p ON p.id = c.parent_id
     WHERE c.education_type = '{EDUCATION_TYPE}'
       AND c.parent_id IS NOT NULL
       AND (p.id IS NULL OR p.education_type <> c.education_type);
    RAISE NOTICE 'S042: ordinatura qatorlari = %, {YEAR} yil bogʻlami = % (kutilgan: {total})',
        spec_count, year_count;
    IF spec_count < {total} THEN
        RAISE NOTICE 'S042 OGOHLANTIRISH: {total} ta kutilgan edi, % ta topildi', spec_count;
    END IF;
    IF orphan_count > 0 THEN
        RAISE NOTICE 'S042 OGOHLANTIRISH: % ta ordinatura qatorining otasi boshqa taʼlim turida — daraxt tekis chiqadi', orphan_count;
    END IF;
END $$;
""")
    OUT_SQL.write_text("".join(lines), encoding="utf-8")

    ids = ",\n    ".join(f"'{row['id']}'" for row in sorted(rows, key=lambda r: r["id"]))
    ancestor_ids = f"'{ANCESTOR_L1_ID}',\n    '{ANCESTOR_L2_ID}'"
    all_ids = ids + f",\n    '{ANCESTOR_L1_ID}',\n    '{ANCESTOR_L2_ID}'"
    OUT_ROLLBACK.write_text(f"""-- =====================================================
-- S042 ROLLBACK: remove the {len(rows)} Ordinatura specialities, their two ancestor categories and
--                the {YEAR} year links of all {len(rows) + 2}
-- =====================================================
-- Deletes by explicit id, so an ordinatura row a curator created through the UI after this seed
-- survives the rollback — a rollback undoes what the changeset did, not what people did later.
--
-- Four statements, in this order, and the order is load-bearing:
--   1. the year rows (h_speciality_year.speciality_id FKs into h_speciality; ON DELETE CASCADE
--      would cover it, but deleting them explicitly keeps the intent readable),
--   2. the 69 leaves,
--   3. the two ancestor categories — SEPARATELY, and only after the leaves are gone. They cannot
--      go in the same statement: fk_h_speciality_parent is ON DELETE RESTRICT, and a single DELETE
--      evaluates its NOT EXISTS guard against one snapshot, so the L2 category would still look
--      like a parent and survive.
-- And the whole changeset must unwind BEFORE M017 re-narrows chk_h_speciality_edu_type to
-- ('11','12') — Liquibase unwinds in execution order and M017 runs immediately before this seed,
-- so it unwinds immediately after. M017's own rollback names any surviving row if that is broken.
--
-- An attached speciality is NOT deleted: university_speciality_attachment FKs into h_speciality
-- with RESTRICT, so a DELETE would fail with 23503 mid-rollback. Those rows are left in place and
-- named in a NOTICE instead — an attachment is real ministry data and outranks a tidy rollback.
-- The same applies to the ancestors: if a curator has hung a new ordinatura speciality off the
-- cloned category, the childless guard leaves that category standing rather than orphaning it.
-- =====================================================

DO $$
DECLARE
    blocked INTEGER;
BEGIN
    SELECT count(*) INTO blocked
      FROM university_speciality_attachment a
     WHERE a.speciality_id IN (
    {ids}
    );
    IF blocked > 0 THEN
        RAISE NOTICE 'S042 rollback: % ta ordinatura mutaxassisligi OTM''ga biriktirilgan — o''chirilmaydi', blocked;
    END IF;
END $$;

-- 1. Year links (leaves + the two ancestor categories)
DELETE FROM h_speciality_year
 WHERE year = {YEAR}
   AND speciality_id IN (
    {all_ids}
   );

-- 2. The 69 imported specialities
DELETE FROM h_speciality s
 WHERE s.id IN (
    {ids}
   )
   AND NOT EXISTS (SELECT 1 FROM university_speciality_attachment a WHERE a.speciality_id = s.id)
   AND NOT EXISTS (SELECT 1 FROM h_speciality c WHERE c.parent_id = s.id);

-- 3. The two cloned ancestor categories — childless only, and only now that the leaves are gone
DELETE FROM h_speciality s
 WHERE s.id IN (
    {ancestor_ids}
   )
   AND NOT EXISTS (SELECT 1 FROM university_speciality_attachment a WHERE a.speciality_id = s.id)
   AND NOT EXISTS (SELECT 1 FROM h_speciality c WHERE c.parent_id = s.id);
""", encoding="utf-8")


def main() -> None:
    rows = load_rows()
    check_identity(rows)
    emit(rows)
    print(f"{len(rows)} ta mutaxassislik + 2 ta ota-kategoriya -> {OUT_SQL.name} + {OUT_ROLLBACK.name}")
    print(f"  L1 {ANCESTOR_L1_CODE} = {ANCESTOR_L1_ID}")
    print(f"  L2 {ANCESTOR_L2_CODE} = {ANCESTOR_L2_ID}   (69 ta barg shu ostida)")
    checked = sum(1 for r in rows if r["is_checked"])
    print(f"  is_checked=true: {checked}/{len(rows)}   name_ru bor: "
          f"{sum(1 for r in rows if r['name_ru'])}   name_en bor: {sum(1 for r in rows if r['name_en'])}")


if __name__ == "__main__":
    main()
