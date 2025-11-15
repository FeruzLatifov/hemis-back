# ⚠️ MANDATORY REQUIREMENTS - NO EXCEPTIONS!

> **These rules are CRITICAL and MUST be followed**  
> **Violation = Code Review REJECTED**  
> **Last Updated:** 2025-11-15

---

## 🚨 NON-NEGOTIABLE RULES

### 1️⃣ SWAGGER DOCUMENTATION - MANDATORY!

```
❌ FORBIDDEN:
- Creating endpoint without Swagger documentation
- Skipping @Operation annotation
- Missing @ApiResponse annotations
- No example values in parameters

✅ REQUIRED:
- @Tag on EVERY controller
- @Operation on EVERY endpoint
- @ApiResponses for ALL HTTP status codes (200, 201, 400, 401, 403, 404, 500)
- @Parameter with description and example
- @RequestBody with schema
- @Schema with example values
- Test endpoint in Swagger UI before commit
```

**Example of CORRECT Swagger documentation:**

```java
@RestController
@RequestMapping("/api/v1/web/students")
@Tag(name = "Students", description = "Student management operations")
@RequiredArgsConstructor
@Slf4j
public class StudentController {
    
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
                examples = @ExampleObject(
                    name = "Successful response",
                    value = """
                        {
                          "success": true,
                          "data": {
                            "id": 1,
                            "firstName": "John",
                            "lastName": "Doe",
                            "email": "john.doe@university.uz",
                            "facultyName": "Computer Science"
                          },
                          "timestamp": "2025-11-15T08:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Authentication token missing or invalid"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User lacks 'students.view' permission"
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('students.view')")
    public ResponseEntity<ResponseWrapper<StudentDto>> findById(
        @Parameter(
            description = "Student ID",
            required = true,
            example = "1",
            schema = @Schema(type = "integer", format = "int64", minimum = "1")
        )
        @PathVariable Long id
    ) {
        StudentDto student = studentService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(student));
    }
    
    @Operation(
        summary = "Create new student",
        description = "Creates a new student record with validation"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Student created successfully",
            content = @Content(schema = @Schema(implementation = StudentDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error - Invalid input data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User lacks 'students.create' permission"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - Email already exists"
        )
    })
    @PostMapping
    @PreAuthorize("hasAuthority('students.create')")
    public ResponseEntity<ResponseWrapper<StudentDto>> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Student creation data",
            required = true,
            content = @Content(
                schema = @Schema(implementation = StudentCreateDto.class),
                examples = @ExampleObject(
                    name = "Valid student data",
                    value = """
                        {
                          "firstName": "Jane",
                          "lastName": "Smith",
                          "email": "jane.smith@university.uz",
                          "phone": "+998901234567",
                          "facultyId": 1,
                          "curriculumId": 10
                        }
                        """
                )
            )
        )
        @Valid @RequestBody StudentCreateDto dto
    ) {
        StudentDto created = studentService.create(dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseWrapper.success(created));
    }
}
```

### 2️⃣ INTEGRATION TESTS - MANDATORY!

```
❌ FORBIDDEN:
- Creating endpoint without integration test
- Testing only happy path
- Skipping error scenarios
- No test for authentication/authorization

✅ REQUIRED:
- Integration test for EVERY endpoint
- Test ALL HTTP status codes (200, 201, 400, 401, 403, 404)
- Test successful scenario
- Test validation errors
- Test authentication (401)
- Test authorization (403)
- Test not found (404)
- Test with invalid input
- Test with missing required fields
```

**Example of COMPLETE integration test:**

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @BeforeAll
    void setup() {
        // Setup test data
    }
    
    @AfterEach
    void cleanup() {
        // Cleanup after each test
    }
    
    // ═══════════════════════════════════════════════════════
    // GET /api/v1/web/students/{id} - ALL SCENARIOS
    // ═══════════════════════════════════════════════════════
    
    @Test
    @Order(1)
    @DisplayName("GET /{id} - Success (200) - With valid ID and permission")
    @WithMockUser(username = "admin", authorities = {"students.view"})
    void getStudentById_WhenExistsAndHasPermission_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.firstName").exists())
            .andExpect(jsonPath("$.data.lastName").exists())
            .andExpect(jsonPath("$.data.email").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }
    
    @Test
    @Order(2)
    @DisplayName("GET /{id} - Not Found (404) - With non-existent ID")
    @WithMockUser(username = "admin", authorities = {"students.view"})
    void getStudentById_WhenNotExists_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/999999")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
            .andExpect(jsonPath("$.error.message").exists());
    }
    
    @Test
    @Order(3)
    @DisplayName("GET /{id} - Unauthorized (401) - Without authentication")
    void getStudentById_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(4)
    @DisplayName("GET /{id} - Forbidden (403) - Without permission")
    @WithMockUser(username = "user", authorities = {"other.permission"})
    void getStudentById_WithoutPermission_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(5)
    @DisplayName("GET /{id} - Bad Request (400) - With invalid ID format")
    @WithMockUser(username = "admin", authorities = {"students.view"})
    void getStudentById_WithInvalidIdFormat_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/invalid")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
    
    // ═══════════════════════════════════════════════════════
    // POST /api/v1/web/students - ALL SCENARIOS
    // ═══════════════════════════════════════════════════════
    
    @Test
    @Order(10)
    @DisplayName("POST / - Created (201) - With valid data")
    @WithMockUser(username = "admin", authorities = {"students.create"})
    void createStudent_WithValidData_Returns201() throws Exception {
        StudentCreateDto dto = StudentCreateDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe.test@university.uz")
            .phone("+998901234567")
            .facultyId(1L)
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.firstName").value("John"))
            .andExpect(jsonPath("$.data.lastName").value("Doe"))
            .andExpect(jsonPath("$.data.email").value("john.doe.test@university.uz"));
    }
    
    @Test
    @Order(11)
    @DisplayName("POST / - Bad Request (400) - With blank firstName")
    @WithMockUser(username = "admin", authorities = {"students.create"})
    void createStudent_WithBlankFirstName_Returns400() throws Exception {
        StudentCreateDto dto = StudentCreateDto.builder()
            .firstName("")  // Invalid - blank
            .lastName("Doe")
            .email("test@university.uz")
            .facultyId(1L)
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.details").isArray());
    }
    
    @Test
    @Order(12)
    @DisplayName("POST / - Bad Request (400) - With invalid email format")
    @WithMockUser(username = "admin", authorities = {"students.create"})
    void createStudent_WithInvalidEmail_Returns400() throws Exception {
        StudentCreateDto dto = StudentCreateDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("invalid-email")  // Invalid format
            .facultyId(1L)
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    @Order(13)
    @DisplayName("POST / - Bad Request (400) - With missing required fields")
    @WithMockUser(username = "admin", authorities = {"students.create"})
    void createStudent_WithMissingRequiredFields_Returns400() throws Exception {
        StudentCreateDto dto = StudentCreateDto.builder()
            .firstName("John")
            // Missing lastName, email, facultyId
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(14)
    @DisplayName("POST / - Conflict (409) - With duplicate email")
    @WithMockUser(username = "admin", authorities = {"students.create"})
    void createStudent_WithDuplicateEmail_Returns409() throws Exception {
        // First, create a student
        StudentCreateDto dto1 = StudentCreateDto.builder()
            .firstName("Jane")
            .lastName("Smith")
            .email("duplicate@university.uz")
            .facultyId(1L)
            .build();
        
        studentService.create(dto1);
        
        // Try to create another with same email
        StudentCreateDto dto2 = StudentCreateDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("duplicate@university.uz")  // Same email
            .facultyId(1L)
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto2)))
            .andDo(print())
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("DUPLICATE_EMAIL"));
    }
    
    @Test
    @Order(15)
    @DisplayName("POST / - Unauthorized (401) - Without authentication")
    void createStudent_WithoutAuth_Returns401() throws Exception {
        StudentCreateDto dto = StudentCreateDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("test@university.uz")
            .facultyId(1L)
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(16)
    @DisplayName("POST / - Forbidden (403) - Without permission")
    @WithMockUser(username = "user", authorities = {"students.view"})
    void createStudent_WithoutPermission_Returns403() throws Exception {
        StudentCreateDto dto = StudentCreateDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("test@university.uz")
            .facultyId(1L)
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
    
    // ═══════════════════════════════════════════════════════
    // PUT /api/v1/web/students/{id} - ALL SCENARIOS
    // ═══════════════════════════════════════════════════════
    
    @Test
    @Order(20)
    @DisplayName("PUT /{id} - Success (200) - With valid data")
    @WithMockUser(username = "admin", authorities = {"students.edit"})
    void updateStudent_WithValidData_Returns200() throws Exception {
        StudentUpdateDto dto = StudentUpdateDto.builder()
            .firstName("Jane")
            .lastName("Smith")
            .build();
        
        mockMvc.perform(put("/api/v1/web/students/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.firstName").value("Jane"))
            .andExpect(jsonPath("$.data.lastName").value("Smith"));
    }
    
    @Test
    @Order(21)
    @DisplayName("PUT /{id} - Not Found (404) - With non-existent ID")
    @WithMockUser(username = "admin", authorities = {"students.edit"})
    void updateStudent_WhenNotExists_Returns404() throws Exception {
        StudentUpdateDto dto = StudentUpdateDto.builder()
            .firstName("Jane")
            .build();
        
        mockMvc.perform(put("/api/v1/web/students/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
    
    // ═══════════════════════════════════════════════════════
    // DELETE /api/v1/web/students/{id} - ALL SCENARIOS
    // ═══════════════════════════════════════════════════════
    
    @Test
    @Order(30)
    @DisplayName("DELETE /{id} - Success (200) - With valid ID")
    @WithMockUser(username = "admin", authorities = {"students.delete"})
    void deleteStudent_WhenExists_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @Order(31)
    @DisplayName("DELETE /{id} - Not Found (404) - With non-existent ID")
    @WithMockUser(username = "admin", authorities = {"students.delete"})
    void deleteStudent_WhenNotExists_Returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/999999"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(32)
    @DisplayName("DELETE /{id} - Unauthorized (401) - Without authentication")
    void deleteStudent_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/1"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(33)
    @DisplayName("DELETE /{id} - Forbidden (403) - Without permission")
    @WithMockUser(username = "user", authorities = {"students.view"})
    void deleteStudent_WithoutPermission_Returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/1"))
            .andDo(print())
            .andExpect(status().isForbidden());
    }
}
```

### 3️⃣ UNIT TESTS - MANDATORY!

```
❌ FORBIDDEN:
- Creating service method without unit test
- Testing only successful scenario
- No test for exceptions

✅ REQUIRED:
- Unit test for EVERY service method
- Test successful scenario
- Test all exceptions
- Mock all dependencies
- Test edge cases
```

**Example of COMPLETE unit test:**

```java
@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private FacultyRepository facultyRepository;
    
    @Mock
    private StudentMapper studentMapper;
    
    @InjectMocks
    private StudentServiceImpl studentService;
    
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
    
    // ═══════════════════════════════════════════════════════
    // findById() - ALL SCENARIOS
    // ═══════════════════════════════════════════════════════
    
    @Test
    @DisplayName("findById() - Success - Returns student when exists")
    void findById_WhenExists_ReturnsStudentDto() {
        // Given
        Long id = 1L;
        when(studentRepository.findById(id)).thenReturn(Optional.of(testStudent));
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);
        
        // When
        StudentDto result = studentService.findById(id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("John", result.getFirstName());
        verify(studentRepository).findById(id);
        verify(studentMapper).toDto(testStudent);
    }
    
    @Test
    @DisplayName("findById() - Exception - Throws when not exists")
    void findById_WhenNotExists_ThrowsResourceNotFoundException() {
        // Given
        Long id = 999L;
        when(studentRepository.findById(id)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> studentService.findById(id)
        );
        
        assertTrue(exception.getMessage().contains("Student not found"));
        assertTrue(exception.getMessage().contains(id.toString()));
        verify(studentRepository).findById(id);
        verify(studentMapper, never()).toDto(any());
    }
    
    @Test
    @DisplayName("findById() - Exception - Throws when ID is null")
    void findById_WhenIdIsNull_ThrowsIllegalArgumentException() {
        // When & Then
        assertThrows(
            IllegalArgumentException.class,
            () -> studentService.findById(null)
        );
    }
    
    // ═══════════════════════════════════════════════════════
    // create() - ALL SCENARIOS
    // ═══════════════════════════════════════════════════════
    
    @Test
    @DisplayName("create() - Success - Creates student with valid data")
    void create_WithValidData_ReturnsCreatedStudent() {
        // Given
        StudentCreateDto createDto = StudentCreateDto.builder()
            .firstName("Jane")
            .lastName("Smith")
            .email("jane@university.uz")
            .facultyId(1L)
            .build();
        
        when(studentRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(facultyRepository.findById(1L)).thenReturn(Optional.of(testFaculty));
        when(studentMapper.toEntity(createDto)).thenReturn(testStudent);
        when(studentRepository.save(testStudent)).thenReturn(testStudent);
        when(studentMapper.toDto(testStudent)).thenReturn(testStudentDto);
        
        // When
        StudentDto result = studentService.create(createDto);
        
        // Then
        assertNotNull(result);
        verify(studentRepository).existsByEmail(createDto.getEmail());
        verify(facultyRepository).findById(1L);
        verify(studentRepository).save(testStudent);
    }
    
    @Test
    @DisplayName("create() - Exception - Throws when email exists")
    void create_WithDuplicateEmail_ThrowsValidationException() {
        // Given
        StudentCreateDto createDto = StudentCreateDto.builder()
            .email("existing@university.uz")
            .build();
        
        when(studentRepository.existsByEmail(createDto.getEmail())).thenReturn(true);
        
        // When & Then
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> studentService.create(createDto)
        );
        
        assertTrue(exception.getMessage().contains("Email already exists"));
        verify(studentRepository).existsByEmail(createDto.getEmail());
        verify(studentRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("create() - Exception - Throws when faculty not found")
    void create_WithInvalidFacultyId_ThrowsResourceNotFoundException() {
        // Given
        StudentCreateDto createDto = StudentCreateDto.builder()
            .email("test@university.uz")
            .facultyId(999L)
            .build();
        
        when(studentRepository.existsByEmail(any())).thenReturn(false);
        when(facultyRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> studentService.create(createDto)
        );
        
        assertTrue(exception.getMessage().contains("Faculty not found"));
        verify(studentRepository, never()).save(any());
    }
}
```

---

## 📋 CHECKLIST BEFORE PULL REQUEST

### ✅ Swagger Documentation Checklist

```
☑ @Tag annotation on controller class
☑ @Operation annotation on EVERY endpoint
☑ @ApiResponses for HTTP codes: 200, 201, 400, 401, 403, 404
☑ @Parameter with description and example on ALL parameters
☑ @RequestBody with schema on POST/PUT endpoints
☑ @Schema with example values on DTOs
☑ @ExampleObject with real JSON examples
☑ Tested endpoint in Swagger UI (http://localhost:8080/api/swagger-ui.html)
☑ Verified response structure matches documentation
☑ All error scenarios documented
```

### ✅ Integration Tests Checklist

```
☑ Integration test class created for controller
☑ Test for successful scenario (200/201)
☑ Test for not found scenario (404)
☑ Test for unauthorized scenario (401)
☑ Test for forbidden scenario (403)
☑ Test for validation errors (400)
☑ Test for conflict/duplicate (409)
☑ Test with valid input data
☑ Test with invalid input data
☑ Test with missing required fields
☑ Test with blank/empty values
☑ Test with invalid format (email, phone, etc.)
☑ Test pagination (if applicable)
☑ Test filtering (if applicable)
☑ Test sorting (if applicable)
☑ All tests pass locally (./gradlew test)
```

### ✅ Unit Tests Checklist

```
☑ Unit test class created for service
☑ Test for EVERY public method
☑ Test successful scenario
☑ Test all exceptions (ResourceNotFoundException, ValidationException, etc.)
☑ Test with null parameters
☑ Test with invalid parameters
☑ Test edge cases
☑ All dependencies mocked
☑ Verify interactions with mocks
☑ Test coverage >= 90% for service layer
☑ All tests pass locally
```

---

## 🚫 CODE REVIEW REJECTION REASONS

### Your PR will be REJECTED if:

1. **Missing Swagger documentation**
   - No @Operation annotation
   - Missing @ApiResponse annotations
   - No example values

2. **Missing integration tests**
   - Endpoint has no test
   - Only happy path tested
   - Missing error scenarios

3. **Missing unit tests**
   - Service method has no test
   - Only successful scenario tested
   - No exception testing

4. **Low test coverage**
   - Coverage below 70%
   - Critical paths not covered

5. **Tests not passing**
   - Failing tests in CI/CD
   - Tests pass locally but fail in CI

6. **Incomplete tests**
   - Missing authentication test (401)
   - Missing authorization test (403)
   - Missing validation test (400)

---

## 💯 EXAMPLE: COMPLETE FEATURE IMPLEMENTATION

**Task:** Add new endpoint to get students by faculty

### Step 1: Controller with FULL Swagger documentation

```java
@Operation(
    summary = "Get students by faculty ID",
    description = "Retrieves all students belonging to a specific faculty with pagination"
)
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
    @PathVariable Long facultyId,
    @PageableDefault(size = 20) Pageable pageable
) {
    Page<StudentDto> students = studentService.findByFacultyId(facultyId, pageable);
    return ResponseEntity.ok(ResponseWrapper.success(students));
}
```

### Step 2: Service with business logic

```java
@Override
@ReadOnly
@Cacheable(value = "studentsByFaculty", key = "#facultyId + '_' + #pageable.pageNumber")
public Page<StudentDto> findByFacultyId(Long facultyId, Pageable pageable) {
    // Verify faculty exists
    if (!facultyRepository.existsById(facultyId)) {
        throw new ResourceNotFoundException("Faculty not found: " + facultyId);
    }
    
    Page<Student> students = studentRepository.findByFacultyId(facultyId, pageable);
    return students.map(studentMapper::toDto);
}
```

### Step 3: COMPLETE integration test

```java
@Test
@DisplayName("GET /faculty/{id} - Success (200)")
@WithMockUser(authorities = {"students.view"})
void getStudentsByFaculty_WhenExists_Returns200() throws Exception {
    mockMvc.perform(get("/api/v1/web/students/faculty/1")
            .param("page", "0")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.totalElements").exists());
}

@Test
@DisplayName("GET /faculty/{id} - Not Found (404)")
@WithMockUser(authorities = {"students.view"})
void getStudentsByFaculty_WhenFacultyNotExists_Returns404() throws Exception {
    mockMvc.perform(get("/api/v1/web/students/faculty/999"))
        .andExpect(status().isNotFound());
}

@Test
@DisplayName("GET /faculty/{id} - Unauthorized (401)")
void getStudentsByFaculty_WithoutAuth_Returns401() throws Exception {
    mockMvc.perform(get("/api/v1/web/students/faculty/1"))
        .andExpect(status().isUnauthorized());
}

@Test
@DisplayName("GET /faculty/{id} - Forbidden (403)")
@WithMockUser(authorities = {"other.permission"})
void getStudentsByFaculty_WithoutPermission_Returns403() throws Exception {
    mockMvc.perform(get("/api/v1/web/students/faculty/1"))
        .andExpect(status().isForbidden());
}
```

### Step 4: COMPLETE unit test

```java
@Test
void findByFacultyId_WhenFacultyExists_ReturnsStudents() {
    // Given
    Long facultyId = 1L;
    Pageable pageable = PageRequest.of(0, 20);
    List<Student> students = List.of(testStudent);
    Page<Student> studentPage = new PageImpl<>(students, pageable, 1);
    
    when(facultyRepository.existsById(facultyId)).thenReturn(true);
    when(studentRepository.findByFacultyId(facultyId, pageable)).thenReturn(studentPage);
    when(studentMapper.toDto(any())).thenReturn(testStudentDto);
    
    // When
    Page<StudentDto> result = studentService.findByFacultyId(facultyId, pageable);
    
    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(facultyRepository).existsById(facultyId);
    verify(studentRepository).findByFacultyId(facultyId, pageable);
}

@Test
void findByFacultyId_WhenFacultyNotExists_ThrowsException() {
    // Given
    Long facultyId = 999L;
    when(facultyRepository.existsById(facultyId)).thenReturn(false);
    
    // When & Then
    assertThrows(ResourceNotFoundException.class, 
        () -> studentService.findByFacultyId(facultyId, PageRequest.of(0, 20)));
    verify(studentRepository, never()).findByFacultyId(any(), any());
}
```

### Step 5: Test in Swagger UI

1. Start application: `./gradlew :app:bootRun`
2. Open: http://localhost:8080/api/swagger-ui.html
3. Find: `GET /api/v1/web/students/faculty/{facultyId}`
4. Click "Try it out"
5. Enter facultyId: 1
6. Click "Execute"
7. Verify response matches documentation

✅ **NOW your feature is COMPLETE and ready for PR!**

---

## 🎯 SUMMARY

```
┌─────────────────────────────────────────────────────┐
│          MANDATORY REQUIREMENTS SUMMARY             │
├─────────────────────────────────────────────────────┤
│ 1. Swagger documentation - 100% coverage           │
│ 2. Integration tests - EVERY endpoint              │
│ 3. Unit tests - EVERY service method               │
│ 4. Test coverage - Minimum 70%                     │
│ 5. All tests passing - No exceptions               │
│ 6. Test in Swagger UI - Before commit              │
│ 7. Code review checklist - Complete                │
└─────────────────────────────────────────────────────┘

VIOLATION = CODE REVIEW REJECTED = REWORK REQUIRED
```

**Remember:** These are NOT suggestions. These are REQUIREMENTS!

---

**Last Updated:** 2025-11-15  
**Enforced By:** CI/CD Pipeline + Code Review  
**No Exceptions Allowed:** ❌
