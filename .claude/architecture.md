# HEMIS Backend - Architecture

> **Pattern:** Modular Monolith + Clean Architecture

---

## Why Modular Monolith?

- Simpler deployment, easier debugging, lower infra costs
- ACID transactions across modules (no distributed tx)
- No network latency between modules
- Can extract to microservices later if needed

---

## Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                          │
│  ┌────────────┬──────────┬─────────────────┬─────────────────┐  │
│  │ api-legacy │ api-web  │ api-university  │  api-external   │  │
│  │ (CUBA API) │(Modern)  │ (B2B 224 OTM)   │  (MyGov/OneID)  │  │
│  └──────┬─────┴────┬─────┴────────┬────────┴─────┬───────────┘  │
├──────────────────────────────────────────────────┤
│                Application Layer                  │
│  app: Configuration, Exception Handling, Auth     │
├──────────────────────────────────────────────────┤
│                 Business Layer                    │
│  service: Business logic, Validation, Tx, Maps   │
├──────────────────────────────────────────────────┤
│               Infrastructure Layer                │
│  security: JWT, OAuth2, RBAC                      │
│  domain: JPA Entities, Repositories, Liquibase    │
├──────────────────────────────────────────────────┤
│                  Common Layer                     │
│  DTOs, Exceptions, Utilities, Constants           │
└──────────────────────────────────────────────────┘
```

### Module Dependency Graph

```
                              app
                               │
    ┌──────────────┬───────────┼───────────┬──────────────┐
    │              │           │           │              │
api-legacy     api-web    api-university api-external    │
    │              │           │           │              │
    └──────────────┴───────────┼───────────┴──────────────┘
                       │
                    service
                       │
    ┌──────────────────┼──────────────────┐
    │                  │                  │
 security           domain             common
    │                  │
    └──────────────────┘
                       │
                    common
```

**Rules:**
- Modules can depend only on modules below them
- NO circular dependencies
- `common` has ZERO external dependencies

---

## Module File Structure

### `common` — DTOs, exceptions, utilities
```
common/src/main/java/uz/hemis/common/
├── dto/              # ResponseWrapper, ErrorResponse, PageResponse, domain DTOs
├── exception/        # ResourceNotFoundException, ValidationException, etc.
├── datasource/       # DataSourceType, DataSourceContextHolder, @ReadOnly, @WriteOnly
└── constants/        # SecurityConstants, ApiConstants
Dependencies: NONE (pure Java + Lombok + Jackson)
```

### `domain` — Entities, repositories, migrations
```
domain/src/main/java/uz/hemis/domain/
├── entity/           # 51 JPA entities (Student, Faculty, User, SecUser, Role, etc.)
├── repository/       # Spring Data JPA repositories (50+)
└── mapper/           # MapStruct entity ↔ DTO mappers (50+)
domain/src/main/resources/db/changelog/changesets/  # Liquibase migrations
Dependencies: common, Spring Data JPA, PostgreSQL, Liquibase, MapStruct
```

### `security` — Authentication & authorization
```
security/src/main/java/uz/hemis/security/
├── config/           # SecurityConfig, JwtGrantedAuthoritiesConverter, RedisConfig
├── crypto/           # LegacyPasswordEncoder (BCrypt + PBKDF2)
├── service/          # HybridUserDetailsService, TokenService, UserPermissionCacheService
├── controller/       # OAuth2TokenController
└── listener/         # CacheInvalidationListener
Dependencies: common, domain, Spring Security, OAuth2 Resource Server, Redis
```

### `service` — Business logic
```
service/src/main/java/uz/hemis/service/
├── {Feature}Service.java       # Interface
├── {Feature}ServiceImpl.java   # Implementation
└── legacy/                     # Legacy-specific service classes
Dependencies: common, domain, security
```
100+ service classes. Pattern: Interface + Impl with `@Transactional`.

### `api-web` — Modern REST API
```
api-web/src/main/java/uz/hemis/api/web/controller/  # 30 controllers
Base Path: /api/v1/web/*
Dependencies: common, domain, service
```

### `api-legacy` — CUBA compatibility layer
```
api-legacy/src/main/java/uz/hemis/api/legacy/controller/  # 56 controllers
Base Path: /app/rest/v2/*
Dependencies: common, domain, service
```

### `api-university` — University B2B sync
```
api-university/src/main/java/uz/hemis/api/university/
├── controller/       # OAuth2 token + building/student sync endpoints
│   └── auth/UniversityOAuthTokenController  # client_credentials grant
└── ...
Base Path: /api/v1/university/*
Security: OAuth 2.0 client_credentials (oauth_client jadval)
Dependencies: common, domain, service
Maqsad: 224 ta OTM PHP backend bilan B2B sync. Hozirgi /app/rest/v2/oauth/token
        password grant'dan TDB'ga muqobil — secret rotation, IP whitelist, per-client rate limit.
```

### `api-external` — Server-to-Server
```
api-external/src/main/java/uz/hemis/api/external/
├── controller/       # 6 integration endpoints (MyGov, OneID, kelajakdagi shartnomalar)
└── client/           # External API clients
Base Path: /api/v1/external/*
Security: API Key + IP Whitelist
Dependencies: common, domain, service
```

### `app` — Main Spring Boot application
```
app/src/main/java/uz/hemis/app/
├── HemisApplication.java              # @SpringBootApplication
├── config/                            # DataSourceConfig, OpenApiConfig, RestTemplateConfig
├── security/                          # UniversityAccessValidator, RateLimitFilter
├── controller/                        # AuthController, CaptchaController (5 endpoints)
└── exception/GlobalExceptionHandler.java  # @ControllerAdvice
app/src/main/resources/
├── application.yml / application-{profile}.yml
└── logback-spring.xml
Dependencies: ALL modules
```

---

## Database: Two Separate PostgreSQL Instances

```
                          Application
                              │
            ┌─────────────────┴─────────────────┐
            ▼                                   ▼
   ┌────────────────────┐              ┌────────────────────┐
   │   hemis (asosiy)   │              │  hemis_audit       │
   │   ────────────     │              │  ──────────────    │
   │   Master + Replica │              │  Master + Replica  │
   │   (Streaming)      │              │  (Streaming)       │
   │                    │              │                    │
   │   31 ta jadval     │              │  3 ta jadval       │
   │   (V001..V014)     │              │  - activity_log    │
   │                    │              │  - error_log       │
   │                    │              │  - login_log       │
   │   Soft delete      │              │  REVOKE UPD,DEL    │
   │   Foreign keys     │              │  (immutable)       │
   └────────────────────┘              └────────────────────┘
```

### Asosiy DB: Master-Replica Routing

```
Application → Dynamic Routing (@ReadOnly annotation)
                │
         ┌──────┴──────┐
         ▼              ▼
     MASTER (RW)    REPLICA (RO)
         │── Async Replication ──│
```

**How it works:**
1. `@ReadOnly` annotation on service/repository method
2. `DataSourceAspect` intercepts → sets `DataSourceContextHolder` to REPLICA
3. `RoutingDataSource` (extends `AbstractRoutingDataSource`) reads context
4. After method completes → context cleared

**Connection Pooling (HikariCP):**

| Pool | max-pool-size | min-idle | Notes |
|------|---------------|----------|-------|
| Master (asosiy) | 10 | 2 | Write + fallback read |
| Replica (asosiy) | 20 | 5 | Read-only, `read-only: true` |
| Audit Master | 5 | 1 | Insert-only (audit yozuvlari) |
| Audit Replica | 10 | 2 | Read-only (audit UI) |

### Audit DB (alohida)

**Maqsad:** Kompyuter va xavfsizlik audit logi asosiy DB'dan ALOHIDA `hemis_audit` bazasida saqlanadi.

**Sabab:**
- Performance izolyatsiya: 1M+ insert/kun audit log asosiy DB ga ta'sir qilmaydi
- Backup farqi: audit 5+ yil saqlanadi (compliance), asosiy DB tezroq
- Xavfsizlik: `REVOKE UPDATE, DELETE ON activity_log FROM PUBLIC` (immutability)

**Schema yaratish:** Liquibase EMAS — Spring `AuditDataSourceConfig.java` orqali
`ResourceDatabasePopulator` bilan boot paytida (faqat master).

**Fayllar:** `app/src/main/resources/db/audit/V001_create_activity_log.sql` va h.k.

**Konfiguratsiya:**
```yaml
hemis.audit.enabled: true
hemis.audit.datasource.master.url: jdbc:postgresql://localhost:5434/hemis_audit
hemis.audit.datasource.replica.url: <fallback master>
hemis.audit.redact-fields: password,token,secret  # Sensitive fields masking
```

---

## Security: Authentication Flow

```
1. Login Request
   ↓
2. HybridUserDetailsService
   ├→ Check 'users' table (BCrypt) — 99% found ✅
   └→ Fallback 'sec_user' table (PBKDF2) — <1% legacy
   ↓
3. Generate JWT (HS256)
   ↓
4. Store session in Redis
   ↓
5. Return token to client
```

### Authorization Flow
```
Request with Bearer token
   ↓
Spring Security validates JWT → Extract username + authorities
   ↓
@PreAuthorize checks permission
   ├→ Granted → Continue
   └→ Denied → 403 Forbidden
```

Permissions cached in Redis. Format: `{resource}.{action}` (e.g. `students.view`).

---

## Caching: Two-Level Architecture

```
Application
   │
   ├→ L1: Caffeine (JVM, per-instance, fast)
   └→ L2: Redis (shared across instances)
         │
      Database
```

| Level | Technology | Scope | Use Case |
|-------|-----------|-------|----------|
| L1 | Caffeine | Per-instance | Hot data (entity by ID) |
| L2 | Redis | Shared | Permissions, sessions, distributed cache |

**Annotations:** `@Cacheable(value, key)`, `@CacheEvict(value, key)`, `@CacheEvict(allEntries=true)`.

---

## Deployment

```
              Internet
                 │
          Load Balancer (Nginx :443)
                 │
      ┌──────────┼──────────┐
      ▼          ▼          ▼
   App-1      App-2      App-3
   :8081      :8081      :8081
      │          │          │
      └──────────┼──────────┘
                 │
      ┌──────────┴──────────┐
      ▼                     ▼
  PostgreSQL             Redis
  Master/Replica         Cluster
```

### Docker (Multi-stage build)

```dockerfile
# Build
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :app:bootJar -x test --no-daemon

# Run
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
```

### Resource Requirements (224 universitet, ~1.15M talaba)

**Production cluster (minimum 3 instances behind LB):**

| Environment | CPU | RAM | JVM Heap | Disk | Notes |
|-------------|-----|-----|----------|------|-------|
| Production (per app instance) | 8 cores | 8 GB | `-Xmx4g -Xms2g` | 50 GB | 3+ instances, stateless |
| PostgreSQL Master | 16 cores | 32 GB | — | 500 GB SSD | shared_buffers=8GB, work_mem=64MB |
| PostgreSQL Replica | 16 cores | 32 GB | — | 500 GB SSD | streaming replication |
| Redis cluster | 4 cores | 16 GB | — | 50 GB SSD | 3 nodes (cache + token + session) |
| Development (per dev) | 2 cores | 4 GB | `-Xmx2g` | 20 GB | H2 in-memory uchun yetadi |

**Capacity planning:**
- Peak: ~1000 concurrent users → 3 instance × 250-350 users each
- DB connections: 30 master pool + 60 replica pool (per cluster, not per instance)
- Redis ops: ~5K req/s peak (cache hit ratio target 85%+)
- Excel report generation: separate executor, max 5 concurrent

**JVM flags (production):**
```
-Xmx4g -Xms2g
-XX:+UseG1GC -XX:MaxGCPauseMillis=200
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/hemis/
-XX:+ExitOnOutOfMemoryError
-Dfile.encoding=UTF-8
```

### Scalability
- Stateless (JWT) → horizontal scaling behind load balancer
- Redis for shared sessions/cache → no sticky sessions needed
- Read replicas for heavy read workloads
- Docker/Kubernetes ready

---

## Monitoring

| Component | Details |
|-----------|---------|
| Actuator | `/actuator/health` (public), `/actuator/metrics`, `/actuator/liquibase` (JWT), `/actuator/env` (admin) |
| Sentry | Exception tracking, performance monitoring, request tracing |
| Logging | SLF4J + Logback; JSON format; daily rotation (30 days) |
| Log files | `/tmp/backend.log`, `/tmp/liquibase.log`, `/tmp/security.log` |
| Log levels | Prod: INFO + ERROR (Sentry); Dev: DEBUG + TRACE (SQL) |
