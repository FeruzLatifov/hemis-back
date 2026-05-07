# service module — Business Logic

> **Markaziy HEMIS-back** business logic qatlami. 230 OTM aggregation + qoidalar joriy qilish (talaba kiritish lock, baho lock) + davlat integratsiya orchestration shu yerda.
>
> Spring `@Service` katlamı. **Hech qanday business logic controller yoki repository'da bo'lmaydi.**
> Bu yerda transaction boundary, cache, validation, exception orchestration.

---

## TOP 10 Service Layer Senior Patterns

### 1. Spring AOP Self-Invocation Trap — `@Cacheable`, `@Transactional`, `@Async`

Same-class method call **proxy'dan o'tmaydi**. Annotation ishlamaydi.

```java
// ✗ XATO
@Service
public class StudentService {
    public StudentDto getById(Long id) {
        return loadFromDb(id);  // proxy bypass — cache yo'q
    }

    @Cacheable("students")
    public StudentDto loadFromDb(Long id) { ... }
}

// ✓ TO'G'RI — alohida bean
@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentLoader loader;  // alohida proxy

    public StudentDto getById(Long id) {
        return loader.loadById(id);  // proxy'dan o'tadi → cache ishlaydi
    }
}

@Service
public class StudentLoader {
    @Cacheable(value = "students", key = "#id", unless = "#result == null")
    public StudentDto loadById(Long id) { ... }
}
```

**Real misol:** `ClassifierReferenceLoader` (private method'dan extract qilingan).

---

### 2. `@Transactional` to'g'ri scope

```java
// ✗ XATO — class-level read-only, lekin write method ham bor
@Service
@Transactional(readOnly = true)
public class StudentService {
    public Student create(Dto dto) { ... }  // read-only tx → write fail
}

// ✓ TO'G'RI — class read-only default, write override
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudentService {
    public StudentDto findById(Long id) { ... }  // read-only ✓

    @Transactional  // override → write tx (replica → master)
    public StudentDto create(StudentCreateDto dto) { ... }
}
```

**Effect:** `@Transactional(readOnly=true)` → DB replica'ga yo'naltiradi (load balance).

---

### 3. `@Cacheable` + `@CacheEvict` pair

Har `@Cacheable` uchun mutation method'da `@CacheEvict` MAJBURIY. Aks holda stale data.

```java
// ✓ TO'G'RI
@Service
@Transactional(readOnly = true)
public class FacultyService {

    @Cacheable(value = "faculties", key = "#id")
    public FacultyDto findById(Long id) { ... }

    @Cacheable(value = "faculties:all", key = "'all'")
    public List<FacultyDto> findAll() { ... }

    // Mutation — cache evict
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "faculties", key = "#dto.id"),
        @CacheEvict(value = "faculties:all", key = "'all'")
    })
    public FacultyDto update(FacultyUpdateDto dto) { ... }

    @Transactional
    @CacheEvict(value = {"faculties", "faculties:all"}, allEntries = true)
    public void delete(Long id) { ... }
}
```

**Cache strategy:**
- Classifier (kam o'zgaradi) → `@Cacheable` 24h TTL
- Hot entity (tez-tez o'qiladi, kam o'zgaradi) → 5-15 min TTL
- Mutable list → cache qilmang (yoki `@CacheEvict allEntries=true`)

---

### 4. Exception Hierarchy — `null` qaytarmaslik

```java
// ✗ XATO — caller tekshirishni unutadi
public Student findById(Long id) {
    return repository.findById(id).orElse(null);
}

// ✗ XATO — generic Exception
public Student findById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new RuntimeException("Not found"));
}

// ✓ TO'G'RI — semantic exception
public Student findById(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Student not found with id: " + id));
}
```

**Custom exception hierarchy:**
- `ResourceNotFoundException` → 404
- `ValidationException` → 400
- `BusinessRuleException` → 422 (Unprocessable)
- `ConflictException` → 409
- `UnauthorizedException` → 401

`@RestControllerAdvice` global handler bularni HTTP'ga aylantiradi.

---

### 5. `@Valid` + Bean Validation at boundary

Validation **service boundary'da**, repository emas.

```java
@Service
@Transactional
public class StudentService {

    public StudentDto create(@Valid StudentCreateDto dto) {  // @Valid majburiy
        // Bean Validation @NotBlank, @Email, @Pattern avtomatik check
        // Custom business rule:
        if (repository.existsByPinfl(dto.pinfl())) {
            throw new ConflictException("PINFL already exists: " + dto.pinfl());
        }
        ...
    }
}
```

**DTO record (Java 25):**
```java
public record StudentCreateDto(
    @NotBlank @Pattern(regexp = "\\d{14}") String pinfl,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email String email,
    @NotNull Long facultyId
) {}
```

---

### 6. MapStruct Mapping — boilerplate'siz

```java
@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDto toDto(Student entity);

    List<StudentDto> toDtoList(List<Student> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Student toEntity(StudentCreateDto dto);

    // Partial update (PUT/PATCH)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Student entity, StudentUpdateDto dto);
}
```

**Diqqat:** MapStruct compile-time codegen. `./gradlew clean build` qayta yaratadi.

---

### 7. `@Async` + Custom Executor

```java
// ✗ XATO — default executor (single-thread, queue unbounded)
@Async
public void sendEmail(...) { ... }

// ✓ TO'G'RI — explicit executor
@Async("auditTaskExecutor")
public void recordAudit(AuditEvent event) { ... }
```

**Audit executor config (`AsyncConfig`):**
```java
executor.setCorePoolSize(2);
executor.setMaxPoolSize(5);
executor.setQueueCapacity(500);
executor.setRejectedExecutionHandler(new CallerRunsPolicy());  // backpressure
```

**Virtual thread (Java 21+):** Pure I/O uchun considering, lekin avval `synchronized` audit kerak (pinning).

---

### 8. Pageable + Sorting — controller'dan pass

```java
// ✓ TO'G'RI — service Pageable qabul qiladi
@Cacheable(value = "studentsByFaculty",
           key = "#facultyId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
public Page<StudentDto> findByFacultyId(Long facultyId, Pageable pageable) {
    if (!facultyRepository.existsById(facultyId)) {
        throw new ResourceNotFoundException("Faculty not found: " + facultyId);
    }
    return studentRepository.findByFacultyId(facultyId, pageable)
                           .map(studentMapper::toDto);
}
```

**Cache key'da `Pageable` to'liq emas, faqat `pageNumber + pageSize`** (Sort bilan kalit ko'p variant).

---

### 9. External Integration — Timeout aniq belgilanishi

External RestClient/RestTemplate uchun connect+read timeout MAJBURIY. To'liq misol va Resilience4j strategiya: **canonical `@api-external/CLAUDE.md`** "Outbound RestClient" bo'limida.

Qisqa qoida: `SimpleClientHttpRequestFactory` bilan `setConnectTimeout(10_000)` + `setReadTimeout(30_000)`. Service modul'da yangi external client kerak bo'lsa — `api-external/.../config/` ga qo'yiladi (modul boundary).

---

### 10. Bulk Operations — JdbcTemplate batch

JPA `saveAll()` 1000 talaba uchun = 1000 INSERT. JDBC batch:

```java
// ✓ TO'G'RI — batch insert
public void bulkInsert(List<Student> students) {
    String sql = "INSERT INTO hemishe_e_student (...) VALUES (...)";
    jdbcTemplate.batchUpdate(sql, students, 100, (ps, s) -> {
        ps.setLong(1, s.getId());
        ps.setString(2, s.getPinfl());
        // ...
    });
}
```

**`spring.jpa.properties.hibernate.jdbc.batch_size: 20`** — JPA uchun batch yoqilgan, lekin katta bulk uchun JDBC tezroq.

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

Bizdagi 2-level cache (Caffeine L1 + Redis L2):

```java
// Cache name → TTL mapping
"classifierEducationType"     → 24h  (kam o'zgaradi)
"hokimiyatClassifiers"        → 24h
"studentGpa"                  → 1h   (o'rtacha)
"users"                       → 30m  (o'zgaradi)
"sessions"                    → 12h  (JWT lifetime)
"permissions"                 → 1h   (RBAC)
"menus"                       → 6h   (UI menyu)
```

**Yangi cache qo'shilganda:**
1. `DashboardCacheConfig.TwoLevelCacheManager` da TTL config
2. `@Cacheable` annotation
3. Mutation method'da `@CacheEvict`
4. Test (cache hit/miss/evict)

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
