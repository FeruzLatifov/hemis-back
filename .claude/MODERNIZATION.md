# Spring Boot 4 / Spring 6 / Java 25 Modernization Guide

> **Manual reference** — yangi kod yozayotganda batafsil misollar uchun.
> **Qisqa qoidalar** `rules.md` ichida (canonical). Bu fayl misollar va migration patternlarini saqlaydi.

---

## Spring Boot 4.x API o'zgarishlari

### `@MockBean` → `@MockitoBean`

`@MockBean` Spring Boot 3.4 da deprecated, 4.x da to'liq olib tashlanishi mumkin.

```java
// ✗ Eski (deprecated)
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class ControllerTest {
    @MockBean private StudentService service;
}

// ✓ Modern (Spring 6.2+)
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ControllerTest {
    @MockitoBean private StudentService service;
}
```

**Migration:** Yangi testlar `@MockitoBean` bilan. Eski testlarni sprint'larda almashtirish.

### `RestTemplate` → `RestClient` (Spring 6.1+)

`AbstractGovernmentApiService`, `RestTemplateConfig` — RestTemplate (eski). Modern:

```java
// ✗ Eski — RestTemplate
ResponseEntity<Map> response = restTemplate.exchange(
    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

// ✓ Modern — RestClient (fluent, type-safe)
@Bean
public RestClient governmentApiClient() {
    return RestClient.builder()
        .baseUrl("https://student.hemis.uz")
        .defaultHeader("Authorization", "Bearer " + token)
        .build();
}

PersonalDataDto data = client.get()
    .uri("/api/persons/{pinfl}", pinfl)
    .retrieve()
    .body(PersonalDataDto.class);
```

**Foyda:** Type-safe, fluent, testable, Spring 6.1+ standart.

### `JdbcTemplate` → `JdbcClient` (Spring 6.1+)

```java
// ✗ Eski
List<Student> students = jdbcTemplate.query(
    "SELECT * FROM hemishe_e_student WHERE faculty_id = ?",
    new Object[]{facultyId}, studentRowMapper);

// ✓ Modern
List<Student> students = jdbcClient.sql("""
        SELECT * FROM hemishe_e_student
        WHERE faculty_id = :facultyId AND delete_ts IS NULL
        """)
    .param("facultyId", facultyId)
    .query(Student.class)
    .list();
```

### HikariCP Leak Detection

```yaml
# application.yml master pool
spring:
  datasource:
    hikari:
      leak-detection-threshold: 60000   # 60s — leak log + stack trace
      maximum-pool-size: 30
      minimum-idle: 10
      connection-timeout: 5000
      max-lifetime: 1800000
      idle-timeout: 600000
```

### PostgreSQL `statement_timeout` (har profilda)

```yaml
spring:
  datasource:
    hikari:
      data-source-properties:
        options: "-c statement_timeout=30000"  # 30s dev, 60s prod
```

Yoki per-role:
```sql
ALTER ROLE hemis_dev SET statement_timeout = '30s';
ALTER ROLE hemis_app SET statement_timeout = '60s';
```

### `@Value` → `@ConfigurationProperties`

Yangi config har doim `@ConfigurationProperties`. 63 ta `@Value` sprint'larda almashtiriladi.

---

## Java 25 Modern Features

### Records (DTO uchun)

```java
// ✗ Lombok @Data (boilerplate)
@Data
@AllArgsConstructor
public class StudentDto {
    private UUID id;
    private String firstName;
    // ...
}

// ✓ Java 25 Record (immutable, type-safe)
public record StudentDto(
    UUID id,
    String firstName,
    String lastName,
    String email,
    UUID facultyId
) {}
```

**Foyda:** Compact, immutable, equals/hashCode auto, JSON serialization native.

### Pattern Matching for switch (JEP 441)

```java
// ✗ Eski
String describe(Object obj) {
    if (obj instanceof Integer i) return "Integer: " + i;
    else if (obj instanceof String s) return "String: " + s;
    else return "Unknown";
}

// ✓ Modern (Java 21+)
String describe(Object obj) {
    return switch (obj) {
        case Integer i -> "Integer: " + i;
        case String s when s.isEmpty() -> "Empty string";
        case String s -> "String: " + s;
        case null -> "null";
        default -> "Unknown";
    };
}
```

### Sealed Classes (Closed Hierarchies)

```java
public sealed interface ApiResult<T>
    permits ApiSuccess, ApiError {}

public record ApiSuccess<T>(T data) implements ApiResult<T> {}
public record ApiError<T>(String code, String message) implements ApiResult<T> {}

// Pattern match exhaustive
String render(ApiResult<StudentDto> result) {
    return switch (result) {
        case ApiSuccess<StudentDto> s -> "Found: " + s.data().firstName();
        case ApiError<StudentDto> e -> "Error: " + e.message();
    };
}
```

### Virtual Threads (audit kerak)

```yaml
# application.yml — yoqishdan oldin audit
spring:
  threads:
    virtual:
      enabled: true   # Java 25 stable, lekin pinning audit qiling
```

**Audit checklist:**
- [ ] `synchronized` bloklar — JEP 491 (Java 24+) yo'q qildi, lekin tekshir
- [ ] JDBC operations — Hibernate 7 `ReentrantLock` ishlatadi (pinning yo'q)
- [ ] ThreadLocal usage — virtual thread'da OK, lekin context propagation tekshir

---

## See also

- `.claude/rules.md` — qisqa qoidalar (canonical)
- `.claude/MANDATORY_REQUIREMENTS.md` — to'liq Swagger + test misollari
- ADR-0002 — Java 25 LTS migration
