# Database Migration Strategy

## Overview

This project uses **Liquibase** for database schema versioning and migrations, following industry best practices for automatic migrations with safeguards.

## Strategy: Automatic Migrations with Profile-Based Control

### ✅ Best Practice (Industry Standard)

We follow the approach used by companies like Netflix, Airbnb, Google, and modern microservices:

- **Development**: Automatic migrations ✅
- **Staging**: Automatic migrations ✅  
- **Production**: Automatic migrations with safeguards ✅

### Why Automatic Migrations?

#### ✅ Advantages
- **Zero manual intervention** - No human error
- **Consistent across environments** - Same process everywhere
- **CI/CD friendly** - Perfect for automation
- **12-Factor App compliant** - Stateless, declarative
- **Developer productivity** - Just run the app
- **No forgotten migrations** - Always up-to-date

#### ⚠️ Considerations
- Startup time increased (1-5 seconds)
- Requires proper safeguards in production
- Database lock prevents concurrent execution

## Configuration

### Development (`application-dev.yml`)

```yaml
spring:
  liquibase:
    enabled: true  # ✅ Automatic migrations
    change-log: classpath:/db/changelog/db.changelog-master.yaml
    contexts: dev
    default-schema: public
```

**Behavior**: Migrations run automatically on every startup. Perfect for rapid development.

### Production (`application-prod.yml`)

```yaml
spring:
  liquibase:
    enabled: true  # ✅ Automatic with safeguards
    change-log: classpath:/db/changelog/db.changelog-master.yaml
    contexts: production
    default-schema: public
    lock-timeout: 30  # Wait max 30 seconds for lock
    drop-first: false  # Safety: never drop
```

**Behavior**: Migrations run automatically with production-safe contexts and database lock.

## Safeguards

### 1. Database Lock (`DATABASECHANGELOGLOCK`)

Liquibase automatically uses a database lock table to ensure **only ONE instance** runs migrations at a time, even in a multi-instance deployment.

```sql
SELECT * FROM databasechangeloglock;
-- id | locked | lockgranted | lockedby
-- 1  | false  |             |
```

### 2. Checksum Validation

Every changeset has an MD5 checksum. If a changeset is modified after execution, Liquibase will fail fast:

```
Validation Failed:
  1 changeset check sum
      db/changelog/db.changelog-master.yaml::v1-complete-schema::hemis-team 
      was: 9:d897d5b74698bf103e9488e9ff4d6e48
      but is now: 9:abc123...
```

### 3. Logical File Path

All changesets use `logicalFilePath` to ensure consistent tracking across environments:

```yaml
- changeSet:
    id: v1-complete-schema
    author: hemis-team
    logicalFilePath: db/changelog/db.changelog-master.yaml  # ← Consistency
```

### 4. Context Filtering

Changesets can be environment-specific:

```yaml
- changeSet:
    id: v1-complete-schema
    context: "dev,staging,production"  # Runs in all environments

- changeSet:
    id: v2-seed-test-data
    context: "dev"  # Only in development
```

### 5. idempotency

All changesets are idempotent - safe to run multiple times:
- `runOnChange: false` (default) - Never re-run
- Checksum validation prevents modifications
- Pre-conditions skip if already applied

## Changeset Best Practices

### 1. Never Modify Executed Changesets

❌ **DON'T:**
```yaml
- changeSet:
    id: v1-create-users
    sqlFile: create-users.sql  # ← Already executed, don't modify!
```

✅ **DO:**
```yaml
- changeSet:
    id: v1-create-users
    sqlFile: create-users.sql  # ← Leave unchanged

- changeSet:
    id: v2-add-email-column  # ← New changeset
    sqlFile: add-email.sql
```

### 2. Use Logical File Paths

✅ Always include `logicalFilePath`:

```yaml
- changeSet:
    id: v3-add-permissions
    logicalFilePath: db/changelog/db.changelog-master.yaml
```

### 3. Provide Rollback

✅ Every changeset should have a rollback:

```yaml
- changeSet:
    id: v3-add-index
    sqlFile: add-index.sql
    rollback:
      sqlFile: drop-index-rollback.sql
```

### 4. Use Tags for Milestones

✅ Tag major versions for easy rollback:

```yaml
- changeSet:
    id: tag-v1
    changes:
      - tagDatabase:
          tag: v1-schema-complete
```

## Manual Operations (Emergency)

While automatic migrations are preferred, manual control is available via Gradle tasks:

```bash
# View migration status
./gradlew :domain:liquibaseStatus

# Apply pending migrations (if disabled in config)
./gradlew :domain:liquibaseUpdate

# Rollback last N changesets
./gradlew :domain:liquibaseRollbackCount -Pcount=1

# Rollback to specific tag
./gradlew :domain:liquibaseRollbackToTag -Ptag=v3-users-migrated

# View migration history
./gradlew :domain:liquibaseHistory
```

## Troubleshooting

### Duplicate Migrations

**Problem**: Changesets executed multiple times (e.g., 252 records instead of 12).

**Root Cause**: Missing `logicalFilePath` or inconsistent filename tracking.

**Solution**: ✅ Already fixed!
- Added `logicalFilePath` to all changesets
- Normalized database `filename` column
- Removed duplicate rollback blocks

**Verification**:
```sql
SELECT COUNT(*), COUNT(DISTINCT id) FROM databasechangelog;
-- Should be equal (e.g., 12 = 12)
```

### Lock Timeout

**Problem**: `Waiting for changelog lock...`

**Cause**: Previous migration crashed without releasing lock.

**Solution**:
```sql
-- Check lock status
SELECT * FROM databasechangeloglock;

-- Release lock (if stuck)
UPDATE databasechangeloglock SET locked = false WHERE id = 1;
```

### Checksum Mismatch

**Problem**: `Validation Failed: changeset check sum`

**Cause**: Changeset file was modified after execution.

**Solutions**:
1. ✅ **Recommended**: Create a new changeset with the fix
2. ⚠️ **Emergency**: Clear checksum (allows re-execution)
   ```bash
   ./gradlew :domain:liquibaseClearChecksums
   ```

## Migration Workflow

### Development
1. Create new changeset in `changesets/` directory
2. Reference in `db.changelog-master.yaml`
3. Run application - migrations execute automatically
4. Verify in database

### Production Deployment

#### Option 1: Automatic (Recommended)
1. Deploy new application version
2. First instance acquires lock
3. Migrations execute during startup
4. Lock released, other instances start
5. All instances see updated schema

#### Option 2: Pre-Deployment (High-Traffic Apps)
```bash
# 1. Run migrations before deployment
./gradlew :domain:liquibaseUpdate

# 2. Verify
./gradlew :domain:liquibaseStatus

# 3. Deploy application (migrations already applied)
```

## File Structure

```
domain/src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Master changelog
└── changesets/
    ├── 01-complete-schema.sql
    ├── 01-complete-schema-rollback.sql
    ├── 02-complete-seed-data.sql
    ├── 02-complete-seed-data-rollback.sql
    ├── 03-migrate-old-users.sql
    ├── 03-migrate-old-users-rollback.sql
    ├── 04-complete-translations.sql
    ├── 04-complete-translations-rollback.sql
    ├── 05-faculty-registry-translations.sql
    ├── 05-faculty-registry-translations-rollback.sql
    ├── 06-add-menu-permissions.sql
    └── 06-add-menu-permissions-rollback.sql
```

## References

- [Liquibase Best Practices](https://www.liquibase.org/get-started/best-practices)
- [Spring Boot Liquibase Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [12-Factor App: Database Migrations](https://12factor.net/dev-prod-parity)

## Changelog

| Date       | Change                                      | Author      |
|------------|---------------------------------------------|-------------|
| 2025-01-15 | Migrated from Flyway to Liquibase           | hemis-team  |
| 2025-01-15 | Fixed duplicate migrations issue            | hemis-team  |
| 2025-01-15 | Implemented automatic migrations strategy   | hemis-team  |
