# HEMIS Backend - Project Context

> **H**igher **E**ducation **M**anagement **I**nformation **S**ystem
> O'zbekiston oliy ta'lim muassasalari uchun kompleks boshqaruv tizimi

---

## Business Domain

**Core Functions:** Student management, academic programs, staff, university structure, finance (payments/scholarships), ministry reports, government integrations (HEMIS, OneID, MyGov).

**Scale:** 340+ users (legacy CUBA dan ko'chirilgan), 5 asosiy rol, 30+ universitetlar.

---

## Tech Stack

| Component | Version |
|-----------|---------|
| Spring Boot | 4.0.2 |
| Java | 21 LTS (Temurin, toolchain; fallback JDK 25) |
| Gradle | 9.3.0 (Kotlin DSL) |
| PostgreSQL | 18 (master/replica) |
| Redis | 7 (cache + token store) |
| Liquibase | 4.31.1 |
| MapStruct | 1.6.3 |
| Lombok | 1.18.x |
| Sentry | 8.29.0 |
| SpringDoc | 3.0.1 (OpenAPI) |
| Jedis | 5.1.0 |

---

## Legacy Schema

**CRITICAL:** Schema is FROZEN — NO ALTER/DROP/RENAME on legacy tables!

| Table | Description |
|-------|-------------|
| `sec_user` (340) | Legacy CUBA users (PBKDF2 passwords) |
| `users` (339) | New users (BCrypt passwords) |
| `hemishe_e_student` (~5,000) | Students |
| `hemishe_e_curriculum` | Academic programs |
| `hemishe_e_subject` | Courses |
| `h_employee` | Staff members |
| `h_system_message_translation` | i18n (508 translations x 4 languages) |

**Total:** ~50 domain tables + 10 security tables

### Migration History
```
v1-schema-complete      → Base schema (7 tables)
v2-seed-data-complete   → Roles + permissions (95 records)
v3-users-migrated       → User migration (339 users)
v4-menu-translations    → i18n messages (508 records)
v5-faculty-translations → Faculty data (50 records)
```

---

## RBAC (Role-Based Access Control)

```
User → Roles → Permissions
339    5 roles   30 perms
```

| Role | Type | Description |
|------|------|-------------|
| `SUPER_ADMIN` | SYSTEM | Full system access (Ministry) |
| `MINISTRY_ADMIN` | SYSTEM | Ministry administrator |
| `UNIVERSITY_ADMIN` | UNIVERSITY | Per-university administrator |
| `VIEWER` | SYSTEM | Read-only access |
| `REPORT_VIEWER` | CUSTOM | Read-only reports |

**Role Types:** SYSTEM (built-in, all institutions), UNIVERSITY (scoped to one OTM), CUSTOM (user-defined).

**Permission Format:** `{resource}.{action}` — e.g. `students.view`, `faculty.create`, `grades.edit`

---

## API Modules

### 1. api-legacy (CUBA Compatibility)
- **Base Path:** `/app/rest/*`
- **Controllers:** 56
- **Format:** CUBA Platform JSON (`_entityName`, `_instanceName`)
- **Status:** Maintained for transition period

```
GET  /app/rest/v2/entities/hemishe_Student
POST /app/rest/v2/entities/hemishe_Student
GET  /app/rest/v2/entities/hemishe_Curriculum/{id}
```

### 2. api-web (Modern REST)
- **Base Path:** `/api/v1/web/*`
- **Controllers:** 30
- **Format:** Clean JSON + full Swagger docs

```
GET    /api/v1/web/students?page=0&size=20
POST   /api/v1/web/students
GET    /api/v1/web/faculty/{id}/departments
GET    /api/v1/web/i18n/messages?lang=uz-UZ
```

### 3. api-external (Server-to-Server)
- **Base Path:** `/api/v1/external/*`
- **Controllers:** 6
- **Security:** API Key + IP whitelist

### Response Format Standards

```json
// Success
{ "success": true, "data": { ... }, "timestamp": "..." }

// Error
{ "success": false, "error": { "code": "RESOURCE_NOT_FOUND", "message": "...", "details": [...] }, "timestamp": "..." }

// Paginated
{ "success": true, "data": [...], "page": { "number": 0, "size": 20, "totalElements": 150, "totalPages": 8 } }
```

---

## External Integrations

| Service | URL | Purpose |
|---------|-----|---------|
| HEMIS Ministry | `https://student.hemis.uz` | University data sync |
| OneID SSO | `https://sso.egov.uz` | Single Sign-On |
| MyGov Portal | `https://my.gov.uz` | Student verification |
| PayMe/Click | — | Payment processing |

**Note:** Government APIs use self-signed certificates — SSL bypass per-connection, NOT global JVM.

---

## Password Encoding (Hybrid)

```
NEW (BCrypt):  $2a$10$N9qo8uLOickgx2ZMRZoMye...
OLD (PBKDF2):  hash:salt:iterations (CUBA format)
```

`LegacyPasswordEncoder` detects format by prefix and handles both.

---

## Further Reading

| File | Purpose |
|------|---------|
| `@architecture.md` | System architecture, modules, DB routing, cache, deployment |
| `@rules.md` | Coding standards, module guidelines |
| `@MANDATORY_REQUIREMENTS.md` | Swagger, test examples, PR checklists |
| `@ENDPOINT_PORTING_GUIDE.md` | Legacy endpoint migration workflow |
| `@LIQUIBASE_GUIDE.md` | Database migration guide |
| `@SWAGGER_GUIDE.md` | Swagger annotation guide |
| `@TESTING_GUIDE.md` | Unit & integration test guide |
