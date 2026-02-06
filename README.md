# HEMIS Backend

> **H**igher **E**ducation **M**anagement **I**nformation **S**ystem - Backend API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.3.0-blue.svg)](https://gradle.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-4.31.1-red.svg)](https://www.liquibase.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16+-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-6+-red.svg)](https://redis.io/)

---

## Umumiy Ma'lumot

HEMIS Backend - bu oliy ta'lim muassasalarini boshqarish uchun zamonaviy RESTful API. Clean Architecture prinsiplariga asoslangan, modular monolith arxitekturasida qurilgan.

### Asosiy Xususiyatlar

- **Clean Architecture** - domain, use case, interface layers
- **Modular Monolith** - 9 ta mustaqil modul
- **Liquibase 4.x** - professional database migration
- **Hybrid Authentication** - legacy (CUBA Platform PBKDF2) + BCrypt
- **RBAC** - role-based access control (90+ permission)
- **Redis Cache** - L1 (Caffeine) + L2 (Redis) hybrid caching
- **Swagger/OpenAPI 3.0** - multi-group API documentation
- **i18n** - 4 til (uz-UZ, oz-UZ, ru-RU, en-US)
- **Master/Replica** - read/write database separation

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

### Yangi Jadvallar (Liquibase bilan yaratilgan)

| Jadval | Tavsif |
|--------|--------|
| `users` | Yangi foydalanuvchilar (BCrypt hash) |
| `roles` | Rollar (5 ta) |
| `permissions` | Huquqlar (90+ ta) |
| `user_roles` | User-Role mapping |
| `role_permissions` | Role-Permission mapping |
| `system_messages` | i18n xabar kalitlari |
| `system_message_translations` | Tarjimalar |
| `menus` | Dinamik menu tuzilmasi |
| `languages` | Qo'llab-quvvatlanadigan tillar |
| `language_translations` | Til nomlari tarjimasi |

### Legacy Jadvallar (ministry.sql)

Mavjud `ministry.sql` schemasi bilan to'liq moslik. Legacy jadvallar o'zgartirilmaydi.

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
    │   ├── V001_create_users.sql
    │   ├── V001_create_users_rollback.sql
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

- Java 21 LTS
- PostgreSQL 16+
- Redis 6+
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
| Frontend | `/home/adm1n/startup/hemis-front` |
| Old Backend | `/home/adm1n/startup/old-hemis` |
| Documentation | `.claude/` folder |

---

## License

Proprietary - HEMIS Project

---

**Versiya:** 1.0.0
**Oxirgi yangilanish:** 2025-02-04
**Holat:** Development
