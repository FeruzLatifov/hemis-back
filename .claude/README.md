# 🎯 HEMIS Backend - Quick Reference

> **Essential rules for developers**  
> **Read this FIRST before coding!**

---

## 🚨 GOLDEN RULES (NEVER VIOLATE)

```
1. NO SCHEMA CHANGES manually
   → Use Liquibase ONLY
   
2. SERVICE LAYER MANDATORY
   → Controller → Service → Repository
   → NO business logic in controllers
   
3. SECURITY BY DEFAULT
   → ALL endpoints need @PreAuthorize
   → Input validation REQUIRED
   
4. SWAGGER + TESTS MANDATORY
   → Every endpoint = Swagger + Integration test
   → Every service method = Unit test
   → Coverage minimum 70%
   
5. IDEMPOTENCY REQUIRED
   → Migrations must be safe to run twice
   → Use IF NOT EXISTS, ON CONFLICT
```

---

## 📁 Project Structure

```
hemis-back/
├── app/              # Main application (Spring Boot entry)
├── common/           # DTOs, exceptions, utilities
├── domain/           # Entities, repositories, migrations
├── security/         # JWT, OAuth2, authentication
├── service/          # Business logic (CRUD operations)
├── api-web/          # Modern REST API
├── api-legacy/       # CUBA compatibility API
└── api-external/     # External integrations
```

---

## 🔧 Technology Stack

- **Java 21** + Spring Boot 3.5.7
- **PostgreSQL 16** (master-replica)
- **Redis 7** (caching)
- **Liquibase 4.31.1** (migrations)
- **JWT** (authentication)

---

## 💻 Daily Commands

```bash
# Run application
./gradlew :app:bootRun

# Run tests
./gradlew test

# Apply migrations
./gradlew :domain:liquibaseUpdate

# Check migration status
./gradlew :domain:liquibaseStatus

# Build
./gradlew clean build -x test
```

---

## 📝 New Endpoint Checklist

```
☑ Controller with @RestController
☑ Service interface + implementation
☑ Repository (if needed)
☑ DTOs (Request/Response)
☑ Mapper (MapStruct)
☑ @PreAuthorize on service method
☑ Swagger annotations (@Operation, @ApiResponses)
☑ Integration test (200, 400, 401, 403, 404)
☑ Unit test for service
☑ Test in Swagger UI
```

---

## 🗄️ New Migration Checklist

```
☑ Files: XX-name.sql + XX-name-rollback.sql
☑ IF NOT EXISTS / IF EXISTS
☑ ON CONFLICT DO NOTHING for inserts
☑ splitStatements: false for DO blocks
☑ Added to db.changelog-master.yaml
☑ Tested: apply → rollback → re-apply
```

---

## 🚫 Common Mistakes

```
❌ Direct database changes (use Liquibase)
❌ Business logic in controllers
❌ Returning entities (use DTOs)
❌ Missing Swagger documentation
❌ No tests
❌ Non-idempotent migrations
❌ Hardcoded UUIDs/passwords
❌ System.out.println (use Logger)
```

---

## 📚 Full Documentation

For detailed information:

- **context.md** - Project overview & architecture
- **rules.md** - Complete coding standards
- **architecture.md** - System architecture details
- **MANDATORY_REQUIREMENTS.md** - Swagger & Testing rules
- **LIQUIBASE_GUIDE.md** - Migration guide

---

## 🆘 Need Help?

1. Read this file first
2. Check specific guide (rules.md, etc.)
3. Look at existing code examples
4. Ask team lead

---

**Remember:** Quality > Speed. Take time to follow standards! 🎯
