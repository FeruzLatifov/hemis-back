---
name: liquibase-changeset
description: Yangi Liquibase migration yaratish (V/M/S### + rollback + master.yaml). Trigger - "yangi migration", "jadval qo'sh", "kolonka qo'sh", "schema o'zgartirish", "Liquibase changeset".
allowed-tools: Read, Write, Edit, Bash, Grep, Glob
---

# Create Liquibase Changeset

## Prefiks tanlash

| Prefiks | Maqsad | Misol |
|---------|--------|-------|
| `V###` | Schema (DDL) — CREATE/ALTER TABLE, INDEX | `V016_create_employee_audit.sql` |
| `M###` | Data migration · index legacy `hemishe_*` jadval uchun | `M003_add_idx_student_pinfl.sql` |
| `S###` | Seed/reference data (INSERT ON CONFLICT) | `S005_seed_h_position.sql` |

## Workflow

### 1. Keyingi raqam

```bash
REPO=$(git rev-parse --show-toplevel)
CS_DIR="$REPO/domain/src/main/resources/db/changelog/changesets"
ls "$CS_DIR"/schema/V[0-9]*.sql | grep -v rollback | sort | tail -1
# → keyingisi +1, 3-zero-pad: V016
```

### 2. ADR-0008 violation tekshirish (kritik)

```bash
# hemishe_* jadvallarga ALTER/DROP/RENAME TAQIQ
grep -iE "(ALTER|DROP|RENAME).*(TABLE|COLUMN).+hemishe_" <new.sql> && echo "🚨 BLOCK: legacy table modification"
```
Faqat `M###` orqali **index/MV qo'shish** ruxsat. ALTER COLUMN/DROP — Univer 224 OTM split-brain.

### 3. Forward fayl yaratish

`<CS_DIR>/schema/V###_short_name.sql` (yoki `data/M###_*.sql` / `seed/S###_*.sql`):

```sql
--liquibase formatted sql

--changeset hemis:V###_short_name splitStatements:false
--comment: ADR-NNNN — qisqa sabab

CREATE TABLE IF NOT EXISTS h_xxx (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(64) NOT NULL UNIQUE,
    name_uz      VARCHAR(255) NOT NULL,
    name_ru      VARCHAR(255),
    name_en      VARCHAR(255),
    created_at   TIMESTAMPTZ DEFAULT now(),
    updated_at   TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_h_xxx_code ON h_xxx(code);
```

Idempotency majburiy: `IF NOT EXISTS` / `ON CONFLICT DO NOTHING`. PL/pgSQL bloki bo'lsa `splitStatements:false`.

### 4. Rollback fayl (MAJBURIY)

`V###_short_name_rollback.sql`:

```sql
--liquibase formatted sql
--changeset hemis:V###_short_name_rollback splitStatements:false

DROP INDEX IF EXISTS idx_h_xxx_code;
DROP TABLE IF EXISTS h_xxx;
```

Rollback **DDL'ni teskari tartibda** + `IF EXISTS`. Data uchun: backup table'dan qayta tiklash yoki dump'dan.

### 5. master.yaml ro'yxat

`domain/src/main/resources/db/changelog/db.changelog-master.yaml` ichiga to'g'ri kategoriyaga (schema/data/seed):

```yaml
  - include:
      file: db/changelog/changesets/schema/V###_short_name.sql
```

> Yo'q bo'lsa migration ishga tushmaydi — eng tez-tez xatolar manbai.

### 6. Apply va status

```bash
./gradlew :domain:liquibaseUpdate
./gradlew :domain:liquibaseStatus      # 0 ta pending bo'lishi kerak
```

### 7. Rollback sinash (lokal)

```bash
./gradlew :domain:liquibaseRollbackCount -PliquibaseCommandValue=1
./gradlew :domain:liquibaseUpdate       # qayta apply
```

## Constraints

- ❌ `hemishe_e_*` jadvallarga ALTER/DROP/RENAME (faqat M### orqali index/MV)
- ❌ Rollback fayl yo'q — pre-commit reject
- ❌ master.yaml'ga qo'shilmagan — silently skip
- ❌ `splitStatements` default'i bilan PL/pgSQL — buziladi
- ❌ Idempotent emas SQL (qayta apply'da xato)
- ✅ ADR ID changeset comment'ida (`comment: "ADR-0006 — ..."`)

## See also

- `.claude/LIQUIBASE_GUIDE.md` — to'liq qoidalar
- `.claude/agents/liquibase-reviewer.md` — review checklist
- `domain/src/main/resources/db/changelog/changesets/` — mavjud misollar
- ADR-0006 (h_ prefiks) · ADR-0008 (legacy table protect)
