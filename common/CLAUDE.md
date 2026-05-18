# common module — Shared DTOs, Exceptions, Utilities

> Markaziy HEMIS-back uchun shared DTOs/exceptions/utilities. Texnik jihatdan kontekst-neutral — barcha modullarda ishlatiladigan yagona tip kutubxonasi.
>
> **ZERO Spring dependency.** Pure Java + Lombok + Jackson.
> Boshqa modullar bog'lanadi, lekin common boshqasiga bog'lanmaydi.

---

## Module Boundaries

```
common can import:
  ✓ Java standard library
  ✓ Lombok (compile-time)
  ✓ Jackson (annotations)
  ✓ Jakarta Bean Validation (annotations only)
  ✓ Slf4j (interface only)

common CANNOT import:
  ✗ Spring (org.springframework.*)
  ✗ Hibernate / JPA
  ✗ Liquibase
  ✗ MapStruct (mapper'lar — domain'da)
  ✗ Any HTTP client
```

**Rule:** common = library. Spring boot context'siz ham compile bo'lishi kerak.

---

## DTO Patterns

### Java 25 Records — Default Choice

```java
// ✓ TO'G'RI — immutable, concise
public record StudentDto(
    Long id,
    String firstName,
    String lastName,
    String maskedPinfl,
    Long facultyId,
    String facultyName
) {}

// ✓ Bean Validation
public record StudentCreateDto(
    @NotBlank @Pattern(regexp = "\\d{14}", message = "{validation.pinfl.format}")
    String pinfl,

    @NotBlank @Size(max = 100)
    String firstName,

    @NotBlank @Size(max = 100)
    String lastName,

    @Email
    String email,

    @NotNull @Positive
    Long facultyId
) {}
```

### Class only when needed

Mutable state, builder, inheritance:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMutableDto {  // request/response with state
    private Long id;
    private String firstName;
}
```

**Eslatma:** `@Data` DTO'da OK (entity emas), lekin `record` afzal.

---

## Exception Hierarchy

```
RuntimeException
└── HemisException (base)
    ├── ResourceNotFoundException     → HTTP 404
    ├── ValidationException           → HTTP 400 (Bean Validation)
    ├── BusinessRuleException         → HTTP 422
    ├── ConflictException             → HTTP 409
    ├── UnauthorizedException         → HTTP 401
    ├── ForbiddenException            → HTTP 403
    ├── RateLimitException            → HTTP 429
    └── ExternalIntegrationException  → HTTP 502/503
```

```java
public class HemisException extends RuntimeException {
    private final String code;
    private final List<FieldError> details;

    public HemisException(String code, String message) {
        super(message);
        this.code = code;
        this.details = List.of();
    }

    public HemisException(String code, String message, List<FieldError> details) {
        super(message);
        this.code = code;
        this.details = details;
    }
}

public class ResourceNotFoundException extends HemisException {
    public ResourceNotFoundException(String entity, Object id) {
        super("RESOURCE_NOT_FOUND", entity + " not found with id: " + id);
    }
}
```

**Qoida:** Yangi exception qo'shsangiz, base'dan extend qiling. Generic `RuntimeException` taqiq.

---

## ResponseWrapper — API Response Structure

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"success", "message", "data", "error"})
public class ResponseWrapper<T> implements Serializable {

    private Boolean success;
    private String message;
    private T data;
    private ErrorResponse error;

    public static <T> ResponseWrapper<T> success(T data) {
        ResponseWrapper<T> r = new ResponseWrapper<>();
        r.setSuccess(true);
        r.setData(data);
        return r;
    }

    public static <T> ResponseWrapper<T> success(T data, String message) {
        ResponseWrapper<T> r = success(data);
        r.setMessage(message);
        return r;
    }

    public static <T> ResponseWrapper<T> error(String message) {
        ResponseWrapper<T> r = new ResponseWrapper<>();
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }

    public static <T> ResponseWrapper<T> error(String message, ErrorResponse error) {
        ResponseWrapper<T> r = error(message);
        r.setError(error);
        return r;
    }
}
```

**Diqqat — `record` emas, `class`:**
DTO uchun `record` afzal, lekin `ResponseWrapper` `@Data + @NoArgsConstructor + @AllArgsConstructor` (Lombok) bilan klass — Jackson legacy serializer'lar va `@JsonPropertyOrder` bilan moslashish uchun.

**ErrorResponse strukturasi** (alohida klass): `timestamp`, `status`, `error`, `message`, `path`, `details` maydonlari mavjud — JSON'da error paytida ko'rinadi.

**Pagination:** `ResponseWrapper`'da `page` maydon **YO'Q**. Sahifalangan natija uchun Spring `Page<T>` to'g'ridan-to'g'ri `data` ichida qaytariladi (`ResponseWrapper<Page<T>>`):

```java
return ResponseEntity.ok(ResponseWrapper.success(service.findAll(pageable)));
```

Klient `data.content`, `data.totalElements`, `data.number`, `data.size` maydonlarini Spring `Page` JSON'idan oladi.

**Sabab:** common = pure-Java library. Spring bog'liqligi bo'lsa, `common`'ni boshqa
kontekstda (masalan SDK / shartnoma loyihasida) ishlatish imkonsiz bo'ladi.

---

## Constants

```java
public final class ApiConstants {
    private ApiConstants() {}

    public static final String API_V1_WEB = "/api/v1/web";
    public static final String API_V1_EXTERNAL = "/api/v1/external";
    public static final String API_V1_UNIVERSITY = "/api/v1/university";
    public static final String API_LEGACY = "/app/rest/v2";

    public static final String HEADER_IDEMPOTENCY = "Idempotency-Key";
    public static final String HEADER_API_KEY = "X-API-Key";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
}

public final class SecurityConstants {
    private SecurityConstants() {}

    public static final int BCRYPT_STRENGTH = 12;  // OWASP 2025
    public static final long ACCESS_TOKEN_VALIDITY = 12 * 60 * 60;  // 12 hours
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60;  // 7 days

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
}
```

**Convention:**
- Class `final` + private constructor (instance yaratilmasligi uchun)
- All fields `public static final`
- Naming: SCREAMING_SNAKE_CASE

---

## Datasource Routing Annotations

```java
// Markup interface — service uses it; aspect (in service module) routes to replica
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteOnly {}

// ThreadLocal context (no Spring dep)
public class DataSourceContextHolder {
    private static final ThreadLocal<DataSourceType> CONTEXT = new ThreadLocal<>();

    public static void set(DataSourceType type) { CONTEXT.set(type); }
    public static DataSourceType get() { return CONTEXT.get(); }
    public static void clear() { CONTEXT.remove(); }
}

public enum DataSourceType { MASTER, REPLICA }
```

---

## Utilities

### PINFL Helper

```java
public final class PinflUtils {
    private PinflUtils() {}

    private static final Pattern PINFL_PATTERN = Pattern.compile("\\d{14}");

    public static boolean isValid(String pinfl) {
        return pinfl != null && PINFL_PATTERN.matcher(pinfl).matches();
    }

    public static String mask(String pinfl) {
        if (pinfl == null || pinfl.length() != 14) return pinfl;
        return pinfl.substring(0, 8) + "****";
    }
}
```

### Date Helper

```java
public final class DateUtils {
    private DateUtils() {}

    public static final ZoneId TASHKENT = ZoneId.of("Asia/Tashkent");

    public static LocalDateTime nowTashkent() {
        return LocalDateTime.now(TASHKENT);
    }

    public static Instant toInstant(LocalDateTime ldt) {
        return ldt.atZone(TASHKENT).toInstant();
    }
}
```

---

## What NOT to put in `common`

- ❌ Repository interfaces (domain'da)
- ❌ Entity classes (domain'da)
- ❌ MapStruct mappers (domain'da)
- ❌ Service interfaces with Spring beans (service'da)
- ❌ HTTP clients (api-external'da)
- ❌ Spring config (security/app'da)
- ❌ Anything that touches DB/Redis/HTTP at runtime

---

## PR Checklist (common)

- [ ] No `org.springframework.*` import
- [ ] No `jakarta.persistence.*` import
- [ ] No HTTP client import
- [ ] DTO: prefer `record` over class
- [ ] Bean Validation annotations only (no behavior)
- [ ] Constants: `final` class + private ctor + `public static final`
- [ ] Exception extends `HemisException`
- [ ] Unit test (no Spring context needed)
- [ ] No business logic — only data structures + utilities

---

## See Also
- `../service/CLAUDE.md` — How services use common DTOs
- `../.claude/rules.md` — DTO + record conventions
