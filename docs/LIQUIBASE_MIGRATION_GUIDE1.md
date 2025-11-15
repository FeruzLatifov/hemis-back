# 📘 HEMIS Backend - Liquibase Migration Master Guide

> **Version:** 1.1.0
> **Last Updated:** 2025-11-14 (Idempotency fix added)
> **Author:** HEMIS Team
> **Liquibase Version:** 4.31.1

---

## 🎯 Ushbu Qo'llanma Haqida

Bu qo'llanma HEMIS Backend loyihasida **Liquibase 4.x** yordamida yangi database migration yozish uchun step-by-step ko'rsatma.

**Maqsad:** Har bir dasturchi professional, rollback-friendly, production-ready migration yoza olishi.

---

## 📋 Tez Boshlanish (Quick Start)

### 1. Yangi Migration Yaratish (5 daqiqa)

```bash
# 1. Changesets papkaga o'tish
cd /home/adm1n/startup/hemis-back/domain/src/main/resources/db/changelog/changesets/

# 2. Yangi fayllar yaratish (XX - keyingi raqam, masalan 06)
touch 06-add-departments-table.sql
touch 06-add-departments-table-rollback.sql

# 3. SQL yozish (quyidagi shablonlar bo'yicha)
nano 06-add-departments-table.sql

# 4. Rollback SQL yozish
nano 06-add-departments-table-rollback.sql

# 5. Changelog'ga qo'shish
nano ../db.changelog-master.yaml
```

### 2. Test Qilish

```bash
cd /home/adm1n/startup/hemis-back

# Preview
./gradlew :domain:liquibaseStatus

# Apply
./gradlew :domain:liquibaseUpdate

# Verify
PGPASSWORD=postgres psql -h localhost -U postgres -d test1_hemis -c "\d departments"

# Test rollback
./gradlew :domain:liquibaseRollbackSQL -Pcount=1
./gradlew :domain:liquibaseRollbackCount -Pcount=1
./gradlew :domain:liquibaseUpdate
```

---

## 🔄 Idempotency (Qayta Ishga Tushishi Xavfsiz)

### Nima Bu?

**Idempotent migration** - migration ni necha marta ishlatsa ham xatolik bermaydi va bir xil natija beradi.

**Sabab:** Spring Boot `./gradlew bootRun` da avtomatik Liquibase ishga tushadi. Agar migration idempotent bo'lmasa, ikkinchi marta ishga tushganda ERROR chiqadi.

### ✅ Real Bug (2025-11-14 da topilgan va to'g'irlandi)

**Muammo:**
```sql
-- 01-complete-schema.sql (Lines 341-342, 355-356)
CREATE INDEX idx_system_messages_category ON system_messages(category);
CREATE INDEX idx_system_messages_key ON system_messages(message_key);
CREATE INDEX idx_system_message_translations_message ON system_message_translations(message_id);
CREATE INDEX idx_system_message_translations_language ON system_message_translations(language);
```

**Xatolik:**
```
ERROR: relation "idx_system_messages_category" already exists
Failed SQL: CREATE INDEX idx_system_messages_category ON system_messages(category);
```

**Yechim (IF NOT EXISTS qo'shish):**
```sql
CREATE INDEX IF NOT EXISTS idx_system_messages_category ON system_messages(category);
CREATE INDEX IF NOT EXISTS idx_system_messages_key ON system_messages(message_key);
CREATE INDEX IF NOT EXISTS idx_system_message_translations_message ON system_message_translations(message_id);
CREATE INDEX IF NOT EXISTS idx_system_message_translations_language ON system_message_translations(language);
```

### 📋 Idempotency Checklist

Har bir migration uchun quyidagilarni tekshiring:

#### 1️⃣ DDL Statements (CREATE/ALTER/DROP)

```sql
-- ✅ CORRECT (idempotent)
CREATE TABLE IF NOT EXISTS users (...);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
DROP TABLE IF EXISTS old_users CASCADE;
DROP INDEX IF EXISTS idx_old_users;

-- ❌ WRONG (not idempotent - fails on second run)
CREATE TABLE users (...);
CREATE INDEX idx_users_email ON users(email);
ALTER TABLE users ADD COLUMN phone VARCHAR(50);
DROP TABLE old_users;
DROP INDEX idx_old_users;
```

#### 2️⃣ DML Statements (INSERT/UPDATE/DELETE)

```sql
-- ✅ CORRECT (idempotent with ON CONFLICT)
INSERT INTO roles (id, code, name) VALUES
    ('uuid-1', 'ADMIN', 'Administrator'),
    ('uuid-2', 'USER', 'Regular User')
ON CONFLICT (code) DO NOTHING;  -- Skip if exists

-- ✅ CORRECT (idempotent with NOT EXISTS)
INSERT INTO users (username, email)
SELECT 'admin', 'admin@example.com'
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

-- ❌ WRONG (not idempotent - unique constraint violation)
INSERT INTO roles (code, name) VALUES ('ADMIN', 'Administrator');
```

#### 3️⃣ Barcha Migration Fayllari Tekshirish

| File | Idempotent? | Method |
|------|-------------|--------|
| 01-complete-schema.sql | ✅ YES | `IF NOT EXISTS` barcha CREATE'larda |
| 02-complete-seed-data.sql | ✅ YES | `ON CONFLICT DO NOTHING` |
| 03-migrate-old-users.sql | ✅ YES | `NOT EXISTS` + `IF NOT EXISTS` |
| 04-complete-translations.sql | ✅ YES | `ON CONFLICT DO NOTHING` |
| 05-faculty-registry-translations.sql | ✅ YES | `ON CONFLICT DO NOTHING` |

### 🧪 Idempotency Testlash

Har doim migration ni 2 marta ishga tushirib test qiling:

```bash
# 1. Birinchi marta apply (yangi migration)
./gradlew :domain:liquibaseUpdate

# 2. Ikkinchi marta apply (idempotency test)
./gradlew bootRun

# Natija: Hech qanday ERROR bo'lmasligi kerak
# Kutilayotgan: "relation already exists, skipping" (WARN)
```

### 🎯 Idempotency Qoidalari (Golden Rules)

**QOIDA #1:** BARCHA CREATE statementlarda `IF NOT EXISTS` ishlatish
**QOIDA #2:** BARCHA DROP statementlarda `IF EXISTS` ishlatish
**QOIDA #3:** BARCHA INSERT statementlarda `ON CONFLICT DO NOTHING` yoki `WHERE NOT EXISTS`
**QOIDA #4:** Har doim `./gradlew bootRun` bilan test qilish

---

## 📁 Fayl Tuzilmasi

```
domain/src/main/resources/db/changelog/
├── db.changelog-master.yaml           # Master changelog
└── changesets/
    ├── 01-complete-schema.sql         # V1: Schema
    ├── 01-complete-schema-rollback.sql
    ├── 02-complete-seed-data.sql      # V2: Seed data
    ├── 02-complete-seed-data-rollback.sql
    ├── 03-migrate-old-users.sql       # V3: User migration
    ├── 03-migrate-old-users-rollback.sql
    ├── 04-complete-translations.sql   # V4: Translations
    ├── 04-complete-translations-rollback.sql
    ├── 05-faculty-registry-translations.sql  # V5
    ├── 05-faculty-registry-translations-rollback.sql
    └── 06-add-your-migration.sql      # Yangi migration
        06-add-your-migration-rollback.sql
```

---

## 🔧 Migration Yozish (Step-by-Step)

### STEP 1: Migration SQL Yozish

**File:** `06-add-departments-table.sql`

```sql
-- =====================================================
-- V6: Add Departments Table
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-14
-- Description: Create departments table for university structure
-- Tables: departments (1)
-- Indexes: 3
-- Initial Data: 3 records
-- =====================================================

-- Create departments table
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    faculty_id UUID,
    head_id UUID,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT chk_departments_code_length CHECK (LENGTH(code) >= 2)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_departments_faculty ON departments(faculty_id);
CREATE INDEX IF NOT EXISTS idx_departments_head ON departments(head_id);
CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);

-- Add table and column comments
COMMENT ON TABLE departments IS 'University departments';
COMMENT ON COLUMN departments.id IS 'Primary key (UUID)';
COMMENT ON COLUMN departments.name IS 'Department name';
COMMENT ON COLUMN departments.code IS 'Unique department code (2-50 chars)';
COMMENT ON COLUMN departments.faculty_id IS 'Reference to faculty';
COMMENT ON COLUMN departments.head_id IS 'Department head (reference to users)';

-- Insert initial data (optional)
INSERT INTO departments (id, name, code, description, is_active) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'Informatika', 'CS', 'Computer Science Department', TRUE),
    ('550e8400-e29b-41d4-a716-446655440002', 'Matematika', 'MATH', 'Mathematics Department', TRUE),
    ('550e8400-e29b-41d4-a716-446655440003', 'Fizika', 'PHYS', 'Physics Department', TRUE)
ON CONFLICT (id) DO NOTHING;
```

### STEP 2: Rollback SQL Yozish

**File:** `06-add-departments-table-rollback.sql`

```sql
-- =====================================================
-- V6 Rollback: Remove Departments Table
-- =====================================================
-- Author: hemis-team
-- Date: 2025-01-14
-- Description: Rollback departments table creation
-- =====================================================

-- Drop indexes first (best practice)
DROP INDEX IF EXISTS idx_departments_code;
DROP INDEX IF EXISTS idx_departments_head;
DROP INDEX IF EXISTS idx_departments_faculty;

-- Drop table with CASCADE (removes dependencies)
DROP TABLE IF EXISTS departments CASCADE;
```

### STEP 3: Changelog'ga Qo'shish

**File:** `db.changelog-master.yaml`

Faylning **oxiriga** quyidagilarni qo'shing:

```yaml
  # =====================================================
  # V6: Add Departments Table
  # =====================================================
  # Create departments table for university structure
  # - Table: departments (11 columns)
  # - Indexes: 3 (faculty, head, code)
  # - Constraints: 1 (code length check)
  # - Initial data: 3 departments
  #
  # Source: University Structure Enhancement
  # Date: 2025-01-14
  # =====================================================

  - changeSet:
      id: v6-add-departments-table
      author: hemis-team
      comment: Create departments table with indexes, constraints and initial data
      context: "!test"
      labels: "schema,departments,ddl"
      runOnChange: false

      sqlFile:
        path: changesets/06-add-departments-table.sql
        relativeToChangelogFile: true
        stripComments: false
        splitStatements: false

      rollback:
        sqlFile:
          path: changesets/06-add-departments-table-rollback.sql
          relativeToChangelogFile: true

  # Tag after V6 for rollback support
  - changeSet:
      id: tag-v6
      author: hemis-team
      changes:
        - tagDatabase:
            tag: v6-departments-complete
```

### STEP 4: Test Qilish

```bash
# 1. Migration holatini ko'rish
./gradlew :domain:liquibaseStatus

# Natija: "1 changesets have not been applied"

# 2. Migration'ni apply qilish
./gradlew :domain:liquibaseUpdate

# Natija: "Running Changeset: ...v6-add-departments-table..."

# 3. Jadval yaratilganini tekshirish
PGPASSWORD=postgres psql -h localhost -U postgres -d test1_hemis -c "\d departments"

# 4. Data mavjudligini tekshirish
PGPASSWORD=postgres psql -h localhost -U postgres -d test1_hemis -c "SELECT * FROM departments;"

# 5. Rollback SQL ni ko'rish (xavfsiz preview)
./gradlew :domain:liquibaseRollbackSQL -Pcount=1

# Natija: DROP TABLE ... ko'rsatiladi

# 6. Rollback qilish
./gradlew :domain:liquibaseRollbackCount -Pcount=1

# 7. Jadval o'chirilganini tekshirish
PGPASSWORD=postgres psql -h localhost -U postgres -d test1_hemis -c "\d departments"

# Natija: "Did not find any relation named "departments""

# 8. Qayta apply qilish
./gradlew :domain:liquibaseUpdate

# 9. Migration tarixini ko'rish
./gradlew :domain:liquibaseHistory
```

---

## 📚 Migration Turlari va Shablonlar

### 1. CREATE TABLE (DDL)

**Misol: Yangi jadval yaratish**

```sql
-- =====================================================
-- VX: Create Courses Table
-- =====================================================

CREATE TABLE IF NOT EXISTS courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    credits INT NOT NULL CHECK (credits BETWEEN 1 AND 10),
    department_id UUID,
    semester INT CHECK (semester BETWEEN 1 AND 8),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_courses_department
        FOREIGN KEY (department_id)
        REFERENCES departments(id)
        ON DELETE SET NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_courses_department ON courses(department_id);
CREATE INDEX IF NOT EXISTS idx_courses_code ON courses(code);
CREATE INDEX IF NOT EXISTS idx_courses_active ON courses(is_active) WHERE is_active = TRUE;

-- Comments
COMMENT ON TABLE courses IS 'University courses catalog';
COMMENT ON COLUMN courses.credits IS 'Course credit hours (1-10)';
```

**Rollback:**

```sql
-- Drop foreign key constraint first (if standalone)
ALTER TABLE courses DROP CONSTRAINT IF EXISTS fk_courses_department;

-- Drop indexes
DROP INDEX IF EXISTS idx_courses_active;
DROP INDEX IF EXISTS idx_courses_code;
DROP INDEX IF EXISTS idx_courses_department;

-- Drop table
DROP TABLE IF EXISTS courses CASCADE;
```

### 2. ALTER TABLE (Ustun Qo'shish)

**Misol: Email ustuni qo'shish**

```sql
-- =====================================================
-- VX: Add Email Column to Users
-- =====================================================

-- Add column (nullable at first)
ALTER TABLE users
ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- Add unique constraint
ALTER TABLE users
ADD CONSTRAINT uk_users_email UNIQUE (email);

-- Create index
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Add comment
COMMENT ON COLUMN users.email IS 'User email address (unique)';

-- Optionally populate existing records
UPDATE users
SET email = CONCAT(username, '@example.com')
WHERE email IS NULL;
```

**Rollback:**

```sql
DROP INDEX IF EXISTS idx_users_email;
ALTER TABLE users DROP CONSTRAINT IF EXISTS uk_users_email;
ALTER TABLE users DROP COLUMN IF EXISTS email;
```

### 3. ALTER TABLE (Ustun O'zgartirish)

**Misol: Nullable → NOT NULL**

```sql
-- =====================================================
-- VX: Make Email Required
-- =====================================================

-- Step 1: Populate NULL values first
UPDATE users
SET email = CONCAT(username, '@default.com')
WHERE email IS NULL;

-- Step 2: Add NOT NULL constraint
ALTER TABLE users
ALTER COLUMN email SET NOT NULL;

-- Step 3: Add default value for future inserts
ALTER TABLE users
ALTER COLUMN email SET DEFAULT '';
```

**Rollback:**

```sql
ALTER TABLE users ALTER COLUMN email DROP DEFAULT;
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
```

### 4. INSERT DATA (Seed Data)

**Misol: Dastlabki rollar**

```sql
-- =====================================================
-- VX: Add Student and Teacher Roles
-- =====================================================

INSERT INTO roles (id, name, code, description, is_active) VALUES
    ('650e8400-e29b-41d4-a716-446655440001', 'Student', 'STUDENT', 'University student role', TRUE),
    ('650e8400-e29b-41d4-a716-446655440002', 'Teacher', 'TEACHER', 'University teacher role', TRUE),
    ('650e8400-e29b-41d4-a716-446655440003', 'Staff', 'STAFF', 'Administrative staff role', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Verify
DO $$
DECLARE
    role_count INT;
BEGIN
    SELECT COUNT(*) INTO role_count FROM roles WHERE code IN ('STUDENT', 'TEACHER', 'STAFF');
    RAISE NOTICE 'Inserted % roles', role_count;
END $$;
```

**Rollback:**

```sql
DELETE FROM roles
WHERE code IN ('STUDENT', 'TEACHER', 'STAFF');
```

### 5. UPDATE DATA (Mavjud Data O'zgartirish)

**Misol: Eski status'larni yangilash**

```sql
-- =====================================================
-- VX: Update User Statuses
-- =====================================================

-- Update inactive users
UPDATE users
SET
    is_active = FALSE,
    updated_at = CURRENT_TIMESTAMP
WHERE last_login < CURRENT_DATE - INTERVAL '365 days';

-- Log changes
DO $$
DECLARE
    updated_count INT;
BEGIN
    GET DIAGNOSTICS updated_count = ROW_COUNT;
    RAISE NOTICE 'Updated % inactive users', updated_count;
END $$;
```

**Rollback (agar backup bor bo'lsa):**

```sql
-- Restore from backup table (if exists)
UPDATE users
SET is_active = backup_users.is_active
FROM backup_users
WHERE users.id = backup_users.id;
```

### 6. Data Migration (Complex)

**Misol: Eski jadvaldan yangi jadvalga**

```sql
-- =====================================================
-- VX: Migrate Students from Old to New Table
-- =====================================================

-- CRITICAL: Use splitStatements: false in changelog!

DO $$
DECLARE
    migrated_count INT := 0;
    student_record RECORD;
BEGIN
    -- Migrate active students
    FOR student_record IN
        SELECT * FROM old_students WHERE is_active = TRUE
    LOOP
        INSERT INTO new_students (
            id,
            first_name,
            last_name,
            email,
            student_number,
            enrollment_date
        ) VALUES (
            student_record.id,
            SPLIT_PART(student_record.full_name, ' ', 1),
            SPLIT_PART(student_record.full_name, ' ', 2),
            student_record.email_address,
            student_record.student_id,
            student_record.enrolled_at
        )
        ON CONFLICT (id) DO NOTHING;

        migrated_count := migrated_count + 1;
    END LOOP;

    RAISE NOTICE 'Successfully migrated % students', migrated_count;

    -- Verify migration
    IF migrated_count = 0 THEN
        RAISE EXCEPTION 'No students were migrated!';
    END IF;
END $$;
```

**Changelog (CRITICAL - splitStatements):**

```yaml
- changeSet:
    id: vX-migrate-students
    author: hemis-team
    sqlFile:
      path: changesets/XX-migrate-students.sql
      relativeToChangelogFile: true
      stripComments: false
      splitStatements: false  # CRITICAL for PL/pgSQL!
```

**Rollback:**

```sql
DELETE FROM new_students
WHERE id IN (SELECT id FROM old_students WHERE is_active = TRUE);
```

### 7. Create Index

**Misol: Performance optimization**

```sql
-- =====================================================
-- VX: Add Performance Indexes
-- =====================================================

-- Composite index
CREATE INDEX IF NOT EXISTS idx_users_active_email
ON users(is_active, email)
WHERE is_active = TRUE;

-- Partial index
CREATE INDEX IF NOT EXISTS idx_users_recent
ON users(created_at DESC)
WHERE created_at > CURRENT_DATE - INTERVAL '30 days';

-- Functional index
CREATE INDEX IF NOT EXISTS idx_users_email_lower
ON users(LOWER(email));

-- GIN index for full-text search (if needed)
CREATE INDEX IF NOT EXISTS idx_courses_description_fts
ON courses USING GIN(to_tsvector('english', description));
```

**Rollback:**

```sql
DROP INDEX IF EXISTS idx_courses_description_fts;
DROP INDEX IF EXISTS idx_users_email_lower;
DROP INDEX IF EXISTS idx_users_recent;
DROP INDEX IF EXISTS idx_users_active_email;
```

### 8. Add Foreign Key

**Misol: Relationship yaratish**

```sql
-- =====================================================
-- VX: Add Foreign Keys to Courses
-- =====================================================

-- Add foreign key
ALTER TABLE courses
ADD CONSTRAINT fk_courses_department
FOREIGN KEY (department_id)
REFERENCES departments(id)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- Add index for foreign key (performance)
CREATE INDEX IF NOT EXISTS idx_courses_department
ON courses(department_id);

-- Verify orphaned records
DO $$
DECLARE
    orphan_count INT;
BEGIN
    SELECT COUNT(*) INTO orphan_count
    FROM courses c
    LEFT JOIN departments d ON c.department_id = d.id
    WHERE c.department_id IS NOT NULL AND d.id IS NULL;

    IF orphan_count > 0 THEN
        RAISE WARNING '% orphaned course records found', orphan_count;
    END IF;
END $$;
```

**Rollback:**

```sql
DROP INDEX IF EXISTS idx_courses_department;
ALTER TABLE courses DROP CONSTRAINT IF EXISTS fk_courses_department;
```

---

## ⚠️ CRITICAL Rules (Qat'iy Qoidalar)

### ✅ ALWAYS DO (Har Doim):

1. **IDEMPOTENT bo'lishi SHART (Most Important!):**
```sql
-- ✅ ALWAYS use IF NOT EXISTS / IF EXISTS
CREATE TABLE IF NOT EXISTS users (...);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
DROP TABLE IF EXISTS old_table CASCADE;
DROP INDEX IF EXISTS idx_old;

-- ✅ ALWAYS use ON CONFLICT for INSERT
INSERT INTO roles (code, name) VALUES ('ADMIN', 'Admin')
ON CONFLICT (code) DO NOTHING;

-- ❌ NEVER omit these - causes bootRun errors!
CREATE INDEX idx_users_email ON users(email);  -- ERROR on 2nd run
INSERT INTO roles (code, name) VALUES ('ADMIN', 'Admin');  -- ERROR on 2nd run
```

2. **Rollback yozing:**
```sql
-- Migration
CREATE TABLE IF NOT EXISTS test (...);

-- Rollback (MANDATORY!)
DROP TABLE IF EXISTS test CASCADE;
```

3. **CASCADE ishlatish:**
```sql
DROP TABLE IF EXISTS users CASCADE;  -- Drops dependencies too
```

4. **Tags qo'shish:**
```yaml
- changeSet:
    id: tag-v6
    author: hemis-team
    changes:
      - tagDatabase:
          tag: v6-your-migration-complete
```

5. **Comments yozish:**
```sql
COMMENT ON TABLE users IS 'System users';
COMMENT ON COLUMN users.email IS 'User email (unique)';
```

6. **Versioning convention:**
```
01-initial-schema.sql
02-add-roles.sql
03-migrate-users.sql
```

7. **Test qilish:**
```bash
# Always test rollback!
./gradlew :domain:liquibaseRollbackSQL -Pcount=1
./gradlew :domain:liquibaseRollbackCount -Pcount=1
./gradlew :domain:liquibaseUpdate
```

### ❌ NEVER DO (Hech Qachon):

1. **Idempotent bo'lmagan migration (CRITICAL BUG SOURCE!):**
```sql
-- ❌ DANGER - bootRun ikkinchi marta ishga tushganda ERROR!
CREATE TABLE users (...);  -- "relation already exists"
CREATE INDEX idx_users_email ON users(email);  -- "relation already exists"
INSERT INTO roles VALUES ('ADMIN', 'Admin');  -- "duplicate key value"

-- ✅ CORRECT - har doim IF NOT EXISTS / ON CONFLICT
CREATE TABLE IF NOT EXISTS users (...);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
INSERT INTO roles VALUES ('ADMIN', 'Admin') ON CONFLICT DO NOTHING;
```
**Oqibat:** Production'da `./gradlew bootRun` ERROR bilan to'xtaydi!

2. **Rollback yozmaslik:**
```yaml
# BAD - no rollback
- changeSet:
    id: v6-add-table
    sqlFile: changesets/06.sql
    # No rollback section!
```

2. **Production data o'chirish:**
```sql
-- DANGEROUS in production!
DELETE FROM users;
TRUNCATE TABLE users;
DROP TABLE users;  -- Without backup!
```

3. **Manual ID yozish (UUID):**
```sql
-- BAD
INSERT INTO users (id, name) VALUES (1, 'Test');

-- GOOD
INSERT INTO users (name) VALUES ('Test');
-- or with UUID
INSERT INTO users (id, name)
VALUES (gen_random_uuid(), 'Test');
```

4. **NOT NULL qo'shmasdan data o'zgartirish:**
```sql
-- BAD - fails if data is NULL
ALTER TABLE users ADD COLUMN name VARCHAR(100) NOT NULL;

-- GOOD - step by step
ALTER TABLE users ADD COLUMN name VARCHAR(100);  -- nullable first
UPDATE users SET name = 'Default' WHERE name IS NULL;  -- populate
ALTER TABLE users ALTER COLUMN name SET NOT NULL;  -- then constraint
```

5. **PL/pgSQL uchun splitStatements: true:**
```yaml
# BAD - PL/pgSQL will fail!
sqlFile:
  path: changesets/06-plpgsql.sql
  splitStatements: true  # WRONG!

# GOOD
sqlFile:
  path: changesets/06-plpgsql.sql
  splitStatements: false  # CORRECT!
```

6. **Author o'zgartirish:**
```yaml
# Always use:
author: hemis-team  # NOT your personal name
```

7. **Tag'siz migration:**
```yaml
# BAD - no tag
- changeSet:
    id: v6-migration
    sqlFile: ...

# GOOD - with tag
- changeSet:
    id: v6-migration
    sqlFile: ...

- changeSet:
    id: tag-v6
    author: hemis-team
    changes:
      - tagDatabase:
          tag: v6-complete
```

---

## 🎨 Changelog Template (Standard)

Har bir yangi migration uchun ushbu shablonni ishlating:

```yaml
  # =====================================================
  # VX: [Migration Title]
  # =====================================================
  # [Detailed description]
  # - Table(s): [table names]
  # - Indexes: [count]
  # - Data: [count] records
  #
  # Source: [Feature name or ticket]
  # Date: YYYY-MM-DD
  # =====================================================

  - changeSet:
      id: vX-[description-kebab-case]
      author: hemis-team
      comment: [Short comment]
      context: "!test"
      labels: "[schema/data/migration],[feature-name]"
      runOnChange: false

      sqlFile:
        path: changesets/XX-[description-kebab-case].sql
        relativeToChangelogFile: true
        stripComments: false
        splitStatements: false  # true for simple SQL, false for PL/pgSQL

      rollback:
        sqlFile:
          path: changesets/XX-[description-kebab-case]-rollback.sql
          relativeToChangelogFile: true

  # Tag after VX for rollback support
  - changeSet:
      id: tag-vX
      author: hemis-team
      changes:
        - tagDatabase:
            tag: vX-[feature]-complete
```

---

## 🔍 Troubleshooting (Muammolarni Hal Qilish)

### Muammo 0: "relation already exists" / "duplicate key value" (MOST COMMON!)

**Sabab:** Migration idempotent emas - `IF NOT EXISTS` yoki `ON CONFLICT` yo'q.

**Xatolik misollari:**
```
ERROR: relation "idx_system_messages_category" already exists
ERROR: duplicate key value violates unique constraint "roles_code_key"
ERROR: column "phone" of relation "users" already exists
```

**Yechim:**
```sql
-- ❌ XATO (idempotent emas)
CREATE INDEX idx_users_email ON users(email);
INSERT INTO roles (code, name) VALUES ('ADMIN', 'Admin');
ALTER TABLE users ADD COLUMN phone VARCHAR(50);

-- ✅ TO'G'RI (idempotent)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
INSERT INTO roles (code, name) VALUES ('ADMIN', 'Admin')
    ON CONFLICT (code) DO NOTHING;
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
```

**Tezkor yechim:**
```bash
# 1. Migration faylini to'g'irlash (IF NOT EXISTS qo'shish)
nano domain/src/main/resources/db/changelog/changesets/XX-your-migration.sql

# 2. Checksum'ni yangilash
PGPASSWORD=postgres psql -h localhost -U postgres -d test1_hemis -c \
  "UPDATE databasechangelog SET md5sum = NULL WHERE id = 'vX-your-migration';"

# 3. Qayta test qilish
./gradlew bootRun

# Natija: "already exists, skipping" (WARN) - bu normal!
```

**Prevention:** Har doim IDEMPOTENCY CHECKLIST dan foydalaning! (Yuqoridagi bo'limga qarang)

---

### Muammo 1: "Checksum mismatch"

**Sabab:** SQL fayl o'zgartirilgan.

**Yechim:**
```bash
# Option 1: Clear checksums (development only!)
PGPASSWORD=postgres psql -h localhost -U postgres -d test1_hemis -c \
  "UPDATE databasechangelog SET md5sum = NULL WHERE id = 'v6-your-migration';"

# Option 2: Rollback and reapply
./gradlew :domain:liquibaseRollbackCount -Pcount=1
# Fix SQL file
./gradlew :domain:liquibaseUpdate
```

### Muammo 2: "Rollback failed"

**Sabab:** Rollback SQL xato yoki incomplete.

**Yechim:**
```bash
# 1. Check rollback SQL
./gradlew :domain:liquibaseRollbackSQL -Pcount=1

# 2. Manually fix database
PGPASSWORD=postgres psql -h localhost -U postgres -d test1_hemis

# 3. Clear changelog entry
DELETE FROM databasechangelog WHERE id = 'v6-your-migration';

# 4. Fix rollback SQL and try again
./gradlew :domain:liquibaseUpdate
```

### Muammo 3: "PL/pgSQL syntax error"

**Sabab:** `splitStatements: true` ishlatilgan.

**Yechim:**
```yaml
# Change in db.changelog-master.yaml
sqlFile:
  path: changesets/XX-migration.sql
  splitStatements: false  # CRITICAL!
```

### Muammo 4: "Foreign key violation"

**Sabab:** Orphaned records.

**Yechim:**
```sql
-- Before adding FK, clean orphans
DELETE FROM courses
WHERE department_id NOT IN (SELECT id FROM departments);

-- Then add FK
ALTER TABLE courses
ADD CONSTRAINT fk_courses_department...
```

### Muammo 5: "Migration takes too long"

**Sabab:** Large data migration without batching.

**Yechim:**
```sql
-- Use batching for large datasets
DO $$
DECLARE
    batch_size INT := 1000;
    processed INT := 0;
BEGIN
    LOOP
        WITH batch AS (
            SELECT id FROM old_table
            WHERE migrated = FALSE
            LIMIT batch_size
        )
        UPDATE old_table
        SET migrated = TRUE
        WHERE id IN (SELECT id FROM batch);

        GET DIAGNOSTICS processed = ROW_COUNT;
        EXIT WHEN processed = 0;

        RAISE NOTICE 'Processed % records', processed;
        COMMIT;  -- Commit each batch
    END LOOP;
END $$;
```

---

## 📊 Migration Checklist

Har bir migration yozishdan oldin va keyin:

### Yozishdan Oldin:

```
☐ Migration raqamini aniqlash (keyingi: 06, 07, ...)
☐ Feature/ticket raqamini bilish
☐ Qaysi table'lar o'zgarishini aniqlash
☐ Data migration kerakmi aniqlash
☐ Rollback strategiyasini o'ylash
```

### Yozish Jarayonida:

```
☐ SQL fayl yaratildi (XX-description.sql)
☐ Rollback fayl yaratildi (XX-description-rollback.sql)

☐ IDEMPOTENCY CHECKS (CRITICAL!):
  ☐ Barcha CREATE'larda IF NOT EXISTS ishlatildi
  ☐ Barcha DROP'larda IF EXISTS ishlatildi
  ☐ Barcha INSERT'larda ON CONFLICT DO NOTHING yoki WHERE NOT EXISTS
  ☐ Barcha ALTER ADD COLUMN'larda IF NOT EXISTS ishlatildi

☐ CASCADE ishlatildi (DROP statements)
☐ Comments qo'shildi
☐ Indexes qo'shildi (agar kerak)
☐ Constraints qo'shildi (agar kerak)
☐ Initial data qo'shildi (agar kerak)
☐ Changelog'ga qo'shildi
☐ Tag qo'shildi
```

### Test Qilish:

```
☐ liquibaseStatus - migration ko'rinadi
☐ liquibaseUpdate - apply muvaffaqiyatli
☐ Database'da table/data mavjud

☐ IDEMPOTENCY TEST (CRITICAL!):
  ☐ ./gradlew bootRun - ikkinchi marta ishga tushirish
  ☐ Hech qanday ERROR yo'q (faqat WARN: "already exists, skipping")
  ☐ Application muvaffaqiyatli ishga tushdi

☐ liquibaseRollbackSQL - SQL to'g'ri
☐ liquibaseRollbackCount - rollback muvaffaqiyatli
☐ Database'da table/data o'chirilgan
☐ liquibaseUpdate - qayta apply muvaffaqiyatli
☐ liquibaseHistory - tarixda ko'rinadi
```

### Production'ga Tayyorlash:

```
☐ Backup strategiyasi mavjud
☐ Rollback test qilingan
☐ Migration tezligi acceptable
☐ Documentation yangilangan
☐ Team review qilingan
☐ Staging'da test qilingan
```

---

## 🚀 Production Deployment Guide

### Pre-deployment:

```bash
# 1. Database backup
pg_dump -U postgres -d production_db > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Check migration status
./gradlew :domain:liquibaseStatus

# 3. Preview rollback SQL (just in case)
./gradlew :domain:liquibaseRollbackSQL -Pcount=1 > /tmp/emergency-rollback.sql
```

### Deployment:

```bash
# 1. Apply migration
SPRING_PROFILES_ACTIVE=production ./gradlew :domain:liquibaseUpdate

# 2. Verify
SPRING_PROFILES_ACTIVE=production ./gradlew :domain:liquibaseHistory

# 3. Test application
curl https://api.example.com/health
```

### Post-deployment:

```bash
# 1. Monitor logs
tail -f /var/log/hemis-backend/application.log

# 2. Check database performance
PGPASSWORD=xxx psql -h prod-db -U postgres -d hemis_prod -c \
  "SELECT * FROM pg_stat_user_tables WHERE schemaname = 'public';"

# 3. If issues, rollback
./gradlew :domain:liquibaseRollbackCount -Pcount=1
```

---

## 📚 Qo'shimcha Resurslar

**Loyiha Hujjatlari:**
- `/home/adm1n/startup/docs/hemis-back/LIQUIBASE_ROLLBACK_GUIDE.md` - Rollback qo'llanma
- `/home/adm1n/startup/hemis-back/domain/build.gradle.kts` - Liquibase konfiguratsiya
- `/home/adm1n/startup/hemis-back/domain/src/main/resources/db/changelog/` - Changesets

**Liquibase 4.x Documentation:**
- https://docs.liquibase.com/
- https://docs.liquibase.com/commands/home.html

**HEMIS Backend Commands:**
```bash
# List all Liquibase tasks
./gradlew :domain:tasks --group=liquibase

# Help for specific task
./gradlew :domain:help --task liquibaseUpdate
```

---

## 🎓 Amaliy Mashqlar (Practice)

### Mashq 1: Yangi Table Yaratish

**Vazifa:** `courses` jadvalini yaratish

**Talablar:**
- id (UUID, PK)
- name (VARCHAR, NOT NULL)
- code (VARCHAR, UNIQUE)
- credits (INT, 1-10)
- department_id (UUID, FK)
- 3 ta index
- 5 ta initial record

**Yechim:**
```bash
cd /home/adm1n/startup/hemis-back/domain/src/main/resources/db/changelog/changesets/
touch 06-add-courses-table.sql
touch 06-add-courses-table-rollback.sql
# ... SQL yozish (yuqoridagi misollar bo'yicha)
# ... Changelog yangilash
./gradlew :domain:liquibaseUpdate
```

### Mashq 2: Data Migration

**Vazifa:** `old_students` dan `new_students` ga data ko'chirish

**Talablar:**
- PL/pgSQL ishlatish
- Faqat active students
- Error handling
- Count logging

### Mashq 3: Rollback Test

**Vazifa:** V6 migration'ni rollback qilib, qayta apply qilish

```bash
./gradlew :domain:liquibaseRollbackCount -Pcount=1
# Verify table dropped
./gradlew :domain:liquibaseUpdate
# Verify table recreated
```

---

## ✅ Summary (Xulosa)

**Esda Qoldirilishi Kerak:**

1. Har bir migration'da **rollback** bo'lishi shart
2. `IF EXISTS`/`IF NOT EXISTS` har doim ishlatish
3. Production'da **backup** olmasdan migration qilmaslik
4. PL/pgSQL uchun `splitStatements: false`
5. Har bir migration'ga **tag** qo'shish
6. Test qilish: apply → verify → rollback → reapply
7. Author har doim: `hemis-team`

**Qo'shimcha Yordam:**

Agar savol yoki muammo bo'lsa:
- README.md ni o'qing
- LIQUIBASE_ROLLBACK_GUIDE.md ga qarang
- Mavjud migration'larni misol sifatida ishlating

**Good Luck! 🚀**

---

**Version History:**
- 1.0.0 (2025-01-14) - Initial version
- Author: HEMIS Team
- Liquibase Version: 4.31.1
