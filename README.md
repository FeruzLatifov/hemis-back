# HEMIS Backend

> **H**igher **E**ducation **M**anagement **I**nformation **S**ystem - Backend API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Gradle](https://img.shields.io/badge/Gradle-9.2.0-blue.svg)](https://gradle.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-4.31.1-red.svg)](https://www.liquibase.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

---

## 📋 Umumiy Ma'lumot

HEMIS Backend - bu oliy ta'lim muassasalarini boshqarish uchun zamonaviy RESTful API. Clean Architecture prinsiplariga asoslangan, modular monolith arxitekturasida qurilgan.

### Asosiy Xususiyatlar

- ✅ **Clean Architecture** - domain, use case, interface layers
- ✅ **Modular Monolith** - 8 ta mustaqil modul
- ✅ **Liquibase 4.x** - professional database migration
- ✅ **Hybrid Authentication** - legacy (CUBA Platform) + BCrypt
- ✅ **RBAC** - role-based access control
- ✅ **Redis Cache** - distributed caching
- ✅ **Swagger/OpenAPI** - API documentation
- ✅ **i18n** - 4 til (uz-UZ, oz-UZ, ru-RU, en-US)

---

## 🏗 Arxitektura

### Modular Monolith Tuzilishi

```
hemis-back/
├── app/                    # Application Layer (main)
│   ├── config/            # Spring configuration
│   └── HemisApplication   # Boot class
├── api-web/               # Public Web API (REST)
├── api-legacy/            # Legacy CUBA API (compatibility)
├── api-external/          # External integrations
├── service/               # Business Logic (use cases)
├── security/              # Authentication & Authorization
│   └── crypto/           # LegacyPasswordEncoder (BCrypt + PBKDF2)
├── domain/                # Entities, Repositories, Migrations
│   └── resources/db/     # Liquibase changesets
└── common/                # Shared utilities
```

### Database Schema

**Asosiy Jadvallar:**
- `users` - Yangi foydalanuvchilar (BCrypt hash)
- `sec_user` - Eski CUBA foydalanuvchilar (PBKDF2 hash) - migration qilingan
- `roles` - Rollar (5 ta)
- `permissions` - Huquqlar (90 ta)
- `h_system_message` - i18n xabarlar
- `h_system_message_translation` - Tarjimalar (508 yozuv)

---

## 💾 Database Migration

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
./gradlew :domain:liquibaseRollbackToTag -Ptag=v3-users-migrated

# Rollback SQL ni ko'rish (xavfsiz preview)
./gradlew :domain:liquibaseRollbackSQL -Pcount=2
```

#### Migration Tag'lar

| Tag | Tavsif | Yozuvlar |
|-----|--------|----------|
| `v1-schema-complete` | Database schema yaratildi (7 ta jadval) | 0 |
| `v2-seed-data-complete` | Boshlang'ich data (roles, permissions) | 95 |
| `v3-users-migrated` | 339 ta user ko'chirildi (sec_user → users) | 339 |
| `v4-menu-translations-complete` | Menu tarjimalari (197 ta message × 4 til) | 508 |
| `v5-faculty-translations-complete` | Fakultet tarjimalari | 50 |

#### Best Practices

**✅ DO:**
- Migration'dan oldin **database backup** oling:
  ```bash
  pg_dump -U postgres test_hemis > backup_$(date +%Y%m%d_%H%M%S).sql
  ```
- `liquibaseRollbackSQL` bilan **preview** qiling
- **Staging** muhitda test qiling
- Production'da **monitoring** qo'ying

**❌ DON'T:**
- Production'da to'g'ridan-to'g'ri rollback qilmang
- Backup olmasdan migration bajarmang
- Migration fayllarni qo'lda o'zgartirmang
- Tag'larni o'chirmang

#### Rollback Stsenariy

```bash
# 1. Avval rollback SQL ni ko'ramiz (xavfsiz)
./gradlew :domain:liquibaseRollbackSQL -Pcount=1 > /tmp/rollback-preview.sql
cat /tmp/rollback-preview.sql

# 2. Agar SQL to'g'ri bo'lsa, rollback qilamiz
./gradlew :domain:liquibaseRollbackCount -Pcount=1

# 3. Holatni tekshiramiz
./gradlew :domain:liquibaseStatus

# 4. Migration faylni tuzatamiz
# 5. Qayta apply qilamiz
./gradlew :domain:liquibaseUpdate
```

**Batafsil ma'lumot:** `/home/adm1n/startup/docs/hemis-back/` papkasida to'liq qo'llanma mavjud.

---

## 🚀 Quick Start

### Requirements

- Java 21+
- PostgreSQL 16+
- Redis 7+
- Gradle 9.2.0 (wrapper bilan birga keladi)

### Database Setup

```bash
# PostgreSQL database yaratish
createdb test_hemis

# Yoki psql orqali:
psql -U postgres
CREATE DATABASE test_hemis;
\q

# .env fayl yaratish (ixtiyoriy)
cat > .env << EOF
DB_MASTER_HOST=localhost
DB_MASTER_PORT=5432
DB_MASTER_NAME=test_hemis
DB_MASTER_USERNAME=postgres
DB_MASTER_PASSWORD=postgres
EOF
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

**Backend:** http://localhost:8082
**Swagger UI:** http://localhost:8082/api/swagger-ui.html

### Initial Login

**Admin foydalanuvchi (legacy):**
```
Username: admin
Password: admin
```

**Legacy foydalanuvchilar:** Eski CUBA tizimdan ko'chirilgan 339 ta user mavjud (sec_user jadvalidan).

---

## 🛠 Development

### Code Generation

```bash
# MapStruct mapper generation
./gradlew :domain:compileJava

# Lombok processing
./gradlew clean :domain:compileJava --no-build-cache
```

### Profil Tanlash

```bash
# Development (default)
./gradlew :app:bootRun

# Production
SPRING_PROFILES_ACTIVE=production ./gradlew :app:bootRun

# Replica (read-only)
SPRING_PROFILES_ACTIVE=replica ./gradlew :app:bootRun
```

### Testing

```bash
# Barcha testlar
./gradlew test

# Ma'lum modul testi
./gradlew :api-web:test
./gradlew :service:test

# Test coverage
./gradlew test jacocoTestReport
```

---

## 📡 API Documentation

### Swagger/OpenAPI

Application ishga tushgandan keyin:

**Swagger UI:** http://localhost:8082/api/swagger-ui.html
**OpenAPI JSON:** http://localhost:8082/api/v3/api-docs

### API Endpoints

```
GET  /api/v1/auth/login           # Login
POST /api/v1/auth/logout          # Logout
GET  /api/v1/users                # Users list
GET  /api/v1/permissions          # Permissions
GET  /api/v1/i18n/messages        # Translations
GET  /api/v1/registry/faculty     # Faculty registry
```

**Batafsil:** Swagger UI da barcha endpoint'lar hujjatlashtirilgan.

---

## 🔐 Security

### Authentication

**Hybrid Authentication System:**
- **LegacyPasswordEncoder** - BCrypt (yangi) + PBKDF2 (eski CUBA) ni qo'llab-quvvatlaydi
- **JWT Tokens** - Spring Security OAuth2 Resource Server
- **Session Management** - Redis-based distributed sessions

**Password Format:**
```java
// Yangi format (BCrypt)
$2a$10$N9qo8uLOickgx2ZMRZoMye...

// Eski format (CUBA PBKDF2)
4Z8b9XJGb/dZWHsF3Uo9Qg==:kR7s2Vp9mN...:50000
```

### RBAC (Role-Based Access Control)

**Rollar:**

| Role | Tavsif | Foydalanuvchilar |
|------|--------|------------------|
| `ROLE_SUPER_ADMIN` | Tizim administratori | 1 |
| `ROLE_ADMINISTRATORS` | Administrator | 90+ |
| `ROLE_TEACHERS` | O'qituvchilar | 200+ |
| `ROLE_STUDENTS` | Talabalar | - |
| `ROLE_EMPLOYEES` | Xodimlar | 50+ |

**Huquqlar:** 90 ta permission (30 CRUD + 60 menu)

---

## 🐛 Troubleshooting

### Liquibase Xatoliklari

**Muammo:** "No changesets to rollback"

```bash
# Holatni tekshiring
./gradlew :domain:liquibaseStatus

# Agar changeset'lar mavjud bo'lmasa, yangi migration apply qiling
./gradlew :domain:liquibaseUpdate
```

**Muammo:** "Rollback script not found"

```bash
# Rollback fayl mavjudligini tekshiring
ls -la domain/src/main/resources/db/changelog/changesets/*rollback*

# db.changelog-master.yaml da rollback path to'g'ri ekanligini tekshiring
```

### Database Connection

**Muammo:** "Connection refused"

```bash
# PostgreSQL statusini tekshiring
sudo systemctl status postgresql

# Yoki Docker container
docker ps | grep postgres

# Connection parametrlarni tekshiring
cat .env  # yoki application-dev.yml
```

### Build Errors

**Muammo:** "MapStruct annotation processor failed"

```bash
# Clean build qiling
./gradlew clean build --no-build-cache

# Gradle daemon ni qayta ishga tushiring
./gradlew --stop
./gradlew build
```

---

## 📊 Monitoring

### Actuator Endpoints

Application ishga tushgandan keyin:

```bash
# Health check
curl http://localhost:8082/api/actuator/health

# Metrics
curl http://localhost:8082/api/actuator/metrics

# Database migration status
curl http://localhost:8082/api/actuator/liquibase
```

### Logging

**Log fayllar:**
```bash
# Backend log
tail -f /tmp/backend.log

# Liquibase log
tail -f /tmp/liquibase.log

# Application log
./gradlew :app:bootRun 2>&1 | tee /tmp/app.log
```

---

## 📚 Qo'shimcha Hujjatlar

Batafsil hujjatlar `/home/adm1n/startup/docs/hemis-back/` papkasida:

- **LIQUIBASE_ROLLBACK_GUIDE.md** - Rollback qo'llanmasi
- **CLEAN_ARCHITECTURE_STATUS.md** - Arxitektura holati
- **API_TESTS.md** - API test qo'llanmasi
- **MIGRATION_TAHLIL.md** - Migration tahlil

---

## 🤝 Contributing

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

## 📄 License

Proprietary - HEMIS Project © 2024

---

## 🔗 Links

- **Frontend:** `/home/adm1n/startup/hemis-front`
- **Old Backend:** `/home/adm1n/startup/old-hemis`
- **Documentation:** `/home/adm1n/startup/docs/hemis-back`

---

**Oxirgi yangilanish:** 2025-01-14
**Versiya:** 1.0.0
**Holat:** Development 🚧
