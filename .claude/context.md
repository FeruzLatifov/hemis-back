# HEMIS Backend - Project Context

> **H**igher **E**ducation **M**anagement **I**nformation **S**ystem
> 
> **Status:** Production-Ready Development 🚀  
> **Version:** 2.0.0  
> **Last Updated:** 2025-11-15

---

## 🎯 Business Domain

**HEMIS** - O'zbekiston oliy ta'lim muassasalari uchun kompleks boshqaruv tizimi.

### Core Functions
- 👥 **Student Management** - talabalar, guruhlar, kontraktlar
- 📚 **Academic Management** - o'quv rejalari, fanlar, baholar  
- 👨‍🏫 **Staff Management** - professor-o'qituvchilar, xodimlar
- 🏛️ **University Structure** - fakultetlar, kafedraları, bo'limlar
- 💰 **Financial Management** - to'lovlar, stipendiyalar, grantlar
- 📊 **Reporting** - Vazirlik hisobotlari, statistika
- 🔗 **External Integrations** - Davlat xizmatlari (HEMIS, OneID, MyGov)

### Users
- **340+ foydalanuvchilar** (legacy CUBA tizimidan ko'chirilgan)
- **5 asosiy rol**: Super Admin, Administrators, Teachers, Students, Employees
- **30+ ta universitetlar** O'zbekiston bo'ylab

---

## 🏗️ Technical Architecture

### Stack Overview
```
Spring Boot:  3.5.7 (Latest stable)
Java:         21 LTS (Temurin JDK)
Gradle:       8.10.2 (Kotlin DSL)
PostgreSQL:   16+ (ACID transactions)
Redis:        7+ (Distributed cache + sessions)
Liquibase:    4.31.1 (Professional migrations)
```

### Architecture Pattern
**Modular Monolith** with **Clean Architecture** principles

```
┌─────────────────────────────────────────────────────┐
│                  Presentation Layer                 │
│  ┌────────────┬────────────┬─────────────────────┐ │
│  │ api-legacy │  api-web   │   api-external      │ │
│  │ (CUBA API) │ (Modern UI)│  (S2S Integration)  │ │
│  │ 56 ctrl    │  30 ctrl   │   6 controllers     │ │
│  └────────────┴────────────┴─────────────────────┘ │
├─────────────────────────────────────────────────────┤
│                  Application Layer                  │
│  ┌───────────────────────────────────────────────┐ │
│  │  app (Main Spring Boot Application)          │ │
│  │  - Configuration (DataSource, Security)      │ │
│  │  - Exception Handling (Global)               │ │
│  │  - Auth Controllers (5 endpoints)            │ │
│  └───────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────┤
│                   Business Layer                    │
│  ┌───────────────────────────────────────────────┐ │
│  │  service (105 service classes)               │ │
│  │  - Business logic                            │ │
│  │  - Validation rules                          │ │
│  │  - Transaction management                    │ │
│  └───────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────┤
│                 Infrastructure Layer                │
│  ┌──────────┬──────────────────────────────────┐  │
│  │ security │  domain                          │  │
│  │ - JWT    │  - 51 JPA Entities (7,958 LOC)  │  │
│  │ - OAuth2 │  - Spring Data Repositories      │  │
│  │ - RBAC   │  - Liquibase Migrations          │  │
│  └──────────┴──────────────────────────────────┘  │
├─────────────────────────────────────────────────────┤
│                    Common Layer                     │
│  ┌───────────────────────────────────────────────┐ │
│  │  common (Shared utilities, DTOs, Exceptions) │ │
│  └───────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

### Module Dependency Graph
```
app → api-legacy ─┐
app → api-web ────┼─→ service → domain → common
app → api-external┘              ↓
app → security ──────────────────┘
```

**CRITICAL RULES:**
- ❌ NO circular dependencies
- ❌ NO direct repository access from controllers
- ✅ Service layer MUST handle ALL business logic
- ✅ Domain layer is READ-ONLY (legacy schema compatibility)

---

## 🗄️ Database Architecture

### Master-Replica Setup
```
                   ┌──────────────┐
                   │  Application │
                   └──────┬───────┘
                          │
            ┌─────────────┴─────────────┐
            ▼                           ▼
     ┌─────────────┐            ┌──────────────┐
     │   Master    │  Replication│   Replica    │
     │ (Write/Read)│───────────→ │  (Read-Only) │
     │ PostgreSQL  │             │  PostgreSQL  │
     └─────────────┘             └──────────────┘
```

### Legacy Schema (ministry.sql)
**CRITICAL:** Schema is FROZEN - NO modifications allowed!

**Tables:**
- `sec_user` (340 users) - Legacy CUBA users (PBKDF2 passwords)
- `users` (339 users) - New users (BCrypt passwords)
- `hemishe_e_student` - Students (~5,000 records)
- `hemishe_e_curriculum` - Academic programs
- `hemishe_e_subject` - Courses/Subjects
- `h_employee` - Staff members
- `h_system_message_translation` - i18n (508 translations × 4 languages)

**Total:** ~50 domain tables + 10 security tables

### Database Migration Strategy
```
Liquibase 4.x (Professional rollback support)
├── v1-schema-complete      → Base schema (7 tables)
├── v2-seed-data-complete   → Roles + permissions (95 records)
├── v3-users-migrated       → User migration (339 users)
├── v4-menu-translations    → i18n messages (508 records)
└── v5-faculty-translations → Faculty data (50 records)
```

**Rollback Support:** ✅ All changesets have rollback scripts

---

## 🔐 Security Architecture

### Authentication Flow (Hybrid System)
```
1. User Login Request
   │
   ▼
2. HybridUserDetailsService
   │
   ├─→ [Check NEW system]  users table (BCrypt)
   │   └─→ 99% users found here ✅
   │
   └─→ [Fallback to OLD]   sec_user table (PBKDF2)
       └─→ <1% legacy users
   
3. Token Generation (JWT)
   │
   ▼
4. Redis Storage (session management)
```

### Password Encoding
```java
// LegacyPasswordEncoder supports BOTH formats:

NEW Format (BCrypt):
$2a$10$N9qo8uLOickgx2ZMRZoMye...

OLD Format (CUBA PBKDF2):
4Z8b9XJGb/dZWHsF3Uo9Qg==:kR7s2Vp9mN...:50000
           │            │            │
        hash          salt       iterations
```

### Authorization (RBAC)
```
User ──has──> Roles ──have──> Permissions
 │              │                  │
 └─ 339       5 roles          30 perms

Roles:
- ROLE_SUPER_ADMIN      (tizim administratori)
- ROLE_ADMINISTRATORS   (universitet admin)
- ROLE_TEACHERS         (o'qituvchilar)
- ROLE_STUDENTS         (talabalar)
- ROLE_EMPLOYEES        (xodimlar)

Permission Format: {resource}.{action}
Examples: students.view, faculty.create, grades.edit
```

### JWT Configuration
```yaml
Token Type:      Bearer
Algorithm:       RS256 (RSA-SHA256)
Validity:        24 hours
Refresh:         7 days
Storage:         Redis (distributed)
Claims:          username, roles, university_id
```

---

## 📡 API Architecture

### API Modules (3-layer separation)

#### 1. api-legacy (CUBA Compatibility Layer)
**Purpose:** Backward compatibility with OLD-HEMIS frontend
- **Port:** 8080 (legacy port)
- **Base Path:** `/app/rest/*`
- **Controllers:** 56 entity controllers
- **Format:** CUBA Platform JSON structure
- **Status:** ⚠️ Maintained for transition period

**Example Endpoints:**
```
GET  /app/rest/v2/entities/hemishe_Student
POST /app/rest/v2/entities/hemishe_Student
GET  /app/rest/v2/entities/hemishe_Curriculum/{id}
```

#### 2. api-web (Modern REST API)
**Purpose:** New frontend (React/Vue) + mobile apps
- **Base Path:** `/api/v1/web/*`
- **Controllers:** 30 REST controllers
- **Format:** Clean JSON (no legacy field names)
- **OpenAPI:** ✅ Full Swagger documentation

**Example Endpoints:**
```
GET    /api/v1/web/students?page=0&size=20
POST   /api/v1/web/students
GET    /api/v1/web/faculty/{id}/departments
GET    /api/v1/web/i18n/messages?lang=uz-UZ
```

#### 3. api-external (Server-to-Server)
**Purpose:** Government service integrations
- **Base Path:** `/api/v1/external/*`
- **Controllers:** 6 integration endpoints
- **Security:** API Key + IP whitelist
- **Format:** Standard JSON

**Integrations:**
```
HEMIS Ministry API  - University data sync
OneID              - Single Sign-On
MyGov              - Student verification
PayMe/Click        - Payment processing
```

### Response Format Standards
```json
// Success Response
{
  "success": true,
  "data": { ... },
  "timestamp": "2025-11-15T08:00:00Z"
}

// Error Response
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Student not found",
    "details": ["id: 12345"]
  },
  "timestamp": "2025-11-15T08:00:00Z"
}

// Paginated Response
{
  "success": true,
  "data": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

## 🚀 Deployment Architecture

### Production Environment
```
┌─────────────────────────────────────────────────┐
│              Load Balancer (Nginx)              │
│                  Port: 443 (HTTPS)              │
└──────────────┬──────────────────────────────────┘
               │
       ┌───────┴───────┬──────────────┐
       ▼               ▼              ▼
   ┌────────┐     ┌────────┐     ┌────────┐
   │ App-1  │     │ App-2  │     │ App-3  │
   │:8080   │     │:8080   │     │:8080   │
   └───┬────┘     └───┬────┘     └───┬────┘
       │              │              │
       └──────────────┴──────────────┘
                      │
       ┌──────────────┴───────────────┐
       ▼                              ▼
┌──────────────┐              ┌──────────────┐
│  PostgreSQL  │              │    Redis     │
│  Master/Rep  │              │   Cluster    │
└──────────────┘              └──────────────┘
```

### Docker Deployment
```bash
# Multi-stage build (Best Practice)
docker build -t hemis-backend:1.0.0 .

# Run with environment variables
docker-compose up -d

# Health check
curl http://localhost:8080/actuator/health
```

### Resource Requirements
```yaml
Production (per instance):
  CPU:    4 cores
  RAM:    4 GB (JVM: -Xmx1024m)
  Disk:   20 GB (logs + temp)
  
Development:
  CPU:    2 cores
  RAM:    2 GB
  Disk:   10 GB
```

---

## 📊 Monitoring & Observability

### Spring Boot Actuator
```
GET /actuator/health      - Health status
GET /actuator/metrics     - JVM metrics
GET /actuator/info        - Build info
GET /actuator/liquibase   - Migration status
GET /actuator/env         - Environment variables
```

### Sentry Integration (v8.16.0)
```yaml
Features:
  - ✅ Exception tracking
  - ✅ Performance monitoring
  - ✅ Request tracing
  - ✅ User context
  - ✅ Breadcrumbs

DSN: ${SENTRY_DSN}
Environment: ${SPRING_PROFILES_ACTIVE}
Release: hemis-backend@1.0.0
```

### Logging Strategy
```
Levels:
  Production:  INFO (files) + ERROR (Sentry)
  Development: DEBUG (console) + TRACE (SQL)

Files:
  /tmp/backend.log      - Application logs
  /tmp/liquibase.log    - Migration logs
  /tmp/security.log     - Auth/authz logs

Format: JSON (for log aggregation)
Rotation: Daily (max 30 days)
```

---

## 🔄 Development Workflow

### Local Development Setup
```bash
# 1. Clone repository
git clone <repo-url> hemis-back

# 2. Setup environment
cp .env.example .env
# Edit .env with your credentials

# 3. Start database
docker-compose up -d postgres redis

# 4. Run migrations
./gradlew :domain:liquibaseUpdate

# 5. Start application
./gradlew :app:bootRun

# 6. Access Swagger
open http://localhost:8080/api/swagger-ui.html
```

### Build Commands
```bash
# Clean build
./gradlew clean build

# Skip tests
./gradlew build -x test

# Specific module
./gradlew :api-web:build

# Boot JAR
./gradlew :app:bootJar
# Output: app/build/libs/hemis-1.0.0.jar
```

### Migration Commands
```bash
# Status
./gradlew :domain:liquibaseStatus

# Apply
./gradlew :domain:liquibaseUpdate

# Rollback (1 changeset)
./gradlew :domain:liquibaseRollbackCount -Pcount=1

# Rollback to tag
./gradlew :domain:liquibaseRollbackToTag -Ptag=v3-users-migrated

# Preview rollback SQL
./gradlew :domain:liquibaseRollbackSQL -Pcount=1
```

---

## 🎨 Code Style & Conventions

### Naming Conventions
```java
// Classes
Entity:        Student, Faculty, Curriculum
DTO:          StudentDto, FacultyDto
Service:      StudentService, FacultyServiceImpl
Controller:   StudentController, FacultyRestController
Repository:   StudentRepository, FacultyRepository

// Methods
GET:     findById(), findAll(), search()
POST:    create(), register()
PUT:     update(), modify()
DELETE:  delete(), remove()
```

### Package Structure
```
uz.hemis
├── common
│   ├── dto/          # Data Transfer Objects
│   ├── exception/    # Custom exceptions
│   └── datasource/   # Master/Replica routing
├── domain
│   ├── entity/       # JPA entities
│   └── repository/   # Spring Data repos
├── security
│   ├── config/       # Security config
│   ├── service/      # Auth services
│   └── crypto/       # Password encoders
├── service
│   └── (business logic)
├── api-legacy
│   └── controller/   # CUBA API
├── api-web
│   └── controller/   # REST API
└── api-external
    └── controller/   # S2S API
```

---

## 🧪 Testing Strategy

### Test Coverage
```
Current Status:
  Unit Tests:        23 tests
  Integration Tests: In progress
  Coverage Target:   70%+ (production requirement)
  
⚠️ MANDATORY REQUIREMENTS:
  - EVERY endpoint MUST have integration test
  - EVERY service method MUST have unit test
  - NO pull request without tests
  - Test coverage below 70% = Build FAILS
  - Missing tests = Code review REJECTED
```

### Test Profiles
```yaml
application-test.yml:
  - H2 in-memory database
  - No Redis (mock cache)
  - No Sentry (disabled)
  - Fast startup (<10 seconds)
```

### Running Tests
```bash
# All tests
./gradlew test

# Specific module
./gradlew :service:test

# With coverage
./gradlew test jacocoTestReport

# Skip tests
./gradlew build -x test
```

**IMPORTANT:** Tests are DISABLED by default!
Set `TESTS_ENABLED=true` in `.env` to enable.

⚠️ **MANDATORY BEFORE MERGE:**
- All tests must pass
- Integration tests for ALL new endpoints
- Unit tests for ALL new service methods
- Test coverage >= 70%

---

## 🚨 Critical Constraints

### ❌ NEVER DO
1. **Schema Modifications**
   - NO ALTER TABLE on legacy tables
   - NO DROP TABLE/COLUMN
   - NO RENAME operations
   
2. **Breaking Changes**
   - NO changes to existing API endpoints
   - NO removal of fields from JSON responses
   - NO modification of legacy field names
   
3. **Data Integrity**
   - NO direct database updates (bypass service layer)
   - NO cascade deletes on legacy tables
   - NO foreign key constraints on legacy schema

### ✅ ALWAYS DO
1. **Use Service Layer**
   - ALL business logic in services
   - Transaction management via `@Transactional`
   - Input validation via `@Valid`

2. **Follow Migration Process**
   - Create Liquibase changesets
   - Add rollback scripts
   - Tag major versions
   - Test on staging first

3. **Security Best Practices**
   - Validate ALL user input
   - Use parameterized queries (JPA)
   - Check permissions via `@PreAuthorize`
   - Sanitize error messages

---

## 📚 Documentation

### Available Docs
```
/docs/
├── API_TESTS.md              - API testing guide
├── LIQUIBASE_MIGRATION_GUIDE.md - Migration manual
├── SWAGGER_SETUP.md          - API documentation
├── FRONTEND_INTEGRATION.md   - Frontend guide
└── MIGRATION_TAHLIL.md       - Migration analysis
```

### API Documentation
- **Swagger UI:** http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api/v3/api-docs
- **Format:** OpenAPI 3.0

⚠️ **MANDATORY REQUIREMENT:**
- **EVERY endpoint MUST be documented in Swagger**
- Missing Swagger documentation = Code review REJECTED
- Swagger annotations are NOT optional!

---

## 🔗 External Dependencies

### Government Services
```
HEMIS Ministry API:  https://student.hemis.uz
OneID SSO:          https://sso.egov.uz
MyGov Portal:       https://my.gov.uz
```

### Third-Party Libraries
```yaml
Spring Boot:     3.5.7
Spring Security: 6.2.x (via Boot)
Hibernate:       6.6.x (via Boot)
Liquibase:       4.31.1
MapStruct:       1.6.3
Lombok:          1.18.x
Sentry:          8.16.0
Jedis (Redis):   5.1.0
SpringDoc:       2.7.0 (OpenAPI)
```

---

## 📞 Support & Contact

### Team Structure
```
Backend Team:    5 developers
Database Admin:  1 DBA
DevOps:         2 engineers
QA:             3 testers
```

### Issue Tracking
- **GitHub Issues:** For bugs and features
- **JIRA:** For sprint planning
- **Slack:** For daily communication

---

## 🎯 Roadmap

### Phase 1: Migration (Completed ✅)
- ✅ User migration (340 → 339 users)
- ✅ Hybrid authentication
- ✅ RBAC implementation
- ✅ i18n system (4 languages)

### Phase 2: API Modernization (In Progress 🚧)
- 🚧 REST API standardization
- 🚧 OpenAPI documentation
- 🚧 Frontend integration
- 🚧 Mobile API endpoints

### Phase 3: Feature Parity (Q2 2025)
- ⏳ All CUBA features in Spring Boot
- ⏳ Legacy API deprecation
- ⏳ Performance optimization
- ⏳ Load testing

### Phase 4: Decommission OLD-HEMIS (Q3 2025)
- ⏳ Full migration to NEW-HEMIS
- ⏳ OLD-HEMIS shutdown
- ⏳ Final data cleanup

---

**Remember:** This is a LEGACY MIGRATION project. 
Stability > Features. Compatibility > Optimization.

**Golden Rule:** "If it works in OLD-HEMIS, it MUST work in NEW-HEMIS exactly the same way."
