# HEMIS Backend - Architecture Documentation

> **Complete architectural blueprint for developers**  
> **Version:** 1.0.0  
> **Pattern:** Modular Monolith + Clean Architecture  
> **Last Updated:** 2025-11-15

---

## 📐 Architecture Overview

### System Type
**Modular Monolith** - Single deployable unit with independent modules

**Why not Microservices?**
- ✅ Simpler deployment
- ✅ Easier debugging
- ✅ Lower infrastructure costs
- ✅ ACID transactions across modules
- ✅ No network latency between modules
- 🔄 Can extract to microservices later if needed

**Why not Classic Monolith?**
- ❌ Tight coupling between components
- ❌ Hard to test independently
- ❌ Difficult to assign teams to modules
- ❌ Merge conflicts in large codebase

---

## 🏛️ Clean Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                    │
│  ┌────────────┬────────────┬──────────────────────┐    │
│  │ api-legacy │  api-web   │   api-external       │    │
│  │ (REST)     │ (REST)     │   (REST)             │    │
│  │ Port: 8080 │ Port: 8080 │   Port: 8080         │    │
│  └──────┬─────┴──────┬─────┴──────┬───────────────┘    │
│         │            │            │                     │
│         └────────────┴────────────┘                     │
│                      ↓                                  │
├─────────────────────────────────────────────────────────┤
│                  Application Layer                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │  app (Main Spring Boot Application)             │  │
│  │  - Spring Boot @Configuration                   │  │
│  │  - DataSource configuration (master/replica)    │  │
│  │  - Security configuration (OAuth2 + JWT)        │  │
│  │  - Exception handlers (@ControllerAdvice)       │  │
│  │  - Auth endpoints (login, logout, refresh)      │  │
│  └──────────────────────────────────────────────────┘  │
│                      ↓                                  │
├─────────────────────────────────────────────────────────┤
│                   Business Layer                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  service (Business Logic)                       │  │
│  │  - Use cases implementation                     │  │
│  │  - Business rules validation                    │  │
│  │  - Transaction management                       │  │
│  │  - Authorization checks (@PreAuthorize)         │  │
│  │  - Entity ↔ DTO mapping (MapStruct)            │  │
│  └──────────────────────────────────────────────────┘  │
│                      ↓                                  │
├─────────────────────────────────────────────────────────┤
│                Infrastructure Layer                     │
│  ┌──────────┬──────────────────────────────────────┐  │
│  │ security │  domain (Persistence)                │  │
│  │          │  - JPA entities (@Entity)            │  │
│  │  - JWT   │  - Spring Data repositories          │  │
│  │  - OAuth │  - Database migrations (Liquibase)   │  │
│  │  - RBAC  │  - Master/Replica routing            │  │
│  └──────────┴──────────────────────────────────────┘  │
│                      ↓                                  │
├─────────────────────────────────────────────────────────┤
│                    Common Layer                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  common (Shared Kernel)                         │  │
│  │  - DTOs (Data Transfer Objects)                 │  │
│  │  - Custom exceptions                            │  │
│  │  - Utility classes                              │  │
│  │  - Constants & enums                            │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↓
                ┌──────────────────┐
                │  External Systems │
                ├──────────────────┤
                │ PostgreSQL 16    │
                │ Redis 7          │
                │ HEMIS Ministry   │
                │ OneID SSO        │
                │ Payment Gateway  │
                └──────────────────┘
```

---

## 📦 Module Structure

### Module Dependency Graph

```
                          app
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
    api-legacy         api-web         api-external
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                        service
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
     security           domain             common
        │                  │
        └──────────────────┘
                           │
                        common
```

**Dependency Rules:**
- ✅ Modules can depend on modules below them
- ❌ NO circular dependencies
- ❌ Lower layers CANNOT depend on higher layers
- ✅ `common` has ZERO dependencies

---

## 🗂️ Module Details

### Module: `common`
**Purpose:** Shared utilities, DTOs, exceptions

```
common/
├── src/main/java/uz/hemis/common/
│   ├── dto/              # Data Transfer Objects
│   │   ├── StudentDto.java
│   │   ├── FacultyDto.java
│   │   ├── ResponseWrapper.java
│   │   ├── ErrorResponse.java
│   │   └── PageResponse.java
│   ├── exception/        # Custom exceptions
│   │   ├── ResourceNotFoundException.java
│   │   ├── ValidationException.java
│   │   ├── UnauthorizedException.java
│   │   └── BadRequestException.java
│   ├── datasource/       # Master/Replica routing
│   │   ├── DataSourceType.java
│   │   ├── DataSourceContextHolder.java
│   │   ├── @ReadOnly.java
│   │   └── @WriteOnly.java
│   └── constants/        # Application constants
│       ├── SecurityConstants.java
│       └── ApiConstants.java
└── build.gradle.kts

Dependencies: NONE (pure Java + Lombok + Jackson)
```

**Key Classes:**
```java
// ResponseWrapper.java - Standard API response
public class ResponseWrapper<T> {
    private boolean success;
    private T data;
    private LocalDateTime timestamp;
}

// ErrorResponse.java - Error handling
public class ErrorResponse {
    private boolean success = false;
    private ErrorDetail error;
    private LocalDateTime timestamp;
}

// @ReadOnly - Route query to replica
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadOnly {
}
```

---

### Module: `domain`
**Purpose:** Persistence layer (JPA entities, repositories, migrations)

```
domain/
├── src/main/java/uz/hemis/domain/
│   ├── entity/              # JPA Entities (51 classes)
│   │   ├── Student.java     # @Entity @Table(name="hemishe_e_student")
│   │   ├── Faculty.java
│   │   ├── Curriculum.java
│   │   ├── Employee.java
│   │   ├── User.java        # New users (BCrypt)
│   │   ├── SecUser.java     # Legacy users (PBKDF2)
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   └── BaseEntity.java  # Audit fields (created_at, updated_at)
│   ├── repository/          # Spring Data JPA Repositories
│   │   ├── StudentRepository.java
│   │   ├── FacultyRepository.java
│   │   ├── UserRepository.java
│   │   └── (47 more repositories)
│   └── mapper/              # MapStruct Entity ↔ DTO
│       ├── StudentMapper.java
│       ├── FacultyMapper.java
│       └── (49 more mappers)
├── src/main/resources/
│   └── db/changelog/        # Liquibase migrations
│       ├── db.changelog-master.yaml
│       └── changesets/
│           ├── 20251101-01-create-schema.yaml
│           ├── 20251102-01-seed-data.yaml
│           ├── 20251103-01-user-migration.yaml
│           └── (5 changesets with rollback support)
└── build.gradle.kts

Dependencies: common, Spring Data JPA, PostgreSQL, Liquibase, MapStruct
```

**Entity Example:**
```java
@Entity
@Table(name = "hemishe_e_student", indexes = {
    @Index(name = "idx_student_email", columnList = "email"),
    @Index(name = "idx_student_faculty", columnList = "faculty_id")
})
@Getter
@Setter
public class Student extends BaseEntity {
    
    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;
    
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;
    
    @Column(name = "email", length = 255, unique = true, nullable = false)
    private String email;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id")
    private Curriculum curriculum;
}

// BaseEntity.java - Audit fields
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

**Repository Example:**
```java
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    // Method naming convention (auto-implemented by Spring Data)
    @ReadOnly
    Optional<Student> findByEmail(String email);
    
    @ReadOnly
    Page<Student> findByFacultyId(Long facultyId, Pageable pageable);
    
    // Custom query
    @Query("SELECT s FROM Student s WHERE s.status = :status")
    @ReadOnly
    List<Student> findActiveStudents(@Param("status") String status);
    
    // Native query (when necessary)
    @Query(value = "SELECT * FROM hemishe_e_student WHERE enrollment_year = ?1", 
           nativeQuery = true)
    @ReadOnly
    List<Student> findByEnrollmentYear(Integer year);
    
    // Count query
    @ReadOnly
    long countByFacultyId(Long facultyId);
    
    // Existence check
    @ReadOnly
    boolean existsByEmail(String email);
}
```

**MapStruct Mapper:**
```java
@Mapper(componentModel = "spring")
public interface StudentMapper {
    
    // Entity → DTO
    @Mapping(target = "facultyName", source = "faculty.name")
    StudentDto toDto(Student entity);
    
    // Entity List → DTO List
    List<StudentDto> toDtoList(List<Student> entities);
    
    // DTO → Entity (for creation)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "faculty", ignore = true)  // Set separately
    Student toEntity(StudentCreateDto dto);
    
    // Partial update (ignore nulls)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(StudentUpdateDto dto, @MappingTarget Student entity);
}
```

---

### Module: `security`
**Purpose:** Authentication, authorization, JWT

```
security/
├── src/main/java/uz/hemis/security/
│   ├── config/              # Security configuration
│   │   ├── SecurityConfig.java
│   │   ├── JwtGrantedAuthoritiesConverter.java
│   │   ├── RedisConfig.java
│   │   └── LegacyOAuthClientProperties.java
│   ├── crypto/              # Password encoding
│   │   └── LegacyPasswordEncoder.java  # BCrypt + PBKDF2 support
│   ├── service/             # Auth services
│   │   ├── CustomUserDetailsService.java
│   │   ├── SecUserDetailsService.java   # Legacy CUBA users
│   │   ├── HybridUserDetailsService.java # NEW + OLD users
│   │   ├── TokenService.java            # JWT generation
│   │   └── UserPermissionCacheService.java
│   ├── controller/
│   │   └── OAuth2TokenController.java   # Token endpoints
│   └── listener/
│       └── CacheInvalidationListener.java
└── build.gradle.kts

Dependencies: common, domain, Spring Security, OAuth2 Resource Server, Redis
```

**Key Components:**

#### SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enable @PreAuthorize
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // JWT is stateless
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no auth required)
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/swagger-ui/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // OAuth2 Resource Server (JWT validation)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            
            // Session management (stateless)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        // Configure JWT validation (RS256 algorithm)
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withPublicKey(rsaPublicKey())
            .build();
        return decoder;
    }
}
```

#### HybridUserDetailsService.java
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class HybridUserDetailsService implements UserDetailsService {
    
    private final CustomUserDetailsService newUserService;     // users table
    private final SecUserDetailsService legacyUserService;     // sec_user table
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        log.debug("Loading user: {}", username);
        
        // 1. Try NEW system first (99% of users)
        try {
            UserDetails user = newUserService.loadUserByUsername(username);
            log.info("User found in NEW system: {}", username);
            return user;
        } catch (UsernameNotFoundException e) {
            log.debug("User not found in NEW system, trying legacy...");
        }
        
        // 2. Fallback to LEGACY system (<1% of users)
        try {
            UserDetails user = legacyUserService.loadUserByUsername(username);
            log.info("User found in LEGACY system: {}", username);
            return user;
        } catch (UsernameNotFoundException e) {
            log.warn("User not found in ANY system: {}", username);
            throw new UsernameNotFoundException("Invalid username or password");
        }
    }
}
```

#### LegacyPasswordEncoder.java
```java
@Component
public class LegacyPasswordEncoder implements PasswordEncoder {
    
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(10);
    
    @Override
    public String encode(CharSequence rawPassword) {
        // Always use BCrypt for NEW passwords
        return bcrypt.encode(rawPassword);
    }
    
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // Detect format by prefix
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
            // BCrypt format (NEW system)
            return bcrypt.matches(rawPassword, encodedPassword);
        } else if (encodedPassword.contains(":")) {
            // PBKDF2 format (LEGACY CUBA system)
            // Format: hash:salt:iterations
            return matchesPBKDF2(rawPassword.toString(), encodedPassword);
        }
        
        throw new IllegalArgumentException("Unknown password format");
    }
    
    private boolean matchesPBKDF2(String rawPassword, String encodedPassword) {
        String[] parts = encodedPassword.split(":");
        if (parts.length != 3) return false;
        
        String storedHash = parts[0];
        String salt = parts[1];
        int iterations = Integer.parseInt(parts[2]);
        
        // Compute PBKDF2 hash
        String computedHash = pbkdf2(rawPassword, salt, iterations);
        return storedHash.equals(computedHash);
    }
}
```

---

### Module: `service`
**Purpose:** Business logic layer

```
service/
├── src/main/java/uz/hemis/service/
│   ├── StudentService.java            # Interface
│   ├── StudentServiceImpl.java        # Implementation
│   ├── FacultyService.java
│   ├── FacultyServiceImpl.java
│   ├── CurriculumService.java
│   └── (100+ service classes)
└── build.gradle.kts

Dependencies: common, domain, security
```

**Service Example:**
```java
public interface StudentService {
    Page<StudentDto> findAll(Pageable pageable);
    StudentDto findById(Long id);
    StudentDto create(StudentCreateDto dto);
    StudentDto update(Long id, StudentUpdateDto dto);
    void delete(Long id);
}

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default read-only
public class StudentServiceImpl implements StudentService {
    
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final StudentMapper studentMapper;
    
    @Override
    @ReadOnly  // Route to replica database
    public Page<StudentDto> findAll(Pageable pageable) {
        log.debug("Finding all students, page: {}", pageable.getPageNumber());
        
        Page<Student> students = studentRepository.findAll(pageable);
        return students.map(studentMapper::toDto);
    }
    
    @Override
    @ReadOnly
    @Cacheable(value = "students", key = "#id")
    public StudentDto findById(Long id) {
        log.debug("Finding student by id: {}", id);
        
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Student not found with id: " + id
            ));
        
        return studentMapper.toDto(student);
    }
    
    @Override
    @Transactional  // Write transaction (master database)
    @PreAuthorize("hasAuthority('students.create')")
    @CacheEvict(value = "studentsList", allEntries = true)
    public StudentDto create(@Valid StudentCreateDto dto) {
        log.info("Creating student: {}", dto.getEmail());
        
        // Validation
        if (studentRepository.existsByEmail(dto.getEmail())) {
            throw new ValidationException("Email already exists: " + dto.getEmail());
        }
        
        // Verify faculty exists
        Faculty faculty = facultyRepository.findById(dto.getFacultyId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Faculty not found: " + dto.getFacultyId()
            ));
        
        // Create entity
        Student student = studentMapper.toEntity(dto);
        student.setFaculty(faculty);
        
        // Save
        Student saved = studentRepository.save(student);
        
        log.info("Student created successfully, id: {}", saved.getId());
        return studentMapper.toDto(saved);
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('students.edit')")
    @CacheEvict(value = "students", key = "#id")
    public StudentDto update(Long id, @Valid StudentUpdateDto dto) {
        log.info("Updating student: {}", id);
        
        Student student = studentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Student not found: " + id
            ));
        
        // Update fields
        studentMapper.updateEntityFromDto(dto, student);
        
        // Save
        Student updated = studentRepository.save(student);
        
        log.info("Student updated successfully: {}", id);
        return studentMapper.toDto(updated);
    }
    
    @Override
    @Transactional
    @PreAuthorize("hasAuthority('students.delete')")
    @CacheEvict(value = "students", key = "#id")
    public void delete(Long id) {
        log.info("Deleting student: {}", id);
        
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found: " + id);
        }
        
        // Soft delete (recommended for audit trail)
        Student student = studentRepository.findById(id).orElseThrow();
        student.setStatus("DELETED");
        studentRepository.save(student);
        
        // Hard delete (if necessary)
        // studentRepository.deleteById(id);
        
        log.info("Student deleted successfully: {}", id);
    }
}
```

---

### Module: `api-web`
**Purpose:** Modern REST API for new frontends

```
api-web/
├── src/main/java/uz/hemis/api/web/
│   ├── controller/          # REST Controllers (30 classes)
│   │   ├── StudentController.java
│   │   ├── FacultyController.java
│   │   ├── CurriculumController.java
│   │   ├── GradeController.java
│   │   └── (26 more controllers)
│   └── dto/                 # Request/Response DTOs specific to API
└── build.gradle.kts

Base Path: /api/v1/web/*
Dependencies: common, domain, service
```

**Controller Example:**
```java
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
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete student")
    @PreAuthorize("hasAuthority('students.delete')")
    public ResponseEntity<ResponseWrapper<Void>> delete(
        @PathVariable Long id
    ) {
        log.info("DELETE /api/v1/web/students/{}", id);
        
        studentService.delete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null));
    }
}
```

---

### Module: `api-legacy`
**Purpose:** CUBA Platform compatibility layer

```
api-legacy/
├── src/main/java/uz/hemis/api/legacy/
│   ├── controller/          # CUBA-style controllers (56 classes)
│   │   ├── LegacyStudentController.java
│   │   └── (55 more entity controllers)
│   └── dto/                 # CUBA-style DTOs
└── build.gradle.kts

Base Path: /app/rest/v2/*
Dependencies: common, domain, service
```

**CUBA-Style Controller:**
```java
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_Student")
@RequiredArgsConstructor
@Slf4j
public class LegacyStudentController {
    
    private final StudentService studentService;
    
    // CUBA list endpoint
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(
        @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "50") int limit
    ) {
        log.info("GET /app/rest/v2/entities/hemishe_Student (LEGACY)");
        
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<StudentDto> students = studentService.findAll(pageable);
        
        // Convert to CUBA format
        List<Map<String, Object>> cubaFormat = students.stream()
            .map(this::toCubaFormat)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(cubaFormat);
    }
    
    private Map<String, Object> toCubaFormat(StudentDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("_entityName", "hemishe$Student");
        map.put("_instanceName", dto.getFirstName() + " " + dto.getLastName());
        map.put("id", dto.getId());
        map.put("firstName", dto.getFirstName());
        map.put("lastName", dto.getLastName());
        map.put("email", dto.getEmail());
        return map;
    }
}
```

---

### Module: `api-external`
**Purpose:** Server-to-Server integrations

```
api-external/
├── src/main/java/uz/hemis/api/external/
│   ├── controller/          # Integration endpoints (6 classes)
│   │   ├── HemisMinistryController.java
│   │   ├── OneIdController.java
│   │   ├── MyGovController.java
│   │   ├── PaymentGatewayController.java
│   │   └── (2 more integration controllers)
│   └── client/              # Feign clients for external APIs
└── build.gradle.kts

Base Path: /api/v1/external/*
Dependencies: common, domain, service
Security: API Key + IP Whitelist
```

---

### Module: `app`
**Purpose:** Main Spring Boot application

```
app/
├── src/main/java/uz/hemis/app/
│   ├── HemisApplication.java           # @SpringBootApplication
│   ├── config/                         # Configuration classes
│   │   ├── DataSourceConfig.java      # Master/Replica setup
│   │   ├── SecurityProperties.java
│   │   ├── OpenApiConfig.java         # Swagger config
│   │   ├── RestTemplateConfig.java
│   │   └── LiquibaseFilenameFixListener.java
│   ├── security/
│   │   ├── UniversityAccessValidator.java
│   │   └── RateLimitFilter.java
│   ├── controller/                     # Auth endpoints (5 controllers)
│   │   ├── AuthController.java
│   │   ├── AdminAuthController.java
│   │   ├── TestController.java
│   │   └── CaptchaController.java
│   └── exception/
│       └── GlobalExceptionHandler.java  # @ControllerAdvice
├── src/main/resources/
│   ├── application.yml                 # Main config
│   ├── application-dev.yml             # Development profile
│   ├── application-prod.yml            # Production profile
│   ├── application-replica.yml         # Replica database profile
│   └── logback-spring.xml              # Logging config
└── build.gradle.kts

Dependencies: ALL modules (assembles the application)
```

**Main Application:**
```java
@SpringBootApplication(scanBasePackages = "uz.hemis")
@EnableJpaRepositories("uz.hemis.domain.repository")
@EntityScan("uz.hemis.domain.entity")
@EnableCaching
public class HemisApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HemisApplication.class, args);
    }
}
```

**DataSource Configuration:**
```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @Primary
    public DataSource routingDataSource(
        @Qualifier("masterDataSource") DataSource master,
        @Qualifier("replicaDataSource") DataSource replica
    ) {
        RoutingDataSource routing = new RoutingDataSource();
        
        Map<Object, Object> sources = new HashMap<>();
        sources.put(DataSourceType.MASTER, master);
        sources.put(DataSourceType.REPLICA, replica);
        
        routing.setTargetDataSources(sources);
        routing.setDefaultTargetDataSource(master);
        
        return routing;
    }
}

// RoutingDataSource - Route queries based on @ReadOnly annotation
public class RoutingDataSource extends AbstractRoutingDataSource {
    
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}

// Aspect to set routing context
@Aspect
@Component
public class DataSourceAspect {
    
    @Before("@annotation(readOnly)")
    public void setReadOnlyDataSource(ReadOnly readOnly) {
        DataSourceContextHolder.set(DataSourceType.REPLICA);
    }
    
    @After("@annotation(readOnly)")
    public void clearDataSource(ReadOnly readOnly) {
        DataSourceContextHolder.clear();
    }
}
```

---

## 🗄️ Database Architecture

### Master-Replica Replication

```
┌─────────────────┐
│   Application   │
│   (HemisApp)    │
└────────┬────────┘
         │
         │ Dynamic Routing
         │ (based on @ReadOnly)
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐  ┌──────────┐
│ MASTER │  │ REPLICA  │
│  (RW)  │─→│   (RO)   │
└────────┘  └──────────┘
    │           │
    └─── Async ─┘
    Replication
```

**Routing Logic:**
```java
// Write operations → Master
@Transactional
public StudentDto create(StudentCreateDto dto) {
    // Routes to MASTER database
}

// Read operations → Replica
@Transactional(readOnly = true)
@ReadOnly
public StudentDto findById(Long id) {
    // Routes to REPLICA database
}
```

### Connection Pooling (HikariCP)

```yaml
Master Pool:
  maximum-pool-size: 10
  minimum-idle: 2
  connection-timeout: 30s
  
Replica Pool:
  maximum-pool-size: 20    # More connections for reads
  minimum-idle: 5
  read-only: true
```

---

## 🔐 Security Architecture

### Authentication Flow

```
1. User Login
   │
   ▼
2. HybridUserDetailsService
   │
   ├─→ Check NEW users table (BCrypt)
   │   └─→ 99% found here ✅
   │
   └─→ Fallback to sec_user (PBKDF2)
       └─→ <1% legacy users
   
3. Generate JWT Token (RS256)
   │
   ▼
4. Store in Redis (session management)
   │
   ▼
5. Return to client
```

### Authorization Flow

```
1. Request with JWT Bearer token
   │
   ▼
2. Spring Security validates JWT
   │
   ▼
3. Extract username + authorities
   │
   ▼
4. Check @PreAuthorize permission
   │
   ├─→ Permission granted → Continue
   │
   └─→ Permission denied → 403 Forbidden
```

### JWT Structure

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "admin",
    "authorities": [
      "students.view",
      "students.create",
      "students.edit",
      "faculty.view"
    ],
    "university_id": 1,
    "roles": ["ROLE_ADMINISTRATORS"],
    "exp": 1731657600,
    "iat": 1731571200
  },
  "signature": "..."
}
```

---

## 📊 Caching Architecture

### Two-Level Cache

```
┌─────────────────┐
│   Application   │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐ ┌─────────┐
│ L1 Cache│ │ L2 Cache│
│ Caffeine│ │  Redis  │
│  (JVM)  │ │(Shared) │
└─────────┘ └─────────┘
    │         │
    └────┬────┘
         │
         ▼
    ┌─────────┐
    │Database │
    └─────────┘
```

**Cache Strategy:**
```java
// L1 (Caffeine) - Fast, per-instance
@Cacheable(value = "students", key = "#id")
public StudentDto findById(Long id) {
    // Cache miss → query database
}

// L2 (Redis) - Shared across instances
@Cacheable(value = "permissions", key = "#userId")
public List<String> getUserPermissions(Long userId) {
    // Shared cache for all app instances
}
```

**Cache Invalidation:**
```java
// Evict single entry
@CacheEvict(value = "students", key = "#id")
public void update(Long id, StudentUpdateDto dto) {
    // Cache evicted after update
}

// Evict all entries
@CacheEvict(value = "studentsList", allEntries = true)
public void create(StudentCreateDto dto) {
    // Clear all cached lists
}
```

---

## 🚀 Deployment Architecture

### Production Setup

```
                    Internet
                       │
                       ▼
               ┌───────────────┐
               │ Load Balancer │
               │    (Nginx)    │
               └───────┬───────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
   ┌────────┐    ┌────────┐    ┌────────┐
   │ App-1  │    │ App-2  │    │ App-3  │
   │ :8080  │    │ :8080  │    │ :8080  │
   └───┬────┘    └───┬────┘    └───┬────┘
       │             │             │
       └─────────────┴─────────────┘
                     │
       ┌─────────────┴─────────────┐
       │                           │
       ▼                           ▼
┌──────────────┐           ┌──────────────┐
│  PostgreSQL  │           │    Redis     │
│ Master/Rep   │           │   Cluster    │
└──────────────┘           └──────────────┘
```

### Docker Deployment

**Dockerfile (Multi-stage build):**
```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :app:bootJar -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: hemis
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
  
  app:
    build: .
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_MASTER_HOST: postgres
      REDIS_HOST: redis
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis

volumes:
  postgres-data:
  redis-data:
```

---

## 📈 Scalability Considerations

### Horizontal Scaling
```
✅ Supported:
- Stateless application (JWT tokens)
- Redis for shared sessions
- Master-Replica for read scaling
- Docker/Kubernetes ready

Scaling Strategy:
- Add more app instances behind load balancer
- Scale read replicas for heavy read workloads
- Redis cluster for cache scaling
```

### Vertical Scaling
```
Resource Limits per instance:
  CPU: 2-4 cores
  RAM: 2-4 GB (JVM heap: 1-2 GB)
  
Recommended:
- Start with 2 instances (HA)
- Scale horizontally as needed
- Monitor CPU/RAM usage
```

---

## 🔍 Monitoring & Observability

### Metrics (Spring Boot Actuator)
```
/actuator/health      - Health status
/actuator/metrics     - JVM metrics
/actuator/prometheus  - Prometheus format
```

### Logging
```
Logback → Files → ELK Stack
         → Sentry (errors)
```

### Tracing (Optional)
```
Spring Cloud Sleuth → Zipkin/Jaeger
```

---

## 🧪 Testing Strategy

### Test Pyramid
```
       ┌─────────┐
       │   E2E   │  10%
       ├─────────┤
       │Integration│  30%
       ├─────────┤
       │   Unit   │  60%
       └─────────┘
```

### Test Isolation
```
Unit Tests:        Mock dependencies
Integration Tests: H2 in-memory database
E2E Tests:        Docker Testcontainers
```

---

**This architecture is designed for:**
- ✅ Maintainability (clear separation of concerns)
- ✅ Testability (isolated modules)
- ✅ Scalability (horizontal scaling ready)
- ✅ Security (defense in depth)
- ✅ Performance (caching, connection pooling, replica routing)
- ✅ Backward Compatibility (legacy API support)

**Remember: Architecture is about trade-offs. This design prioritizes stability and maintainability over cutting-edge features.**
