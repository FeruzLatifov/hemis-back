# api-web module — Modern REST API

> **Markaziy vazirlik web frontend** (yagona React app) uchun. **Mijozlari:** ADMIN (S038'gacha MINISTRY_ADMIN) va OTM_API roles — markaziy HEMIS-back orqali ishlaydi.
>
> Per-OTM frontend deploy YO'Q — bitta vazirlik web app, OTM admin'lar `university_code` filter bilan o'z scope'ini ko'radi.
>
> **Sof REST + JSON, Swagger to'liq, ResponseWrapper.** URL: `/api/v1/web/*`
>
> **Base package:** `uz.hemis.web.controller` (controllerlar), `uz.hemis.web.service` (DTO mapping + auth), `uz.hemis.web.dto`.

---

## Modul holati (kod bilan tasdiqlangan)

- **19 controller / 124 endpoint / 101 `@PreAuthorize`** (`uz.hemis.web.controller.*`).
- **Admin/maxsus controllerlar:**
  - `OutboxAdminController` → `/api/v1/web/admin/outbox`
  - `WebhookTargetController` → `/api/v1/web/admin/webhooks`
  - `AuditLogController` → `/api/v1/web/audit`
  - `DashboardController` → `/api/v1/web/dashboard`
- **Auth = JWT HTTPOnly cookie:** `accessToken`/`refreshToken` cookie'lar `SameSite` bilan `WebAuthCookieService` orqali set/clear qilinadi (`Authorization: Bearer` header EMAS, asosiy oqim cookie).
- **MapStruct YO'Q** — entity↔DTO mapping `uz.hemis.web.service.*` ichida qo'lda yoziladi (service'ga delegate).
- **POI dependency** (`org.apache.poi:poi:5.5.1`, `poi-ooxml`) `build.gradle.kts`'da deklaratsiya qilingan, lekin hozircha kodda **ishlatilmagan** (Excel export rejada).

---

## TOP Senior REST Patterns

### 1. ResponseWrapper — barcha response uchun

Real strukturasi: `{success, message, data, error}` (4 maydon, `@JsonInclude(NON_NULL)`).
Tafsilot: [`common/CLAUDE.md` ResponseWrapper bo'limi](../common/CLAUDE.md).

```java
// ✓ Success (data bilan)
{
  "success": true,
  "data": { ... }
}

// ✓ Success (xabar bilan)
{
  "success": true,
  "message": "Student created successfully",
  "data": { "id": 1, "firstName": "Jane" }
}

// ✓ Error (oddiy xabar)
{
  "success": false,
  "message": "Validation failed"
}

// ✓ Error (ErrorResponse tafsiloti bilan)
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "timestamp": "2026-05-02T10:30:00Z",
    "status": 400,
    "error": "VALIDATION_ERROR",
    "message": "PINFL must be 14 digits",
    "path": "/api/v1/web/students"
  }
}

// ✓ Paginated — Spring Page<T> to'g'ridan-to'g'ri data ichida
{
  "success": true,
  "data": {
    "content": [...],
    "number": 0,
    "size": 20,
    "totalElements": 1500,
    "totalPages": 75
  }
}
```

```java
// Controller pattern
@GetMapping("/{id}")
public ResponseEntity<ResponseWrapper<StudentDto>> findById(@PathVariable Long id) {
    StudentDto dto = service.findById(id);
    return ResponseEntity.ok(ResponseWrapper.success(dto));
}
```

---

### 2. HTTP Status Code — Semantic to'g'ri

| Status | Qachon |
|--------|--------|
| `200 OK` | Success GET, PUT, DELETE |
| `201 Created` | Success POST (resource created) |
| `204 No Content` | Success DELETE (no body) |
| `400 Bad Request` | Validation error (Bean Validation) |
| `401 Unauthorized` | Token yo'q yoki noto'g'ri |
| `403 Forbidden` | Token bor, lekin permission yo'q |
| `404 Not Found` | Resource topilmadi |
| `409 Conflict` | Duplikat (unique constraint) |
| `422 Unprocessable` | Business rule violation (validation o'tdi, lekin ish bajarib bo'lmaydi) |
| `429 Too Many` | Rate limit |
| `500 Server Error` | Bug, log + Sentry |
| `503 Service Unavailable` | Dependency down (DB, Redis), circuit open |

```java
// ✓ TO'G'RI
@PostMapping
public ResponseEntity<ResponseWrapper<StudentDto>> create(@Valid @RequestBody StudentCreateDto dto) {
    StudentDto created = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
}

// ✗ XATO — har doim 200 qaytaradi
return ResponseEntity.ok(...);  // POST uchun 201 kerak
```

---

### 3. Validation — controller boundary

```java
// ✓ TO'G'RI
@PostMapping
public ResponseEntity<ResponseWrapper<StudentDto>> create(
    @Valid @RequestBody StudentCreateDto dto  // @Valid avto-validate
) { ... }

// DTO record (Java 25)
public record StudentCreateDto(
    @NotBlank(message = "{validation.pinfl.required}")
    @Pattern(regexp = "\\d{14}", message = "{validation.pinfl.format}")
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

**Effect:** `@Valid` failsa → `MethodArgumentNotValidException` → `@RestControllerAdvice` → 400 + structured error.

---

### 4. Pagination + Sorting

```java
@GetMapping
public ResponseEntity<ResponseWrapper<Page<StudentDto>>> list(
    @RequestParam(required = false) Long facultyId,
    @RequestParam(required = false) String search,
    @PageableDefault(size = 20, sort = "lastName") Pageable pageable
) {
    Page<StudentDto> page = service.search(facultyId, search, pageable);
    return ResponseEntity.ok(ResponseWrapper.success(page));
}
```

**URL:** `/api/v1/web/students?page=0&size=20&sort=lastName,asc&facultyId=1`

**Pageable trap:** sort parameter abuse hujum'i — column ro'yxatini whitelist qilish:
```java
private static final Set<String> ALLOWED_SORTS = Set.of("id", "firstName", "lastName", "createdAt");

public Page<StudentDto> search(..., Pageable pageable) {
    Sort safeSort = sanitizeSort(pageable.getSort(), ALLOWED_SORTS);
    Pageable safe = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    ...
}
```

---

### 5. `@PreAuthorize` har endpoint'da

```java
@RestController
@RequestMapping("/api/v1/web/students")
public class StudentController {

    @GetMapping
    @PreAuthorize("hasAuthority('students.view')")
    public ResponseEntity<...> list(...) { ... }

    @PostMapping
    @PreAuthorize("hasAuthority('students.create')")
    public ResponseEntity<...> create(...) { ... }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('students.edit') and @studentSecurity.canEdit(#id, authentication)")
    public ResponseEntity<...> update(@PathVariable Long id, ...) { ... }
}
```

**`@studentSecurity.canEdit(...)`:** custom SpEL bean — university-scoped check (UNIVERSITY_ADMIN faqat o'z universiteti talabasini ko'radi).

---

### 6. Idempotency Key (POST/PUT) — ⚠️ REJADA, IMPLEMENT QILINMAGAN

> **Status:** Hozircha **kodda YO'Q**. `IdempotencyService` / idempotency-key handling repoda mavjud emas —
> hech qaysi controller `Idempotency-Key` header'ni o'qib duplikatni oldini olmaydi. Quyidagi snippet —
> kelajakda qo'shilishi mumkin bo'lgan **maqsadli pattern** (target), joriy holat emas. Yangi POST yozayotganda
> bu pattern majburiy emas; kerak bo'lsa avval `IdempotencyService` qo'shilishi kerak.

```java
// MAQSADLI PATTERN (hozircha mavjud emas — idempotencyService yozilishi kerak)
@PostMapping
@PreAuthorize("hasAuthority('students.create')")
public ResponseEntity<...> create(
    @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
    @Valid @RequestBody StudentCreateDto dto
) {
    if (idempotencyKey != null) {
        Optional<StudentDto> cached = idempotencyService.get(idempotencyKey);
        if (cached.isPresent()) return ResponseEntity.ok(ResponseWrapper.success(cached.get()));
    }
    StudentDto created = service.create(dto);
    if (idempotencyKey != null) {
        idempotencyService.put(idempotencyKey, created, Duration.ofHours(24));
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
}
```

---

### 7. Swagger — to'liq annotations

```java
@RestController
@RequestMapping("/api/v1/web/students")
@Tag(name = "Students", description = "Student management operations")
public class StudentController {

    @Operation(
        summary = "Get student by ID",
        description = "Returns student detail with faculty and curriculum"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student found",
            content = @Content(schema = @Schema(implementation = StudentDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden — lacks 'students.view'"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('students.view')")
    public ResponseEntity<ResponseWrapper<StudentDto>> findById(
        @Parameter(description = "Student ID", required = true, example = "1")
        @PathVariable Long id
    ) { ... }
}
```

**Swagger UI:** `http://localhost:8081/swagger-ui.html` (server context-path `/`, prefiks YO'Q).

---

### 8. CORS + Rate Limiting

```yaml
# application.yml
hemis:
  cors:
    allowed-origins:
      - https://hemis.uz
      - https://*.hemis.uz
    allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
    allowed-headers: [Authorization, Content-Type, Idempotency-Key]
    max-age: 3600

  # Rate limit konfiguratsiyasi — canonical: security/CLAUDE.md "Rate Limiting" bo'limi
  # Bu yerda faqat istalsa override misoli (per-module override emas, hozir global config).
```

**Filter:** `RateLimitFilter` (`security/filter/`). To'liq misol va 429 response shape: `@security/CLAUDE.md`.

---

### 9. Long-running Endpoint — Async

5+ sekund operatsiyalar (Excel report, bulk import):

```java
// ✓ TO'G'RI — async + status polling
@PostMapping("/reports/generate")
public ResponseEntity<...> generateReport(@Valid @RequestBody ReportRequest req) {
    String jobId = reportService.startAsync(req);
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .header("Location", "/api/v1/web/jobs/" + jobId)
        .body(ResponseWrapper.success(Map.of("jobId", jobId, "status", "PENDING")));
}

@GetMapping("/jobs/{jobId}")
public ResponseEntity<...> jobStatus(@PathVariable String jobId) {
    JobStatus status = jobService.get(jobId);
    return ResponseEntity.ok(ResponseWrapper.success(status));
}
```

**Sabab:** HTTP timeout 30s default. Long-running blocking → tarmoq xatosi.

---

### 10. Sensitive Field Filtering

```java
// ✓ TO'G'RI — DTO'da sensitive field yo'q
public record StudentDto(
    Long id,
    String firstName,
    String lastName,
    String maskedPinfl,  // "12345678****"
    String facultyName
    // PINFL to'liq — separate endpoint /admin only
) {}

// PINFL maskirovka helper
public static String maskPinfl(String pinfl) {
    return pinfl == null ? null : pinfl.substring(0, 8) + "****";
}
```

---

## Controller + Exception Handler

> Batafsil misollar: [`MANDATORY_REQUIREMENTS.md`](../.claude/MANDATORY_REQUIREMENTS.md) "Complete Feature Example" bo'limi.

**Controller pattern:**
- `@RestController` + `@RequestMapping("/api/v1/web/<resource>")`
- `@PreAuthorize("hasAuthority('<resource>.<action>')")` har endpoint
- Custom SpEL scope check: `@PreAuthorize("... and @<entity>Security.canEdit(#id, authentication)")`
- `@Valid @RequestBody` + `@PageableDefault(size=20, sort="...")`
- Return `ResponseEntity<ResponseWrapper<T>>` har doim

(Eslatma: `Idempotency-Key` handling hozircha implement qilinmagan — §6 ga qarang.)

**Exception handling — ikki bosqichli:** common `GlobalExceptionHandler` markazlashgan (umumiy xatolar:
`ResourceNotFoundException`, `BusinessRuleException`, `AccessDeniedException`, `Exception` fallback va h.k.).
`WebExceptionHandler` (`@RestControllerAdvice(basePackages = "uz.hemis.web.controller")`) faqat **web-spetsifik**
xatolarni override qiladi — auth, optimistic-lock, data-integrity, validation; qolgani common handler'ga delegate.

`WebExceptionHandler` (`uz.hemis.web.controller.exception.WebExceptionHandler`) handle qiladigan xatolar:

| Exception | HTTP | error code |
|-----------|------|-----------|
| `UsernameNotFoundException`, `BadCredentialsException` | 401 | `AUTH_FAILED` (localized + Sentry) |
| `OptimisticLockException`, `ObjectOptimisticLockingFailureException` | 409 | `OPTIMISTIC_LOCK_CONFLICT` |
| `DataIntegrityViolationException` (FK/CHECK/UNIQUE) | 400 | `DATA_INTEGRITY_VIOLATION` |
| `MethodArgumentNotValidException` (@Valid body) | 400 | `VALIDATION_FAILED` + field details |
| `ConstraintViolationException` (path/param @Validated) | 400 | `CONSTRAINT_VIOLATION` |

> Boshqa barcha xatolar (404, 422, 403, 500, …) common `GlobalExceptionHandler`'da markazlashgan —
> bu yerda takrorlanmaydi.

---

## PR Checklist (api-web)

- [ ] `@RestController` + `@RequestMapping` URL: `/api/v1/web/*`
- [ ] Har endpoint `@PreAuthorize` permission
- [ ] DTO record (Java 25) + Bean Validation annotations
- [ ] `@Valid @RequestBody` POST/PUT'da
- [ ] HTTP status to'g'ri (200/201/204/400/404/409)
- [ ] `ResponseWrapper<T>` har response'da
- [ ] Swagger: `@Tag`, `@Operation`, `@ApiResponses`, `@Parameter`
- [ ] Pagination: `Pageable` + `@PageableDefault`
- [ ] Sort whitelist (column abuse'dan himoya)
- [ ] Long-running (5+s) async pattern
- [ ] Sensitive field DTO'da yo'q (yoki masked)
- [ ] Integration test: success + 400 + 401 + 403 + 404 + 409
- [ ] Service'da business logic, controller'da YO'Q

---

## See Also
- `../service/CLAUDE.md` — Business logic patterns
- `../.claude/MANDATORY_REQUIREMENTS.md` — Test misollari
- `../security/CLAUDE.md` — Auth patterns
