# ⚠️ LIQUIBASE MIGRATION GUIDE - MANDATORY!

> **Database Migration Best Practices**  
> **Version:** 2.0.0  
> **Liquibase:** 4.31.1  
> **Last Updated:** 2025-11-15

---

## 🚨 CRITICAL RULES - NO EXCEPTIONS!

### Golden Rules (NEVER VIOLATE)

```
1. IDEMPOTENCY - MANDATORY
   - Migration MUST be safe to run multiple times
   - Use IF NOT EXISTS / IF EXISTS
   - Handle already existing data
   
2. ROLLBACK - MANDATORY
   - EVERY migration MUST have rollback
   - Test rollback BEFORE production
   - splitStatements: false for PostgreSQL DO blocks
   
3. TESTING - MANDATORY
   - Test on local database first
   - Test rollback locally
   - Test on staging before production
   - NEVER test directly on production
   
4. NAMING - MANDATORY
   - Format: XX-descriptive-name.sql
   - XX: Sequential number (06, 07, 08...)
   - Rollback: XX-descriptive-name-rollback.sql
   
5. NO DIRECT DDL - FORBIDDEN
   - NO manual ALTER TABLE on production
   - NO psql direct execution
   - ALWAYS use Liquibase
```

---

## 📋 Migration Structure

### File Organization

```
domain/src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Master changelog
└── changesets/
    ├── 01-complete-schema.sql        # V1: Schema
    ├── 01-complete-schema-rollback.sql
    ├── 02-complete-seed-data.sql     # V2: Seed data
    ├── 02-complete-seed-data-rollback.sql
    ├── 03-migrate-old-users.sql      # V3: User migration
    ├── 03-migrate-old-users-rollback.sql
    ├── 04-complete-translations.sql  # V4: I18n
    ├── 04-complete-translations-rollback.sql
    ├── 05-faculty-registry-translations.sql  # V5
    ├── 05-faculty-registry-translations-rollback.sql
    ├── 06-add-menu-permissions.sql   # V6
    ├── 06-add-menu-permissions-rollback.sql
    └── 07-YOUR-NEW-MIGRATION.sql     # ← Next migration
        07-YOUR-NEW-MIGRATION-rollback.sql
```

---

## 🔧 Step-by-Step: Creating New Migration

### Step 1: Plan Your Migration

**Questions to answer:**

1. What am I changing?
   - New table?
   - New column?
   - New data?
   - Update existing data?

2. Can it be rolled back?
   - What's the reverse operation?
   - Will rollback cause data loss?

3. Is it idempotent?
   - Can I run it twice safely?
   - What if data already exists?

4. Does it affect running application?
   - Breaking change?
   - Requires application restart?

### Step 2: Create Migration Files

```bash
cd /home/adm1n/startup/hemis-back/domain/src/main/resources/db/changelog/changesets/

# Create forward migration
touch 07-add-departments-table.sql

# Create rollback migration
touch 07-add-departments-table-rollback.sql
```

### Step 3: Write Forward Migration

#### Example 1: Create Table (DDL)

```sql
-- =====================================================
-- V7: Add Departments Table
-- =====================================================
-- Author: your-name
-- Date: 2025-11-15
-- Description: Add departments table for organizational structure
-- =====================================================

-- Create table (IDEMPOTENT!)
CREATE TABLE IF NOT EXISTS departments (
    -- Primary Key
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Basic Info
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    
    -- Hierarchy
    parent_id UUID REFERENCES departments(id) ON DELETE SET NULL,
    level INTEGER NOT NULL DEFAULT 1,
    
    -- Faculty Reference
    faculty_id UUID NOT NULL,
        -- Note: No FK constraint because faculty table might not exist yet
    
    -- Status
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Indexes (IDEMPOTENT!)
CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);
CREATE INDEX IF NOT EXISTS idx_departments_faculty ON departments(faculty_id);
CREATE INDEX IF NOT EXISTS idx_departments_parent ON departments(parent_id);
CREATE INDEX IF NOT EXISTS idx_departments_active ON departments(is_active) WHERE deleted_at IS NULL;

-- Comments
COMMENT ON TABLE departments IS 'Organizational departments structure';
COMMENT ON COLUMN departments.code IS 'Unique department code';
COMMENT ON COLUMN departments.parent_id IS 'Parent department for hierarchical structure';
```

#### Example 2: Add Column (DDL)

```sql
-- =====================================================
-- V8: Add Phone Column to Users
-- =====================================================
-- Author: your-name
-- Date: 2025-11-15
-- Description: Add phone number field to users table
-- =====================================================

-- Add column (IDEMPOTENT!)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'phone_number'
    ) THEN
        ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
    END IF;
END $$;

-- Add index (IDEMPOTENT!)
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);

-- Comment
COMMENT ON COLUMN users.phone_number IS 'User phone number in format +998XXXXXXXXX';
```

#### Example 3: Insert Data (DML)

```sql
-- =====================================================
-- V9: Add New Permissions for Department Module
-- =====================================================
-- Author: your-name
-- Date: 2025-11-15
-- Description: Add CRUD permissions for department management
-- =====================================================

-- Insert permissions (IDEMPOTENT with ON CONFLICT!)
INSERT INTO permissions (id, resource, action, code, name, description, category, created_at)
VALUES
    (gen_random_uuid(), 'departments', 'view', 'departments.view', 'View Departments', 'View departments list', 'CORE', CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'departments', 'create', 'departments.create', 'Create Department', 'Create new department', 'CORE', CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'departments', 'edit', 'departments.edit', 'Edit Department', 'Edit department details', 'CORE', CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'departments', 'delete', 'departments.delete', 'Delete Department', 'Delete department', 'CORE', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;  -- ← IDEMPOTENT!

-- Assign to admin role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    r.id,
    p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'ROLE_SUPER_ADMIN'
  AND p.code LIKE 'departments.%'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );  -- ← IDEMPOTENT!
```

#### Example 4: Update Data (DML)

```sql
-- =====================================================
-- V10: Update User Types
-- =====================================================
-- Author: your-name
-- Date: 2025-11-15
-- Description: Update legacy user types to new format
-- =====================================================

-- Update user types (IDEMPOTENT!)
UPDATE users 
SET user_type = 'UNIVERSITY'
WHERE user_type = 'UNIV'
  AND entity_code IS NOT NULL;

UPDATE users 
SET user_type = 'MINISTRY'
WHERE user_type = 'MIN'
  AND entity_code IS NULL;

-- Update is idempotent because we check current value
```

### Step 4: Write Rollback Migration

#### Rollback for Create Table

```sql
-- =====================================================
-- ROLLBACK: V7 - Remove Departments Table
-- =====================================================

-- Drop indexes first
DROP INDEX IF EXISTS idx_departments_active;
DROP INDEX IF EXISTS idx_departments_parent;
DROP INDEX IF EXISTS idx_departments_faculty;
DROP INDEX IF EXISTS idx_departments_code;

-- Drop table
DROP TABLE IF EXISTS departments CASCADE;
```

#### Rollback for Add Column

```sql
-- =====================================================
-- ROLLBACK: V8 - Remove Phone Column
-- =====================================================

-- Drop index
DROP INDEX IF EXISTS idx_users_phone;

-- Drop column
ALTER TABLE users DROP COLUMN IF EXISTS phone_number;
```

#### Rollback for Insert Data

```sql
-- =====================================================
-- ROLLBACK: V9 - Remove Department Permissions
-- =====================================================

-- Delete role-permission mappings
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permissions WHERE code LIKE 'departments.%'
);

-- Delete permissions
DELETE FROM permissions WHERE code LIKE 'departments.%';
```

#### Rollback for Update Data

```sql
-- =====================================================
-- ROLLBACK: V10 - Revert User Types
-- =====================================================

-- Revert user types
UPDATE users 
SET user_type = 'UNIV'
WHERE user_type = 'UNIVERSITY'
  AND entity_code IS NOT NULL;

UPDATE users 
SET user_type = 'MIN'
WHERE user_type = 'MINISTRY'
  AND entity_code IS NULL;
```

### Step 5: Add to Master Changelog

Edit: `domain/src/main/resources/db/changelog/db.changelog-master.yaml`

```yaml
  # =====================================================
  # V7: Add Departments Table
  # =====================================================
  # Add organizational departments structure
  # =====================================================

  - changeSet:
      id: v7-add-departments-table
      author: your-name
      comment: Add departments table for organizational structure
      context: "!test"
      labels: "schema,ddl,departments"
      runOnChange: false

      sqlFile:
        path: changesets/07-add-departments-table.sql
        relativeToChangelogFile: true
        stripComments: false
        splitStatements: false  # CRITICAL for PostgreSQL DO blocks

      rollback:
        sqlFile:
          path: changesets/07-add-departments-table-rollback.sql
          relativeToChangelogFile: true
          stripComments: false
          splitStatements: false

  # Tag after V7 for rollback support
  - changeSet:
      id: tag-v7
      author: your-name
      changes:
        - tagDatabase:
            tag: v7-departments-complete
      rollback:
        - sql:
            sql: "-- Tag rollback (no-op)"
```

### Step 6: Test Locally

```bash
cd /home/adm1n/startup/hemis-back

# 1. Check status
./gradlew :domain:liquibaseStatus

# Output should show:
# 1 changeSets have not been applied to postgres@localhost:5432/test_hemis

# 2. Preview SQL
./gradlew :domain:liquibaseUpdateSQL > /tmp/preview.sql
cat /tmp/preview.sql

# 3. Apply migration
./gradlew :domain:liquibaseUpdate

# 4. Verify in database
PGPASSWORD=postgres psql -h localhost -U postgres -d test_hemis -c "\d departments"

# 5. Check Liquibase history
./gradlew :domain:liquibaseHistory
```

### Step 7: Test Rollback

```bash
# 1. Preview rollback SQL
./gradlew :domain:liquibaseRollbackSQL -Pcount=2

# Output shows what will be executed

# 2. Execute rollback
./gradlew :domain:liquibaseRollbackCount -Pcount=2

# Note: count=2 because we rollback both migration AND tag

# 3. Verify rollback
PGPASSWORD=postgres psql -h localhost -U postgres -d test_hemis -c "\d departments"
# Should show: relation "departments" does not exist

# 4. Re-apply
./gradlew :domain:liquibaseUpdate

# 5. Verify re-apply
PGPASSWORD=postgres psql -h localhost -U postgres -d test_hemis -c "\d departments"
# Should show table structure
```

---

## ⚠️ IDEMPOTENCY - CRITICAL!

### What is Idempotency?

**Definition:** Migration can run multiple times safely without errors.

**Why?** Spring Boot runs Liquibase automatically on `bootRun`. If migration isn't idempotent, second run fails!

### Idempotency Checklist

#### ✅ CREATE TABLE
```sql
-- ✅ CORRECT (Idempotent)
CREATE TABLE IF NOT EXISTS departments (...);

-- ❌ WRONG (Not Idempotent)
CREATE TABLE departments (...);
-- Second run: ERROR: relation "departments" already exists
```

#### ✅ CREATE INDEX
```sql
-- ✅ CORRECT (Idempotent)
CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);

-- ❌ WRONG (Not Idempotent)
CREATE INDEX idx_departments_code ON departments(code);
-- Second run: ERROR: relation "idx_departments_code" already exists
```

#### ✅ ALTER TABLE ADD COLUMN
```sql
-- ✅ CORRECT (Idempotent with DO block)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'phone_number'
    ) THEN
        ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
    END IF;
END $$;

-- ❌ WRONG (Not Idempotent)
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
-- Second run: ERROR: column "phone_number" already exists
```

#### ✅ INSERT DATA
```sql
-- ✅ CORRECT (Idempotent with ON CONFLICT)
INSERT INTO permissions (id, code, name)
VALUES (gen_random_uuid(), 'departments.view', 'View Departments')
ON CONFLICT (code) DO NOTHING;

-- ✅ ALSO CORRECT (Check existence)
INSERT INTO permissions (id, code, name)
SELECT gen_random_uuid(), 'departments.view', 'View Departments'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'departments.view'
);

-- ❌ WRONG (Not Idempotent)
INSERT INTO permissions (id, code, name)
VALUES (gen_random_uuid(), 'departments.view', 'View Departments');
-- Second run: ERROR: duplicate key value violates unique constraint
```

#### ✅ UPDATE DATA
```sql
-- ✅ CORRECT (Idempotent - checks condition)
UPDATE users 
SET user_type = 'UNIVERSITY'
WHERE user_type = 'UNIV';  -- Only updates if value is 'UNIV'

-- ✅ ALSO CORRECT (With existence check)
UPDATE users 
SET is_verified = TRUE
WHERE email LIKE '%@university.uz'
  AND is_verified = FALSE;  -- Only updates if not already verified
```

#### ✅ DELETE DATA
```sql
-- ✅ CORRECT (Idempotent - WHERE clause)
DELETE FROM permissions WHERE code LIKE 'old.%';

-- If no rows match, no error - idempotent!
```

#### ✅ DROP TABLE
```sql
-- ✅ CORRECT (Idempotent)
DROP TABLE IF EXISTS old_departments CASCADE;

-- ❌ WRONG (Not Idempotent)
DROP TABLE old_departments CASCADE;
-- Second run: ERROR: table "old_departments" does not exist
```

---

## 🔄 ROLLBACK - CRITICAL!

### Why Rollback?

1. **Production Safety:** Revert failed migration
2. **Testing:** Verify migration can be undone
3. **Development:** Iterate on migration design

### Rollback Count Formula

```
count = (number_of_migrations × 2)

Because:
- Each migration has 2 changesets:
  1. The actual migration
  2. The tag

Example:
- Rollback 1 migration = count 2
- Rollback 2 migrations = count 4
- Rollback 3 migrations = count 6
```

### Rollback Examples

```bash
# Rollback last migration (V7 + tag-v7)
./gradlew :domain:liquibaseRollbackCount -Pcount=2

# Rollback last 2 migrations (V7, V6 + their tags)
./gradlew :domain:liquibaseRollbackCount -Pcount=4

# Rollback to specific tag
./gradlew :domain:liquibaseRollbackToTag -Ptag=v6-menu-permissions-complete

# Preview rollback (safe, doesn't execute)
./gradlew :domain:liquibaseRollbackSQL -Pcount=2
```

### splitStatements: false - CRITICAL!

**Problem:** PostgreSQL `DO $$ ... $$` blocks

```sql
-- This BREAKS with splitStatements=true:
DO $$
BEGIN
    IF NOT EXISTS (...) THEN
        ALTER TABLE users ADD COLUMN phone VARCHAR(20);
    END IF;
END $$;
```

**Reason:** Liquibase splits on `;` and breaks the `DO` block!

**Solution:** Always use `splitStatements: false`

```yaml
sqlFile:
  path: changesets/07-add-departments.sql
  stripComments: false
  splitStatements: false  # ← MANDATORY!

rollback:
  sqlFile:
    path: changesets/07-add-departments-rollback.sql
    stripComments: false
    splitStatements: false  # ← CRITICAL!
```

---

## 📊 Migration Types & Templates

### Type 1: Schema Creation (DDL)

**Use Case:** Create new table

**Template:**
```sql
-- Create table
CREATE TABLE IF NOT EXISTS table_name (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- columns...
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_table_column ON table_name(column);

-- Comments
COMMENT ON TABLE table_name IS 'Description';
```

**Rollback:**
```sql
DROP INDEX IF EXISTS idx_table_column;
DROP TABLE IF EXISTS table_name CASCADE;
```

### Type 2: Schema Modification (DDL)

**Use Case:** Add/modify column

**Template:**
```sql
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'new_column'
    ) THEN
        ALTER TABLE users ADD COLUMN new_column VARCHAR(255);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_users_new_column ON users(new_column);
```

**Rollback:**
```sql
DROP INDEX IF EXISTS idx_users_new_column;
ALTER TABLE users DROP COLUMN IF EXISTS new_column;
```

### Type 3: Reference Data (DML)

**Use Case:** Insert lookup/reference data

**Template:**
```sql
INSERT INTO reference_table (id, code, name)
VALUES
    (gen_random_uuid(), 'CODE1', 'Name 1'),
    (gen_random_uuid(), 'CODE2', 'Name 2')
ON CONFLICT (code) DO NOTHING;
```

**Rollback:**
```sql
DELETE FROM reference_table WHERE code IN ('CODE1', 'CODE2');
```

### Type 4: Data Migration (DML)

**Use Case:** Migrate data from old to new structure

**Template:**
```sql
INSERT INTO new_table (id, name, legacy_id)
SELECT 
    gen_random_uuid(),
    old_name,
    id
FROM old_table
WHERE NOT EXISTS (
    SELECT 1 FROM new_table WHERE legacy_id = old_table.id
);
```

**Rollback:**
```sql
DELETE FROM new_table WHERE legacy_id IS NOT NULL;
```

### Type 5: Complex Migration (PL/pgSQL)

**Use Case:** Complex data transformation

**Template:**
```sql
DO $$
DECLARE
    v_record RECORD;
    v_count INTEGER := 0;
BEGIN
    FOR v_record IN 
        SELECT * FROM source_table WHERE condition
    LOOP
        -- Complex logic
        INSERT INTO target_table (...)
        VALUES (...);
        
        v_count := v_count + 1;
    END LOOP;
    
    RAISE NOTICE 'Migrated % records', v_count;
END $$;
```

**Rollback:**
```sql
DELETE FROM target_table WHERE migration_flag = TRUE;
```

---

## 🚫 COMMON MISTAKES & FIXES

### Mistake 1: Not Idempotent

```sql
-- ❌ WRONG
CREATE TABLE departments (...);

-- ✅ CORRECT
CREATE TABLE IF NOT EXISTS departments (...);
```

### Mistake 2: No Rollback

```yaml
# ❌ WRONG - No rollback section
- changeSet:
    id: v7-add-departments
    sqlFile:
      path: changesets/07-add-departments.sql

# ✅ CORRECT - Has rollback
- changeSet:
    id: v7-add-departments
    sqlFile:
      path: changesets/07-add-departments.sql
    rollback:
      sqlFile:
        path: changesets/07-add-departments-rollback.sql
```

### Mistake 3: splitStatements=true for DO blocks

```yaml
# ❌ WRONG - Will break DO $$ blocks
sqlFile:
  path: changesets/07-migration.sql
  splitStatements: true

# ✅ CORRECT
sqlFile:
  path: changesets/07-migration.sql
  splitStatements: false
```

### Mistake 4: Hardcoded UUIDs

```sql
-- ❌ WRONG - Hardcoded UUID
INSERT INTO permissions (id, code, name)
VALUES ('123e4567-e89b-12d3-a456-426614174000', 'code', 'name');

-- ✅ CORRECT - Generated UUID
INSERT INTO permissions (id, code, name)
VALUES (gen_random_uuid(), 'code', 'name');
```

### Mistake 5: No ON CONFLICT for Inserts

```sql
-- ❌ WRONG - Fails on duplicate
INSERT INTO permissions (id, code, name)
VALUES (gen_random_uuid(), 'code', 'name');

-- ✅ CORRECT - Idempotent
INSERT INTO permissions (id, code, name)
VALUES (gen_random_uuid(), 'code', 'name')
ON CONFLICT (code) DO NOTHING;
```

### Mistake 6: Testing on Production

```bash
# ❌ WRONG - Direct production test
ssh production
./gradlew :domain:liquibaseUpdate  # DANGEROUS!

# ✅ CORRECT - Test locally, staging, then production
# 1. Local
./gradlew :domain:liquibaseUpdate

# 2. Staging
ssh staging
./gradlew :domain:liquibaseUpdate

# 3. Verify staging for 1 day

# 4. Production (with backup!)
ssh production
pg_dump -U postgres hemis > backup_$(date +%Y%m%d).sql
./gradlew :domain:liquibaseUpdate
```

---

## ✅ MIGRATION CHECKLIST

### Before Writing Migration

```
☑ I understand what needs to change
☑ I know how to rollback the change
☑ I checked existing migrations for similar patterns
☑ I have test data ready
☑ I have backup of my local database
```

### While Writing Migration

```
☑ Migration file name follows convention (XX-descriptive-name.sql)
☑ Rollback file name matches (XX-descriptive-name-rollback.sql)
☑ Header comment explains purpose and author
☑ All DDL uses IF NOT EXISTS / IF EXISTS
☑ All DML uses ON CONFLICT DO NOTHING or WHERE NOT EXISTS
☑ Complex logic uses DO $$ blocks
☑ splitStatements: false for DO blocks
☑ Rollback script exists and tested
```

### Before Committing

```
☑ Tested forward migration locally
☑ Tested rollback locally
☑ Tested re-apply after rollback
☑ Checked liquibaseStatus shows correct state
☑ Added to db.changelog-master.yaml
☑ Tag changeSet included
☑ All SQL is idempotent
☑ No hardcoded IDs or timestamps
☑ Comments explain complex logic
```

### Before Production

```
☑ Tested on staging environment
☑ Monitoring shows no errors on staging
☑ Rollback tested on staging
☑ Database backup prepared
☑ Rollback plan documented
☑ Team notified of deployment
☑ Maintenance window scheduled (if needed)
```

---

## 🎯 QUICK REFERENCE

### Liquibase Commands

```bash
# Status (what's pending?)
./gradlew :domain:liquibaseStatus

# History (what's applied?)
./gradlew :domain:liquibaseHistory

# Apply pending migrations
./gradlew :domain:liquibaseUpdate

# Preview SQL (doesn't execute)
./gradlew :domain:liquibaseUpdateSQL

# Rollback count
./gradlew :domain:liquibaseRollbackCount -Pcount=2

# Rollback to tag
./gradlew :domain:liquibaseRollbackToTag -Ptag=v6-menu-permissions-complete

# Preview rollback
./gradlew :domain:liquibaseRollbackSQL -Pcount=2
```

### PostgreSQL Verification

```bash
# Connect to database
PGPASSWORD=postgres psql -h localhost -U postgres -d test_hemis

# List tables
\dt

# Describe table
\d table_name

# List indexes
\di

# Check migration history
SELECT * FROM databasechangelog ORDER BY dateexecuted DESC LIMIT 10;

# Check specific table
SELECT COUNT(*) FROM departments;
```

---

## 📚 SUMMARY

### MANDATORY Requirements

1. **Idempotency** - MUST run multiple times safely
2. **Rollback** - MUST have rollback script
3. **Testing** - MUST test locally before staging/production
4. **Naming** - MUST follow XX-descriptive-name.sql convention
5. **splitStatements: false** - MUST use for PostgreSQL DO blocks

### FORBIDDEN Actions

1. ❌ Direct DDL on production
2. ❌ Migrations without rollback
3. ❌ Non-idempotent migrations
4. ❌ Testing directly on production
5. ❌ Hardcoded UUIDs or timestamps

### Best Practices

1. ✅ Use IF NOT EXISTS / IF EXISTS
2. ✅ Use ON CONFLICT DO NOTHING
3. ✅ Use gen_random_uuid() for IDs
4. ✅ Test rollback before committing
5. ✅ Add comments explaining complex logic
6. ✅ Keep migrations small and focused
7. ✅ One migration = one logical change

---

**Remember:** Migrations are PERMANENT! 
Once applied to production, they cannot be removed from changelog.
Take time to get them right! ⚠️

**Violation = Production Issues = Data Loss Risk = NO EXCEPTIONS!**
