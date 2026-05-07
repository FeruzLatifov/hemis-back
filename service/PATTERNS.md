# service/PATTERNS.md — Kod Misollar

> **Manual reference** — `service/CLAUDE.md` qoidalarini batafsil misollar bilan to'ldiradi.

---

## 1. Spring AOP Self-Invocation Trap

```java
// ✗ XATO — same-class call proxy bypass
@Service
public class StudentService {
    public StudentDto getById(UUID id) {
        return loadFromDb(id);  // proxy bypass — cache yo'q
    }

    @Cacheable("students")
    public StudentDto loadFromDb(UUID id) { ... }
}

// ✓ TO'G'RI — alohida bean
@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentLoader loader;
    public StudentDto getById(UUID id) {
        return loader.loadById(id);  // proxy'dan o'tadi
    }
}

@Service
public class StudentLoader {
    @Cacheable(value = "students", key = "#id", unless = "#result == null")
    public StudentDto loadById(UUID id) { ... }
}
```

## 2. `@Transactional` scope

```java
// ✓ TO'G'RI — class read-only default, write override
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StudentService {
    public StudentDto findById(UUID id) { ... }   // read-only ✓ (replica)

    @Transactional  // override → write tx (master)
    public StudentDto create(StudentCreateDto dto) { ... }
}
```

## 3. `@Cacheable` + `@CacheEvict` pair

```java
@Service
@Transactional(readOnly = true)
public class FacultyService {

    @Cacheable(value = "faculties", key = "#id")
    public FacultyDto findById(UUID id) { ... }

    @Cacheable(value = "faculties:all", key = "'all'")
    public List<FacultyDto> findAll() { ... }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "faculties", key = "#dto.id"),
        @CacheEvict(value = "faculties:all", key = "'all'")
    })
    public FacultyDto update(FacultyUpdateDto dto) { ... }

    @Transactional
    @CacheEvict(value = {"faculties", "faculties:all"}, allEntries = true)
    public void delete(UUID id) { ... }
}
```

**Cache strategy:**
- Classifier (kam o'zgaradi) → `@Cacheable` 24h TTL
- Hot entity → 5-15 min TTL
- Mutable list → cache qilmang yoki `@CacheEvict allEntries=true`

## 4. Exception Hierarchy

```java
// ✗ XATO — null
public Student findById(UUID id) {
    return repository.findById(id).orElse(null);
}

// ✓ TO'G'RI — semantic exception
public Student findById(UUID id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Student not found with id: " + id));
}
```

**Exception → HTTP:**
- `ResourceNotFoundException` → 404
- `ValidationException` → 400
- `BusinessRuleException` → 422
- `ConflictException` → 409
- `UnauthorizedException` → 401

## 5. `@Valid` Bean Validation

```java
public StudentDto create(@Valid StudentCreateDto dto) {
    if (repository.existsByPinfl(dto.pinfl())) {
        throw new ConflictException("PINFL already exists: " + dto.pinfl());
    }
    ...
}

// DTO record (Java 25)
public record StudentCreateDto(
    @NotBlank @Pattern(regexp = "\\d{14}") String pinfl,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email String email,
    @NotNull UUID facultyId
) {}
```

## 6. MapStruct Mapping

```java
@Mapper(componentModel = "spring")
public interface StudentMapper {
    StudentDto toDto(Student entity);
    List<StudentDto> toDtoList(List<Student> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Student toEntity(StudentCreateDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Student entity, StudentUpdateDto dto);
}
```

> **Diqqat:** MapStruct compile-time codegen. `./gradlew clean build` qayta yaratadi.
> **api-legacy modul:** MapStruct ISHLATILMAYDI — `toMap()` + `LinkedHashMap` patterni (CUBA dynamic field). Tafsilot: `api-legacy/CLAUDE.md`.

## 7. `@Async` + Custom Executor

```java
// ✗ XATO — default executor
@Async
public void sendEmail(...) { ... }

// ✓ TO'G'RI — explicit executor
@Async("auditTaskExecutor")
public void recordAudit(AuditEvent event) { ... }
```

**Executor config:**
```java
executor.setCorePoolSize(2);
executor.setMaxPoolSize(5);
executor.setQueueCapacity(500);
executor.setRejectedExecutionHandler(new CallerRunsPolicy());  // backpressure
```

## 8. Pageable + Sorting

```java
@Cacheable(value = "studentsByFaculty",
           key = "#facultyId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
public Page<StudentDto> findByFacultyId(UUID facultyId, Pageable pageable) {
    if (!facultyRepository.existsById(facultyId)) {
        throw new ResourceNotFoundException("Faculty not found: " + facultyId);
    }
    return studentRepository.findByFacultyId(facultyId, pageable)
                           .map(studentMapper::toDto);
}
```

> Cache key'da Pageable to'liq emas, faqat `pageNumber + pageSize`.

## 10. Bulk Operations — JdbcTemplate batch

```java
public void bulkInsert(List<Student> students) {
    String sql = "INSERT INTO hemishe_e_student (...) VALUES (...)";
    jdbcTemplate.batchUpdate(sql, students, 100, (ps, s) -> {
        ps.setObject(1, s.getId());
        ps.setString(2, s.getPinfl());
        // ...
    });
}
```

`spring.jpa.properties.hibernate.jdbc.batch_size: 20` — JPA batch, lekin katta bulk uchun JDBC tezroq.

---

## Cache Stampede Prevention

### Caffeine `refreshAfterWrite` (afzal)

```java
@Bean
public Cache<String, Object> stampedeProtectedCache() {
    return Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(Duration.ofMinutes(15))
        .refreshAfterWrite(Duration.ofMinutes(10))  // 10-minutda asyncronus refresh
        .build();
}
```

### Distributed Lock (Redis-based)

```java
public StudentDto findByIdWithLock(UUID id) {
    String lockKey = "lock:student:" + id;
    Boolean acquired = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", Duration.ofSeconds(30));

    if (Boolean.TRUE.equals(acquired)) {
        try {
            // Cache miss — DB'dan olish va cache'ga qo'yish
            return loadAndCache(id);
        } finally {
            redisTemplate.delete(lockKey);
        }
    } else {
        // Boshqa instance refresh qilyapti — kut va cache'dan o'qi
        Thread.sleep(100);
        return cacheGetOrThrow(id);
    }
}
```

### Qaysi yondashuv

| Kontekst | Yondashuv |
|----------|-----------|
| Hot entity, single instance | Caffeine refreshAfterWrite |
| Hot entity, 3 instance cluster | Caffeine + Redis distributed lock |
| Klassifikator (kam o'zgaradi) | Caffeine 24h TTL |

---

## See also

- `service/CLAUDE.md` — qisqa qoidalar
- `.claude/rules.md` — cache invariant
- `.claude/agents/cache-strategist.md` — automated cache audit
- `api-external/CLAUDE.md` — external integration timeout (canonical)
