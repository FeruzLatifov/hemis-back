package uz.hemis.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.hemis.service.StudentService;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.StudentDto;

import java.util.UUID;

/**
 * Student REST Controller - API Layer
 *
 * <p><strong>CRITICAL - Legacy URL Preservation:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/students (unchanged from CUBA)</li>
 *   <li>200+ universities depend on this API contract</li>
 *   <li>Response format must match legacy (ResponseWrapper + PageResponse)</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /app/rest/v2/students - List all students (paginated)</li>
 *   <li>GET /app/rest/v2/students/{id} - Find by ID</li>
 *   <li>GET /app/rest/v2/students?university={code} - Filter by university</li>
 *   <li>POST /app/rest/v2/students - Create new student</li>
 *   <li>PUT /app/rest/v2/students/{id} - Update existing student</li>
 *   <li>PATCH /app/rest/v2/students/{id} - Partial update</li>
 *   <li>❌ NO DELETE endpoint (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>All exceptions handled by {@link uz.hemis.app.exception.GlobalExceptionHandler}</p>
 *
 * @since 1.0.0
 */
@Tag(name = "Students")
@RestController
@RequestMapping("/app/rest/v2/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    // =====================================================
    // Read Operations
    // =====================================================

    /**
     * Get all students (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/students</p>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li>page - page number (default: 0)</li>
     *   <li>size - page size (default: 20)</li>
     *   <li>sort - sorting (e.g., lastname,asc)</li>
     * </ul>
     *
     * <p><strong>Response Format:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "content": [...],
     *     "page": 0,
     *     "size": 20,
     *     "totalElements": 100,
     *     "totalPages": 5
     *   }
     * }
     * </pre>
     *
     * @param pageable pagination parameters
     * @return paginated list of students
     */
    @Operation(
            summary = "Get all students (paginated)",
            description = """
                Barcha talabalarni ro'yxatini olish (sahifalangan).

                **Query Parameters:**
                - `page` - Sahifa raqami (default: 0)
                - `size` - Sahifadagi elementlar soni (default: 20, max: 100)
                - `sort` - Tartiblash (masalan: `lastname,asc` yoki `createTs,desc`)

                **Filterlash:**
                - Universitet bo'yicha: `?university=UNI001`
                - PINFL bo'yicha: `?pinfl=12345678901234`
                - Status bo'yicha: `?status=ACTIVE`

                **Misol so'rovlar:**
                - Birinchi sahifa: `/app/rest/v2/students?page=0&size=20`
                - Familiya bo'yicha tartiblangan: `/app/rest/v2/students?sort=lastname,asc`
                - Universitet filtri: `/app/rest/v2/students?university=UNI001&page=0&size=10`
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Talabalar muvaffaqiyatli qaytarildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    summary = "Muvaffaqiyatli javob",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "Students retrieved successfully",
                                      "data": {
                                        "content": [
                                          {
                                            "id": "550e8400-e29b-41d4-a716-446655440000",
                                            "code": "STU2024001",
                                            "_university": "UNI001",
                                            "firstname": "Ali",
                                            "lastname": "Valiyev",
                                            "pinfl": "12345678901234",
                                            "_student_status": "ACTIVE",
                                            "gpa": 3.85,
                                            "createTs": "2024-01-15T10:30:00Z",
                                            "updateTs": "2024-01-15T10:30:00Z"
                                          },
                                          {
                                            "id": "550e8400-e29b-41d4-a716-446655440001",
                                            "code": "STU2024002",
                                            "_university": "UNI001",
                                            "firstname": "Nodira",
                                            "lastname": "Karimova",
                                            "pinfl": "98765432109876",
                                            "_student_status": "ACTIVE",
                                            "gpa": 4.0,
                                            "createTs": "2024-01-16T11:00:00Z",
                                            "updateTs": "2024-01-16T11:00:00Z"
                                          }
                                        ],
                                        "page": 0,
                                        "size": 20,
                                        "totalElements": 150,
                                        "totalPages": 8,
                                        "first": true,
                                        "last": false
                                      },
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Avtorizatsiya xatosi - Token yaroqsiz yoki muddati o'tgan",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unauthorized Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 401,
                                      "error": "UNAUTHORIZED",
                                      "message": "Invalid or expired JWT token",
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Ruxsat yo'q - Universitet ma'lumotlariga kirish taqiqlangan",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Forbidden Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "You don't have permission to access this university's data",
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            )
    })
    @SecurityRequirement(name = "apiKeyAuth")
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<StudentDto>>> getAllStudents(
            @Parameter(description = "Pagination parameters (page, size, sort)")
            @PageableDefault(size = 20, sort = "createTs", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/students - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<StudentDto> students = studentService.findAll(pageable);
        PageResponse<StudentDto> pageResponse = PageResponse.of(students);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get student by ID
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/students/{id}</p>
     *
     * <p><strong>Response Format:</strong></p>
     * <pre>
     * {
     *   "success": true,
     *   "data": {
     *     "id": "...",
     *     "code": "STU001",
     *     "_university": "UNI001",
     *     ...
     *   }
     * }
     * </pre>
     *
     * @param id student ID (UUID)
     * @return student details
     * @throws uz.hemis.common.exception.ResourceNotFoundException if not found (handled by GlobalExceptionHandler)
     */
    @Operation(
            summary = "Get student by ID",
            description = "Get detailed information about a specific student"
    )
    @ApiResponses(value = {
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
                    responseCode = "401",
                    description = "Unauthorized - Invalid API key"
            )
    })
    @SecurityRequirement(name = "apiKeyAuth")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<StudentDto>> getStudentById(
            @Parameter(description = "Student UUID")
            @PathVariable UUID id) {
        log.debug("GET /app/rest/v2/students/{}", id);

        StudentDto student = studentService.findById(id);

        return ResponseEntity.ok(ResponseWrapper.success(student));
    }

    /**
     * Get students by university code
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/students?university={code}</p>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li>university - university code (required)</li>
     *   <li>page - page number (default: 0)</li>
     *   <li>size - page size (default: 20)</li>
     * </ul>
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return paginated list of students for the university
     */
    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<StudentDto>>> getStudentsByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "createTs", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/students?university={}", universityCode);

        Page<StudentDto> students = studentService.findByUniversity(universityCode, pageable);
        PageResponse<StudentDto> pageResponse = PageResponse.of(students);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    // =====================================================
    // Write Operations
    // =====================================================

    /**
     * Create new student
     *
     * <p><strong>Legacy URL:</strong> POST /app/rest/v2/students</p>
     *
     * <p><strong>Request Body:</strong></p>
     * <pre>
     * {
     *   "code": "STU001",
     *   "_university": "UNI001",
     *   "firstname": "John",
     *   "lastname": "Doe",
     *   "pinfl": "12345678901234",
     *   ...
     * }
     * </pre>
     *
     * <p><strong>Validations:</strong></p>
     * <ul>
     *   <li>PINFL must be unique</li>
     *   <li>Code must be unique</li>
     *   <li>Required fields must be present</li>
     * </ul>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN or ROLE_UNIVERSITY_ADMIN</p>
     *
     * @param studentDto student data
     * @return created student (HTTP 201 CREATED)
     * @throws uz.hemis.common.exception.ValidationException if validation fails (handled by GlobalExceptionHandler)
     */
    @Operation(
            summary = "Create new student",
            description = """
                Yangi talaba yaratish.

                **Majburiy maydonlar:**
                - `code` - Talaba kodi (unique)
                - `_university` - Universitet kodi (masalan: UNI001)
                - `firstname` - Ism
                - `lastname` - Familiya
                - `pinfl` - PINFL (14 raqam, unique)
                - `_student_status` - Status (ACTIVE, GRADUATED, EXPELLED, etc.)

                **Ixtiyoriy maydonlar:**
                - `middlename` - Otasining ismi
                - `gpa` - O'rtacha ball
                - `_faculty` - Fakultet kodi
                - `_specialty` - Mutaxassislik kodi
                - `_group` - Guruh kodi

                **Validatsiya:**
                - PINFL 14 raqamdan iborat bo'lishi kerak
                - PINFL unique bo'lishi kerak (duplikat ruxsat etilmaydi)
                - Code unique bo'lishi kerak
                - Email formati to'g'ri bo'lishi kerak

                **Xavfsizlik:**
                - Faqat ADMIN yoki UNIVERSITY_ADMIN ro'yxatdagi foydalanuvchilar talaba qo'sha oladi
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Talaba muvaffaqiyatli yaratildi",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class),
                            examples = @ExampleObject(
                                    name = "Created Student",
                                    summary = "Yaratilgan talaba",
                                    value = """
                                    {
                                      "success": true,
                                      "message": "Student created successfully",
                                      "data": {
                                        "id": "550e8400-e29b-41d4-a716-446655440010",
                                        "code": "STU2025001",
                                        "_university": "UNI001",
                                        "firstname": "Jasur",
                                        "lastname": "Toshmatov",
                                        "middlename": "Akmal o'g'li",
                                        "pinfl": "32109876543210",
                                        "_student_status": "ACTIVE",
                                        "_faculty": "FAC001",
                                        "_specialty": "SPEC001",
                                        "_group": "GRP001",
                                        "gpa": 0.0,
                                        "email": "jasur.toshmatov@student.uni001.uz",
                                        "createTs": "2025-11-06T10:30:00Z",
                                        "updateTs": "2025-11-06T10:30:00Z",
                                        "deleteTs": null
                                      },
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validatsiya xatosi - Majburiy maydonlar to'ldirilmagan yoki format xato",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 400,
                                      "error": "VALIDATION_ERROR",
                                      "message": "Validation failed for object='studentDto'. Error count: 2",
                                      "errors": [
                                        {
                                          "field": "pinfl",
                                          "message": "PINFL must be exactly 14 digits",
                                          "rejectedValue": "12345"
                                        },
                                        {
                                          "field": "email",
                                          "message": "Invalid email format",
                                          "rejectedValue": "invalid-email"
                                        }
                                      ],
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - PINFL yoki Code allaqachon mavjud",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Duplicate Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 409,
                                      "error": "DUPLICATE_RESOURCE",
                                      "message": "Student with PINFL '12345678901234' already exists",
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Avtorizatsiya xatosi",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Unauthorized Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 401,
                                      "error": "UNAUTHORIZED",
                                      "message": "Invalid or expired JWT token",
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Ruxsat yo'q - ADMIN yoki UNIVERSITY_ADMIN roli kerak",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Forbidden Error",
                                    value = """
                                    {
                                      "success": false,
                                      "status": 403,
                                      "error": "FORBIDDEN",
                                      "message": "Access denied. Required role: ADMIN or UNIVERSITY_ADMIN",
                                      "timestamp": "2025-11-06T10:30:00Z"
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<StudentDto>> createStudent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Yangi talaba ma'lumotlari",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StudentDto.class),
                            examples = @ExampleObject(
                                    name = "New Student Request",
                                    summary = "Yangi talaba yaratish so'rovi",
                                    value = """
                                    {
                                      "code": "STU2025001",
                                      "_university": "UNI001",
                                      "firstname": "Jasur",
                                      "lastname": "Toshmatov",
                                      "middlename": "Akmal o'g'li",
                                      "pinfl": "32109876543210",
                                      "_student_status": "ACTIVE",
                                      "_faculty": "FAC001",
                                      "_specialty": "SPEC001",
                                      "_group": "GRP001",
                                      "email": "jasur.toshmatov@student.uni001.uz"
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody StudentDto studentDto
    ) {
        log.info("POST /app/rest/v2/students - creating student: {}", studentDto.getCode());

        StudentDto created = studentService.create(studentDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(created));
    }

    /**
     * Update existing student (full update)
     *
     * <p><strong>Legacy URL:</strong> PUT /app/rest/v2/students/{id}</p>
     *
     * <p><strong>Request Body:</strong> Complete student object</p>
     *
     * <p><strong>Note:</strong> All fields are updated (except audit fields)</p>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN or ROLE_UNIVERSITY_ADMIN</p>
     *
     * @param id student ID (UUID)
     * @param studentDto student data
     * @return updated student
     * @throws uz.hemis.common.exception.ResourceNotFoundException if student not found
     * @throws uz.hemis.common.exception.ValidationException if validation fails
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<StudentDto>> updateStudent(
            @PathVariable UUID id,
            @Valid @RequestBody StudentDto studentDto
    ) {
        log.info("PUT /app/rest/v2/students/{} - updating student", id);

        StudentDto updated = studentService.update(id, studentDto);

        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    /**
     * Partial update (PATCH)
     *
     * <p><strong>Legacy URL:</strong> PATCH /app/rest/v2/students/{id}</p>
     *
     * <p><strong>Request Body:</strong> Partial student object (only fields to update)</p>
     *
     * <p><strong>Note:</strong> Only non-null fields are updated</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * PATCH /app/rest/v2/students/{id}
     * {
     *   "_student_status": "GRADUATED"
     * }
     * </pre>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN or ROLE_UNIVERSITY_ADMIN</p>
     *
     * @param id student ID (UUID)
     * @param studentDto partial student data
     * @return updated student
     * @throws uz.hemis.common.exception.ResourceNotFoundException if student not found
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<StudentDto>> partialUpdateStudent(
            @PathVariable UUID id,
            @RequestBody StudentDto studentDto
    ) {
        log.info("PATCH /app/rest/v2/students/{} - partial update", id);

        StudentDto updated = studentService.partialUpdate(id, studentDto);

        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // Soft Delete (Not exposed as public API)
    // =====================================================

    /**
     * Soft delete student
     *
     * <p><strong>⚠️ NOT EXPOSED AS PUBLIC ENDPOINT</strong></p>
     *
     * <p>This method exists for administrative purposes only.</p>
     * <p>Soft delete can be triggered via:</p>
     * <ul>
     *   <li>Admin panel (separate admin-api module)</li>
     *   <li>Internal system processes</li>
     *   <li>Scheduled cleanup jobs</li>
     * </ul>
     *
     * <p><strong>CRITICAL:</strong> NO public DELETE endpoint allowed (NDG)</p>
     */
    // Intentionally commented out - no public DELETE endpoint
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteStudent(@PathVariable UUID id) {
    //     studentService.softDelete(id);
    //     return ResponseEntity.noContent().build();
    // }

    // =====================================================
    // NOTE: NO DELETE ENDPOINT
    // =====================================================
    // Physical DELETE is prohibited (NDG - Non-Deletion Guarantee)
    // Soft delete is handled internally by admin-api module
    // =====================================================
}
