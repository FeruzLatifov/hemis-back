# HEMIS Backend – Project Memory

## Project Overview

HEMIS Backend — Java 25 LTS + Spring Boot modular monolith. Clean architecture: API → Service → Domain → Common.
Legacy CUBA platform bilan 100% backward compatibility saqlanadi.

**Stack:** Spring Boot 4.0.6, PostgreSQL 18, Redis 7, Liquibase 4.31.1, MapStruct, Lombok
**Auxiliary DB:** `hemis_audit` (alohida PostgreSQL — activity_log, error_log, login_log)
**Universitetlar soni:** 224 ta hemis_NNN bazasi (224 OTM)

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

## Module-Level Memory (per modul ishlaganda avtomatik yuklanadi)

Har modul'da o'z `CLAUDE.md` mavjud — modulga xos qoidalar va patternlar:

| Modul | Maqsad | Asosiy mavzular |
|-------|--------|-----------------|
| `domain/CLAUDE.md` | JPA + Liquibase | Lombok @Data taqiq, N+1, indeks, soft-delete, transaction isolation, PostgreSQL specific |
| `service/CLAUDE.md` | Business logic | AOP self-invocation, cache+evict, MapStruct, exception hierarchy, timeout config |
| `security/CLAUDE.md` | Auth | BCrypt-12, JWT, RBAC, OWASP Top 10:2025, PII logging taqiq |
| `api-web/CLAUDE.md` | Modern REST | ResponseWrapper, HTTP status, idempotency, async, sort whitelist |
| `api-legacy/CLAUDE.md` | CUBA compat | LinkedHashMap, _entityName, FK nested, datetime format, error format |
| `api-external/CLAUDE.md` | S2S integration | API Key, timeout, idempotency, webhook, SSL per-conn |
| `api-university/CLAUDE.md` | University scope | Multi-tenant, scope validation, cache key, cross-uni taqiq |
| `common/CLAUDE.md` | Shared lib | Records, exception hierarchy, NO Spring dep, constants |
| `app/CLAUDE.md` | Bootstrap | GlobalExceptionHandler, filter chain, profile, JVM flags |

## Subagent'lar (`.claude/agents/`)

PR review yoki code audit uchun maxsus agent'lar:

| Agent | Vazifa | Qachon ishlatish |
|-------|--------|------------------|
| `n-plus-one-detector` | JPA N+1 antipattern | Service/repo/mapper o'zgarganda |
| `liquibase-reviewer` | Migration safety | `V###*.sql` qo'shilganda |
| `cache-strategist` | @Cacheable/@CacheEvict audit | Cache annotation o'zgarganda |
| `cuba-format-checker` | api-legacy CUBA compat | api-legacy o'zgarganda |
| `security-auditor` | OWASP Top 10:2025 | Security/auth/controller o'zgarganda |

## Slash Commands (`.claude/commands/`)

| Command | Maqsad |
|---------|--------|
| `/port-endpoint <METHOD> <path>` | Old-hemis endpoint'ni api-legacy'ga ko'chirish |
| `/check-coverage [module]` | Jacoco coverage tekshirish va gap'lar |
| `/audit-cache` | Butun loyiha cache audit |
| `/review-pr [PR#]` | Multi-agent parallel PR review |

## Further Reading (.claude/)

| Fayl | Maqsad |
|------|--------|
| `@.claude/ENDPOINT_PORTING_GUIDE.md` | Old-hemis endpoint ko'chirish workflow |
| `@.claude/LIQUIBASE_GUIDE.md` | Database migration yaratish |
| `@.claude/architecture.md` | Modul diagrammalari, DB routing, cache, deploy |
| `@.claude/context.md` | Biznes domain (230 universitet), tech stack |
| `@.claude/rules.md` | Kodlash standartlari v3.0 (Java 21, OWASP 2025) |
| `@.claude/MANDATORY_REQUIREMENTS.md` | Swagger, test environment, kod misollari |
| `@.claude/MENU_GUIDE.md` | Menu + i18n + xavfsizlik arxitekturasi |

`@` sintaksisi fayllarni on-demand import qiladi.
