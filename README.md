# HEMIS Backend

> **H**igher **E**ducation **M**anagement **I**nformation **S**ystem - Backend API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25%20LTS-orange.svg)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.3.0-blue.svg)](https://gradle.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-4.31.1-red.svg)](https://www.liquibase.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)

---

## Umumiy Ma'lumot

HEMIS Backend — Oliy ta'lim vazirligi tasarrufidagi **MARKAZIY** Spring Boot server. `/home/adm1n/projects/startup/old-hemis` (CUBA Platform 7.3 PHP) ning Java 25 + Spring Boot 4.0.6 ga qayta yozilishi.

### Loyiha maqsadi (per-OTM EMAS — vazirlik markaziy)

1. **Aggregation:** 230 OTM dan o'quv ma'lumotini markaziy yig'ish (talaba, baho, o'qituvchi, hisobot)
2. **Klassifikator distribution:** `h_*` jadvallari yagona markaziy manba — Univer (per-OTM Yii2 PHP, 224 ta) markazdan sync qiladi
3. **Qoidalar joriy qilish:** talaba kiritish vaqt cheklovi, baho o'zgartirish lock, va boshqa biznes konstraint markaziy darajada
4. **Davlat integratsiya:** MyGov, MSPD, BIMM, Tax/Soliq, GUVD, OneID — S2S markaziy aloqa

### Univer (per-OTM Yii2 PHP) ↔ HEMIS-back

```
[230 OTM Univer (Yii2 PHP)]  ──REST API──▶  [HEMIS-back MARKAZIY]
   • Per-OTM lokal DB                           • Yagona markaziy DB
   • hemis_337, hemis_401, …                     • env: DB_MASTER_NAME
   • 224 ta Univer ishlatuvchi                  • 230 OTM aggregation
                                                 • Davlat integratsiya
```

### Asosiy Xususiyatlar

- **Clean Architecture** — domain, use case, interface layers
- **Modular Monolith** — 9 ta mustaqil modul
- **Liquibase 4.x** — professional database migration (V###/M###/S### naming)
- **Hybrid Authentication** — legacy CUBA PBKDF2 + BCrypt (api-legacy backward-compat)
- **OAuth 2.1** — `client_credentials` grant 224 OTM Univer client'lari uchun (api-university)
- **RBAC** — role-based access control (90+ permission, OTM scope filter)
- **Redis Cache** — L1 (Caffeine) + L2 (Redis) hybrid caching
- **Swagger/OpenAPI 3.0** — multi-group API documentation
- **i18n** — 4 til (uz-UZ, oz-UZ, ru-RU, en-US) — markaziy `system_message` jadvalida
- **Master/Replica** — read/write database separation
- **Davlat integratsiya** — MyGov, MSPD, BIMM, Tax, GUVD (api-external)

---

## Arxitektura

### Modular Monolith Tuzilishi

```
hemis-back/
├── app/                    # Application Layer (entry point)
│   ├── config/            # Spring configuration
│   ├── controller/        # Auth, Captcha, Test endpoints
│   └── HemisApplication   # @SpringBootApplication
├── api-web/               # Modern Web API (/api/v1/web/*)
├── api-legacy/            # Legacy CUBA API (/app/rest/v2/*)
├── api-external/          # External integrations (GUVD, Tax, BIMM)
├── api-university/        # University API (/api/v1/university/*)
├── service/               # Business Logic (use cases, mappers)
├── security/              # JWT OAuth2 + RBAC
│   └── crypto/           # LegacyPasswordEncoder (BCrypt + PBKDF2)
├── domain/                # JPA Entities, Repositories
│   └── resources/db/     # Liquibase changesets
└── common/                # Shared DTOs, Ports, Exceptions
```

### Modul Dependency Graph

```
common (no dependencies)
    ↑
security + domain
    ↑
service
    ↑
api-legacy + api-web + api-external + api-university
    ↑
app (entry point)
```

### Loyiha Statistikasi

| Metrika | Qiymat |
|---------|--------|
| Modullar | 9 ta |
| REST Controllers | 151 ta |
| JPA Entities | 100+ ta |
| Repositories | 92 ta |
| API Endpoints | 200+ ta |
| Permissions | 90+ ta |
| Tillar | 4 ta |

---

## Database Schema

### Yangi Jadvallar (Liquibase V001-V014 bilan yaratilgan)

| Changeset | Jadvallar |
|-----------|-----------|
| `V001` | `role` |
| `V002` | `permission` |
| `V003` | `h_position_type`, `h_position` (ADR-0006) |
| `V004` | `employee`, `employee_job`, `employee_academic_credential` |
| `V005` | `organization`, `university_profile` |
| `V006` | `users` (PLURAL — PostgreSQL reserved), `password_history`, `password_reset_token`, `oauth_client`, `oauth_client_role` |
| `V007` | `user_role`, `role_permission` |
| `V008` | `university_founder` |
| `V009` | `university_lifecycle` (immutable log) |
| `V011` | `university_building`, `building_lifecycle`, `h_building_category`, `h_construction_material`, `h_roof_type` (ADR-0001, ADR-0006) |
| `V012` | `system_message`, `system_message_translation` (i18n) |
| `V013` | `language`, `configuration` |
| `V014` | `menu`, `user_favorite` (UI) |
| `M001` | `sec_user → users` migratsiyasi |
| `M002` | `hemishe_e_student` performance indexes |
| `M003` | Student duplicates MV |
| `S001-S010` | Seed data (roles, permissions, languages, translations) |

> **Naming exception:** `users` PLURAL (PostgreSQL `user` reserved word). Boshqa hammasi SINGULAR. Tafsilot: `.claude/rules.md` "Cross-Cutting Database Rules".

### DB Bootstrap — Source of Truth

**Markaziy HEMIS-back DB:** bitta deploy uchun bitta DB (`DB_MASTER_NAME` env'dan). Lokal `test1_hemis`, prod turli (`hemis`, `hemis_prod`).

> **Eslatma:** `hemis_337`, `hemis_401`, …, `hemis_NNN` — bu **bizning DB EMAS**. Bu **224 ta OTM tomonidagi Univer Yii2 PHP** ekosistemining lokal bazalari nomi (per-OTM deploy).

Loyihada ikki tipdagi jadvallar mavjud:

1. **Bizning Liquibase changesets** (yuqorida) — V001-V014, M001-M003, S001-S010. Markaziy HEMIS-back DB'siga tegishli.

2. **Legacy `hemishe_*` va `sec_*` jadvallar** — `CREATE TABLE` bizda YO'Q. Manbai:
   - **old-hemis CUBA Platform** (`/home/adm1n/projects/startup/old-hemis`) ishlatadi shu schema'ni
   - `test1_hemis` lokal DB old-hemis'dan `pg_dump` orqali olingan baseline (initial dump)
   - Ushbu jadvallarni **HECH QACHON ALTER/DROP/RENAME qilmang** — Univer 224 OTM (Yii2 PHP, per-OTM `hemis_NNN`) api-legacy orqali eski format kutadi. Tafsilot: `.claude/rules.md` Golden Rules.

**Lokal setup uchun:**
```bash
# 1. PostgreSQL 18 + Redis 7 ishlatib turing (docker-compose.yml)
docker-compose up -d postgres redis

# 2. .env faylida DB nomi (lokal): DB_MASTER_NAME=test1_hemis

# 3. Legacy baseline yuklang (faqat birinchi marta — old-hemis dump'idan)
psql -U postgres -d test1_hemis < /path/to/old-hemis-baseline.sql

# 4. Liquibase migration'larni qo'llang
./gradlew :domain:liquibaseUpdate

# 5. Mapping moslikni tekshiring (ADR-0008)
./scripts/check_table_mappings.sh

# 6. Git pre-commit hook o'rnating (Golden Rule #1 + #3 enforcement)
cp scripts/git-hooks-pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

> **DIQQAT:** Production DB nomi `.env` orqali kelishadi (`DB_MASTER_NAME`). Hech qachon hard-code qilmang. CLAUDE.md "Golden Rule #1".

---

## Database Migration

### Liquibase 4.x Modern CLI

HEMIS Backend professional migration tizimiga ega. Barcha migration'lar tag'lar bilan belgilangan va rollback qo'llab-quvvatlanadi.

#### Migration Komandalar

```bash
# Migration holatini ko'rish
./gradlew :domain:liquibaseStatus

# Barcha yangi migration'larni bajarish
./gradlew :domain:liquibaseUpdate

# Migration tarixini ko'rish
./gradlew :domain:liquibaseHistory
```

#### Rollback Komandalar

```bash
# Oxirgi N ta changeset'ni rollback qilish
./gradlew :domain:liquibaseRollbackCount -Pcount=2

# Ma'lum tag'ga rollback qilish
./gradlew :domain:liquibaseRollbackToTag -Ptag=seed-v1.0

# Rollback SQL ni ko'rish (xavfsiz preview)
./gradlew :domain:liquibaseRollbackSQL -Pcount=2
```

#### Migration Tuzilmasi

```
domain/src/main/resources/db/changelog/
├── db.changelog-master.yaml     # Master changelog
└── changesets/
    ├── schema/                  # DDL (V001-V010)
    │   ├── V005_create_users.sql
    │   ├── V005_create_users_rollback.sql
    │   └── ...
    ├── seed/                    # Reference data (S001-S006)
    │   ├── S001_seed_roles.sql
    │   ├── S001_seed_roles_rollback.sql
    │   └── ...
    └── migration/               # Data migrations (M001-M003)
        ├── M001_migrate_old_hemis_users.sql
        └── ...
```

#### Migration Tag'lar

| Tag | Tavsif |
|-----|--------|
| `schema-v1.0` | Database schema yaratildi (10 ta jadval) |
| `seed-v1.0` | Boshlang'ich data (roles, permissions, languages) |
| `v1.0.0` | Birinchi reliz (legacy users migrated) |

#### Best Practices

**DO:**
- Migration'dan oldin **database backup** oling
- `liquibaseRollbackSQL` bilan **preview** qiling
- **Staging** muhitda test qiling

**DON'T:**
- Production'da to'g'ridan-to'g'ri rollback qilmang
- Migration fayllarni qo'lda o'zgartirmang
- Tag'larni o'chirmang

---

## Quick Start

### Requirements

- Java 25 LTS (Temurin tavsiya etiladi; Gradle toolchain auto-download mavjud)
- PostgreSQL 18 (master/replica)
- Redis 7 (cache + token store)
- Gradle 9.3.0 (wrapper bilan birga keladi)

### Database Setup

```bash
# PostgreSQL database yaratish
createdb hemis_db

# .env fayl yaratish
cp .env.example .env
# .env faylni tahrirlang va qiymatlarni kiriting
```

### Build & Run

```bash
# Dependencies yuklab olish va build qilish
./gradlew build

# Application'ni ishga tushirish
./gradlew :app:bootRun

# Yoki clean build bilan
./gradlew clean :app:bootRun
```

### URL'lar

| Xizmat | URL |
|--------|-----|
| Backend | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI JSON | http://localhost:8081/v3/api-docs |
| Health Check | http://localhost:8081/actuator/health |

### Test Credentials

API test uchun credential'lar `.env` faylda yoki `/docs/endpoint_tester.html` da belgilangan.

---

## Environment Variables

`.env.example` faylini `.env` ga nusxalab, quyidagi o'zgaruvchilarni sozlang:

```bash
# Database (Master - Write)
DB_MASTER_HOST=localhost
DB_MASTER_PORT=5432
DB_MASTER_NAME=hemis_db
DB_MASTER_USERNAME=postgres
DB_MASTER_PASSWORD=<secret>

# Database (Replica - Read)
DB_REPLICA_HOST=localhost
DB_REPLICA_PORT=5432
DB_REPLICA_NAME=hemis_db
DB_REPLICA_USERNAME=postgres
DB_REPLICA_PASSWORD=<secret>

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Server
SERVER_PORT=8081

# JWT
JWT_SECRET=<min-32-characters-secret>
JWT_EXPIRATION=43200
JWT_REFRESH_EXPIRATION=604800

# OAuth2
OAUTH_CLIENT_ID=<client>
OAUTH_CLIENT_SECRET=<secret>

# Testing
TESTS_ENABLED=false
```

---

## API Documentation

### API Guruhlari

| Guruh | Path | Maqsad |
|-------|------|--------|
| Web Frontend | `/api/v1/web/**` | React/Vue frontend uchun |
| Legacy CUBA | `/app/rest/v2/**` | Eski tizim bilan moslik |
| University | `/api/v1/university/**` | Universitet API |
| External | `/services/**` | Tashqi integratsiyalar |

### Asosiy Endpoints

```
# Authentication
POST /app/rest/v2/oauth/token          # OAuth2 token olish
POST /api/v1/web/auth/login            # Web login
POST /api/v1/web/auth/logout           # Logout
GET  /api/v1/web/auth/me               # Current user info

# Web API
GET  /api/v1/web/menu                  # Dynamic menu
GET  /api/v1/web/dashboard/statistics  # Dashboard stats
GET  /api/v1/web/registry/faculties    # Faculty registry
GET  /api/v1/web/i18n/messages         # Translations

# Legacy API
GET  /app/rest/v2/services/*           # CUBA services
GET  /app/rest/v2/entities/*           # CUBA entities
```

---

## Development

### Build Commands

```bash
# Full build
./gradlew clean build

# Specific module
./gradlew :service:build
./gradlew :api-web:build

# Skip tests
./gradlew build -x test
```

### Test Commands

```bash
# Run all tests (TESTS_ENABLED=true required in .env)
./gradlew test

# Specific module
./gradlew :api-web:test
./gradlew :service:test

# Test coverage
./gradlew test jacocoTestReport
```

### Profile Selection

```bash
# Development (default)
./gradlew :app:bootRun

# Production
SPRING_PROFILES_ACTIVE=prod ./gradlew :app:bootRun
```

---

## Security

### Authentication

**Hybrid Authentication System:**
- **LegacyPasswordEncoder** - BCrypt (yangi) + PBKDF2 (eski CUBA) ni qo'llab-quvvatlaydi
- **JWT Tokens** - HS256 signed, 12h expiration
- **Refresh Tokens** - 7 days expiration
- **Token Blacklist** - Redis-based logout support

### RBAC (Role-Based Access Control)

| Role | Tavsif |
|------|--------|
| `SUPER_ADMIN` | Tizim administratori |
| `MINISTRY_ADMIN` | Vazirlik administratori |
| `UNIVERSITY_ADMIN` | OTM administratori |
| `TEACHERS` | O'qituvchilar |
| `EMPLOYEES` | Xodimlar |

**Permissions:** 90+ permission (CRUD + menu + system)

---

## Caching Strategy

### Hybrid L1 + L2 Cache

| Layer | Technology | Scope | TTL |
|-------|------------|-------|-----|
| L1 | Caffeine | JVM-local | Fast |
| L2 | Redis | Distributed | 30-60 min |

### Cached Data

- Menu (role-based)
- User permissions
- Dashboard statistics
- i18n translations

---

## Monitoring

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8081/actuator/health

# Metrics
curl http://localhost:8081/actuator/metrics

# Migration status
curl http://localhost:8081/actuator/liquibase
```

### Sentry Integration

Error tracking via Sentry (configure `SENTRY_DSN` in `.env`).

---

## Troubleshooting

### Liquibase Xatoliklari

**Muammo:** "No changesets to rollback"
```bash
./gradlew :domain:liquibaseStatus
./gradlew :domain:liquibaseUpdate
```

### Database Connection

**Muammo:** "Connection refused"
```bash
# PostgreSQL statusini tekshiring
sudo systemctl status postgresql

# .env sozlamalarini tekshiring
cat .env | grep DB_
```

### Build Errors

**Muammo:** "MapStruct annotation processor failed"
```bash
./gradlew clean build --no-build-cache
./gradlew --stop && ./gradlew build
```

---

## Project Structure

```
hemis-back/
├── app/                          # Main application
├── api-web/                      # Web Frontend API
├── api-legacy/                   # Legacy CUBA API
├── api-external/                 # External integrations
├── api-university/               # University API
├── service/                      # Business logic
├── security/                     # Auth & RBAC
├── domain/                       # Entities & migrations
├── common/                       # Shared code
├── docs/                         # Documentation
├── scripts/                      # Utility scripts
├── build.gradle.kts              # Root build config
├── settings.gradle.kts           # Module definitions
├── gradle.properties             # Gradle settings
├── .env.example                  # Environment template
├── Dockerfile                    # Container image
└── docker-compose.yml            # Local dev stack
```

---

## Contributing

### Commit Message Format

```
feat: yangi feature qo'shish
fix: bug tuzatish
refactor: kod refactoring
docs: hujjatlarni yangilash
test: testlar qo'shish
chore: texnik o'zgarishlar
```

### Pull Request

1. Yangi branch yarating: `git checkout -b feature/new-feature`
2. O'zgarishlarni commit qiling: `git commit -m "feat: add new feature"`
3. Push qiling: `git push origin feature/new-feature`
4. Pull request oching

---

## Links

| Resource | Path |
|----------|------|
| Frontend | `/home/adm1n/projects/startup/hemis-front` |
| Old Backend (CUBA) | `/home/adm1n/projects/startup/old-hemis` |
| Integration tools | `/home/adm1n/projects/startup/hemis-tools` |
| Documentation | `.claude/` folder |

---

## License

Proprietary - HEMIS Project

---

**Versiya:** 1.0.0
**Oxirgi yangilanish:** 2025-02-04
**Holat:** Development
