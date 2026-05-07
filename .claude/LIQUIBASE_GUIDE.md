# Liquibase Migration Guide

> Database schema o'zgartirish uchun migration yaratish (V001-V014, M001+, S001+ patterni)

---

## Golden Rules

1. **Naming convention:** `V###_short_name.sql` (forward) + `V###_short_name_rollback.sql` (rollback). Prefiks majburiy:
   - `V###` — schema (DDL: CREATE TABLE, ALTER TABLE, INDEX)
   - `M###` — data migration yoki performance index (legacy `hemishe_*` jadvallar uchun ham)
   - `S###` — seed (reference data, INSERT ... ON CONFLICT)
2. **Idempotency** — `IF NOT EXISTS` / `ON CONFLICT DO NOTHING` ishlatish. Qayta ishga tushirilsa xato bermasligi kerak.
3. **Rollback** — Har bir migration uchun rollback script MAJBURIY. `splitStatements: false` — PL/pgSQL bloklar uchun.
4. **No direct DDL** — Manual `ALTER` / `DROP` qilmaslik, faqat Liquibase orqali.
5. **Master.yaml majburiy** — Yangi changeset `db.changelog-master.yaml` ga qo'shilmasa ishga tushmaydi.
6. **Legacy `hemishe_*` jadvallarga ALTER/DROP/RENAME TAQIQLANADI** — Univer 224 OTM sync. Faqat `M###` orqali index/MV qo'shish ruxsat.
7. **ADR ID changeset comment'da** — qaror sababi: `comment: "ADR-0006 — h_position classifier"`.

---

## Fayl Strukturasi

```
domain/src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Master fayl (changeset tartibi)
└── changesets/
    ├── schema/                       # V### — DDL
    │   ├── V015_create_X.sql
    │   └── V015_create_X_rollback.sql
    ├── seed/                         # S### — reference data
    │   ├── S011_seed_X.sql
    │   └── S011_seed_X_rollback.sql
    └── migration/                    # M### — data migration / legacy indexlar
        ├── M004_classifier_fk_indexes.sql
        └── M004_classifier_fk_indexes_rollback.sql
```

Har SQL fayl boshida: version, author (`hemis-team`), date, ADR ID (agar bo'lsa), description.

---

## Migration Yaratish

### 1. Rejalashtirish

- Nima o'zgaradi? (yangi jadval, ustun, seed data, data fix)
- Qaysi ADR'ga asoslangan? (ADR ID changeset comment'da yoziladi)
- Rollback mumkinmi? Ma'lumotlarga ta'siri?
- Idempotent? Ikki marta ishga tushsa xavfsizmi?

### 2. Forward Migration (V### namuna)

```sql
-- V015: Create department table
-- Author: hemis-team
-- Date: 2026-05-07
-- ADR: ADR-0006 (klassifikator emas — prefiks-siz)

CREATE TABLE IF NOT EXISTS department (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    parent_id UUID REFERENCES department(id) ON DELETE SET NULL,
    faculty_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Soft-delete bilan Partial UNIQUE (rules.md "Soft-Delete UNIQUE")
CREATE UNIQUE INDEX IF NOT EXISTS uq_department_code
    ON department(code) WHERE deleted_at IS NULL;

-- FK Index Mandate (rules.md "FK Index Mandate")
CREATE INDEX IF NOT EXISTS idx_department_parent
    ON department(parent_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_department_faculty
    ON department(faculty_id) WHERE deleted_at IS NULL;

COMMENT ON TABLE department IS 'Organizational department structure (ADR-NNNN)';
```

### 3. Rollback Script (V###_rollback.sql)

```sql
-- Rollback for V015: Drop department table
DROP TABLE IF EXISTS department CASCADE;
```

### 4. Master changelog'ga qo'shish

`db.changelog-master.yaml`'ga changeSet bloki:

```yaml
- changeSet:
    id: V015_create_department
    author: hemis-team
    logicalFilePath: ${changelog.path}
    comment: "ADR-NNNN — department domain"
    preConditions:
      - onFail: MARK_RAN
      - not: { tableExists: { tableName: department } }
    changes:
      - sqlFile: { path: changesets/schema/V015_create_department.sql, relativeToChangelogFile: true, splitStatements: false }
    rollback:
      - sqlFile: { path: changesets/schema/V015_create_department_rollback.sql, relativeToChangelogFile: true, splitStatements: false }
```

### 5. Test va Verifikatsiya

```bash
./gradlew :domain:liquibaseStatus       # Pending changesetlar ro'yxati
./gradlew :domain:liquibaseUpdateSQL    # SQL preview (ishlatmasdan)
./gradlew :domain:liquibaseUpdate       # Qo'llash
./scripts/check_table_mappings.sh       # Entity ↔ DB moslik
```

---

## Common patterns

| Holat | Pattern |
|-------|---------|
| Yangi jadval | `V### CREATE TABLE` (yuqorida) |
| Ustun qo'shish | `ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...` + default qiymat |
| Seed data | `S### INSERT INTO ... ON CONFLICT (code) DO NOTHING` (yoki `DO UPDATE` agar idempotent rebuild kerak) |
| Legacy `hemishe_*` performance | `M### CREATE INDEX CONCURRENTLY` (CONCURRENTLY transaction'da ishlamaydi — alohida changeset) |
| Materialized view | `M### CREATE MATERIALIZED VIEW IF NOT EXISTS` + `REFRESH MATERIALIZED VIEW CONCURRENTLY` |

---

## Anti-patterns (TAQIQLANADI)

- ❌ Eski `07-add-X-table.sql` naming (V###/M###/S### prefiks majburiy)
- ❌ `hemishe_*` jadvallarga ALTER TABLE / DROP TABLE / RENAME COLUMN
- ❌ Cross-changeset `ALTER TABLE ... ADD CONSTRAINT` (FK inline yoziladi)
- ❌ Rollback fayl `-- TODO` bilan bo'sh qoldirish
- ❌ Master.yaml ga qo'shmasdan changeset commit qilish
- ❌ FK declaration index'siz (har FK ga partial index majburiy)
- ❌ Soft-delete jadvallarda oddiy UNIQUE (Partial UNIQUE majburiy)

---

## See also

- `.claude/rules.md` — "Cross-Cutting Database Rules" (FK Index Mandate, Soft-Delete UNIQUE, Naming Exceptions)
- `.claude/agents/liquibase-reviewer.md` — automated changeset audit
- `domain/src/main/resources/db/changelog/db.changelog-master.yaml` — master indeks
- `docs/adr/0006-classifier-h-prefix.md` — `h_*` prefiks qachon
