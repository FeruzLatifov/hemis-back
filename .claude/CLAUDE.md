# HEMIS Backend – Project Memory

## Project Overview

HEMIS Backend — Java 21 + Spring Boot modular monolith. Clean architecture: API → Service → Domain → Common.
Legacy CUBA platform bilan 100% backward compatibility saqlanadi.

**Stack:** Spring Boot 4.0.2, PostgreSQL 18, Redis 7, Liquibase 4.31.1, MapStruct, Lombok

## Daily Commands

```bash
./gradlew clean build           # Build
./gradlew :app:bootRun          # Run (port 8081)
./gradlew test                  # Tests (TESTS_ENABLED=true kerak)
./gradlew :domain:liquibaseUpdate    # Apply migrations
./gradlew :domain:liquibaseStatus    # Migration status
```

## Test Credentials

- **Manba:** `/home/adm1n/startup/hemis-back/docs/endpoint_tester.html`
- **Token:** `POST /app/rest/v2/oauth/token` (Basic Auth credentials `.env` da)

## Environment

`.env` fayldan o'qiladi (`.gitignore` ga qo'shilgan, commit qilmang):

| Variable | Tavsif |
|----------|--------|
| `SERVER_PORT` | HTTP port (default: 8081) |
| `DB_MASTER_*` / `DB_REPLICA_*` | PostgreSQL master/replica connection |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` | Redis (cache + token store) |
| `TESTS_ENABLED` | `true` bo'lmasa testlar ishlamaydi |
| `hemis.security.jwt.secret` | JWT signing secret (HS256) |

## JWT

- **Algorithm:** HS256 (lokal) yoki RS256 (`JWT_JWK_SET_URI` orqali)
- **Access token:** 12 soat, **Refresh token:** 7 kun
- **Claims:** `sub` (user ID), `username`, `scope` — rollar Redis da cache qilinadi

## Actuator Endpoints

| Endpoint | Access |
|----------|--------|
| `/actuator/health`, `/actuator/info` | Public |
| `/actuator/metrics`, `/actuator/liquibase` | JWT (protected) |
| `/actuator/env` | Admin only |

---

## CRITICAL: "Porting" vs "Migration"

Bu ikki **butunlay boshqa** ish turi — aralashtirib yubormaslik!

### 1. ENDPOINT PORTING (Controller ko'chirish)

**Nima:** Old-hemis REST endpointlarni api-legacy modulga ko'chirish
**Trigger:** `PORT: GET /services/tax/rent` yoki `/services/*`, `/app/rest/*` URL pattern
**Fayllar:**
- `/home/adm1n/startup/old_hemis.json` — Old API hujjati
- `/home/adm1n/startup/hemis-back/docs/endpoint_tester.html` — Test UI
**Natija:** Controller.java + Swagger + Test buttons

### 2. DATABASE MIGRATION (Liquibase)

**Nima:** Database schema o'zgartirish
**Trigger:** "database", "schema", "table", "column", "changeset", "liquibase"
**Fayllar:** `domain/src/main/resources/db/changelog/changesets/`
**Natija:** SQL changeset + Rollback script

---

## Porting Triggers

| Format | Misol |
|--------|-------|
| `PORT:` prefix (tavsiya) | `PORT: GET /services/tax/rent` |
| URL pattern | `GET /app/rest/v2/services/tax/rent` |
| Batch | `PORT:` + bir nechta URL (har biri yangi qatorda) |

**Porting ishlamaydigan holatlar:** oddiy savol, code review, schema o'zgartirish, arxitektura haqida savol.

Porting trigger bo'lganda Claude avtomatik:
1. `old_hemis.json` dan metadata oladi
2. `rest-services.xml` dan parametrlar o'qiydi
3. Dublikat tekshiradi
4. Controller + Swagger generatsiya qiladi
5. `endpoint_tester.html` ga test buttonlar qo'shadi

**Batafsil workflow:** `@ENDPOINT_PORTING_GUIDE.md`

---

## Further Reading

| Fayl | Maqsad |
|------|--------|
| `@ENDPOINT_PORTING_GUIDE.md` | Old-hemis endpoint ko'chirish workflow |
| `@LIQUIBASE_GUIDE.md` | Database migration yaratish |
| `@architecture.md` | Modul diagrammalari, DB routing, cache, deploy |
| `@context.md` | Biznes domain, tech stack, API modullari |
| `@rules.md` | Kodlash standartlari, modul qoidalari |
| `@MANDATORY_REQUIREMENTS.md` | Swagger, test environment, kod misollari, PR checklist |
| `@MENU_GUIDE.md` | Menu + i18n + xavfsizlik arxitekturasi |

`@` sintaksisi fayllarni on-demand import qiladi.
