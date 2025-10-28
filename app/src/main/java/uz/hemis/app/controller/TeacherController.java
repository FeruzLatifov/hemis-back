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
import uz.hemis.app.service.TeacherService;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.TeacherDto;

import java.util.List;
import java.util.UUID;

/**
 * Teacher REST Controller - API Layer
 *
 * <p><strong>CRITICAL - Legacy URL Preservation:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/teachers (unchanged from CUBA)</li>
 *   <li>200+ universities depend on this API contract</li>
 *   <li>Response format must match legacy (ResponseWrapper + PageResponse)</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /app/rest/v2/teachers - List all teachers (paginated)</li>
 *   <li>GET /app/rest/v2/teachers/{id} - Find by ID</li>
 *   <li>GET /app/rest/v2/teachers/university/{code} - Filter by university</li>
 *   <li>GET /app/rest/v2/teachers/search - Search by name</li>
 *   <li>POST /app/rest/v2/teachers - Create new teacher</li>
 *   <li>PUT /app/rest/v2/teachers/{id} - Update existing teacher</li>
 *   <li>PATCH /app/rest/v2/teachers/{id} - Partial update</li>
 *   <li>❌ NO DELETE endpoint (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>All exceptions handled by {@link uz.hemis.app.exception.GlobalExceptionHandler}</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/teachers")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final TeacherService teacherService;

    // =====================================================
    // Read Operations
    // =====================================================

    /**
     * Get all teachers (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/teachers</p>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li>page - page number (default: 0)</li>
     *   <li>size - page size (default: 20)</li>
     *   <li>sort - sorting (e.g., lastname,asc)</li>
     * </ul>
     *
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getAllTeachers(
            @PageableDefault(size = 20, sort = "lastname", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<TeacherDto> teachers = teacherService.findAll(pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get teacher by ID
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/teachers/{id}</p>
     *
     * @param id teacher ID (UUID)
     * @return ResponseWrapper with TeacherDto
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<TeacherDto>> getTeacherById(@PathVariable UUID id) {
        log.info("GET /app/rest/v2/teachers/{}", id);

        TeacherDto teacher = teacherService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(teacher));
    }

    /**
     * Get teachers by university code (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/teachers/university/{code}</p>
     *
     * @param code university code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/university/{code}")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getTeachersByUniversity(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/university/{}", code);

        Page<TeacherDto> teachers = teacherService.findByUniversity(code, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get all teachers by university (no pagination)
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/university/{code}/all</p>
     *
     * @param code university code
     * @return ResponseWrapper with List<TeacherDto>
     */
    @GetMapping("/university/{code}/all")
    public ResponseEntity<ResponseWrapper<List<TeacherDto>>> getAllTeachersByUniversity(@PathVariable String code) {
        log.info("GET /app/rest/v2/teachers/university/{}/all", code);

        List<TeacherDto> teachers = teacherService.findAllByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(teachers));
    }

    /**
     * Search teachers by lastname (partial match, case-insensitive)
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/search/lastname?q={lastname}</p>
     *
     * @param q lastname search term (partial)
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/search/lastname")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> searchByLastname(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/search/lastname?q={}", q);

        Page<TeacherDto> teachers = teacherService.findByLastname(q, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Search teachers by name (partial match on any name field, case-insensitive)
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/search?name={name}</p>
     *
     * @param name search term (applied to firstname, lastname, fathername)
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> searchTeachers(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/search?name={}", name);

        Page<TeacherDto> teachers = teacherService.findByName(name, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get teachers by academic degree
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/degree/{degreeCode}</p>
     *
     * @param degreeCode academic degree code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/degree/{degreeCode}")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getTeachersByDegree(
            @PathVariable String degreeCode,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/degree/{}", degreeCode);

        Page<TeacherDto> teachers = teacherService.findByAcademicDegree(degreeCode, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get teachers by academic rank
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/rank/{rankCode}</p>
     *
     * @param rankCode academic rank code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/rank/{rankCode}")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getTeachersByRank(
            @PathVariable String rankCode,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/rank/{}", rankCode);

        Page<TeacherDto> teachers = teacherService.findByAcademicRank(rankCode, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(teachers);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get professors by university
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/university/{code}/professors</p>
     *
     * @param code university code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<TeacherDto>
     */
    @GetMapping("/university/{code}/professors")
    public ResponseEntity<ResponseWrapper<PageResponse<TeacherDto>>> getProfessorsByUniversity(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "lastname") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/teachers/university/{}/professors", code);

        Page<TeacherDto> professors = teacherService.findProfessorsByUniversity(code, pageable);
        PageResponse<TeacherDto> pageResponse = PageResponse.of(professors);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Count teachers by university
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/university/{code}/count</p>
     *
     * @param code university code
     * @return ResponseWrapper with count
     */
    @GetMapping("/university/{code}/count")
    public ResponseEntity<ResponseWrapper<Long>> countTeachersByUniversity(@PathVariable String code) {
        log.info("GET /app/rest/v2/teachers/university/{}/count", code);

        long count = teacherService.countByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    /**
     * Count professors by university
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/teachers/university/{code}/professors/count</p>
     *
     * @param code university code
     * @return ResponseWrapper with count
     */
    @GetMapping("/university/{code}/professors/count")
    public ResponseEntity<ResponseWrapper<Long>> countProfessorsByUniversity(@PathVariable String code) {
        log.info("GET /app/rest/v2/teachers/university/{}/professors/count", code);

        long count = teacherService.countProfessorsByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    // =====================================================
    // Write Operations (ADMIN/UNIVERSITY_ADMIN only)
    // =====================================================

    /**
     * Create new teacher
     *
     * <p><strong>Legacy URL:</strong> POST /app/rest/v2/teachers</p>
     *
     * <p><strong>Authorization:</strong> ADMIN or UNIVERSITY_ADMIN</p>
     *
     * @param dto teacher DTO
     * @return ResponseWrapper with created TeacherDto
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<TeacherDto>> createTeacher(@Valid @RequestBody TeacherDto dto) {
        log.info("POST /app/rest/v2/teachers - {} {}", dto.getFirstname(), dto.getLastname());

        TeacherDto created = teacherService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    /**
     * Update existing teacher
     *
     * <p><strong>Legacy URL:</strong> PUT /app/rest/v2/teachers/{id}</p>
     *
     * <p><strong>Authorization:</strong> ADMIN or UNIVERSITY_ADMIN</p>
     *
     * @param id teacher ID
     * @param dto teacher DTO
     * @return ResponseWrapper with updated TeacherDto
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<TeacherDto>> updateTeacher(
            @PathVariable UUID id,
            @Valid @RequestBody TeacherDto dto
    ) {
        log.info("PUT /app/rest/v2/teachers/{}", id);

        TeacherDto updated = teacherService.update(id, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    /**
     * Partial update (PATCH) - only update non-null fields
     *
     * <p><strong>Legacy URL:</strong> PATCH /app/rest/v2/teachers/{id}</p>
     *
     * <p><strong>Authorization:</strong> ADMIN or UNIVERSITY_ADMIN</p>
     *
     * @param id teacher ID
     * @param dto teacher DTO with fields to update
     * @return ResponseWrapper with updated TeacherDto
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<TeacherDto>> partialUpdateTeacher(
            @PathVariable UUID id,
            @RequestBody TeacherDto dto
    ) {
        log.info("PATCH /app/rest/v2/teachers/{}", id);

        TeacherDto updated = teacherService.partialUpdate(id, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // NO DELETE ENDPOINT
    // =====================================================
    // Physical DELETE operations are PROHIBITED (NDG).
    // Soft delete is handled via service layer if needed.
    // Any DELETE request will return 405 Method Not Allowed.
    // =====================================================
}
