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
┌──────────────────────────────────────────────────┐
│                Presentation Layer                 │
│  ┌────────────┬──────────┬─────────────────────┐ │
│  │ api-legacy │ api-web  │   api-external      │ │
│  │ (CUBA API) │(Modern)  │   (S2S)             │ │
│  └──────┬─────┴────┬─────┴──────┬──────────────┘ │
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
    ┌──────────────────┼──────────────────┐
    │                  │                  │
api-legacy         api-web         api-external
    │                  │                  │
    └──────────────────┼──────────────────┘
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

### `api-external` — Server-to-Server
```
api-external/src/main/java/uz/hemis/api/external/
├── controller/       # 6 integration endpoints
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

## Database: Master-Replica Routing

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
| Master | 10 | 2 | Write + fallback read |
| Replica | 20 | 5 | Read-only, `read-only: true` |

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
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :app:bootJar -x test --no-daemon

# Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Resource Requirements

| Environment | CPU | RAM | Disk |
|-------------|-----|-----|------|
| Production (per instance) | 4 cores | 4 GB (JVM: -Xmx1024m) | 20 GB |
| Development | 2 cores | 2 GB | 10 GB |

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
