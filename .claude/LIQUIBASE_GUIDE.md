# Liquibase Migration Guide

> **Database Migration Best Practices for HEMIS Backend**

This guide summarises the essential rules and workflow for creating, testing and applying database migrations using Liquibase.  It is a condensed version of the full guide located at the root of the repository (`LIQUIBASE_GUIDE.md`).  Always refer to the full guide for detailed examples and edge cases.

---

## 🚨 Golden Rules (No Exceptions)

1. **Idempotency** – Every migration must be safe to run multiple times.  Use `IF NOT EXISTS` / `IF EXISTS` and handle existing data gracefully.
2. **Rollback** – Every migration MUST have a rollback script.  Test the rollback locally and on staging before deploying to production.  Remember to set `splitStatements: false` in `db.changelog-master.yaml` when using PostgreSQL DO blocks.
3. **Testing** – Test your migration on a local database, then on staging.  Never test directly on production.
4. **Naming** – Use sequential numbering with two digits (`01`, `02`, etc.) and descriptive names, e.g. `07-add-departments-table.sql` and `07-add-departments-table-rollback.sql`.
5. **No direct DDL** – Never run manual `ALTER` or `DROP` statements directly on a database.  Always use Liquibase migrations instead.

---

## 📁 Migration Structure

- Migrations live in `domain/src/main/resources/db/changelog/changesets/`.
- Each migration has two files: a forward SQL file and a corresponding rollback SQL file.
- The master file `db.changelog-master.yaml` registers the migrations in order; add new files to this list.
- Top of each SQL file should include comments: version (`V7`), author, date, and description.
- Use idempotent DDL and DML:
  - `CREATE TABLE IF NOT EXISTS ...`
  - `ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...`
  - `INSERT ... ON CONFLICT DO NOTHING` for seed data.
- Create indexes conditionally: `CREATE INDEX IF NOT EXISTS ...`.
- Do not hardcode schema names; rely on the default search path.

---

## 🛠️ Creating a New Migration

1. **Plan your migration**
   - What are you changing? (new table, new column, seed data, data fix)
   - Can it be rolled back?  How will rollback affect data?
   - Is the change idempotent?  Can the script run twice safely?
   - Will the change affect a running application?  Does it require a deployment?

2. **Create migration files**
   - Navigate to `domain/src/main/resources/db/changelog/changesets/`.
   - Choose the next sequential number (e.g. `07`) and descriptive name.
   - Create two files:
     - `07-your-change.sql` – forward migration
     - `07-your-change-rollback.sql` – rollback script

3. **Write the forward migration**
   - Use idempotent DDL.  For example:

     ```sql
     -- Create table (idempotent)
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

     -- Indexes (idempotent)
     CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);
     CREATE INDEX IF NOT EXISTS idx_departments_parent ON departments(parent_id);
     CREATE INDEX IF NOT EXISTS idx_departments_active ON departments(is_active) WHERE deleted_at IS NULL;

     -- Comments
     COMMENT ON TABLE departments IS 'Organizational departments structure';
     COMMENT ON COLUMN departments.code IS 'Unique department code';
     COMMENT ON COLUMN departments.parent_id IS 'Parent department for hierarchical structure';
     ```

   - When adding columns, use `ALTER TABLE ... ADD COLUMN IF NOT EXISTS ...` and provide default values if necessary.
   - For seed data or data migration, use `INSERT INTO ... ON CONFLICT DO NOTHING` or `UPDATE ...` statements within a transaction.

4. **Write the rollback script**
   - The rollback should reverse the forward migration.  For example:

     ```sql
     -- Rollback for departments table
     DROP TABLE IF EXISTS departments;
     -- Or: ALTER TABLE departments DROP COLUMN IF EXISTS phone;
     ```

   - If data was inserted or updated, delete or revert those changes carefully.

5. **Register the migration**
   - Edit `db.changelog-master.yaml` and add entries for both the forward and rollback files with the correct order.
   - Example snippet:

     ```yaml
     - changeLog: classpath:/db/changelog/changesets/07-your-change.sql
     - changeLog: classpath:/db/changelog/changesets/07-your-change-rollback.sql
     ```

6. **Test thoroughly**
   - Run `./gradlew :domain:liquibaseUpdate` to apply the migration on your local database.
   - Verify the schema and data.
   - Run `./gradlew :domain:liquibaseRollback` or `liquibaseRollbackSQL` to test the rollback.
   - Apply the migration on a staging environment and monitor for issues.

---

## 📚 Further Examples

Refer to the full `LIQUIBASE_GUIDE.md` in the repository root for:

- Detailed SQL examples for creating tables, adding columns, inserting and updating data.
- Complex scenarios involving constraints, sequences and triggers.
- Guidelines for splitting large migrations into multiple steps.
- Notes on performance optimisation and index management.

This condensed guide provides the high‑level process.  Always read the full guide before implementing a non‑trivial migration.
