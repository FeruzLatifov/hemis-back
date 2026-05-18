# app module — Main Spring Boot Application

> **Markaziy ministry server entry point.** Vazirlik markazida 3+ instance Kubernetes cluster sifatida ishlaydi (per-OTM deploy YO'Q).
>
> Bootstrap, configuration, global exception handling, filter chain.
> Hech qanday business logic — faqat orchestration.

---

## Critical Rules

### 1. Application Configuration

```java
@SpringBootApplication(scanBasePackages = "uz.hemis")
@EnableJpaRepositories(basePackages = "uz.hemis.domain.repository")
@EntityScan(basePackages = "uz.hemis.domain.entity")
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class HemisApplication {
    public static void main(String[] args) {
        SpringApplication.run(HemisApplication.class, args);
    }
}
```

**Diqqat:** `scanBasePackages = "uz.hemis"` — barcha modullar import bo'lishi uchun. Aks holda boshqa modul bean'lari topilmaydi.

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
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ResponseWrapper.error("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> details = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new FieldError(fe.getField(), fe.getCode(),
                Optional.ofNullable(fe.getDefaultMessage()).orElse("Invalid")))
            .toList();
        return ResponseEntity.badRequest()
            .body(ResponseWrapper.error("VALIDATION_ERROR", "Validation failed", details));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ResponseWrapper.error("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ResponseWrapper.error("FORBIDDEN", "Insufficient permissions"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ResponseWrapper.error("DATA_INTEGRITY",
                "Operation conflicts with existing data"));  // Don't leak DB constraint name
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        Sentry.captureException(ex);
        return ResponseEntity.internalServerError()
            .body(ResponseWrapper.error("INTERNAL_ERROR", "An unexpected error occurred"));
        // ✗ NEVER expose ex.getMessage() to client (may leak internal info)
    }
}
```

**Diqqat:** `api-legacy` o'z `@RestControllerAdvice(basePackages = "uz.hemis.api.legacy")` bilan override qiladi (CUBA error format).

### 4. Filter Chain Order

```java
@Configuration
public class FilterChainConfig {

    // Order: First = outermost (runs first on request, last on response)
    @Bean public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> reg = new FilterRegistrationBean<>(new TraceIdFilter());
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);  // 1st - inject traceId to MDC
        return reg;
    }

    @Bean public FilterRegistrationBean<RequestLoggingFilter> requestLogging() {
        FilterRegistrationBean<RequestLoggingFilter> reg = new FilterRegistrationBean<>(new RequestLoggingFilter());
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return reg;
    }

    // RateLimitFilter — security/filter/ modulida @Component, auto-registratsiya
    // (default order). Agar explicit order kerak bo'lsa, FilterRegistrationBean
    // bilan inject qilib o'rab oling va auto-registratsiyani o'chiring:
    @Bean public FilterRegistrationBean<RateLimitFilter> rateLimitRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);  // Before auth — fail fast
        return reg;
    }
    // Spring Security filters here (auto-ordered)
}
```

**Order:**
1. TraceId injection (MDC for logging)
2. Request logging (before auth — log even unauthorized)
3. Rate limit (before auth — protect login from brute force)
4. Spring Security (JWT validation, @PreAuthorize)
5. Controller dispatch

### 5. Bean Configuration Centralization

```java
// ✓ TO'G'RI — config in dedicated class
@Configuration
public class RestClientConfig {
    @Bean
    public RestClient ministryRestClient() { ... }

    @Bean
    public RestClient mspdRestClient() { ... }
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
        include: health,info,metrics,prometheus,liquibase
  endpoint:
    health:
      show-details: when-authorized  # Anonymous: just status
      probes.enabled: true             # k8s readiness/liveness
    env.enabled: true
  health:
    db.enabled: true
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
# application.yml
hemis:
  jwt:
    secret: ${HEMIS_JWT_SECRET:default-dev-secret-change-me}
```

Production'da `HEMIS_JWT_SECRET` ENV majburiy. Default bo'lsa, log'ga warning.

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
