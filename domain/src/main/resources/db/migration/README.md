# FLYWAY MIGRATION POLICY

**Database:** test_hemis (ministry.sql)
**Strategy:** Baseline-only (NO destructive DDL allowed)

---

## V1__baseline.sql - DOCUMENTATION ONLY

**Purpose:** Capture current database schema as golden baseline

**CRITICAL:**
- **NOT EXECUTED** by Flyway (existing database)
- **DOCUMENTATION ONLY** - reference for future migrations
- **NO CHANGES** to actual database schema
- Baseline version: 1.0.0

**Usage:**
```bash
# Flyway baseline (mark existing schema as version 1)
./gradlew flywayBaseline

# This sets flyway_schema_history to version 1 without running DDL
```

---

## Flyway Configuration

**File:** `application-dev.yml`

```yaml
spring:
  flyway:
    enabled: false                 # DISABLED - existing database
    baseline-on-migrate: true      # Accept existing schema as baseline
    baseline-version: 1            # Start from version 1
    clean-disabled: true           # NEVER allow flyway clean
    validate-on-migrate: true      # Validate migration checksums
    out-of-order: true             # Allow out-of-order migrations
```

---

## Migration Linter Rules

**PROHIBITED SQL Patterns (CI will FAIL):**

```sql
DROP TABLE             -- FAIL: breaks replication
DROP COLUMN            -- FAIL: schema mismatch
DROP INDEX             -- FAIL: performance regression
ALTER TABLE ... RENAME -- FAIL: breaks replication slot
TRUNCATE               -- FAIL: data loss
DELETE                 -- FAIL: NDG violation (use UPDATE with deleted_ts)
```

**ALLOWED SQL Patterns:**

```sql
CREATE INDEX CONCURRENTLY   -- OK: non-blocking, backward compatible
CREATE VIEW                 -- OK: compatibility layer
ALTER TABLE ADD COLUMN      -- OK: nullable or with default
GRANT/REVOKE                -- OK: permission management
COMMENT ON                  -- OK: documentation
```

---

## Future Migrations

**Naming Convention:**
```
V<VERSION>__<description>.sql
R__<description>.sql (repeatable)
```

**Examples:**
```
V2__Add_student_external_id_column.sql
V3__Create_diploma_verification_view.sql
R__Refresh_materialized_views.sql
```

**Template:**
```sql
-- V2__Add_student_external_id_column.sql
-- Purpose: Add external_id for inter-university transfers
-- Author: hemis-team
-- Date: 2025-10-30
-- Jira: HEMIS-123

-- SAFE: Nullable column, no data migration
ALTER TABLE hemishe_e_student
    ADD COLUMN IF NOT EXISTS external_id VARCHAR(50);

-- Optional: Add index for lookups
CREATE INDEX CONCURRENTLY IF NOT EXISTS
    idx_student_external_id
ON hemishe_e_student (external_id)
WHERE external_id IS NOT NULL;

-- Documentation
COMMENT ON COLUMN hemishe_e_student.external_id IS
    'External system ID for inter-university transfers';
```

---

## Verification Checklist

Before creating a migration:

- [ ] **Replication-safe:** No RENAME, DROP, TRUNCATE
- [ ] **NDG-compliant:** No DELETE (use soft-delete)
- [ ] **Backward-compatible:** Nullable columns or with defaults
- [ ] **Non-blocking:** Use CONCURRENTLY for indexes
- [ ] **Tested:** Run on dev/staging first
- [ ] **Documented:** Clear purpose and author
- [ ] **Reversible:** Can rollback if needed

---

## Baseline Generation (For Reference)

**Command:**
```bash
pg_dump -h localhost -U postgres -d test_hemis \
    --schema-only \
    --no-owner \
    --no-privileges \
    --no-tablespaces \
    > V1__baseline_reference.sql
```

**Note:** This file is TOO LARGE for version control (2758 objects).
Instead, we use `DB_SCHEMA_CATALOG.md` for documentation.

---

**END OF MIGRATION POLICY**
