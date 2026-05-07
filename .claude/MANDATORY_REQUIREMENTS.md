# Code Examples — Swagger, Tests, Patterns

> **Maqsad:** Yangi feature yaratganda kod misollar to'plami. **Qoidalar uchun** `.claude/rules.md` (canonical) o'qing. Bu fayl — faqat misollar.
>
> **Prioritet:** `rules.md` → MAJBURIY qoidalar; `MANDATORY_REQUIREMENTS.md` (bu fayl) → REFERENCE misollar; ziddiyat bo'lsa `rules.md` ustun.

Every new feature MUST include: Swagger documentation, integration tests, unit tests.

---

## Test Environment

Tests are gated by `TESTS_ENABLED=true` in `.env`. Without it, `./gradlew test` aborts.

| Module | Database | Notes |
|--------|----------|-------|
| `app` | Real PostgreSQL (from `.env` `DB_MASTER_*`) | Shared local DB; H2 ishlatilmaydi (CUBA legacy schema-ga moslashmaydi) |
| `service`, `security` | Real PostgreSQL (from `.env` `DB_MASTER_*`) | Runs against existing schema, never production DB |
| `domain` | Real PostgreSQL | Tafsilot: `domain/CLAUDE.md` "Testing Strategy" |

Redis va boshqa servislar `.env` orqali. Test izolyatsiyasi uchun lokal Redis (Docker compose).

---

## Swagger: Required Annotations

| Where | Annotations |
|-------|-------------|
| Controller class | `@Tag(name, description)` |
| Every endpoint | `@Operation(summary, description)` |
| Every endpoint | `@ApiResponses` for: 200/201, 400, 401, 403, 404, 409, 500 |
| Path/query params | `@Parameter(description, required, example)` with `@Schema` |
| POST/PUT body | `@io.swagger.v3.oas.annotations.parameters.RequestBody` with `@Schema` + `@ExampleObject` |
| DTOs | `@Schema(example = "...")` on fields |

**Tips:** Keep summaries short, details in `description`. Don't duplicate schemas — use shared DTOs. Test in Swagger UI before commit.

---

## Complete Feature Example

**Task:** Add endpoint to get students by faculty — shown through all layers.

### 1. Controller (Swagger + PreAuthorize)

```java
@RestController
@RequestMapping("/api/v1/web/students")
@Tag(name = "Students", description = "Student management operations")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    @Operation(
        summary = "Get student by ID",
        description = "Retrieves detailed student information including faculty and curriculum data"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Student found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StudentDto.class),
                examples = @ExampleObject(value = """
                    {
                      "success": true,
                      "data": {
                        "id": "00000000-0000-0000-0000-000000000001",
                        "firstName": "John", "lastName": "Doe",
                        "email": "john.doe@university.uz", "facultyName": "Computer Science"
                      },
                      "timestamp": "2026-05-07T08:30:00Z"
                    }
                    """)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Student not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'students.view' permission")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('students.view')")
    public ResponseEntity<ResponseWrapper<StudentDto>> findById(
        @Parameter(description = "Student ID", required = true,
            example = "00000000-0000-0000-0000-000000000001",
            schema = @Schema(type = "string", format = "uuid"))
        @PathVariable UUID id
    ) {
        StudentDto student = studentService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(student));
    }

    @Operation(summary = "Create new student", description = "Creates a new student record with validation")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Student created successfully",
            content = @Content(schema = @Schema(implementation = StudentDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - lacks 'students.create' permission"),
        @ApiResponse(responseCode = "409", description = "Conflict - Email already exists")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('students.create')")
    public ResponseEntity<ResponseWrapper<StudentDto>> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Student creation data", required = true,
            content = @Content(schema = @Schema(implementation = StudentCreateDto.class),
                examples = @ExampleObject(value = """
                    {
                      "firstName": "Jane", "lastName": "Smith",
                      "email": "jane.smith@university.uz", "phone": "+998901234567",
                      "facultyId": 1, "curriculumId": 10
                    }
                    """)))
        @Valid @RequestBody StudentCreateDto dto
    ) {
        StudentDto created = studentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    @Operation(summary = "Get students by faculty ID",
        description = "Retrieves all students belonging to a specific faculty with pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Faculty not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/faculty/{facultyId}")
    @PreAuthorize("hasAuthority('students.view')")
    public ResponseEntity<ResponseWrapper<Page<StudentDto>>> getByFaculty(
        @Parameter(description = "Faculty ID", required = true, example = "1")
        @PathVariable UUID facultyId,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<StudentDto> students = studentService.findByFacultyId(facultyId, pageable);
        return ResponseEntity.ok(ResponseWrapper.success(students));
    }
}
```

### 2. Service

```java
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final StudentMapper studentMapper;

    @Override
    @ReadOnly
    @Cacheable(value = "students", key = "#id")
    public StudentDto findById(UUID id) {
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return studentMapper.toDto(student);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('students.create')")
    @CacheEvict(value = "studentsList", allEntries = true)
    public StudentDto create(@Valid StudentCreateDto dto) {
        if (studentRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Email already exists: " + dto.getEmail());
        }
        Faculty faculty = facultyRepository.findById(dto.getFacultyId())
            .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + dto.getFacultyId()));
        Student student = studentMapper.toEntity(dto);
        student.setFaculty(faculty);
        Student saved = studentRepository.save(student);
        return studentMapper.toDto(saved);
    }

    @Override
    @ReadOnly
    @Cacheable(value = "studentsByFaculty", key = "#facultyId + '_' + #pageable.pageNumber")
    public Page<StudentDto> findByFacultyId(UUID facultyId, Pageable pageable) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResourceNotFoundException("Faculty not found: " + facultyId);
        }
        return studentRepository.findByFacultyId(facultyId, pageable).map(studentMapper::toDto);
    }
}
```

### 3. Integration Test

Test ALL scenarios for each endpoint: success, not found, unauthorized, forbidden, validation, conflict.

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // ── GET /{id} ──────────────────────────────────────────

    @Test @Order(1)
    @DisplayName("GET /{id} - 200 Success")
    @WithMockUser(username = "admin", authorities = {"students.view"})
    void getById_success() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.firstName").exists());
    }

    @Test @Order(2)
    @DisplayName("GET /{id} - 404 Not Found")
    @WithMockUser(username = "admin", authorities = {"students.view"})
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/999999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test @Order(3)
    @DisplayName("GET /{id} - 401 Unauthorized")
    void getById_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test @Order(4)
    @DisplayName("GET /{id} - 403 Forbidden")
    @WithMockUser(username = "user", authorities = {"other.permission"})
    void getById_forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1"))
            .andExpect(status().isForbidden());
    }

    // ── POST / ─────────────────────────────────────────────

    @Test @Order(10)
    @DisplayName("POST - 201 Created")
    @WithMockUser(username = "admin", authorities = {"students.create"})
    void create_success() throws Exception {
        var dto = StudentCreateDto.builder()
            .firstName("John").lastName("Doe")
            .email("john.test@university.uz").facultyId(1L).build();

        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists());
    }

    @Test @Order(11)
    @DisplayName("POST - 400 Validation Error (blank/missing/invalid fields)")
    @WithMockUser(username = "admin", authorities = {"students.create"})
    void create_validationError() throws Exception {
        var dto = StudentCreateDto.builder().firstName("").build(); // missing required fields

        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test @Order(12)
    @DisplayName("POST - 409 Conflict (duplicate email)")
    @WithMockUser(username = "admin", authorities = {"students.create"})
    void create_conflict() throws Exception {
        var dto = StudentCreateDto.builder()
            .firstName("Jane").lastName("Smith")
            .email("duplicate@university.uz").facultyId(1L).build();

        // Create first, then attempt duplicate
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("DUPLICATE_EMAIL"));
    }

    @Test @Order(13)
    @DisplayName("POST - 401 Unauthorized")
    void create_unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test @Order(14)
    @DisplayName("POST - 403 Forbidden")
    @WithMockUser(username = "user", authorities = {"students.view"})
    void create_forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    // ── PUT /{id} ──────────────────────────────────────────

    @Test @Order(20)
    @DisplayName("PUT /{id} - 200 Success")
    @WithMockUser(username = "admin", authorities = {"students.edit"})
    void update_success() throws Exception {
        var dto = StudentUpdateDto.builder().firstName("Jane").lastName("Smith").build();

        mockMvc.perform(put("/api/v1/web/students/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test @Order(21)
    @DisplayName("PUT /{id} - 404 Not Found")
    @WithMockUser(username = "admin", authorities = {"students.edit"})
    void update_notFound() throws Exception {
        mockMvc.perform(put("/api/v1/web/students/999999")
                .contentType(MediaType.APPLICATION_JSON).content("{\"firstName\":\"X\"}"))
            .andExpect(status().isNotFound());
    }

    // ── DELETE /{id} ───────────────────────────────────────

    @Test @Order(30)
    @DisplayName("DELETE /{id} - 200 Success")
    @WithMockUser(username = "admin", authorities = {"students.delete"})
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test @Order(31)
    @DisplayName("DELETE /{id} - 404 Not Found")
    @WithMockUser(username = "admin", authorities = {"students.delete"})
    void delete_notFound() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/999999"))
            .andExpect(status().isNotFound());
    }

    @Test @Order(32)
    @DisplayName("DELETE /{id} - 401 Unauthorized")
    void delete_unauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test @Order(33)
    @DisplayName("DELETE /{id} - 403 Forbidden")
    @WithMockUser(username = "user", authorities = {"students.view"})
    void delete_forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/1"))
            .andExpect(status().isForbidden());
    }
}
```

### 4. Unit Test

Test success, exceptions, edge cases. Use Given-When-Then pattern.

```java
@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock private StudentRepository studentRepository;
    @Mock private FacultyRepository facultyRepository;
    @Mock private StudentMapper studentMapper;
    @InjectMocks private StudentServiceImpl studentService;

    private Student testStudent;
    private StudentDto testStudentDto;
    private Faculty testFaculty;

    @BeforeEach
    void setup() {
        testFaculty = new Faculty();
        testFaculty.setId(1L);
        testFaculty.setName("Computer Science");

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setFirstName("John");
        testStudent.setLastName("Doe");
        testStudent.setEmail("john@university.uz");
        testStudent.setFaculty(testFaculty);

        testStudentDto = new StudentDto();
        testStudentDto.setId(1L);
        testStudentDto.setFirstName("John");
        testStudentDto.setLastName("Doe");
    }

    // ── findById() ─────────────────────────────────────────

    @Test
    @DisplayName("findById - success")
    void findById_success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        StudentDto result = studentService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(studentRepository).findById(1L);
    }

    @Test
    @DisplayName("findById - not found throws ResourceNotFoundException")
    void findById_notFound() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.findById(999L));
        verify(studentMapper, never()).toDto(any());
    }

    // ── create() ───────────────────────────────────────────

    @Test
    @DisplayName("create - success")
    void create_success() {
        var dto = StudentCreateDto.builder()
            .firstName("Jane").lastName("Smith")
            .email("jane@university.uz").facultyId(1L).build();

        when(studentRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(testFaculty));
        when(studentMapper.toEntity(dto)).thenReturn(testStudent);
        when(studentRepository.save(testStudent)).thenReturn(testStudent);
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);

        StudentDto result = studentService.create(dto);

        assertNotNull(result);
        verify(studentRepository).save(testStudent);
    }

    @Test
    @DisplayName("create - duplicate email throws ValidationException")
    void create_duplicateEmail() {
        var dto = StudentCreateDto.builder().email("existing@university.uz").build();
        when(studentRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(ValidationException.class, () -> studentService.create(dto));
        verify(studentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create - invalid faculty throws ResourceNotFoundException")
    void create_invalidFaculty() {
        var dto = StudentCreateDto.builder().email("test@university.uz").facultyId(999L).build();
        when(studentRepository.existsByEmail(any())).thenReturn(false);
        when(facultyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.create(dto));
        verify(studentRepository, never()).save(any());
    }

    // ── findByFacultyId() ──────────────────────────────────

    @Test
    @DisplayName("findByFacultyId - success")
    void findByFacultyId_success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Student> page = new PageImpl<>(List.of(testStudent), pageable, 1);
        when(facultyRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.findByFacultyId(1L, pageable)).thenReturn(page);
        when(studentMapper.toDto(any())).thenReturn(testStudentDto);

        Page<StudentDto> result = studentService.findByFacultyId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        verify(studentRepository).findByFacultyId(1L, pageable);
    }

    @Test
    @DisplayName("findByFacultyId - faculty not found throws exception")
    void findByFacultyId_notFound() {
        when(facultyRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
            () -> studentService.findByFacultyId(999L, PageRequest.of(0, 20)));
        verify(studentRepository, never()).findByFacultyId(any(), any());
    }
}
```

---

## PR Checklist

Before submitting a pull request, verify ALL items:

### Swagger
- [ ] `@Tag` on controller class
- [ ] `@Operation` + `@ApiResponses` on every endpoint (200, 201, 400, 401, 403, 404)
- [ ] `@Parameter` with description + example on all path/query params
- [ ] `@RequestBody` with `@Schema` + `@ExampleObject` on POST/PUT
- [ ] Tested in Swagger UI (`http://localhost:8081/api/swagger-ui.html`)

### Integration Tests
- [ ] Test class for controller with `@SpringBootTest` + `@AutoConfigureMockMvc`
- [ ] Success scenario (200/201) with valid data
- [ ] Not found (404), unauthorized (401), forbidden (403)
- [ ] Validation errors (400) — blank, missing, invalid format
- [ ] Conflict/duplicate (409) where applicable
- [ ] Pagination, filtering, sorting where applicable

### Unit Tests
- [ ] Test class for service with `@ExtendWith(MockitoExtension.class)`
- [ ] Success scenario for every public method
- [ ] All exception paths (`ResourceNotFoundException`, `ValidationException`, etc.)
- [ ] Edge cases and null inputs
- [ ] All dependencies mocked; verify mock interactions

### Coverage
- [ ] Overall coverage >= 70%
- [ ] Service layer coverage >= 90%
- [ ] All tests pass: `./gradlew test`
