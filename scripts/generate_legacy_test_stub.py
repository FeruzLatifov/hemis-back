#!/usr/bin/env python3
"""
Integratsiya testlari uchun eski CUBA (old-hemis) jadvallarining stub DDL'ini generatsiya qiladi.

NEGA KERAK
----------
`db.changelog-master.yaml` O'ZINI O'ZI TA'MINLAMAYDI: u eski CUBA jadvallariga FK /
SELECT / CREATE INDEX bilan tayanadi, lekin ularni yaratmaydi — real muhitlarda ular
old-hemis dump'idan tiklangan bo'ladi. Shu sababli toza PostgreSQL'da (Testcontainers)
migratsiya yiqilardi va `app` integratsiya testlari hech qachon ishlamagan; ular
jimgina dasturchining REAL lokal bazasiga ulanib turgan.

Bu skript `domain` moduldagi @Entity mapping'laridan stub DDL yasaydi — ya'ni manba
kodning O'ZI haqiqat manbai bo'ladi, qo'lda yozilgan ro'yxat emas.

ISHLATISH
---------
    python3 scripts/generate_legacy_test_stub.py

Natija: app/src/test/resources/db/testfixture/legacy-cuba-stub.sql (qayta yoziladi).

QACHON QAYTA ISHGA TUSHIRISH
----------------------------
  · yangi legacy @Entity qo'shilganda yoki mavjudiga ustun qo'shilganda
  · S018 seed'idagi OTM kodlari o'zgarganda
  · integratsiya testi "relation ... does not exist" yoki "column ... does not exist"
    bilan yiqilganda

CHEKLOV
-------
Bu real legacy schema'ning nusxasi EMAS — faqat migratsiya va testlar o'tishi uchun
yetarli minimum. Yetishmovchilik BALAND OVOZ bilan yiqiladi (jimgina noto'g'ri
ishlamaydi), shuning uchun drift xavfsiz tarzda oshkor bo'ladi.
"""

from __future__ import annotations

import re
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
ENTITY_ROOT = REPO / "domain/src/main/java/uz/hemis/domain/entity"
CHANGESETS = REPO / "domain/src/main/resources/db/changelog/changesets"
SEED_S018 = CHANGESETS / "seed/S018_seed_speciality_attachment_2026.sql"
OUT = REPO / "app/src/test/resources/db/testfixture/legacy-cuba-stub.sql"

LEGACY_PREFIXES = ("hemishe_", "sec_")

JAVA_TO_SQL = {
    "String": "TEXT",
    "Integer": "INTEGER", "int": "INTEGER",
    "Long": "BIGINT", "long": "BIGINT",
    "Short": "SMALLINT", "short": "SMALLINT",
    "Boolean": "BOOLEAN", "boolean": "BOOLEAN",
    "LocalDateTime": "TIMESTAMP", "Instant": "TIMESTAMP", "OffsetDateTime": "TIMESTAMP",
    "LocalDate": "DATE", "LocalTime": "TIME",
    "UUID": "UUID",
    "BigDecimal": "NUMERIC", "Double": "DOUBLE PRECISION", "double": "DOUBLE PRECISION",
    "Float": "REAL", "float": "REAL",
    "byte[]": "BYTEA",
}

# Migratsiyalar talab qiladigan, lekin entity map qilmaydigan ustunlar.
# (M002b/c/d/e indekslari, M003 materialized view — o'lchangan.)
EXTRA_COLUMNS = {
    "hemishe_e_student": {
        "code": "TEXT", "pinfl": "TEXT", "firstname": "TEXT", "lastname": "TEXT",
        "is_duplicate": "BOOLEAN", "create_ts": "TIMESTAMP", "delete_ts": "TIMESTAMP",
        "_course": "TEXT", "_education_form": "TEXT", "_education_type": "TEXT",
        "_education_year": "TEXT", "_faculty": "TEXT", "_gender": "TEXT",
        "_payment_form": "TEXT", "_student_status": "TEXT", "_university": "TEXT",
        "_speciality_bachelor": "TEXT", "_speciality_master": "TEXT",
        "_speciality_ordinatura": "TEXT",
    },
    "hemishe_e_student_meta": {"u_id": "TEXT", "_university": "TEXT", "delete_ts": "TIMESTAMP"},
    "hemishe_e_student_diploma": {"diploma_number": "TEXT", "delete_ts": "TIMESTAMP"},
    "hemishe_h_education_year": {"code": "TEXT", "name": "TEXT", "name_ru": "TEXT",
                                 "name_en": "TEXT", "active": "BOOLEAN", "delete_ts": "TIMESTAMP"},
    "hemishe_h_education_form": {"code": "TEXT", "name": "TEXT", "name_ru": "TEXT",
                                 "name_en": "TEXT", "active": "BOOLEAN", "delete_ts": "TIMESTAMP"},
}

# Changelog FK bilan tayanadigan, lekin entity'si bo'lmasligi mumkin bo'lgan jadvallar.
FK_ONLY_TABLES = {
    "hemishe_h_gender", "hemishe_h_nationality", "hemishe_h_citizenship",
    "hemishe_h_academic_degree", "hemishe_h_academic_rank", "hemishe_h_education_type",
    "hemishe_h_university_employee_form", "hemishe_h_university_employee_rate",
    "hemishe_h_university_activity_status", "hemishe_h_education_year",
    "hemishe_h_education_form", "hemishe_e_university", "hemishe_e_university_department",
}

# LegacyClassifierEntity shakli — barcha hemishe_h_* klassifikatorlari shu bazadan meros oladi.
CLASSIFIER_SHAPE = [
    ("code", "TEXT"), ("name", "TEXT"), ("name_ru", "TEXT"), ("name_en", "TEXT"),
    ("active", "BOOLEAN"), ("version", "INTEGER"),
    ("create_ts", "TIMESTAMP"), ("created_by", "TEXT"),
    ("update_ts", "TIMESTAMP"), ("updated_by", "TEXT"),
    ("delete_ts", "TIMESTAMP"), ("deleted_by", "TEXT"),
]

COLUMN_RE = re.compile(
    r'@(?:Column|JoinColumn)\(\s*name\s*=\s*"([^"]+)"[^)]*\)\s*'
    r'(?:@[^\n]*\n\s*)*'
    r'(?:private|protected|public)\s+([A-Za-z_][\w<>,\.\s\[\]]*?)\s+(\w+)\s*[;=]',
    re.S,
)
TABLE_RE = re.compile(r'@Table\(\s*(?:.*?)name\s*=\s*"([a-z0-9_]+)"', re.S)
EXTENDS_RE = re.compile(r'\bclass\s+\w+\s+extends\s+(\w+)')


NATIVE_SQL_RE = re.compile(
    r"SELECT\s+(.+?)\s+FROM\s+((?:hemishe|sec)_[a-z0-9_]+)", re.I | re.S
)
COLUMN_TOKEN_RE = re.compile(r"^[a-z_][a-z0-9_]*$", re.I)

SQL_NOISE = {
    "distinct", "as", "case", "when", "then", "else", "end", "null", "true", "false",
    "count", "sum", "min", "max", "avg", "coalesce", "nullif", "cast", "and", "or", "not",
}


def native_sql_columns() -> dict[str, set[str]]:
    """`src/main` dagi xom SQL'lardan legacy jadval ustunlarini yig'adi.

    Nega kerak: entity mapping'i to'liq emas. Masalan `hemishe_h_soato` ni
    StudentLegacyMapper `SELECT code, name_uz, name_ru, parent_code, version` bilan
    o'qiydi, entity esa `LegacyClassifierEntity` shaklida (`name`, `name_en` ...).
    Stub'da `name_uz` bo'lmasa so'rov yiqiladi, chaqiruvchi esa xatoni yutadi
    (`catch → log.warn`) va natija JIMGINA bo'sh bo'ladi — aynan stub sarlavhasi
    va'da qilgan "baland ovoz" buzilgan holat. Shu sababli xom SQL ham skanerlanadi.
    """
    found: dict[str, set[str]] = {}
    for java in REPO.glob("*/src/main/java/**/*.java"):
        text = java.read_text(encoding="utf-8", errors="replace")
        if "hemishe_" not in text and "sec_user" not in text:
            continue
        for m in NATIVE_SQL_RE.finditer(text):
            select_list, table = m.group(1), m.group(2).lower()
            if "*" in select_list or len(select_list) > 400:
                continue
            for raw in select_list.split(","):
                token = raw.strip().strip('"').split()[0] if raw.strip() else ""
                token = token.strip('"').split(".")[-1]
                if not COLUMN_TOKEN_RE.match(token) or token.lower() in SQL_NOISE:
                    continue
                found.setdefault(table, set()).add(token)
    return found


def java_files() -> list[Path]:
    return sorted(ENTITY_ROOT.rglob("*.java"))


def sql_type(java_type: str) -> str:
    base = java_type.split("<")[0].strip().replace("final ", "")
    return JAVA_TO_SQL.get(base, "TEXT")


def collect_columns(path: Path, by_class: dict[str, Path], depth: int = 0) -> list[tuple[str, str]]:
    """Entity + uning superclass zanjiridan ustunlarni yig'adi."""
    if depth > 6:
        return []
    src = path.read_text(encoding="utf-8", errors="replace")
    cols: list[tuple[str, str]] = []
    parent = EXTENDS_RE.search(src)
    if parent and parent.group(1) in by_class:
        cols.extend(collect_columns(by_class[parent.group(1)], by_class, depth + 1))
    for m in COLUMN_RE.finditer(src):
        cols.append((m.group(1), sql_type(m.group(2))))
    return cols


def changelog_creates() -> set[str]:
    created = set()
    for p in CHANGESETS.rglob("*.sql"):
        text = p.read_text(encoding="utf-8", errors="replace")
        for m in re.finditer(
            r"CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:public\.)?([a-z0-9_]+)", text, re.I
        ):
            created.add(m.group(1).lower())
    return created


def university_codes() -> list[str]:
    if not SEED_S018.exists():
        return []
    text = SEED_S018.read_text(encoding="utf-8", errors="replace")
    return sorted({m.group(1) for m in re.finditer(r"^\s*\('[0-9a-f-]{36}',\s*'([^']+)'", text, re.M)})


def quote(name: str) -> str:
    return f'"{name}"' if name.startswith("_") or name.endswith("_") else name


def main() -> None:
    files = java_files()
    by_class = {p.stem: p for p in files}

    tables: dict[str, list[tuple[str, str]]] = {}
    for path in files:
        src = path.read_text(encoding="utf-8", errors="replace")
        m = TABLE_RE.search(src)
        if not m:
            continue
        table = m.group(1)
        if not table.startswith(LEGACY_PREFIXES):
            continue
        cols = collect_columns(path, by_class)
        if not cols and table.startswith("hemishe_h_"):
            cols = list(CLASSIFIER_SHAPE)
        tables.setdefault(table, [])
        tables[table] = cols or tables[table]

    for t in FK_ONLY_TABLES:
        tables.setdefault(t, list(CLASSIFIER_SHAPE))

    created = changelog_creates()
    tables = {t: c for t, c in tables.items() if t not in created}

    for table, extra in EXTRA_COLUMNS.items():
        merged = dict(tables.get(table, []))
        for name, typ in extra.items():
            merged.setdefault(name, typ)
        tables[table] = list(merged.items())

    # Xom SQL'da o'qiladigan, lekin entity map qilmaydigan ustunlar
    # (masalan hemishe_h_soato.name_uz / parent_code)
    for table, cols in native_sql_columns().items():
        if table in created or table not in tables:
            continue
        merged = dict(tables[table])
        for name in sorted(cols):
            merged.setdefault(name, "TEXT")
        tables[table] = list(merged.items())

    # Klassifikatorlar to'liq shaklga ega bo'lsin (JPA ularni o'qiydi)
    for table, cols in tables.items():
        if not table.startswith("hemishe_h_"):
            continue
        merged = dict(cols)
        for name, typ in CLASSIFIER_SHAPE:
            merged.setdefault(name, typ)
        tables[table] = list(merged.items())

    out: list[str] = [HEADER.format(count=len(tables))]

    for table in sorted(tables):
        cols = tables[table]
        seen: dict[str, str] = {}
        for name, typ in cols:
            seen.setdefault(name, typ)
        if "code" in seen:
            pk = "code"
        elif "id" in seen:
            pk = "id"
        else:
            pk = None
            seen["stub_id"] = "UUID"
        lines = []
        for name, typ in seen.items():
            suffix = " PRIMARY KEY" if name == pk else ""
            if name == "id" and pk == "id":
                typ = "UUID"
                suffix = " PRIMARY KEY DEFAULT gen_random_uuid()"
            lines.append(f"    {quote(name):38} {typ}{suffix}")
        out.append(f"CREATE TABLE IF NOT EXISTS {table} (\n" + ",\n".join(lines) + "\n);\n")

    codes = university_codes()
    if codes:
        rows = [
            "    " + ", ".join(f"('{c}', 0, 'OTM {c}')" for c in codes[i:i + 8])
            for i in range(0, len(codes), 8)
        ]
        out.append(UNIVERSITY_SEED.format(count=len(codes), rows=",\n".join(rows)))

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(out), encoding="utf-8")
    print(f"{OUT.relative_to(REPO)} — {len(tables)} jadval, {len(codes)} OTM kodi")


HEADER = """-- =====================================================
-- TEST FIXTURE — eski CUBA (old-hemis) jadvallarining stub'i ({count} jadval)
-- =====================================================
-- ⚠️ GENERATSIYA QILINGAN — QO'LDA TAHRIRLAMANG.
--    Manba: scripts/generate_legacy_test_stub.py (domain @Entity mapping'laridan)
--    Qayta generatsiya: python3 scripts/generate_legacy_test_stub.py
--
-- FAQAT integratsiya testlari uchun. Prod changelog'iga HECH QACHON qo'shilmaydi —
-- bu fayl app/src/test/resources ostida va uni yagona ishlatuvchi
-- IntegrationTestDatabaseConfig.
--
-- NEGA KERAK:
--   db.changelog-master.yaml o'zini o'zi ta'minlamaydi: u eski CUBA jadvallariga FK /
--   SELECT / CREATE INDEX bilan tayanadi, lekin ularni yaratmaydi (real muhitlarda ular
--   old-hemis dump'idan keladi). Busiz toza PostgreSQL'da migratsiya
--   V004_create_employee da "relation hemishe_h_gender does not exist" bilan to'xtaydi.
--
-- CHEKLOV:
--   Bu real legacy schema'ning nusxasi EMAS — migratsiya va testlar o'tishi uchun
--   yetarli minimum. Yetishmovchilik BALAND OVOZ bilan yiqiladi, jimgina noto'g'ri
--   ishlamaydi.
-- =====================================================
"""

UNIVERSITY_SEED = """-- --- OTM kodlari ({count} ta) — S018 seed'i uchun FK ma'lumot bazasi ---
-- S018_seed_speciality_attachment_2026 fk_univ_spec_attach_univ orqali
-- hemishe_e_university(code) ga bog'lanadi; bo'sh jadval bilan seed FK buzilishi
-- bilan to'xtaydi. Kodlar seed'ning O'ZIDAN olinadi (generator).
--
-- version = 0 MAJBURIY: University.version @Version (optimistic locking). NULL version
-- bilan Hibernate qatorni TRANSIENT deb hisoblaydi va unga bog'langan oauth_client
-- saqlanganda "references an unsaved transient instance" bilan yiqiladi.
INSERT INTO hemishe_e_university (code, version, name) VALUES
{rows}
ON CONFLICT (code) DO NOTHING;
"""


if __name__ == "__main__":
    main()
