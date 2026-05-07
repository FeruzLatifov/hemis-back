# api-web module — Modern REST API

> **Markaziy vazirlik web frontend** (yagona React app) uchun. **Mijozlari:** MINISTRY_ADMIN va UNIVERSITY_ADMIN roles — markaziy HEMIS-back orqali ishlaydi.
>
> Per-OTM frontend deploy YO'Q — bitta vazirlik web app, OTM admin'lar `university_code` filter bilan o'z scope'ini ko'radi.
>
> **Sof REST + JSON, Swagger to'liq, ResponseWrapper.** URL: `/api/v1/web/*`

---

## TOP Senior REST Patterns

### 1. ResponseWrapper — barcha response uchun

```java
// ✓ Success
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-05-02T10:30:00Z"
}

// ✓ Error
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "PINFL must be 14 digits",
    "details": [
      { "field": "pinfl", "code": "Pattern", "message": "..." }
    ]
  },
  "timestamp": "2026-05-02T10:30:00Z"
}

// ✓ Paginated
{
  "success": true,
  "data": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 1500,
    "totalPages": 75
  },
  "timestamp": "..."
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

### 6. Idempotency Key (POST/PUT)

Tarmoq xatosi → klient retry → duplikat yaratilmasligi uchun:

```java
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

**Swagger UI:** `http://localhost:8081/api/swagger-ui.html`

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

**Filter:** `RateLimitFilter` (`app/security/`). To'liq misol va 429 response shape: `@security/CLAUDE.md`.

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

## Controller Anatomy

```java
@RestController
@RequestMapping("/api/v1/web/students")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Students", description = "Student management")
public class StudentController {

    private final StudentService service;

    @Operation(summary = "List students with filters and pagination")
    @ApiResponses({...})
    @GetMapping
    @PreAuthorize("hasAuthority('students.view')")
    public ResponseEntity<ResponseWrapper<Page<StudentDto>>> list(
        @Parameter(description = "Faculty filter") @RequestParam(required = false) Long facultyId,
        @Parameter(description = "Name search") @RequestParam(required = false) String search,
        @PageableDefault(size = 20, sort = "lastName") Pageable pageable
    ) {
        Page<StudentDto> page = service.search(facultyId, search, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(page));
    }

    @Operation(summary = "Create new student")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Validation"),
        @ApiResponse(responseCode = "409", description = "Duplicate PINFL")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('students.create')")
    public ResponseEntity<ResponseWrapper<StudentDto>> create(
        @Valid @RequestBody StudentCreateDto dto,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        StudentDto created = service.createIdempotent(dto, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }
}
```

---

## Global Exception Handler

```java
@RestControllerAdvice(basePackages = "uz.hemis.api.web")
@Slf4j
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ResponseWrapper.error("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> details = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> new FieldError(fe.getField(), fe.getCode(), fe.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest()
            .body(ResponseWrapper.error("VALIDATION_ERROR", "Validation failed", details));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseWrapper<Void>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ResponseWrapper.error("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
            .body(ResponseWrapper.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

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
