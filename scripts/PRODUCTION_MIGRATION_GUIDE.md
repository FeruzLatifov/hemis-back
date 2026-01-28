# Production Migration Guide

> **Industry Best Practice Guide** - Google SRE + Netflix DBLog + Shopify Methodology
>
> **Last Updated:** 2025-11-17

---

## 📋 Overview

This guide provides **step-by-step instructions** for safely deploying Liquibase migrations to production.

**Based on:**
- Google SRE Handbook (Site Reliability Engineering)
- Netflix DBLog (Database Changelog System)
- Shopify Zero-Downtime Migrations
- PostgreSQL Official Best Practices

---

## ⏱️ Migration Time Estimates

### Current Changesets (v1-v8):

| Changeset | Purpose | Rows | Time (100K) | Time (1M) | Locks |
|-----------|---------|------|-------------|-----------|-------|
| **v1** | Schema (7 tables) | 0 | ~2 sec | ~5 sec | DDL locks |
| **v2** | Seed data | ~150 | ~1 sec | ~1 sec | Row locks |
| **v3** | Migrate users | ~357 | ~3 sec | ~30 sec | **SHARE lock** |
| **v4** | Translations | ~500 | ~2 sec | ~5 sec | Row locks |
| **v5** | Faculty i18n | ~50 | ~1 sec | ~1 sec | Row locks |
| **v6** | Permissions | ~58 | ~1 sec | ~1 sec | Row locks |
| **v7** | Menus table | 0 | ~1 sec | ~2 sec | DDL locks |
| **v8** | Menu seed | ~21 | ~1 sec | ~1 sec | Row locks |

**Total Time:**
- **100K users:** ~12 seconds
- **1M users:** ~47 seconds

**Critical:** V3 (user migration) is the longest - plan maintenance window accordingly.

---

## 🔒 Index Creation - CONCURRENTLY Strategy

### Development (Current Approach):

```sql
-- Works fine in dev (fast, no users)
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
```

### Production (Recommended - Zero Downtime):

```sql
-- Zero downtime - no table locking
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_username ON users(username);

-- Update query optimizer statistics
ANALYZE users;
```

### Why CONCURRENTLY?

**Without CONCURRENTLY:**
```
08:00:00 - CREATE INDEX starts
08:00:01 - Table LOCKED 🔒 (no reads/writes)
08:00:30 - Users cannot login ❌
08:00:30 - Index complete
08:00:30 - Table UNLOCKED ✅
```

**With CONCURRENTLY:**
```
08:00:00 - CREATE INDEX CONCURRENTLY starts
08:00:01 - Users can still login ✅ (background indexing)
08:00:30 - Index complete (zero downtime)
```

### Performance Impact:

| Database Size | Without CONCURRENTLY | With CONCURRENTLY |
|---------------|---------------------|-------------------|
| 10K users | 1 sec downtime | 0 downtime ✅ |
| 100K users | 10 sec downtime | 0 downtime ✅ |
| 1M users | 2 min downtime ❌ | 0 downtime ✅ |

---

## 🧪 Pre-Deployment Testing (MANDATORY)

### Step 1: Run Test Script

```bash
cd /home/adm1n/startup/hemis-back
./scripts/test-migrations.sh
```

This script performs:
1. ✅ Check current migration status
2. ✅ Generate migration SQL preview (dry run)
3. ✅ Generate rollback SQL preview
4. ✅ Display summary and recommendations

### Step 2: Manual Commands (Alternative)

```bash
# 1. Check what will be applied
./gradlew :domain:liquibaseStatus

# 2. Preview migration SQL (does NOT modify database)
./gradlew :domain:liquibaseUpdateSQL > /tmp/migration-preview.sql

# 3. Review the SQL
cat /tmp/migration-preview.sql

# 4. Preview rollback SQL
./gradlew :domain:liquibaseRollbackSQL -Pcount=1 > /tmp/rollback-preview.sql

# 5. Review rollback SQL
cat /tmp/rollback-preview.sql
```

---

## 🚀 Production Deployment Procedure

### Pre-Deployment Checklist:

- [ ] Database backup created (pg_dump)
- [ ] Maintenance window scheduled
- [ ] Users notified (if downtime expected)
- [ ] Migration preview reviewed (`liquibaseUpdateSQL`)
- [ ] Rollback plan tested (`liquibaseRollbackSQL`)
- [ ] Monitoring dashboard open
- [ ] Team on standby

### Deployment Steps:

#### Option A: Spring Boot Auto-Apply (Recommended)

```bash
# Migration runs automatically on startup
./gradlew :app:bootRun

# Monitor logs:
tail -f app/build/logs/application.log | grep -i liquibase
```

**Pros:**
- ✅ Automatic (no manual commands)
- ✅ Integrated with app startup
- ✅ Locking prevents concurrent runs

**Cons:**
- ⚠️ Application won't start if migration fails
- ⚠️ Need to restart app to retry

#### Option B: Manual Gradle Task (More Control)

```bash
# Apply migrations manually BEFORE starting app
./gradlew :domain:liquibaseUpdate

# Then start app (migrations already applied)
./gradlew :app:bootRun
```

**Pros:**
- ✅ More control over timing
- ✅ Can retry without restarting app
- ✅ Separate logs for migration

**Cons:**
- ⚠️ Manual step (more error-prone)
- ⚠️ Need to remember to run it

---

## 🔄 Rollback Procedure

### When to Rollback:

- ❌ Migration fails halfway
- ❌ Data corruption detected
- ❌ Application errors after migration
- ❌ Performance degradation

### Rollback Commands:

```bash
# Rollback last changeset (v8)
./gradlew :domain:liquibaseRollbackCount -Pcount=1

# Rollback last 2 changesets (v7, v8)
./gradlew :domain:liquibaseRollbackCount -Pcount=2

# Rollback to specific tag
./gradlew :domain:liquibaseRollback -Ptag=v6-menu-permissions-complete

# Preview rollback first (RECOMMENDED)
./gradlew :domain:liquibaseRollbackSQL -Pcount=1
```

### Rollback Time Estimates:

| Changeset | Rollback Time (100K) | Rollback Time (1M) |
|-----------|---------------------|-------------------|
| v8 | ~1 sec | ~2 sec |
| v7 | ~1 sec | ~2 sec |
| v6 | ~1 sec | ~2 sec |
| v5 | ~1 sec | ~2 sec |
| v4 | ~2 sec | ~5 sec |
| v3 | ~3 sec | ~30 sec |
| v2 | ~2 sec | ~5 sec |
| v1 | ~1 sec | ~2 sec |

---

## 📊 Monitoring During Migration

### What to Monitor:

```bash
# 1. Migration logs
tail -f /tmp/liquibase-migration.log

# 2. Database connections
SELECT count(*) FROM pg_stat_activity WHERE datname = 'hemis';

# 3. Table locks
SELECT * FROM pg_locks WHERE relation = 'users'::regclass;

# 4. Migration progress (DATABASECHANGELOG)
SELECT id, author, dateexecuted, orderexecuted
FROM DATABASECHANGELOG
ORDER BY dateexecuted DESC
LIMIT 10;
```

### Red Flags (Stop Immediately):

- 🚨 **Timeout errors** - Database overloaded
- 🚨 **Lock wait timeouts** - Conflicting queries
- 🚨 **Constraint violations** - Data integrity issues
- 🚨 **Disk full** - Not enough space
- 🚨 **Out of memory** - Need more RAM

---

## 🎯 Best Practices

### DO ✅

1. **Always test in staging first**
   ```bash
   # Staging
   ./scripts/test-migrations.sh
   ./gradlew :domain:liquibaseUpdate
   ```

2. **Create database backup**
   ```bash
   pg_dump -h localhost -U postgres hemis > /backup/hemis-$(date +%Y%m%d).sql
   ```

3. **Use maintenance window**
   - Schedule: 03:00-04:00 AM (low traffic)
   - Notify users 24 hours in advance
   - Have rollback plan ready

4. **Monitor logs**
   ```bash
   tail -f app/build/logs/application.log
   ```

5. **Use CONCURRENTLY for production indexes**
   ```sql
   CREATE INDEX CONCURRENTLY ...;
   ANALYZE table_name;
   ```

### DON'T ❌

1. **Never modify applied changesets**
   - Liquibase tracks checksums
   - Modification will cause errors

2. **Never run migrations concurrently**
   - DATABASECHANGELOGLOCK prevents this
   - But don't try to bypass it

3. **Never skip testing**
   - Always run `liquibaseUpdateSQL` first
   - Review SQL before applying

4. **Never ignore warnings**
   - Liquibase warnings = potential problems
   - Investigate before proceeding

5. **Never run without backup**
   - Backup = insurance policy
   - Always have rollback option

---

## 🛠️ Troubleshooting

### Issue: Migration Stuck

**Symptoms:**
```
Waiting for changelog lock...
```

**Cause:** Previous migration crashed, lock not released

**Solution:**
```sql
-- Check lock
SELECT * FROM DATABASECHANGELOGLOCK;

-- Release lock (ONLY if no migration running)
UPDATE DATABASECHANGELOGLOCK SET locked = FALSE;
```

### Issue: Checksum Mismatch

**Symptoms:**
```
Validation Failed:
     1 changesets check sum
          changesets/01-complete-schema.sql::v1-complete-schema::hemis-team was: 8:abcd1234 but is now: 8:efgh5678
```

**Cause:** Changeset file was modified after being executed

**Solution:**
```bash
# Option 1: Clear checksums (ONLY in development)
./gradlew :domain:liquibaseClearChecksums

# Option 2: Create new changeset (PRODUCTION)
# DO NOT modify old changesets!
# Create v9-fix-something.sql instead
```

### Issue: Migration Failed Halfway

**Symptoms:**
```
ERROR: relation "users" already exists
Migration failed!
```

**Cause:** Previous migration partially completed

**Solution:**
```bash
# 1. Check what was applied
./gradlew :domain:liquibaseStatus

# 2. Manual cleanup (if needed)
psql -U postgres hemis -c "DROP TABLE IF EXISTS users CASCADE;"

# 3. Retry migration
./gradlew :domain:liquibaseUpdate
```

---

## 📚 References

1. **Google SRE Handbook**
   - Chapter 14: Managing Critical State
   - Best practices for database changes

2. **Netflix DBLog**
   - Zero-downtime migrations
   - Rollback strategies

3. **Shopify Engineering Blog**
   - "Deferred Migrations"
   - Large-scale database changes

4. **PostgreSQL Documentation**
   - CREATE INDEX CONCURRENTLY
   - Lock management

5. **Liquibase Best Practices**
   - Official documentation
   - Community guidelines

---

## 🆘 Emergency Contacts

**During Production Deployment:**

1. **Database Team:** database-team@example.com
2. **DevOps On-Call:** +998-XX-XXX-XXXX
3. **CTO (Escalation):** cto@example.com

**Rollback Authority:**
- **Staging:** Developers (anyone)
- **Production:** DevOps Lead + CTO approval

---

**Document Version:** 1.0
**Last Updated:** 2025-11-17
**Next Review:** Every 3 months or after major migration
