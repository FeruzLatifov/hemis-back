# HEMIS Backend - Project Context

> **H**igher **E**ducation **M**anagement **I**nformation **S**ystem
> Vazirlik darajasidagi **MARKAZIY agregator** (1 deploy/cluster) — `/home/adm1n/projects/startup/old-hemis` (**CUBA Platform 7.3 — Java + Groovy**, Haulmont) ning Spring Boot 4 + Java 25 ga **qayta yozilishi va optimizatsiyasi** (Java → Java modernizatsiya).

---

## Loyiha maqsadi

HEMIS-back **per-OTM EMAS** — bu vazirlik darajasidagi yagona markaziy backend.

**4 ta asosiy maqsad:**

1. **Aggregation (markaziy yig'ish):** 230 ta OTM dan o'quv ma'lumotini yagona markaziy DB ga yig'ish (talabalar, baholar, o'qituvchilar, hisobotlar)
2. **Klassifikator distribution:** `h_*` jadvallari (gender, soato, position_type, va h.k.) — markaziy server **yagona manba**, Univer (per-OTM) markazdan sync qiladi
3. **Qoidalar joriy qilish:** vaqt cheklovi (talaba kiritish, baho o'zgartirish), biznes konstraint — markaziy daraja
4. **Davlat integratsiya:** MyGov, MSPD, BIMM, Tax/Soliq, GUVD, OneID — S2S aloqa markaziy server orqali

## Business Domain

**Core Functions:** Markaziy aggregation (230 OTM), klassifikator markaziy taqsimot, biznes qoidalar joriy qilish, davlat integratsiya (MyGov, OneID, MSPD, BIMM, Tax, GUVD), reports.

**Scale (markaziy server agregati):**
- **230 ta OTM** (vazirlik tasarrufida — umumiy)
  - Shundan **224 ta** Univer Yii2 PHP ishlatadi (api-legacy mijozlari — har OTM o'z lokal `hemis_NNN` DB bilan)
  - 6 ta — boshqa stack (Univer'siz)
  - **Univer ↔ HEMIS-back** = network REST API (lokalhost EMAS)
- **~5,000 admin foydalanuvchi** (markaziy DB da, 230 OTM bo'ylab — taxminiy 20-30 admin/OTM)
- **~1.15M talaba metadata** (markaziy aggregation taxminiy: 230 OTM × ~5K talaba/OTM o'rtacha; real son `hemishe_e_student.count(*)` orqali olinadi)
- **~50,000 o'qituvchi metadata** (markaziy aggregation taxminiy: 230 × ~200/OTM)
- **Inson rollari 3 pog'ona** (SUPER_ADMIN > ADMIN > TECH_STAFF="Texnik xodim") + CLASSIFIER_MANAGER, VIEWER, INSPECTOR, REPORT_VIEWER; mashina: OTM_API, EXTERNAL_API
- **Peak concurrent:** ~500-1000 markaziy server'da (sessiya boshlanishi, hisobotlar davrida 2x)
- **224 Univer client** parallel — har OTM Yii2 PHP backend api-legacy/api-university orqali
- **Migrated baseline (M001):** sec_user → users (~340 legacy CUBA users, V3.0 snapshot)

---

## Tech Stack

| Component | Version |
|-----------|---------|
| Spring Boot | 4.0.6 |
| Java | 25 LTS (Temurin, toolchain) |
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
| `ADMIN` | SYSTEM | Administrator — kundalik boshqaruv (S038'gacha kod `MINISTRY_ADMIN` edi) |
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

## External Integrations (api-external — davlat tashkilotlari S2S)

> **Eslatma:** HEMIS-back **o'zi vazirlik markaziy server**. Quyidagilar — TASHQI davlat sistemalari:

| Service | URL | Purpose |
|---------|-----|---------|
| OneID SSO | `https://sso.egov.uz` | Yagona davlat kirish (federation) |
| MyGov Portal | `https://my.gov.uz` | Fuqaro verifikatsiyasi |
| MSPD | `http://172.18.9.171` | Sotsiologik tekshiruv |
| BIMM | — | Sertifikatlar |
| Tax/Soliq | — | Sub'ekt PINFL check |
| GUVD | — | Passport ma'lumoti |
| PayMe/Click | — | To'lov |

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
