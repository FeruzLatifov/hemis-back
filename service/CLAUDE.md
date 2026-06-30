# service module — Business Logic

> **Markaziy HEMIS-back** business logic qatlami. 230 OTM aggregation + qoidalar joriy qilish (talaba kiritish lock, baho lock) + davlat integratsiya orchestration shu yerda.
>
> Spring `@Service` katlamı. **Hech qanday business logic controller yoki repository'da bo'lmaydi.**
> Bu yerda transaction boundary, cache, validation, exception orchestration.

---

## TOP 10 Service Layer Patterns

> **Batafsil misollar:** [`service/PATTERNS.md`](PATTERNS.md)

| # | Pattern | Qoida |
|---|---------|-------|
| 1 | **Spring AOP Self-Invocation Trap** | `@Cacheable`/`@Transactional`/`@Async` same-class call'da ishlamaydi → alohida `@Service` bean'ga ko'chir |
| 2 | **`@Transactional` scope** | Class-level `readOnly=true` (replica), write methodlarda `@Transactional` override (master) |
| 3 | **`@Cacheable` + `@CacheEvict` pair** | Har `@Cacheable` uchun mutation method'da `@CacheEvict` MAJBURIY (`@Caching` bilan multi-evict) |
| 4 | **Exception hierarchy** | `null` qaytarmaslik — `orElseThrow(ResourceNotFoundException::new)`. `@RestControllerAdvice` HTTP'ga aylantiradi |
| 5 | **`@Valid` Bean Validation** | Service boundary'da. DTO record + `@NotBlank`/`@Pattern`/`@Email`. Business rule manual check (PINFL uniqueness) |
| 6 | **MapStruct Mapping** | `@Mapper(componentModel="spring")`. `@BeanMapping(...IGNORE)` partial update. **api-legacy istisno** — `toMap()` |
| 7 | **`@Async` Custom Executor** | Default executor TAQIQ — explicit `@Async("...")`. Backpressure `CallerRunsPolicy` |
| 8 | **Pageable cache key** | `#facultyId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize` (Sort bilan kalit ko'p variant) |
| 9 | **External timeout** | `setConnectTimeout(10_000) + setReadTimeout(30_000)` — canonical [`api-external/CLAUDE.md`](../api-external/CLAUDE.md) |
| 10 | **Bulk operations** | JPA `saveAll()` 1000 INSERT — JDBC batch tezroq. `jdbcTemplate.batchUpdate(sql, list, 100, ps -> ...)` |

**Eng kritik (1, 3, 4):** AOP trap silent fail; cache evict yo'q → stale data; null qaytarish silent NPE.

---

## Service Method Anatomy

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository repository;
    private final FacultyRepository facultyRepository;
    private final StudentMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Cacheable(value = "students", key = "#id", unless = "#result == null")
    public StudentDto findById(Long id) {
        // 1. Validation (input)
        // 2. Load
        Student student = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student: " + id));
        // 3. Map
        return mapper.toDto(student);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('students.create')")
    @CacheEvict(value = "students:list", allEntries = true)
    public StudentDto create(@Valid StudentCreateDto dto) {
        // 1. Business validation
        if (repository.existsByPinfl(dto.pinfl())) {
            throw new ConflictException("PINFL exists: " + dto.pinfl());
        }
        Faculty faculty = facultyRepository.findById(dto.facultyId())
            .orElseThrow(() -> new ResourceNotFoundException("Faculty: " + dto.facultyId()));

        // 2. Map + persist
        Student entity = mapper.toEntity(dto);
        entity.setFaculty(faculty);
        Student saved = repository.save(entity);

        // 3. Side effects (async)
        eventPublisher.publishEvent(new StudentCreatedEvent(saved.getId()));

        // 4. Log + return
        log.info("Student created: id={}, pinfl={}", saved.getId(), saved.getPinfl());
        return mapper.toDto(saved);
    }
}
```

**Patterns:**
- Class-level: `readOnly=true` default
- Method-level: `@Transactional` write override
- `@PreAuthorize` permission check
- Validation → Load → Mutate → Side effects → Log → Return
- Events for cross-cutting (audit, notification)

---

## Cache Stampede Prevention (single-flight)

> **Real xavf:** Markaziy 3-instance Kubernetes cluster + 224 ta Univer client traffic — hot cache miss 3 ta DB query parallel + Univer'lardan kelayotgan yuk → DB CPU spike, lock contention, connection pool exhaust. (Per-OTM deploy YO'Q — markaziy server.)

### Muammo

```java
// ✗ Stampede — birdaniga ko'p instance bir xil so'rov yuboradi
@Cacheable("popularReport")
public ReportDto getPopularReport() {
    return repository.expensiveQuery();  // 5 sekund
}
// Cache expire bo'ldi → 100 ta concurrent request → 100 ta query
```

### Yechim 1 — Caffeine `refreshAfterWrite` (afzal)

`refreshAfterWrite` cache stale bo'lishidan oldin background'da yangilaydi. Stampede yo'q:

```java
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager mgr = new CaffeineCacheManager();
        mgr.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(15))   // hard expire
            .refreshAfterWrite(Duration.ofMinutes(10))   // soft refresh
            .maximumSize(10_000)
        );
        return mgr;
    }
}

// Refresh handler
@Bean
public CacheLoader<Object, Object> cacheLoader() {
    return (key) -> reload(key);  // background thread, single-flight
}
```

**Effect:** Stale entry stale bo'lganida birinchi request fresh qiymatni oladi (boshqalari eski qiymat bilan ishlaydi background'da yangilanyapti).

### Yechim 2 — Distributed Lock (Redis-based)

Bir vaqtda faqat bitta instance refresh qilsin (qolganlari kutadi):

```java
@Service
public class ReportService {

    private final DistributedCachePort cache;
    private final ReportRepository repo;

    public ReportDto getPopularReport() {
        // 1. Cache hit?
        ReportDto cached = cache.get(KEY, ReportDto.class);
        if (cached != null) return cached;

        // 2. Try lock
        if (cache.acquireLock(LOCK_KEY, Duration.ofSeconds(30))) {
            try {
                ReportDto fresh = repo.expensiveQuery();
                cache.put(KEY, fresh, Duration.ofMinutes(15));
                return fresh;
            } finally {
                cache.releaseLock(LOCK_KEY);
            }
        }

        // 3. Boshqa instance refresh qilyapti — kichik kutish + retry
        Thread.sleep(100);
        return cache.get(KEY, ReportDto.class);  // endi to'lgan bo'lishi kerak
    }
}
```

### Qachon qaysi yondashuv

| Vaqt | Strategy |
|------|----------|
| Hot read (>100 req/s) + tez refresh (~1s) | `refreshAfterWrite` |
| Hot read + sekin refresh (>5s) | Distributed lock |
| Cold read (har soatda 1 marta) | Standart `@Cacheable` yetadi |

---

## Cache Strategy (DashboardCacheConfig)

Bizdagi 2-level cache (Caffeine L1 + Redis L2). **L1 Caffeine unified 30m** (`TwoLevelCacheManager.java:52`); L2 Redis TTL per-cache (`DashboardCacheConfig.cacheManager()` ichidagi `redisCacheConfigurations` map):

```java
// Real cache name → L2 TTL mapping (DashboardCacheConfig.cacheManager())
"userPermissions"             → 30m  (DEFAULT_TTL)
"menu"                        → 30m  (DEFAULT_TTL)
"i18n"                        → 30m  (DEFAULT_TTL)
"stats"                       → 30m  (DASHBOARD_TTL)
"studentGpa"                  → 24h  (heavy aggregation)
"classifier*" / "hokimiyatClassifiers" → 24h  (reference, kam o'zgaradi)
"university*"                 → 1h–24h (List/Children 24h, Profile/Rector 1h)
```

> **DIQQAT:** `users`, `sessions`, `permissions`, `menus` (plural) nomlari bu modulda **YO'Q**. JWT session lifecycle bu modulda emas — **security** modulda. Real RBAC cache nomi `userPermissions` (`menu`, `i18n` singular).

**Yangi cache qo'shilganda:**
1. `DashboardCacheConfig.cacheManager()` `redisCacheConfigurations` map'da TTL config
2. `@Cacheable` annotation
3. Mutation method'da `@CacheEvict`
4. Test (cache hit/miss/evict)

---

## Subsystem Packages (sync + integratsiya)

### `webhook/` — markaz → OTM outbound (ADR-0012)

13 fayl. Markaz event'larni 224 OTM Univer'iga HMAC-imzolangan webhook orqali yetkazadi.

- `WebhookFanoutConsumer` — Kafka fanout consumer, `concurrency = "3"`.
- `WebhookDispatcher` — per-target deliver, `@KafkaListener concurrency = "5"` (224 OTM parallel).
- `HmacSigner` — `X-Hemis-Signature/Timestamp/University-Code` HMAC imzo.
- `WebhookSecretCipher` / `WebhookSecretVault` / `WebhookSecretService` — secret persistence (K1: secret_enc at-rest shifrlash).
- `WebhookAckService` — **K2 apply-status feedback** ("delivered != applied" gap). Univer apply qilgach markazga ack POST → `webhook_apply_result` upsert.
- `WebhookRetryScheduler` — retry backoff scheduler.
- `WebhookTargetService`, `WebhookMetrics`, `WebhookFanoutMetrics`, `WebhookEventEnvelope`, `DevWebhookBootstrap`.

### `outbox/` — transactional outbox → Kafka (ADR-0007)

- `OutboxEventPublisher` — business transaction ichida outbox row INSERT (at-least-once).
- `OutboxPoller` — `@Scheduled(fixedDelayString=...interval-ms:1000)` (1s) batch `batch-size:100`; **retention cleanup** `@Scheduled(cron = "0 0 3 * * *")` default 30 kun (`hemis.outbox.retention.days:30`).
- `KafkaTopicConfig` — topic deklaratsiya, retention 30d (DLQ 90d).
- `OutboxAdminService`, `OutboxMetrics`, `OutboxPublishException`.

### `employee/` — Univer inbound sync consumer

- `EmployeeSyncConsumer` — `@KafkaListener` concurrency = 12 (= topic partition soni; bir xil PINFL bir partition → serial, optimistic-lock collision yo'q). **deserialize xato → Sentry FATAL** (poison pill, retry foydasi yo'q) → throw → DefaultErrorHandler DLQ. **process xato → Sentry** + throw → retry → DLQ.
- `EmployeeSyncProcessor` (1 message = 1 transaction = 1 row), `EmployeeSyncProducer`, `EmployeeSyncMetrics`.

### `integration/` — davlat/OTM REST klientlar

> **DIQQAT:** `UniverApiService` nomli klass **YO'Q**. Univer/gov REST klientlari:

- `HemisApiService`, `LegacyOtmIntegrationService`, `GatewayService` — OTM/legacy integratsiya.
- `ApiMspdClient` + `ApiMspdTokenService` — MSPD.
- `GuvdTokenService` — GUVD token.
- `LegacyBimmTokenService` — BIMM token.
- `ExternalIntegrationService`, `EmploymentIntegrationService`.

### Sentry konvensiyasi (5 fayl)

`Sentry.captureException(err, scope -> {...})` — `WebhookRetryScheduler`, `WebhookDispatcher`, `WebhookFanoutConsumer`, `EmployeeSyncConsumer`, `OutboxPoller`. `scope.setTag("phase", ...)` bilan markirovka. **PII (PINFL) tag/extra'ga YOZILMAYDI** — faqat partition/offset/universityCode kabi non-PII identifikatorlar.

---

## Logging Standards

```java
// ✓ TO'G'RI — structured key=value
log.info("Student created: id={}, pinfl={}, facultyId={}",
         student.getId(), student.getPinfl(), student.getFaculty().getId());

// ✗ XATO — string concat, hard to parse
log.info("Student " + student.getId() + " created");

// ✗ MUTLAQO TAQIQLANGAN — PII log'da
log.info("Login attempt: username={}, password={}", user, pass);  // PASSWORD!
log.info("User: {}", user);  // toString() PII chiqarishi mumkin
```

**Log levels:**
- `DEBUG` — dev only (SQL, detailed flow)
- `INFO` — business events (create, update, delete)
- `WARN` — recoverable (fallback, retry, validation)
- `ERROR` — exceptions, integration failures

**PII taqiqi:** PINFL, parol, JWT token, telefon, email, address — log'ga yozilmasligi shart.

---

## PR Checklist (service)

- [ ] `@Service` + `@RequiredArgsConstructor` + `@Slf4j`
- [ ] Class `@Transactional(readOnly=true)`, write methodga `@Transactional` override
- [ ] Public method `@PreAuthorize` permission
- [ ] Hech qanday `null` return — `ResourceNotFoundException` yoki `Optional` (faqat repository)
- [ ] Validation Bean Validation + business rule check
- [ ] `@Cacheable` qo'shilsa, mutation'da `@CacheEvict`
- [ ] AOP self-invocation trap'i tekshirilgan
- [ ] External call'da timeout aniq belgilangan (RestClient request factory)
- [ ] Logging structured, PII yo'q
- [ ] Unit test (Mockito): success + error + edge case (Given-When-Then)
- [ ] Integration test (`@SpringBootTest`): real flow
- [ ] Coverage ≥ 90% (service layer)

---

## See Also
- `../.claude/MANDATORY_REQUIREMENTS.md` — Service test misollari
- `../.claude/architecture.md` — Cache + auth architecture
