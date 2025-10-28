package uz.hemis.app.controller;

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
import uz.hemis.app.service.StudentService;
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
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<StudentDto>>> getAllStudents(
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
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<StudentDto>> getStudentById(@PathVariable UUID id) {
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
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<StudentDto>> createStudent(
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
