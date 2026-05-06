# HEMIS Backend – Project Memory

## Project Overview

HEMIS Backend — Java 25 LTS + Spring Boot modular monolith. Clean architecture: API → Service → Domain → Common.
Legacy CUBA platform bilan 100% backward compatibility saqlanadi.

**Stack:** Spring Boot 4.0.6, PostgreSQL 18, Redis 7, Liquibase 4.31.1, MapStruct, Lombok
**Auxiliary DB:** `hemis_audit` (alohida PostgreSQL — activity_log, error_log, login_log)
**Univer (OTM klient) bazalari:** 224 ta hemis_NNN (har OTM uchun bittadan, Yii2 PHP backend tomonida)

### 🔒 GOLDEN RULE — Bizning baza har doim `.env`'dan keladi

**Hech qachon hard-code qilmang DB nomi/jadvalni.** Production'da bizning baza tiklanadi va nomi `.env`'da ko'rsatiladi (lokal: `test1_hemis`, prod: turli xil bo'lishi mumkin). Hech qachon "biz hemis_NNN ishlatamiz" deb taxmin qilmang — bu Univer (OTM Yii2 PHP) tomonidagi alohida ekosistem.

| Soha | Bizning hemis-back | Univer (OTM, 224 ta) |
|------|--------------------|----------------------|
| **DB nomi** | `${DB_MASTER_NAME}` (`.env`) | `hemis_337`, `hemis_401`, …, `hemis_NNN` |
| **Stack** | Java 25 + Spring + JPA | Yii2 PHP |
| **Tegish usuli** | `@Entity`/`@Table` + Hibernate | REST API (`UniverApiService`) |
| **Misol jadvallar bizda** | `hemishe_e_student`, `hemishe_e_teacher`, `hemishe_e_university`, `hemishe_e_student_diploma`, `hemishe_e_student_meta`, `hemishe_e_faculty`, `hemishe_e_publication_*` | — |
| **Misol jadvallar Univer'da** (bizda **YO'Q**) | — | `hemishe_e_grade`, `hemishe_e_attendance`, `hemishe_e_course`, `hemishe_e_exam`, `hemishe_e_schedule`, `hemishe_e_contract`, `hemishe_e_curriculum`, `hemishe_e_employment`, `hemishe_e_enrollment`, `hemishe_e_department` |

### Yangi `@Table(name = "hemishe_e_*")` qo'shganda — MAJBURIY tekshirish

```bash
# Code'dagi mapping'larni .env'dagi DB bilan solishtirish:
./scripts/check_table_mappings.sh
```

Skript ikki xato turini topadi: (1) faraz qilingan jadvallar (DB'da yo'q), (2) api-legacy yangi schema entity'ni ishlatadi. **Pre-commit hook sifatida ishlating.**

### ⚠️ ENG MUHIM QOIDA — api-legacy old-hemis bilan 1:1 (BUZILMASDAN)

> **api-legacy modul `/home/adm1n/projects/startup/old-hemis` (eski CUBA loyihasi) bilan AYNI XULQ ko'rsatishi kerak.** Status code, body shape, validation, permission, error format — hammasi mos.

**Tekshirish:**
```bash
# Ikkala server ishga tushadi (old:8082, new:8081)
cd /home/adm1n/projects/startup/hemis-tools/docs/univer_tool
node compare_endpoints.js
# Maqsad: MATCH 175/175 (100%)
```

**Yangi qaror qabul qilishdan oldin** old-hemis kodiga qaramasdan turib o'zgarish kiritmang:
```bash
grep -rn "<endpoint>" /home/adm1n/projects/startup/old-hemis/modules/
```

Tafsilot: `api-legacy/CLAUDE.md` → "ENG MUHIM QOIDA" bo'limi.

### 🔒 Modul ↔ Jadval mosligi qoidasi (BUZILMASDAN)

> **Asosiy qoida:** api-legacy'ga **Univer'dan kelgan ma'lumotlar har doim ESKI jadvallarga yoziladi** (mantiq old-hemis bilan **1:1 bir xil**). Yangi jadvallar uchun **api-university** (yoki boshqa yangi modul) **yangi endpoint** chiqaramiz va kerak bo'lsa mapping/sync qilamiz.

| Modul | Vazifa | Jadval prefiks | Misol |
|-------|--------|----------------|-------|
| **api-legacy** | Univer endpoint'larini xizmat qilish, **old-hemis 1:1** | **Faqat eski** — `hemishe_*`, `sec_*` | `Student` → `hemishe_e_student`, `SecUser` → `sec_user` |
| **api-university** | Yangi 224 OTM B2B API (yangi schema) | **Faqat yangi** — `users`, `employee`, `employee_job`, `organization`, `h_*`, `university_building` | `User` → `users`, `Employee` → `employee` |
| **api-web** | Modern web frontend | **Faqat yangi** | Bir xil yangi entity'lar |
| **api-external** | S2S integratsiya (vazirlik, MyGov) | Vaziyatga qarab | (alohida) |

**Sabab — split-brain xavfi:**
```
Univer Yii2 → POST /app/rest/v2/entities/hemishe_EEmployeeJobs (eski URL)
   → api-legacy controller xato YANGI jadvalga yozsa: INSERT INTO employee_job
   → Univer keyingi GET — eski hemishe_e_employee_jobs'dan o'qiydi (topa olmaydi)
   → Univer user: "Yangi xodim qo'shganman, lekin ko'rinmaydi!"
```

To'g'ri yondashuv:
```
api-legacy:                INSERT INTO hemishe_e_employee_jobs    ← eski URL → eski jadval
api-university (kelajak):  INSERT INTO employee_job               ← yangi endpoint → yangi jadval
mapping/sync (alohida):    eski → yangi yoki yangi → eski (async, kerakli bo'lganda)
```

Tafsilot: `api-legacy/CLAUDE.md` → "GOLDEN RULE" bo'limi.

Agar Univer tomonidagi ma'lumot kerak bo'lsa — REST integratsiya:
```java
// ❌ XATO — bizda hemishe_e_grade jadval YO'Q
@Entity @Table(name = "hemishe_e_grade")
public class Grade { ... }

// ✅ TO'G'RI — Univer REST orqali
@Service
public class UniverGradeApiService {
    public List<GradeDto> findByStudent(String universityCode, String pinfl) {
        return univerClient.fetchGrades(universityCode, pinfl);
    }
}
```

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
| `@.claude/context.md` | Biznes domain (224 universitet), tech stack |
| `@.claude/rules.md` | Kodlash standartlari v3.0 (Java 25, OWASP 2025) |
| `@.claude/MANDATORY_REQUIREMENTS.md` | Swagger, test environment, kod misollari |
| `@.claude/MENU_GUIDE.md` | Menu + i18n + xavfsizlik arxitekturasi |

## ⚠️ Univer 224 OTM Klient Contract — MAJBURIY o'qish

**Har api-legacy o'zgarish oldidan** quyidagi ikki hujjatni o'qing:

| Hujjat | Maqsad |
|--------|--------|
| `@docs/UNIVER_CONTRACT.md` | Univer (Yii2 PHP, 224 OTM) **67 endpoint** contract'i — frozen properties, refactor workflow |
| `@docs/UNIVER_ENDPOINT_AUDIT.md` | Per-endpoint detal audit (35 caller class, 67 endpoint) — file:line bilan aniq mapping |

**Real test infrastructure:** `/home/adm1n/projects/startup/hemis-tools/docs/univer_tool/` —
175 ta integration test (`compare_endpoints.js`), old-hemis (:8082) bilan side-by-side
solishtiradi. Server'lar ishga tushganda majburiy run.

`@` sintaksisi fayllarni on-demand import qiladi.

## Architecture Decision Records (ADR) — `docs/adr/`

Muhim qarorlar yozma tarixi. Yangi feature yoki refactoring oldidan o'qish tavsiya etiladi:

| ADR | Mavzu | Qachon o'qish |
|-----|-------|----------------|
| [0001](docs/adr/0001-building-table-design.md) | university_building alohida jadval (cadastre kengaytirish emas) | Bino-related ish |
| [0002](docs/adr/0002-java-25-upgrade.md) | Java 25 LTS + Spring Boot 4.0.6 | Build/dependency ish |
| [0003](docs/adr/0003-audit-db-isolation.md) | Audit DB alohida `hemis_audit` bazasi | Audit/logging ish |
| [0004](docs/adr/0004-api-university-module.md) | api-university yangi modul (224 OTM B2B) | API endpoint ish |
| [0005](docs/adr/0005-oauth-client-credentials.md) | OAuth client_credentials migration plan | Auth/security ish |
| [0006](docs/adr/0006-classifier-h-prefix.md) | Klassifikatorlar `h_*` prefiks konventsiyasi | Yangi klassifikator yaratish |
| [0007](docs/adr/0007-sync-architecture-evolution.md) | Sync Architecture — Kafka-first Approach (Greenfield) | Kafka/Outbox/event-driven sync ish |

Yangi qaror qabul qilinganda — `docs/adr/template.md` orqali yangi ADR yarating.

## Recent State (2026-05-04)

- `users` jadvali toza schema'ga o'tdi (41 → 30 ustun, 11 ta legacy CUBA olib tashlangan)
- Soft-delete + UNIQUE konflikti hal qilindi (partial UNIQUE indekslar)
- 224 OTM B2B uchun `oauth_client` migration plan (ADR-0005)
- Old-hemis CUBA `sec_user` parallel ishlaydi (HybridUserDetailsService)
- 5 ta klassifikator butun stack bo'ylab `h_*`/`H` prefiks oldi (h_position_type, h_position, h_building_category, h_construction_material, h_roof_type — Java: HPositionType, HPosition, HBuildingCategory, HConstructionMaterial, HRoofType). 224 OTM ekosistemi konvensiyasi (ADR-0006).
