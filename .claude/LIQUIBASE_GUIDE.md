# Liquibase Migration Guide

> Database schema o'zgartirish uchun migration yaratish

---

## Golden Rules

1. **Idempotency** — `IF NOT EXISTS` / `ON CONFLICT DO NOTHING` ishlatish. Qayta ishga tushirilsa xato bermasligi kerak
2. **Rollback** — Har bir migration uchun rollback script MAJBURIY. `splitStatements: false` — PL/pgSQL bloklar uchun
3. **Test** — Local → Staging → Production. Hech qachon productionda to'g'ridan-to'g'ri test qilmaslik
4. **Naming** — `07-add-departments-table.sql` + `07-add-departments-table-rollback.sql`
5. **No direct DDL** — Manual `ALTER` / `DROP` qilmaslik, faqat Liquibase orqali

---

## Fayl Strukturasi

```
domain/src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Master fayl (migratsiyalar tartibi)
└── changesets/
    ├── 07-add-departments-table.sql          # Forward migration
    └── 07-add-departments-table-rollback.sql # Rollback script
```

Har bir SQL fayl boshida: version, author, date, description comment.

---

## Migration Yaratish

### 1. Rejalashtirish

- Nima o'zgaradi? (yangi jadval, ustun, seed data, data fix)
- Rollback mumkinmi? Ma'lumotlarga ta'siri?
- Idempotent? Ikki marta ishga tushsa xavfsizmi?

### 2. Forward Migration

```sql
-- V7: Add departments table
-- Author: developer, Date: 2025-11-15

CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    parent_id UUID REFERENCES departments(id) ON DELETE SET NULL,
    level INTEGER NOT NULL DEFAULT 1,
    faculty_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);
CREATE INDEX IF NOT EXISTS idx_departments_parent ON departments(parent_id);
CREATE INDEX IF NOT EXISTS idx_departments_active ON departments(is_active) WHERE deleted_at IS NULL;

COMMENT ON TABLE departments IS 'Organizational departments structure';
```

Ustun qo'shish: `ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...` + default qiymat.
Seed data: `INSERT INTO ... ON CONFLICT DO NOTHING`.

### 3. Rollback Script

```sql
-- Rollback: departments table
DROP TABLE IF EXISTS departments;
-- Ustun uchun: ALTER TABLE ... DROP COLUMN IF EXISTS phone;
```

### 4. Registratsiya

`db.changelog-master.yaml` ga forward va rollback fayllarni qo'shish:

```yaml
- changeLog: classpath:/db/changelog/changesets/07-add-departments-table.sql
- changeLog: classpath:/db/changelog/changesets/07-add-departments-table-rollback.sql
```

### 5. Test

```bash
./gradlew :domain:liquibaseUpdate      # Apply
./gradlew :domain:liquibaseStatus      # Status tekshirish
./gradlew :domain:liquibaseRollback    # Rollback test
```
