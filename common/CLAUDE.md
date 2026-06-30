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

**Eslatma:** `record` afzal (immutable, concise), **lekin** mavjud kod ko'pincha `@Data` class
(Jackson/Lombok legacy moslik). Hozirgi holat: **23 record vs 58 class**, 51 fayl `@Data`. Yangi
DTO uchun `record`'ni boshlang'ich tanlov sifatida ko'ring, eski class'larni majburan migratsiya qilmang.

---

## Exception Hierarchy

Umumiy base klass **YO'Q**. `exception/` paketda **5 ta** exception, hammasi to'g'ridan-to'g'ri `RuntimeException`'dan extend qiladi + `ExceptionHandlerUtils` helper:

```
RuntimeException
├── BadRequestException         → HTTP 400  (message / message+cause)
├── ValidationException         → HTTP 400  (errors: Map<String,String> field-level)
├── BusinessRuleException       → HTTP 422  (ruleCode field, ADR-0013 rules engine)
├── ConflictException           → HTTP 409  (duplicate PINFL / OTM code / webhook target)
└── ResourceNotFoundException   → HTTP 404  (resourceName/fieldName/fieldValue yoki message)
```

```java
// 422 — biznes qoidasi buzilgan (input sintaktik to'g'ri)
public class BusinessRuleException extends RuntimeException {
    private final String ruleCode;  // OTM_CLOSED, GRADE_FINALIZED, ENROLLMENT_WINDOW_EXPIRED
    public BusinessRuleException(String ruleCode, String message) { ... }
    public String getRuleCode() { return ruleCode; }
}

// 400 — field-level xatolar bilan
public class ValidationException extends RuntimeException {
    private final Map<String, String> errors;
    public ValidationException(String message, Map<String, String> errors) { ... }
    public boolean hasErrors() { ... }
}
```

**Tafovut:** `ValidationException` (400) format/syntax · `BusinessRuleException` (422) biznes qoidasi · `ConflictException` (409) mavjud yozuv bilan to'qnashuv.

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

    // data'siz, faqat success message
    public static <T> ResponseWrapper<T> success(String message) {
        ResponseWrapper<T> r = new ResponseWrapper<>();
        r.setSuccess(true);
        r.setMessage(message);
        return r;
    }

    public static <T> ResponseWrapper<T> error(ErrorResponse error) {
        ResponseWrapper<T> r = new ResponseWrapper<>();
        r.setSuccess(false);
        r.setError(error);
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

> **Hozircha YO'Q (rejalashtirilgan).** `ApiConstants` / `SecurityConstants` kabi markaziy
> constant klasslar `common`'da hali mavjud emas — kerak bo'lganda konvensiya:
> `final` class + private constructor, barcha maydon `public static final`, SCREAMING_SNAKE_CASE.

---

## Datasource Routing Annotations

`datasource/` paket — **3 ta** tip (`DataSourceContextHolder` YO'Q; ThreadLocal context service modulida):

```java
// Markup annotation — service ishlatadi, aspect (service modulida) replica/master'ga yo'naltiradi
@Target({ElementType.METHOD, ElementType.TYPE})  // METHOD + TYPE (klass-level ham)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {}   // REPLICA

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteOnly {}  // MASTER

public enum DataSourceType { MASTER, REPLICA }
```

---

## Utilities, Value Objects & Validation

`PinflUtils` / `DateUtils` **YO'Q**. Real tarkib:

### util/ — log-safe helperlar

```java
// PII maskirovka (OWASP A09 — Logging Failures). PII log'da hech qachon plain chiqmaydi.
PiiMask.phone("+998901234567");  // → +998*****4567
PiiMask.email("john.doe@example.com");  // → j***@example.com
PiiMask.name("Aliyev Ali");  // → A*********

// Native SQL table-name guard (SQL injection defense-in-depth)
// Regex: ^(hemishe_[her]_[a-z][a-z0-9_]*|h_[a-z][a-z0-9_]*)$
SqlTableValidator.validateLegacyClassifier(tableName);  // SQL build'dan oldin
```

### vo/ — Value Objects + validation/ — Jakarta constraint juftlari

`vo/{Pinfl, Tin, PhoneNumber, DateRange}` — type-safe value object'lar.
Har biriga `validation/` da Jakarta constraint + validator jufti:

| VO | Constraint | Validator |
|----|-----------|-----------|
| `Pinfl` | `@ValidPinfl` | `PinflValidator` |
| `Tin` | `@ValidTin` | `TinValidator` |
| `PhoneNumber` | `@ValidPhoneNumber` | `PhoneNumberValidator` |

> PINFL maskirovka uchun `Pinfl.maskOrEmpty(String)` ishlatiladi (PiiMask emas).

---

## Notable packages

- `dto/webhook/` — `WebhookAckRequest`, `WebhookApplyResultDto`, `WebhookDeliveryLogDto`,
  `WebhookSecretResponse`, `WebhookTarget{Create,Update}Request`, `WebhookTargetDto` (ADR-0012 outbound webhook).
- `dto/employee/` — `EmployeeSyncEvent`, `EmployeeSyncDto`, `EmployeeSyncAcceptedResponse` (employee sync).
- `port/security/` — `UserLoadingPort`, `LegacyUserLoadingPort`, `PermissionLoadingPort`,
  `UserIdentificationPort`; `port/cache/` — `CachePort`, `CacheEvictionPort`, `DistributedCachePort`.
  Bular **interfeys** (hexagonal port): common implementatsiya saqlamaydi, faqat shartnoma (Spring'siz).

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
- [ ] Exception: `extends RuntimeException` (umumiy base klass yo'q)
- [ ] Unit test (no Spring context needed)
- [ ] No business logic — only data structures + utilities

---

## See Also
- `../service/CLAUDE.md` — How services use common DTOs
- `../.claude/rules.md` — DTO + record conventions
