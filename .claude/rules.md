# HEMIS Backend - Coding Standards & Rules

> **Mandatory rules for ALL code contributions**  
> **Version:** 1.0.0  
> **Last Updated:** 2025-11-15

---

## 🎯 Core Principles

### Golden Rules (NEVER VIOLATE)

```
1. STABILITY FIRST
   - Existing functionality MUST NOT break
   - Backward compatibility is mandatory
   - Legacy APIs remain unchanged

2. NO SCHEMA CHANGES
   - NO ALTER TABLE on ministry.sql schema
   - NO DROP operations
   - NO RENAME operations on existing tables/columns
   
3. SERVICE LAYER IS MANDATORY
   - Controllers → Services → Repositories
   - NO direct repository calls from controllers
   - ALL business logic in service layer
   
4. SECURITY BY DEFAULT
   - ALL endpoints require authentication (except public)
   - Input validation on ALL user data
   - NO raw SQL queries (use JPA)

5. SWAGGER DOCUMENTATION MANDATORY
   - EVERY endpoint MUST have Swagger annotations
   - @Operation, @ApiResponse, @Parameter are REQUIRED
   - Swagger UI must show complete API documentation
   
6. TESTING MANDATORY
   - EVERY endpoint MUST have integration tests
   - EVERY service method MUST have unit tests
   - Test coverage MINIMUM 70%
   - NO pull request without tests

7. DATABASE MIGRATIONS MANDATORY
   - EVERY schema change via Liquibase ONLY
   - IDEMPOTENCY required (safe to run multiple times)
   - ROLLBACK script required for EVERY migration
   - NO direct ALTER/CREATE/DROP on database
   - Test locally, then staging, NEVER directly on production
```

---

## 📋 Module-Specific Rules

### Module: `common`
**Purpose:** Shared utilities, DTOs, exceptions

#### Rules
```java
✅ DO:
- Create DTOs with Jackson annotations
- Use @Data, @Builder from Lombok
- Immutable DTOs preferred (use @Value)
- Add JavaDoc for public APIs

❌ DON'T:
- Add Spring dependencies (keep it POJO)
- Create service/repository classes here
- Use entity classes (belongs in domain)

Example:
@Data
@Builder
public class StudentDto {
    private Long id;
    
    @JsonProperty("first_name")  // Legacy field name
    private String firstName;
    
    @NotBlank(message = "First name is required")
    private String lastName;
}
```

---

### Module: `domain`
**Purpose:** JPA entities, repositories, Liquibase migrations

#### Entity Rules
```java
✅ DO:
- Use @Table(name="legacy_table_name") EXACTLY as in ministry.sql
- Use @Column(name="legacy_column") for ALL fields
- Extend BaseEntity for audit fields (created_at, updated_at)
- Use Lombok @Entity, @Table, @Getter, @Setter
- Add database indexes: @Table(indexes = @Index(...))

❌ DON'T:
- Change table/column names (legacy compatibility!)
- Add @GeneratedValue on existing tables
- Use CascadeType.REMOVE (data integrity risk)
- Add @OnDelete annotations

Example:
@Entity
@Table(name = "hemishe_e_student")
@Getter
@Setter
public class Student extends BaseEntity {
    
    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;
}
```

#### Repository Rules
```java
✅ DO:
- Extend JpaRepository<Entity, ID>
- Use method naming convention (findByFirstName)
- Add @Query for complex queries
- Use Pageable for large result sets
- Add @ReadOnly annotation for replica routing

❌ DON'T:
- Override delete() methods (soft delete preferred)
- Use native queries without necessity
- Fetch EAGER relationships (performance!)
- Return entities directly to controller (use DTOs)

Example:
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    @ReadOnly
    Page<Student> findByFacultyId(Long facultyId, Pageable pageable);
    
    @Query("SELECT s FROM Student s WHERE s.email = :email")
    Optional<Student> findByEmail(@Param("email") String email);
    
    // ❌ NO: void deleteById(Long id);
    // ✅ Use: void softDeleteById(Long id);
}
```

#### Liquibase Migration Rules
```yaml
✅ DO:
- Create changesets with unique IDs (YYYYMMDD-sequence)
- Add rollback scripts for ALL changes
- Tag major versions (v5-*, v6-*)
- Test rollback before production
- Use YAML format (more readable)

❌ DON'T:
- Modify existing changesets (create new ones)
- Skip rollback scripts
- Use SQL without validation
- Directly run DDL on production

Example Changeset:
# domain/src/main/resources/db/changelog/changesets/20251115-01-add-student-photo.yaml
databaseChangeLog:
  - changeSet:
      id: 20251115-01-add-student-photo
      author: developer_name
      changes:
        - addColumn:
            tableName: hemishe_e_student
            columns:
              - column:
                  name: photo_url
                  type: varchar(255)
      rollback:
        - dropColumn:
            tableName: hemishe_e_student
            columnName: photo_url
```

---

### Module: `security`
**Purpose:** Authentication, authorization, JWT

#### Security Configuration Rules
```java
✅ DO:
- Use @PreAuthorize on service methods (not controllers)
- Validate permissions via custom validators
- Use JWT for stateless auth
- Cache user permissions in Redis (L1) + Caffeine (L2)
- Hash passwords with BCrypt (strength 10+)

❌ DON'T:
- Store passwords in plaintext EVER
- Use basic authentication (use JWT)
- Skip permission checks
- Hardcode credentials
- Log sensitive data (passwords, tokens)

Example:
@Service
public class StudentService {
    
    @PreAuthorize("hasAuthority('students.view')")
    public StudentDto findById(Long id) {
        // Implementation
    }
    
    @PreAuthorize("hasAuthority('students.create')")
    @Transactional
    public StudentDto create(@Valid StudentCreateDto dto) {
        // Implementation
    }
}
```

#### Password Encoding
```java
// LegacyPasswordEncoder usage
@Autowired
private LegacyPasswordEncoder passwordEncoder;

// For NEW users
String hashedPassword = passwordEncoder.encode(rawPassword);
// Result: $2a$10$N9qo8uLOickgx2ZMRZoMye...

// For authentication (supports both BCrypt and PBKDF2)
boolean matches = passwordEncoder.matches(rawPassword, storedHash);
```

---

### Module: `service`
**Purpose:** Business logic, validation, transactions

#### Service Layer Rules
```java
✅ DO:
- Annotate with @Service
- Use @Transactional for write operations
- Validate input with @Valid
- Convert entities to DTOs before returning
- Handle exceptions gracefully
- Log important actions (audit trail)

❌ DON'T:
- Return entities directly (always DTOs)
- Catch exceptions without logging
- Use System.out.println (use Logger)
- Mix business logic with infrastructure code
- Create transactions in controllers

Example:
@Service
@Slf4j
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    
    @Override
    @Transactional(readOnly = true)
    @ReadOnly  // Route to replica
    public Page<StudentDto> findAll(Pageable pageable) {
        log.debug("Finding all students, page: {}", pageable.getPageNumber());
        
        Page<Student> students = studentRepository.findAll(pageable);
        return students.map(studentMapper::toDto);
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('students.create')")
    public StudentDto create(@Valid StudentCreateDto dto) {
        log.info("Creating student: {}", dto.getEmail());
        
        // Validation
        if (studentRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Email already exists");
        }
        
        // Map and save
        Student student = studentMapper.toEntity(dto);
        Student saved = studentRepository.save(student);
        
        log.info("Student created successfully, id: {}", saved.getId());
        return studentMapper.toDto(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    @ReadOnly
    public StudentDto findById(Long id) {
        return studentRepository.findById(id)
            .map(studentMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Student not found with id: " + id
            ));
    }
}
```

---

### Module: `api-web`, `api-legacy`, `api-external`
**Purpose:** REST controllers (presentation layer)

#### Controller Rules
```java
✅ DO:
- Use @RestController + @RequestMapping
- Return ResponseEntity<ResponseWrapper<T>>
- Add Swagger annotations (@Operation, @Tag, @ApiResponse, @Parameter) - MANDATORY!
- Validate input with @Valid
- Use standard HTTP status codes
- Add pagination support (Pageable)
- Log requests (INFO level)
- Write integration tests for EVERY endpoint - MANDATORY!

❌ DON'T:
- Add business logic in controllers
- Return entities (use DTOs)
- Use query parameters for sensitive data
- Skip input validation
- Return 200 OK for all responses
- Expose internal exceptions
- Skip Swagger documentation - THIS IS FORBIDDEN!
- Skip integration tests - NO EXCEPTIONS!

Example (api-web):
@RestController
@RequestMapping("/api/v1/web/students")
@Tag(name = "Students", description = "Student management API")
@RequiredArgsConstructor
@Slf4j
public class StudentController {
    
    private final StudentService studentService;
    
    @GetMapping
    @Operation(summary = "Get all students with pagination")
    @PreAuthorize("hasAuthority('students.view')")
    public ResponseEntity<ResponseWrapper<Page<StudentDto>>> findAll(
        @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        log.info("GET /api/v1/web/students - page: {}", pageable.getPageNumber());
        
        Page<StudentDto> students = studentService.findAll(pageable);
        return ResponseEntity.ok(ResponseWrapper.success(students));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get student by ID")
    @PreAuthorize("hasAuthority('students.view')")
    public ResponseEntity<ResponseWrapper<StudentDto>> findById(
        @PathVariable Long id
    ) {
        log.info("GET /api/v1/web/students/{}", id);
        
        StudentDto student = studentService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(student));
    }
    
    @PostMapping
    @Operation(summary = "Create new student")
    @PreAuthorize("hasAuthority('students.create')")
    public ResponseEntity<ResponseWrapper<StudentDto>> create(
        @Valid @RequestBody StudentCreateDto dto
    ) {
        log.info("POST /api/v1/web/students - email: {}", dto.getEmail());
        
        StudentDto created = studentService.create(dto);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseWrapper.success(created));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update student")
    @PreAuthorize("hasAuthority('students.edit')")
    public ResponseEntity<ResponseWrapper<StudentDto>> update(
        @PathVariable Long id,
        @Valid @RequestBody StudentUpdateDto dto
    ) {
        log.info("PUT /api/v1/web/students/{}", id);
        
        StudentDto updated = studentService.update(id, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }
}
```

#### api-legacy Compatibility Rules
```java
✅ DO:
- Maintain exact same JSON structure as OLD-HEMIS
- Use legacy field names (_instanceName, _entityName)
- Return same HTTP status codes
- Support existing query parameters

❌ DON'T:
- Change endpoint URLs (/app/rest/v2/*)
- Modify response format
- Remove fields from responses
- Change error message format

Example:
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_Student")
public class LegacyStudentController {
    // CUBA-style API endpoints
    // MUST maintain backward compatibility!
}
```

---

## 🔧 Technical Standards

### Exception Handling

```java
✅ DO: Custom exception hierarchy

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class ValidationException extends RuntimeException {
    private List<String> errors;
    // ...
}

public class UnauthorizedException extends RuntimeException {
    // ...
}

// Global handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .code("RESOURCE_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

### Validation Rules

```java
✅ DO: Bean Validation (JSR-380)

public class StudentCreateDto {
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be 2-100 characters")
    private String firstName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Faculty ID is required")
    @Positive(message = "Faculty ID must be positive")
    private Long facultyId;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
}

// Custom validator
@Constraint(validatedBy = UniqueEmailValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Transaction Management

```java
✅ DO: Use @Transactional correctly

// Read-only (routes to replica)
@Transactional(readOnly = true)
@ReadOnly
public StudentDto findById(Long id) {
    // SELECT query → replica database
}

// Write operations (routes to master)
@Transactional
public StudentDto create(StudentCreateDto dto) {
    // INSERT query → master database
    // Auto-rollback on exception
}

// Multiple operations in single transaction
@Transactional
public void enrollStudent(Long studentId, Long curriculumId) {
    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    
    Curriculum curriculum = curriculumRepository.findById(curriculumId)
        .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found"));
    
    Enrollment enrollment = new Enrollment();
    enrollment.setStudent(student);
    enrollment.setCurriculum(curriculum);
    enrollmentRepository.save(enrollment);
    
    // Both operations succeed or both rollback
}

❌ DON'T:
- Forget @Transactional on write operations
- Use propagation = REQUIRES_NEW without reason
- Catch exceptions inside @Transactional (breaks rollback)
```

### Logging Standards

```java
✅ DO: Use SLF4J + Logback

@Slf4j
public class StudentService {
    
    public StudentDto create(StudentCreateDto dto) {
        log.debug("Creating student: {}", dto);  // Development details
        log.info("Student created: email={}", dto.getEmail());  // Production audit
        log.warn("Duplicate email attempt: {}", dto.getEmail());  // Suspicious activity
        log.error("Failed to create student", exception);  // Errors with stacktrace
    }
}

Log Levels:
- TRACE: Very detailed (SQL queries, method entry/exit)
- DEBUG: Development debugging
- INFO:  Important business events
- WARN:  Unexpected but recoverable situations
- ERROR: Application errors requiring attention

❌ DON'T:
- Use System.out.println()
- Log sensitive data (passwords, tokens, PII)
- Log in loops (performance impact)
- Use INFO for debugging details
```

### MapStruct Entity ↔ DTO Conversion

```java
✅ DO: Use MapStruct for clean mapping

@Mapper(componentModel = "spring")
public interface StudentMapper {
    
    StudentDto toDto(Student entity);
    
    List<StudentDto> toDtoList(List<Student> entities);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Student toEntity(StudentCreateDto dto);
    
    @Mapping(target = "faculty", ignore = true)  // Prevent lazy loading
    StudentDto toDtoWithoutFaculty(Student entity);
}

// Usage in service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository repository;
    private final StudentMapper mapper;
    
    public StudentDto findById(Long id) {
        Student entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Not found"));
        return mapper.toDto(entity);  // Auto-conversion
    }
}
```

---

## 📐 Code Style

### Java Code Style

```java
✅ DO:

// Class naming: PascalCase
public class StudentService { }
public class UserRepository { }

// Method naming: camelCase (verb + noun)
public StudentDto findById(Long id) { }
public void createStudent(StudentDto dto) { }
public boolean existsByEmail(String email) { }

// Variable naming: camelCase (noun)
String firstName;
LocalDateTime createdAt;
List<Student> activeStudents;

// Constants: UPPER_SNAKE_CASE
public static final String DEFAULT_LANGUAGE = "uz-UZ";
public static final int MAX_PAGE_SIZE = 100;

// Package naming: lowercase
uz.hemis.domain.entity
uz.hemis.service.impl

// Indentation: 4 spaces (NO tabs)
public void method() {
    if (condition) {
        doSomething();
    }
}

// Braces: Same line (Java standard)
if (condition) {
    // code
} else {
    // code
}

// Line length: Max 120 characters
// Break long lines at logical points

// Import order:
// 1. Java standard library
// 2. Third-party libraries
// 3. Spring Framework
// 4. Project packages
import java.util.List;
import org.springframework.stereotype.Service;
import uz.hemis.domain.entity.Student;
```

### Lombok Usage

```java
✅ DO: Use Lombok to reduce boilerplate

// Entities
@Entity
@Table(name = "student")
@Getter
@Setter
public class Student {
    @Id
    private Long id;
    private String firstName;
}

// DTOs
@Data
@Builder
public class StudentDto {
    private Long id;
    private String firstName;
}

// Immutable DTOs
@Value
@Builder
public class StudentResponse {
    Long id;
    String firstName;
}

// Services
@Service
@RequiredArgsConstructor  // Constructor injection
@Slf4j  // Logger
public class StudentService {
    private final StudentRepository repository;
}

❌ DON'T:
- Use @Data on entities (breaks lazy loading)
- Use @ToString on entities with relationships (StackOverflowError)
- Use @EqualsAndHashCode on JPA entities (Hibernate issues)
```

---

## 🧪 Testing Standards (MANDATORY!)

### ⚠️ CRITICAL TESTING RULES

```
1. EVERY endpoint MUST have integration test
2. EVERY service method MUST have unit test
3. Test coverage MINIMUM 70%
4. NO pull request without tests
5. Tests MUST pass before merge
6. Failed tests block deployment
```

### Test Coverage Requirements

```
Module              Minimum Coverage    Status
────────────────────────────────────────────────
Controllers         80%                 ⚠️ MANDATORY
Services            90%                 ⚠️ MANDATORY
Repositories        70%                 ⚠️ MANDATORY
Domain Entities     60%                 ⚠️ MANDATORY
Common/Utils        80%                 ⚠️ MANDATORY
────────────────────────────────────────────────
Overall             70%                 ⚠️ MANDATORY
```

### Unit Testing (MANDATORY!)

```java
✅ DO: Write unit tests for services - NO EXCEPTIONS!

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    
    @Mock
    private StudentRepository repository;
    
    @Mock
    private StudentMapper mapper;
    
    @InjectMocks
    private StudentServiceImpl service;
    
    @Test
    void findById_WhenExists_ReturnsStudent() {
        // Given
        Long id = 1L;
        Student entity = new Student();
        entity.setId(id);
        StudentDto dto = new StudentDto();
        
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);
        
        // When
        StudentDto result = service.findById(id);
        
        // Then
        assertNotNull(result);
        verify(repository).findById(id);
        verify(mapper).toDto(entity);
    }
    
    @Test
    void findById_WhenNotExists_ThrowsException() {
        // Given
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(
            ResourceNotFoundException.class,
            () -> service.findById(id)
        );
    }
}
```

### Integration Testing (MANDATORY FOR EVERY ENDPOINT!)

```java
✅ DO: Test controller + service + repository - REQUIRED FOR EVERY ENDPOINT!

⚠️ CRITICAL: Integration test is MANDATORY for EVERY REST endpoint!

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // ⚠️ MANDATORY: Test EVERY endpoint scenario!
    
    @Test
    @DisplayName("GET /api/v1/web/students/{id} - Success (200)")
    @WithMockUser(authorities = {"students.view"})
    void getStudent_WhenExists_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }
    
    @Test
    @DisplayName("GET /api/v1/web/students/{id} - Not Found (404)")
    @WithMockUser(authorities = {"students.view"})
    void getStudent_WhenNotExists_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    @DisplayName("GET /api/v1/web/students/{id} - Unauthorized (401)")
    void getStudent_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("GET /api/v1/web/students/{id} - Forbidden (403)")
    @WithMockUser(authorities = {"wrong.permission"})
    void getStudent_WithoutPermission_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/web/students/1"))
            .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("POST /api/v1/web/students - Success (201)")
    @WithMockUser(authorities = {"students.create"})
    void createStudent_WithValidData_Returns201() throws Exception {
        StudentCreateDto dto = StudentCreateDto.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john@example.com")
            .facultyId(1L)
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").exists());
    }
    
    @Test
    @DisplayName("POST /api/v1/web/students - Validation Error (400)")
    @WithMockUser(authorities = {"students.create"})
    void createStudent_WithInvalidData_Returns400() throws Exception {
        StudentCreateDto dto = StudentCreateDto.builder()
            .firstName("")  // Invalid - blank
            .lastName("Doe")
            .email("invalid-email")  // Invalid - wrong format
            .build();
        
        mockMvc.perform(post("/api/v1/web/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
    
    @Test
    @DisplayName("PUT /api/v1/web/students/{id} - Success (200)")
    @WithMockUser(authorities = {"students.edit"})
    void updateStudent_WhenExists_Returns200() throws Exception {
        StudentUpdateDto dto = StudentUpdateDto.builder()
            .firstName("Jane")
            .lastName("Smith")
            .build();
        
        mockMvc.perform(put("/api/v1/web/students/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @DisplayName("DELETE /api/v1/web/students/{id} - Success (200)")
    @WithMockUser(authorities = {"students.delete"})
    void deleteStudent_WhenExists_Returns200() throws Exception {
        mockMvc.perform(delete("/api/v1/web/students/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

### Integration Test Checklist (MANDATORY FOR EVERY ENDPOINT!)

**Before creating Pull Request, verify ALL tests exist:**

```
☑ Test successful scenario (200/201)
☑ Test not found scenario (404)
☑ Test unauthorized scenario (401)
☑ Test forbidden scenario (403)
☑ Test validation errors (400)
☑ Test with invalid input
☑ Test with missing required fields
☑ Test with pagination (if applicable)
☑ Test with filters (if applicable)
☑ Test response structure matches Swagger
```

---

## 🚀 Performance Guidelines

### Database Performance

```java
✅ DO:

// Use pagination for large result sets
Page<Student> findAll(Pageable pageable);

// Use projection for specific fields
@Query("SELECT new StudentSummaryDto(s.id, s.firstName) FROM Student s")
List<StudentSummaryDto> findAllSummary();

// Batch operations
@Modifying
@Query("UPDATE Student s SET s.status = :status WHERE s.faculty.id = :facultyId")
int updateStatusByFaculty(@Param("status") String status, @Param("facultyId") Long facultyId);

// Use @ReadOnly annotation to route to replica
@ReadOnly
List<Student> findByFacultyId(Long facultyId);

❌ DON'T:
// Avoid N+1 queries
// BAD:
List<Student> students = studentRepository.findAll();
students.forEach(s -> s.getFaculty().getName());  // N+1!

// GOOD:
@Query("SELECT s FROM Student s JOIN FETCH s.faculty")
List<Student> findAllWithFaculty();

// Avoid loading large collections
// Use pagination or projection instead
```

### Caching Strategy

```java
✅ DO:

@Service
public class StudentService {
    
    // Cache frequently accessed data
    @Cacheable(value = "students", key = "#id")
    public StudentDto findById(Long id) {
        // Cache miss → query database
        // Cache hit → return from cache
    }
    
    // Invalidate cache on updates
    @CacheEvict(value = "students", key = "#id")
    public StudentDto update(Long id, StudentUpdateDto dto) {
        // Update database
        // Evict from cache
    }
    
    // Cache list results with pagination
    @Cacheable(value = "studentsList", 
               key = "#facultyId + '_' + #pageable.pageNumber")
    public Page<StudentDto> findByFaculty(Long facultyId, Pageable pageable) {
        // Cached per faculty + page
    }
}

Cache Configuration:
- L1 (JVM): Caffeine (fast, per-instance)
- L2 (Distributed): Redis (shared across instances)
- TTL: 5 minutes (reference data), 1 hour (static data)
```

---

## 📝 Documentation Standards

### JavaDoc

```java
✅ DO: Document public APIs

/**
 * Creates a new student in the system.
 * 
 * @param dto the student creation data
 * @return the created student with generated ID
 * @throws ValidationException if email already exists
 * @throws UnauthorizedException if user lacks 'students.create' permission
 */
@PreAuthorize("hasAuthority('students.create')")
public StudentDto create(@Valid StudentCreateDto dto);

/**
 * Finds student by ID.
 * 
 * @param id the student ID
 * @return the student DTO
 * @throws ResourceNotFoundException if student not found
 */
public StudentDto findById(Long id);
```

### Swagger/OpenAPI (MANDATORY!)

```java
✅ DO: Add comprehensive API documentation - THIS IS REQUIRED!

@RestController
@Tag(name = "Students", description = "Student management operations")
public class StudentController {
    
    // ⚠️ CRITICAL: ALL these annotations are MANDATORY!
    @Operation(
        summary = "Get student by ID",
        description = "Retrieves detailed information about a specific student"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Student found",
            content = @Content(schema = @Schema(implementation = StudentDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Student not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - token missing or invalid"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> findById(
        @Parameter(description = "Student ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        // Implementation
    }
}

❌ FORBIDDEN: Endpoints without Swagger documentation
// This will be REJECTED in code review:
@GetMapping("/{id}")
public ResponseEntity<StudentDto> findById(@PathVariable Long id) {
    // NO Swagger annotations - FORBIDDEN!
}
```

### Swagger Documentation Checklist (MANDATORY)

**Before creating Pull Request, verify:**

```
☑ @Tag on controller class
☑ @Operation on EVERY endpoint
☑ @ApiResponses for ALL possible HTTP status codes
☑ @Parameter on ALL path variables and query params
☑ @RequestBody with schema for POST/PUT endpoints
☑ Example values in @Parameter and @Schema
☑ Description for ALL parameters
☑ Test in Swagger UI before commit
```

---

## 🔒 Security Rules

### Input Validation

```java
✅ DO: Validate ALL user input

// Use Bean Validation
public class StudentCreateDto {
    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;
    
    @Email
    @NotBlank
    private String email;
    
    @Pattern(regexp = "^\\+998\\d{9}$", message = "Invalid phone number")
    private String phone;
}

// Validate in controller
@PostMapping
public ResponseEntity<StudentDto> create(
    @Valid @RequestBody StudentCreateDto dto  // @Valid triggers validation
) {
    // ...
}

// Custom validation in service
public void validateUniqueness(String email) {
    if (repository.existsByEmail(email)) {
        throw new ValidationException("Email already exists");
    }
}
```

### SQL Injection Prevention

```java
✅ DO: Use JPA with parameterized queries

// SAFE: JPA named parameter
@Query("SELECT s FROM Student s WHERE s.email = :email")
Optional<Student> findByEmail(@Param("email") String email);

// SAFE: Method naming convention
Optional<Student> findByEmail(String email);

❌ DON'T: Native queries with string concatenation

// DANGEROUS! SQL Injection vulnerability
@Query(value = "SELECT * FROM student WHERE email = '" + email + "'", nativeQuery = true)
List<Student> findByEmailUnsafe(String email);

// If native query is necessary, use parameters:
@Query(value = "SELECT * FROM student WHERE email = ?1", nativeQuery = true)
List<Student> findByEmailSafe(String email);
```

### Authorization Checks

```java
✅ DO: Check permissions at service layer

@Service
public class StudentService {
    
    @PreAuthorize("hasAuthority('students.create')")
    public StudentDto create(StudentCreateDto dto) {
        // Method only executes if user has permission
    }
    
    @PreAuthorize("hasAnyAuthority('students.edit', 'students.admin')")
    public StudentDto update(Long id, StudentUpdateDto dto) {
        // Multiple permissions (OR logic)
    }
    
    @PreAuthorize("hasAuthority('students.view') and #id == authentication.principal.studentId")
    public StudentDto findOwnRecord(Long id) {
        // Students can only view their own record
    }
}
```

---

## 🎨 Response Format Standards

### Success Response

```java
public class ResponseWrapper<T> {
    private boolean success = true;
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> ResponseWrapper<T> success(T data) {
        ResponseWrapper<T> response = new ResponseWrapper<>();
        response.setSuccess(true);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}

// Usage in controller
return ResponseEntity.ok(ResponseWrapper.success(studentDto));

// JSON output:
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "John"
  },
  "timestamp": "2025-11-15T08:00:00"
}
```

### Error Response

```java
public class ErrorResponse {
    private boolean success = false;
    private ErrorDetail error;
    private LocalDateTime timestamp;
}

public class ErrorDetail {
    private String code;
    private String message;
    private List<String> details;
}

// JSON output:
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": [
      "firstName: must not be blank",
      "email: must be a valid email address"
    ]
  },
  "timestamp": "2025-11-15T08:00:00"
}
```

---

## ⚡ Git Commit Standards

### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>

Types:
- feat:     New feature
- fix:      Bug fix
- refactor: Code refactoring
- docs:     Documentation
- test:     Tests
- chore:    Build/config changes
- perf:     Performance improvement

Examples:

feat(student): add student enrollment API

- Add POST /api/v1/web/students/{id}/enroll
- Validate curriculum prerequisites
- Send confirmation email

Closes #123

---

fix(security): prevent password enumeration

- Return generic error message for invalid login
- Add rate limiting to login endpoint
- Log suspicious login attempts

Fixes #456

---

refactor(service): extract common validation logic

- Create BaseValidator abstract class
- Reduce code duplication in 5 services
- No functional changes
```

---

## 🚫 Common Mistakes to Avoid

```java
❌ WRONG:
// Returning entity from controller
@GetMapping("/{id}")
public Student findById(@PathVariable Long id) {
    return studentRepository.findById(id).orElse(null);
}

✅ CORRECT:
@GetMapping("/{id}")
public ResponseEntity<ResponseWrapper<StudentDto>> findById(@PathVariable Long id) {
    StudentDto dto = studentService.findById(id);
    return ResponseEntity.ok(ResponseWrapper.success(dto));
}

---

❌ WRONG:
// Business logic in controller
@PostMapping
public ResponseEntity<Student> create(@RequestBody StudentDto dto) {
    if (repository.existsByEmail(dto.getEmail())) {
        throw new RuntimeException("Email exists");
    }
    Student student = new Student();
    student.setFirstName(dto.getFirstName());
    // ... more mapping
    return ResponseEntity.ok(repository.save(student));
}

✅ CORRECT:
@PostMapping
public ResponseEntity<ResponseWrapper<StudentDto>> create(
    @Valid @RequestBody StudentCreateDto dto
) {
    StudentDto created = studentService.create(dto);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ResponseWrapper.success(created));
}

---

❌ WRONG:
// Catching exceptions without logging
try {
    studentService.create(dto);
} catch (Exception e) {
    // Silently ignore - BAD!
}

✅ CORRECT:
try {
    studentService.create(dto);
} catch (ValidationException e) {
    log.warn("Validation failed: {}", e.getMessage());
    throw e;  // Re-throw to be handled by @ControllerAdvice
} catch (Exception e) {
    log.error("Unexpected error creating student", e);
    throw new InternalServerErrorException("Failed to create student");
}

---

❌ WRONG:
// N+1 query problem
List<Student> students = studentRepository.findAll();
students.forEach(s -> {
    Faculty faculty = s.getFaculty();  // Lazy loading! N queries!
    log.info("Student {} in {}", s.getName(), faculty.getName());
});

✅ CORRECT:
@Query("SELECT s FROM Student s JOIN FETCH s.faculty")
List<Student> findAllWithFaculty();

List<Student> students = studentRepository.findAllWithFaculty();
students.forEach(s -> {
    log.info("Student {} in {}", s.getName(), s.getFaculty().getName());
});
```

---

## 📚 Recommended Reading

### Books
- "Clean Code" by Robert C. Martin
- "Effective Java" by Joshua Bloch
- "Spring in Action" by Craig Walls
- "Domain-Driven Design" by Eric Evans

### Spring Boot Guides
- https://spring.io/guides
- https://docs.spring.io/spring-boot/docs/current/reference/html/
- https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

### Security
- OWASP Top 10: https://owasp.org/www-project-top-ten/
- Spring Security: https://docs.spring.io/spring-security/reference/

---

**Remember:** Code is read more often than it is written.  
**Write code that your future self (and teammates) will thank you for!**


---


See detailed guide: .claude/LIQUIBASE_GUIDE.md

### Critical Rules

```
⚠️ EVERY schema change MUST use Liquibase!

✅ REQUIRED:
- Idempotent migrations (IF NOT EXISTS, ON CONFLICT)
- Rollback script for EVERY migration
- Test locally → staging → production
- splitStatements: false for PostgreSQL DO blocks
- Sequential numbering (06, 07, 08...)

❌ FORBIDDEN:
- Direct ALTER/CREATE/DROP on database
- Migrations without rollback
- Non-idempotent migrations
- Testing on production directly
- Hardcoded UUIDs/timestamps
```

### Quick Commands

```bash
# Check status
./gradlew :domain:liquibaseStatus

# Apply migrations
./gradlew :domain:liquibaseUpdate

# Rollback (count = migrations × 2)
./gradlew :domain:liquibaseRollbackCount -Pcount=2

# Preview rollback
./gradlew :domain:liquibaseRollbackSQL -Pcount=2
```

**For complete migration guide, see:** `.claude/LIQUIBASE_GUIDE.md`

---

