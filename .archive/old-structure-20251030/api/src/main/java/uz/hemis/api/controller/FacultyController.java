package uz.hemis.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uz.hemis.common.dto.FacultyDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.domain.service.FacultyService;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Faculty operations
 *
 * <p><strong>Base URL:</strong> /app/rest/v2/faculties</p>
 * <p>CRITICAL: URL preserved from CUBA Platform API for backward compatibility</p>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET    / → List all faculties (paginated)</li>
 *   <li>GET    /{id} → Get faculty by ID</li>
 *   <li>GET    /code/{code} → Get faculty by code</li>
 *   <li>GET    /university/{code} → List faculties by university</li>
 *   <li>GET    /university/{code}/all → List all faculties by university (no pagination)</li>
 *   <li>GET    /active → List active faculties</li>
 *   <li>GET    /type/{code} → List faculties by type</li>
 *   <li>GET    /search → Search faculties by name</li>
 *   <li>GET    /university/{code}/count → Count faculties by university</li>
 *   <li>POST   / → Create new faculty (ADMIN/UNIVERSITY_ADMIN)</li>
 *   <li>PUT    /{id} → Update faculty (ADMIN/UNIVERSITY_ADMIN)</li>
 *   <li>PATCH  /{id}/deactivate → Soft delete faculty (ADMIN)</li>
 * </ul>
 *
 * <p><strong>Authorization:</strong></p>
 * <ul>
 *   <li>Read operations: PUBLIC (authenticated users)</li>
 *   <li>Create/Update: ADMIN, UNIVERSITY_ADMIN</li>
 *   <li>Soft Delete: ADMIN only</li>
 * </ul>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/faculties")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Faculty", description = "Faculty management API")
public class FacultyController {

    private final FacultyService facultyService;

    // =====================================================
    // READ Operations
    // =====================================================

    /**
     * Get all faculties (paginated)
     *
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of faculties
     */
    @GetMapping
    @Operation(summary = "Get all faculties", description = "Returns paginated list of all faculties")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> getAllFaculties(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/faculties - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<FacultyDto> page = facultyService.findAll(pageable);
        PageResponse<FacultyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get faculty by ID
     *
     * @param id Faculty UUID
     * @return Faculty details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get faculty by ID", description = "Returns faculty details by UUID")
    public ResponseEntity<ResponseWrapper<FacultyDto>> getFacultyById(
            @Parameter(description = "Faculty UUID") @PathVariable UUID id
    ) {
        log.debug("GET /app/rest/v2/faculties/{}", id);

        FacultyDto faculty = facultyService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(faculty));
    }

    /**
     * Get faculty by code
     *
     * @param code Faculty code
     * @return Faculty details
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Get faculty by code", description = "Returns faculty details by unique code")
    public ResponseEntity<ResponseWrapper<FacultyDto>> getFacultyByCode(
            @Parameter(description = "Faculty code") @PathVariable String code
    ) {
        log.debug("GET /app/rest/v2/faculties/code/{}", code);

        FacultyDto faculty = facultyService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(faculty));
    }

    /**
     * Get faculties by university (paginated)
     *
     * @param code     University code
     * @param pageable Pagination parameters
     * @return Page of faculties
     */
    @GetMapping("/university/{code}")
    @Operation(summary = "Get faculties by university", description = "Returns paginated list of faculties for a university")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> getFacultiesByUniversity(
            @Parameter(description = "University code") @PathVariable String code,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/faculties/university/{}", code);

        Page<FacultyDto> page = facultyService.findByUniversity(code, pageable);
        PageResponse<FacultyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get all faculties by university (no pagination)
     *
     * @param code University code
     * @return List of all faculties
     */
    @GetMapping("/university/{code}/all")
    @Operation(summary = "Get all faculties by university", description = "Returns complete list of faculties for a university (no pagination)")
    public ResponseEntity<ResponseWrapper<List<FacultyDto>>> getAllFacultiesByUniversity(
            @Parameter(description = "University code") @PathVariable String code
    ) {
        log.debug("GET /app/rest/v2/faculties/university/{}/all", code);

        List<FacultyDto> faculties = facultyService.findAllByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(faculties));
    }

    /**
     * Search faculties by name
     *
     * @param name     Name to search (partial match, case-insensitive)
     * @param pageable Pagination parameters
     * @return Page of faculties
     */
    @GetMapping("/search")
    @Operation(summary = "Search faculties by name", description = "Returns faculties matching the name pattern")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> searchFaculties(
            @Parameter(description = "Name to search") @RequestParam String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/faculties/search?name={}", name);

        Page<FacultyDto> page = facultyService.findByNameContaining(name, pageable);
        PageResponse<FacultyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get active faculties
     *
     * @param pageable Pagination parameters
     * @return Page of active faculties
     */
    @GetMapping("/active")
    @Operation(summary = "Get active faculties", description = "Returns paginated list of active faculties")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> getActiveFaculties(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/faculties/active");

        Page<FacultyDto> page = facultyService.findActive(pageable);
        PageResponse<FacultyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get faculties by type
     *
     * @param code     Faculty type code
     * @param pageable Pagination parameters
     * @return Page of faculties
     */
    @GetMapping("/type/{code}")
    @Operation(summary = "Get faculties by type", description = "Returns paginated list of faculties by type")
    public ResponseEntity<ResponseWrapper<PageResponse<FacultyDto>>> getFacultiesByType(
            @Parameter(description = "Faculty type code") @PathVariable String code,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/faculties/type/{}", code);

        Page<FacultyDto> page = facultyService.findByType(code, pageable);
        PageResponse<FacultyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Count faculties by university
     *
     * @param code University code
     * @return Count of faculties
     */
    @GetMapping("/university/{code}/count")
    @Operation(summary = "Count faculties by university", description = "Returns count of faculties for a university")
    public ResponseEntity<ResponseWrapper<Long>> countFacultiesByUniversity(
            @Parameter(description = "University code") @PathVariable String code
    ) {
        log.debug("GET /app/rest/v2/faculties/university/{}/count", code);

        long count = facultyService.countByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    // =====================================================
    // WRITE Operations (CREATE/UPDATE)
    // =====================================================

    /**
     * Create new faculty
     *
     * <p>Requires ADMIN or UNIVERSITY_ADMIN role</p>
     *
     * @param dto FacultyDto
     * @return Created faculty with generated ID
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Create faculty", description = "Creates new faculty (requires ADMIN or UNIVERSITY_ADMIN role)")
    public ResponseEntity<ResponseWrapper<FacultyDto>> createFaculty(
            @Valid @RequestBody FacultyDto dto
    ) {
        log.debug("POST /app/rest/v2/faculties - dto: {}", dto);

        FacultyDto created = facultyService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(created));
    }

    /**
     * Update existing faculty
     *
     * <p>Partial update: Only non-null fields are updated</p>
     * <p>Requires ADMIN or UNIVERSITY_ADMIN role</p>
     *
     * @param id  Faculty UUID
     * @param dto FacultyDto with updated values
     * @return Updated faculty
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Update faculty", description = "Updates existing faculty (requires ADMIN or UNIVERSITY_ADMIN role)")
    public ResponseEntity<ResponseWrapper<FacultyDto>> updateFaculty(
            @Parameter(description = "Faculty UUID") @PathVariable UUID id,
            @Valid @RequestBody FacultyDto dto
    ) {
        log.debug("PUT /app/rest/v2/faculties/{} - dto: {}", id, dto);

        FacultyDto updated = facultyService.update(id, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // DELETE Operations (SOFT DELETE ONLY)
    // =====================================================

    /**
     * Soft delete faculty (deactivate)
     *
     * <p><strong>CRITICAL - NO-DELETE Constraint:</strong></p>
     * <ul>
     *   <li>NO physical DELETE operation</li>
     *   <li>Sets deleteTs = current timestamp</li>
     *   <li>Record remains in database (hidden by @Where clause)</li>
     * </ul>
     *
     * <p>Requires ADMIN role</p>
     *
     * @param id Faculty UUID
     * @return Success message
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete faculty", description = "Deactivates faculty (soft delete, requires ADMIN role)")
    public ResponseEntity<ResponseWrapper<Void>> deactivateFaculty(
            @Parameter(description = "Faculty UUID") @PathVariable UUID id
    ) {
        log.debug("PATCH /app/rest/v2/faculties/{}/deactivate", id);

        facultyService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Faculty deactivated successfully"));
    }
}
