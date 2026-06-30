# app module — Main Spring Boot Application

> **Markaziy ministry server entry point.** Vazirlik markazida 3+ instance Kubernetes cluster sifatida ishlaydi (per-OTM deploy YO'Q).
>
> Bootstrap, configuration, global exception handling, filter chain.
> Hech qanday business logic — faqat orchestration.

---

## Critical Rules

### 1. Application Configuration

```java
@EnableCaching
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@SpringBootApplication(scanBasePackages = {
        "uz.hemis.common",
        "uz.hemis.security",
        "uz.hemis.domain",
        "uz.hemis.service",
        "uz.hemis.api.legacy",     // CUBA entity APIs (/app/rest/v2/entities/*)
        "uz.hemis.api.web",        // Modern UI APIs (/app/rest/v2/*, /api/v1/web/*)
        "uz.hemis.web",            // Web authentication controllers (/api/v1/web/auth/*)
        "uz.hemis.api.external",   // S2S integrations
        "uz.hemis.api.university", // University APIs (/api/v1/university/*)
        "uz.hemis.app"
})
@EntityScan(basePackages = "uz.hemis.domain.entity")
@EnableJpaRepositories(basePackages = "uz.hemis.domain.repository")
public class HemisApplication {
    public static void main(String[] args) {
        SpringApplication.run(HemisApplication.class, args);
    }
}
```

**Diqqat:** `scanBasePackages` — yagona `"uz.hemis"` EMAS, har modul aniq sanab o'tilgan (9 paket + app). Aks holda boshqa modul bean'lari topilmaydi. `@EnableTransactionManagement` YO'Q — Spring Boot auto-config kifoya.

### 2. Profile Strategy

```
application.yml             — Common (har profilda)
application-dev.yml         — Local development (real PostgreSQL via DB_MASTER_*, minimal config)
application-prod.yml        — Production (real DB, full security, no debug)
application-replica.yml     — Replica DB connection
application-redis.yml       — Redis connection (split for clarity)
application-migrate.yml     — Liquibase only profile (CI/CD migration job)
application-test.yml        — Tests (real shared PostgreSQL — see domain/CLAUDE.md Testing Strategy)
```

**Activation:**
```bash
# Dev
SPRING_PROFILES_ACTIVE=dev,redis ./gradlew :app:bootRun

# Production
SPRING_PROFILES_ACTIVE=prod,redis java -jar app.jar

# Migration only (CI/CD)
SPRING_PROFILES_ACTIVE=migrate ./gradlew :domain:liquibaseUpdate
```

### 3. Global Exception Handler

```java
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)   // module-level @RestControllerAdvice (api-legacy/web/...) avval ishlasin
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ApplicationEventPublisher eventPublisher;   // 500 → ErrorEvent (audit)
    private final ObjectMapper objectMapper;

    @ExceptionHandler(ResourceNotFoundException.class)         // 404
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(404, "Not Found", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(BusinessRuleException.class)             // 422 (ADR-0013) — biznes qoidasi buzilgan
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ErrorResponse.of(422, ex.getRuleCode(), ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(ResponseStatusException.class)          // status/reason exception'dan; generic 500 ga tushib qolmasin
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) { ... }

    @ExceptionHandler(NoResourceFoundException.class)         // missing static resource → 404 (500 emas)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) { ... }

    @ExceptionHandler(Exception.class)                        // 500 — fallback
    public ResponseEntity<?> handleGenericException(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        publishErrorEvent(ex, req);                            // audit (hemis.audit.enabled=true bo'lsa)
        if (isLegacyEndpoint(req)) { /* CUBA {error, details} format */ }
        String eventId = String.valueOf(Sentry.captureException(ex));
        return ResponseEntity.internalServerError().body(ErrorResponse.of(500, "Internal Server Error",
            "An unexpected error occurred. Please try again later.", req.getRequestURI(), eventId, "INTERNAL_ERROR"));
        // ✗ NEVER expose ex.getMessage() to client (may leak internal info)
    }
    // Qolgan handlerlar: BadRequest/IllegalArgument/Validation/MethodArgumentNotValid/
    // ConstraintViolation/MethodArgumentTypeMismatch (400), AccessDenied/AuthorizationDenied (403),
    // HttpMessageNotReadable (400, legacy'da 500), HttpMediaTypeNotSupported (415).
}
```

**Diqqat:**
- Body tipi `ResponseWrapper` EMAS — `uz.hemis.common.dto.ErrorResponse` (`.of(...)` / `.validationError(...)` factory).
- `ConflictException` / `DataIntegrityViolationException` handler **YO'Q** (`ConflictException` class hatto yaratilmagan — `application.yml` Sentry ignore'da TODO).
- Legacy CUBA error format **alohida advice EMAS** — shu handler ichida inline `isLegacyEndpoint(req)` tekshiruvi orqali (`/app/rest/v2/`, `/rest/v2/` → `{error, details}` shape). Modul-darajadagi `LegacyExceptionHandler` (api-legacy) o'z scope'ida ustun.

### 4. Filter Chain Order

Real fayllar: `app/filter/TraceIdFilter.java` + `app/filter/AuditRequestFilter.java`
(ikkalasi ham `@Component extends OncePerRequestFilter`). `FilterChainConfig` /
`RequestLoggingFilter` class **YO'Q** — quyidagi `@Order` to'g'ridan-to'g'ri
filter class'da qo'yilgan, alohida registration config emas.

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)   // 1-o'rin — traceId MDC'ga (har log uchun)
public class TraceIdFilter extends OncePerRequestFilter {
    // X-Request-ID / X-Trace-Id header'dan oladi yoki 8-char UUID generate qiladi,
    // MDC.put("traceId", ...) + response.setHeader("X-Request-ID", ...)
}

@Component   // AuditRequestFilter — body capture (ContentCachingRequestWrapper) + audit log
public class AuditRequestFilter extends OncePerRequestFilter { ... }
```

**Order (haqiqiy):**
1. TraceId injection (MDC for logging) — `HIGHEST_PRECEDENCE`
2. Audit request (body capture; `hemis.audit.enabled` bilan gated)
3. Rate limit (security modulida, before auth — login brute-force himoyasi)
4. Spring Security (JWT validation, @PreAuthorize)
5. Controller dispatch

### 5. Bean Configuration Centralization

Real S2S HTTP client config: `app/config/RestTemplateConfig.java` (`RestClientConfig`
EMAS) — Apache HttpClient 5 connection pool + timeout bilan bitta `RestTemplate` bean.
Quyidagi `RestClient` misol **illustrativ** (bir nechta named client pattern):

```java
// ✓ TO'G'RI — config in dedicated class (haqiqiy: RestTemplateConfig)
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() { /* HttpClient 5 pool + timeout */ }
}

// ✗ XATO — bean in arbitrary class
@Service
public class StudentService {
    @Bean  // <-- TAQIQ - service'da bean bo'lmaydi
    public RestClient ministryClient() { ... }
}
```

### 6. Sentry Integration

```yaml
sentry:
  dsn: ${SENTRY_DSN:}
  environment: ${SPRING_PROFILES_ACTIVE:dev}
  release: ${HEMIS_VERSION:dev}
  traces-sample-rate: 0.1  # 10% transactions traced
  send-default-pii: false  # Never send PII to Sentry
  ignored-exceptions-for-type:
    - org.springframework.security.access.AccessDeniedException
    - org.springframework.web.bind.MethodArgumentNotValidException
    - uz.hemis.common.exception.ResourceNotFoundException
```

**Filtered exceptions:** Don't pollute Sentry with normal 4xx flow.

### 7. Actuator Security

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,mappings,liquibase
        # prometheus DEFERRED — Spring Boot 4 micrometer-registry-prometheus dep conflict.
        # "mappings" bor (endpoint inventarizatsiya), "prometheus" hozircha YO'Q.
  endpoint:
    health:
      show-details: when-authorized  # Anonymous: just status
      probes:
        enabled: true                # k8s readiness/liveness
  health:
    db.enabled: true
    diskspace.enabled: true
    redis.enabled: true
```

**Authorization:**
```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
    .requestMatchers("/actuator/info").permitAll()
    .requestMatchers("/actuator/prometheus").hasIpAddress("10.0.0.0/8")  // Internal only
    .requestMatchers("/actuator/**").hasAuthority("admin.full")  // Rest: admin only
);
```

### 8. JVM Configuration (production)

`Dockerfile`:
```dockerfile
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY app.jar app.jar
ENV JAVA_TOOL_OPTIONS="-Xmx4g -Xms2g -XX:+UseZGC -XX:+ZGenerational \
                       -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/hemis/ \
                       -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8 \
                       -Dspring.profiles.active=prod"
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Notes:**
- `-XX:+ExitOnOutOfMemoryError` — Pod restart on OOM (Kubernetes)
- `-XX:+HeapDumpOnOutOfMemoryError` — Post-mortem analysis
- ZGC + Generational ZGC (Java 25 LTS default tavsiya) — pause times <1ms (G1GC: ~10ms)
- Java 25 LTS — ADR-0002 (support 2033'gacha)

### 9. Liquibase Auto-Update Strategy

```yaml
spring:
  liquibase:
    enabled: ${LIQUIBASE_ENABLED:true}
    change-log: classpath:db/changelog/db.changelog-master.yaml
    contexts: ${LIQUIBASE_CONTEXTS:default}
```

**Production:** `LIQUIBASE_ENABLED=false` — migration alohida CI job (rollback nazorati uchun).

**Dev:** `true` — fast iteration.

### 10. Graceful Shutdown

```yaml
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
server:
  shutdown: graceful
```

**Effect:** SIGTERM keladi → in-flight request'lar yakunlanmaguncha shutdown qilmaydi (max 30s).

**Kubernetes:** `terminationGracePeriodSeconds: 60` (Spring 30s + buffer).

### 11. Notable `application.yml` Blocks

```yaml
spring:
  threads:
    virtual:
      enabled: true   # Java 25 LTS, JEP 491 — Tomcat HTTP + @Async default executor → virtual threads
```

`application.yml` da quyidagi domen bloklari ham mavjud (qaytadan yozmang, mavjudini sozlang):
- `spring.kafka.*` — Transactional Outbox + webhook fanout (ADR-0007, ADR-0012)
- `hemis.employee-sync.*` — 224 Univer → markaz xodim push (topic/DLQ, consumer concurrency=12)
- `hemis.webhook.*` — markaz → 224 Univer outbound callback (retry, secret-encryption, retention)
- `hemis.outbox.*` — poller interval + retention (30 kun)

---

## Configuration Hierarchy

```
ENV variable (highest priority)
   ↓
application-{profile}.yml
   ↓
application.yml
   ↓
@Value default (lowest priority)
```

**Misol:**
```yaml
# application.yml (haqiqiy yo'l: hemis.security.jwt.secret)
hemis:
  security:
    jwt:
      secret: ${JWT_SECRET}          # default YO'Q — har profilda MAJBURIY (fail-fast)
      expiration: ${JWT_EXPIRATION:3600}   # 1 soat (ADR-0009 qo'llangan, 2026-05-18)
```

`JWT_SECRET` default'siz — ENV o'rnatilmasa app ko'tarilmaydi (zaif default sirib ketmasligi uchun). Access token TTL default 3600s = 1 soat (ADR-0009 implemented, eski 12 soat emas).

---

## PR Checklist (app)

- [ ] Bean'lar `@Configuration` class'larda (service'da emas)
- [ ] Global exception handler covers: 400, 401, 403, 404, 409, 422, 500
- [ ] No internal exception message leaks to client
- [ ] Filter chain order explicit
- [ ] Profile-specific config (dev/prod/test) isolated
- [ ] Sentry filtered (no 4xx noise)
- [ ] Actuator endpoints authorized
- [ ] Liquibase auto-update disabled in prod
- [ ] Graceful shutdown configured
- [ ] JVM flags appropriate for production

---

## See Also
- `../.claude/architecture.md` — Module dependency graph
- `../security/CLAUDE.md` — Security filter chain
- `../.claude/rules.md` — Reliability + cache patterns
