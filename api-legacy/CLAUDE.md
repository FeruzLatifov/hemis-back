# api-legacy module — CUBA Platform Compatibility

> **CUBA Platform 7.3 bilan 100% backward compatibility.** Format buzilishi = production'da xato.
> Eski Univer Yii2 PHP frontend + 3rd-party clientlar bu API'ni ishlatadi.
>
> **Qoida:** Format conversion only. Business logic NOL.

---

## Eng Kritik Qoidalar

### 1. Response — `LinkedHashMap` (HashMap **EMAS**)

CUBA klient'lar field tartibni kutadi (JSON parsing'da). HashMap insertion order'ni saqlamaydi.

```java
// ✗ XATO — order yo'qoladi, klient parser breakdown
Map<String, Object> response = new HashMap<>();

// ✓ TO'G'RI
Map<String, Object> response = new LinkedHashMap<>();
response.put("_entityName", "hemishe$Student");
response.put("_instanceName", student.getName());
response.put("id", student.getId());
// ...
```

**Diqqat:** Jackson `ObjectMapper` default LinkedHashMap chiqaradi DTO'dan, lekin manual `Map` yaratganda HashMap default — buni tekshirish.

---

### 2. `@JsonPropertyOrder` har DTO'da

```java
// ✓ TO'G'RI
@JsonPropertyOrder({
    "_entityName",
    "_instanceName",
    "id",
    "code",
    "name",
    "active",
    "createTs",
    "createdBy",
    "updateTs",
    "updatedBy"
})
public class StudentLegacyDto {
    @JsonProperty("_entityName")
    private String entityName;

    @JsonProperty("_instanceName")
    private String instanceName;

    private Long id;
    // ...
}
```

**Sabab:** CUBA framework field tartib'ni qat'iy aniqlaydi. Yangi tartib = klient JSON deserialize'da xato.

---

### 3. CUBA System Field'lari MAJBURIY

| Field | Qiymat | Misol |
|-------|--------|-------|
| `_entityName` | CUBA entity nomi (dollar separator) | `hemishe$Student` |
| `_instanceName` | Display name | `John Doe (PINFL: 123)` |

```java
// ✓ Helper method
private static StudentLegacyDto toLegacy(Student s) {
    StudentLegacyDto dto = new StudentLegacyDto();
    dto.setEntityName("hemishe$Student");
    dto.setInstanceName(s.getLastName() + " " + s.getFirstName());
    dto.setId(s.getId());
    // ...
    return dto;
}
```

---

### 4. Foreign Key — Nested Object (flat string EMAS)

CUBA FK'larni nested object sifatida ifodalaydi:

```json
// ✗ XATO — yangi REST format
{
  "id": "uuid-1",
  "facultyId": "uuid-2"
}

// ✓ TO'G'RI — CUBA format
{
  "_entityName": "hemishe$Student",
  "id": "uuid-1",
  "faculty": {
    "_entityName": "hemishe$Faculty",
    "id": "uuid-2",
    "_instanceName": "Fakultet nomi"
  }
}
```

```java
// ✓ DTO struktura
public class StudentLegacyDto {
    @JsonProperty("_entityName")
    private String entityName;

    private String id;

    private FacultyReference faculty;  // nested object

    // Faculty FK
    public static class FacultyReference {
        @JsonProperty("_entityName")
        private String entityName = "hemishe$Faculty";

        private String id;

        @JsonProperty("_instanceName")
        private String instanceName;
    }
}
```

---

### 5. Datetime Format — CUBA convention

```java
// ✓ TO'G'RI — CUBA ISO 8601 format
@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
private LocalDateTime createTs;

@JsonFormat(pattern = "yyyy-MM-dd")
private LocalDate birthDate;
```

**Diqqat:** Timezone handling. CUBA UTC, klientlar lokal — shunchaki UTC saqlash, klient o'zgartiradi.

---

### 6. Soft Delete — `delete_ts` ko'rinadi (har doim filtered)

```java
// ✓ Repository — entity-level @SQLRestriction avtomat filter
List<Student> students = repository.findAll();  // delete_ts IS NULL avto

// ⚠ Native query — manual
@Query(nativeQuery = true,
       value = "SELECT * FROM hemishe_e_student WHERE faculty_id = ? AND delete_ts IS NULL")
List<Student> findByFacultyNative(Long facultyId);
```

---

### 7. Business Logic — TAQIQLANGAN

api-legacy faqat **format conversion** qiladi. Business rule har doim `service` layer'da.

```java
// ✗ XATO — controller'da business logic
@RestController
public class StudentLegacyController {
    @PostMapping("/entities/hemishe$Student")
    public StudentLegacyDto create(@RequestBody StudentLegacyDto dto) {
        // PINFL validation, faculty check, ... ❌ shu yerda emas
        ...
    }
}

// ✓ TO'G'RI — service'ga delegate
@RestController
@RequiredArgsConstructor
public class StudentLegacyController {
    private final StudentService studentService;  // SAME service as api-web!
    private final StudentLegacyMapper legacyMapper;

    @PostMapping("/entities/hemishe$Student")
    public ResponseEntity<StudentLegacyDto> create(@RequestBody StudentCreateRequest req) {
        StudentCreateDto modernDto = legacyMapper.toModern(req);
        StudentDto created = studentService.create(modernDto);  // shared logic
        return ResponseEntity.ok(legacyMapper.toLegacy(created));
    }
}
```

**Pattern:** `api-legacy` = thin adapter. api-web va api-legacy **bir xil service'lardan** foydalanadi.

---

### 8. URL Pattern — `/app/rest/v2/*`

```java
@RestController
@RequestMapping("/app/rest/v2")  // CUBA convention
@Tag(name = "Legacy CUBA API")
public class StudentLegacyController {

    @GetMapping("/entities/hemishe$Student")
    public List<StudentLegacyDto> list(...) { ... }

    @GetMapping("/entities/hemishe$Student/{id}")
    public StudentLegacyDto get(@PathVariable String id) { ... }

    @PostMapping("/entities/hemishe$Student")
    public StudentLegacyDto create(...) { ... }
}
```

URL'da `$` belgisi — CUBA dollar separator. Spring `@PathVariable` da escape kerak emas.

---

### 9. Error Response — CUBA Format

CUBA xato format'i yangi `ErrorResponse` formatdan farq qiladi:

```json
// ✓ CUBA format
{
  "error": "EntityValidationException",
  "details": "PINFL must be 14 digits",
  "exception": "javax.validation.ConstraintViolationException"
}

// ✗ Yangi format (api-web uchun)
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "...",
    "details": [...]
  }
}
```

**Yechim:** alohida `@RestControllerAdvice` (basePackages = "uz.hemis.api.legacy"):
```java
@RestControllerAdvice(basePackages = "uz.hemis.api.legacy")
public class LegacyExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(...) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", "EntityNotFoundException");
        response.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
```

---

### 10. Pagination — CUBA Convention

CUBA pagination response formati:

```json
{
  "data": [...],
  "totalCount": 1500
}
```

URL params: `?offset=0&limit=20` (Spring `Pageable` → manual conversion).

```java
@GetMapping("/entities/hemishe$Student")
public Map<String, Object> list(
    @RequestParam(defaultValue = "0") int offset,
    @RequestParam(defaultValue = "20") int limit
) {
    Pageable pageable = PageRequest.of(offset / limit, limit);
    Page<StudentDto> page = studentService.findAll(pageable);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("data", page.getContent().stream().map(legacyMapper::toLegacy).toList());
    response.put("totalCount", page.getTotalElements());
    return response;
}
```

---

## Porting Workflow (Old-Hemis → api-legacy)

### Trigger formatlari

```
PORT: GET /services/tax/rent
PORT: POST /entities/hemishe$Student
```

### Avtomatik qadamlar

1. `/home/adm1n/startup/old_hemis.json` dan endpoint metadata
2. `rest-services.xml` dan parametrlar
3. Mavjud Spring Boot duplikatini tekshirish
4. Old-hemis live response'i save (test fixture)
5. Controller + DTO + Mapper + Service yaratish
6. Swagger annotations to'liq
7. Integration test (success + error)
8. `endpoint_tester.html` ga test button

**Batafsil:** `@../.claude/ENDPOINT_PORTING_GUIDE.md`

---

## CUBA Format Validation — Test Fixture

Har endpoint uchun:
1. Old-hemis dan response oli (real data)
2. `src/test/resources/legacy-fixtures/student_response.json` saqla
3. Integration test'da diff:

```java
@Test
void getStudent_shouldMatchLegacyFormat() throws Exception {
    String expected = Files.readString(
        Paths.get("src/test/resources/legacy-fixtures/student_response.json")
    );
    String actual = mockMvc.perform(get("/app/rest/v2/entities/hemishe$Student/1"))
        .andReturn().getResponse().getContentAsString();

    JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT_ORDER);
}
```

**`STRICT_ORDER` — field tartib ham tekshiriladi.**

---

## PR Checklist (api-legacy)

- [ ] Response: `LinkedHashMap` (HashMap emas)
- [ ] DTO'da `@JsonPropertyOrder` to'liq
- [ ] `_entityName`, `_instanceName` har response'da
- [ ] FK nested object format (`{"id": "...", "_entityName": "..."}`)
- [ ] Datetime: CUBA pattern (`yyyy-MM-dd'T'HH:mm:ss.SSS`)
- [ ] Business logic: NOL (faqat format conversion)
- [ ] Service: api-web bilan **bir xil** service ishlatiladi
- [ ] URL: `/app/rest/v2/*` pattern
- [ ] Error: CUBA format (`error`, `details`, `exception`)
- [ ] Test fixture: real old-hemis response bilan diff
- [ ] Swagger annotations (`@Tag`, `@Operation`, `@ApiResponses`)
- [ ] `endpoint_tester.html` ga test button qo'shilgan

---

## Forbidden patterns

```java
// ✗ HashMap response
return new HashMap<>();

// ✗ Yangi business logic
public StudentLegacyDto create(StudentLegacyDto dto) {
    // PINFL check, faculty validation — TAQIQ
    return ...;
}

// ✗ api-web service'ni override
@Override
public StudentDto findById(Long id) { ... }  // FORK - taqiq

// ✗ Modern error format
throw new ApiException(ErrorCode.NOT_FOUND, "...");
// Legacy → CUBA format kerak
```

---

## See Also
- `@../.claude/ENDPOINT_PORTING_GUIDE.md` — Porting workflow
- `@../.claude/context.md` — CUBA legacy schema
- `@../service/CLAUDE.md` — Shared business logic
