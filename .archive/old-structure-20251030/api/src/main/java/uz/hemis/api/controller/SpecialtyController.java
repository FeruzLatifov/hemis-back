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
import uz.hemis.common.dto.SpecialtyDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.domain.service.SpecialtyService;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Specialty operations
 *
 * <p><strong>Base URL:</strong> /app/rest/v2/specialties</p>
 * <p>CRITICAL: URL preserved from CUBA Platform API for backward compatibility</p>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET    / → List all specialties (paginated)</li>
 *   <li>GET    /{id} → Get specialty by ID</li>
 *   <li>GET    /code/{code} → Get specialty by code</li>
 *   <li>GET    /university/{code} → List specialties by university</li>
 *   <li>GET    /faculty/{id} → List specialties by faculty</li>
 *   <li>GET    /faculty/{id}/all → List all specialties by faculty (no pagination)</li>
 *   <li>GET    /active → List active specialties</li>
 *   <li>GET    /education-type/{code} → List specialties by education type</li>
 *   <li>GET    /education-form/{code} → List specialties by education form</li>
 *   <li>GET    /university/{code}/education-type/{typeCode} → List by university and education type</li>
 *   <li>GET    /search → Search specialties by name</li>
 *   <li>GET    /university/{code}/count → Count specialties by university</li>
 *   <li>GET    /faculty/{id}/count → Count specialties by faculty</li>
 *   <li>POST   / → Create new specialty (ADMIN/UNIVERSITY_ADMIN)</li>
 *   <li>PUT    /{id} → Update specialty (ADMIN/UNIVERSITY_ADMIN)</li>
 *   <li>PATCH  /{id}/deactivate → Soft delete specialty (ADMIN)</li>
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
@RequestMapping("/app/rest/v2/specialties")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Specialty", description = "Specialty management API")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    // =====================================================
    // READ Operations
    // =====================================================

    /**
     * Get all specialties (paginated)
     *
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of specialties
     */
    @GetMapping
    @Operation(summary = "Get all specialties", description = "Returns paginated list of all specialties")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getAllSpecialties(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/specialties - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<SpecialtyDto> page = specialtyService.findAll(pageable);
        PageResponse<SpecialtyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get specialty by ID
     *
     * @param id Specialty UUID
     * @return Specialty details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get specialty by ID", description = "Returns specialty details by UUID")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> getSpecialtyById(
            @Parameter(description = "Specialty UUID") @PathVariable UUID id
    ) {
        log.debug("GET /app/rest/v2/specialties/{}", id);

        SpecialtyDto specialty = specialtyService.findById(id);
        return ResponseEntity.ok(ResponseWrapper.success(specialty));
    }

    /**
     * Get specialty by code
     *
     * @param code Specialty code
     * @return Specialty details
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "Get specialty by code", description = "Returns specialty details by unique code")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> getSpecialtyByCode(
            @Parameter(description = "Specialty code") @PathVariable String code
    ) {
        log.debug("GET /app/rest/v2/specialties/code/{}", code);

        SpecialtyDto specialty = specialtyService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(specialty));
    }

    /**
     * Get specialties by university (paginated)
     *
     * @param code     University code
     * @param pageable Pagination parameters
     * @return Page of specialties
     */
    @GetMapping("/university/{code}")
    @Operation(summary = "Get specialties by university", description = "Returns paginated list of specialties for a university")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByUniversity(
            @Parameter(description = "University code") @PathVariable String code,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/specialties/university/{}", code);

        Page<SpecialtyDto> page = specialtyService.findByUniversity(code, pageable);
        PageResponse<SpecialtyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get specialties by faculty (paginated)
     *
     * @param id       Faculty UUID
     * @param pageable Pagination parameters
     * @return Page of specialties
     */
    @GetMapping("/faculty/{id}")
    @Operation(summary = "Get specialties by faculty", description = "Returns paginated list of specialties for a faculty")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByFaculty(
            @Parameter(description = "Faculty UUID") @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/specialties/faculty/{}", id);

        Page<SpecialtyDto> page = specialtyService.findByFaculty(id, pageable);
        PageResponse<SpecialtyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get all specialties by faculty (no pagination)
     *
     * @param id Faculty UUID
     * @return List of all specialties
     */
    @GetMapping("/faculty/{id}/all")
    @Operation(summary = "Get all specialties by faculty", description = "Returns complete list of specialties for a faculty (no pagination)")
    public ResponseEntity<ResponseWrapper<List<SpecialtyDto>>> getAllSpecialtiesByFaculty(
            @Parameter(description = "Faculty UUID") @PathVariable UUID id
    ) {
        log.debug("GET /app/rest/v2/specialties/faculty/{}/all", id);

        List<SpecialtyDto> specialties = specialtyService.findAllByFaculty(id);
        return ResponseEntity.ok(ResponseWrapper.success(specialties));
    }

    /**
     * Search specialties by name
     *
     * @param name     Name to search (partial match, case-insensitive)
     * @param pageable Pagination parameters
     * @return Page of specialties
     */
    @GetMapping("/search")
    @Operation(summary = "Search specialties by name", description = "Returns specialties matching the name pattern")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> searchSpecialties(
            @Parameter(description = "Name to search") @RequestParam String name,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/specialties/search?name={}", name);

        Page<SpecialtyDto> page = specialtyService.findByNameContaining(name, pageable);
        PageResponse<SpecialtyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get active specialties
     *
     * @param pageable Pagination parameters
     * @return Page of active specialties
     */
    @GetMapping("/active")
    @Operation(summary = "Get active specialties", description = "Returns paginated list of active specialties")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getActiveSpecialties(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/specialties/active");

        Page<SpecialtyDto> page = specialtyService.findActive(pageable);
        PageResponse<SpecialtyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get specialties by education type
     *
     * @param code     Education type code ('11' = Bachelor, '12' = Master, '13' = PhD)
     * @param pageable Pagination parameters
     * @return Page of specialties
     */
    @GetMapping("/education-type/{code}")
    @Operation(summary = "Get specialties by education type", description = "Returns specialties by education type (11=Bachelor, 12=Master, 13=PhD)")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByEducationType(
            @Parameter(description = "Education type code") @PathVariable String code,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/specialties/education-type/{}", code);

        Page<SpecialtyDto> page = specialtyService.findByEducationType(code, pageable);
        PageResponse<SpecialtyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get specialties by education form
     *
     * @param code     Education form code ('11' = Full-time, '12' = Part-time, '13' = Evening, '14' = Distance)
     * @param pageable Pagination parameters
     * @return Page of specialties
     */
    @GetMapping("/education-form/{code}")
    @Operation(summary = "Get specialties by education form", description = "Returns specialties by education form (11=Full-time, 12=Part-time, 13=Evening, 14=Distance)")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByEducationForm(
            @Parameter(description = "Education form code") @PathVariable String code,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/specialties/education-form/{}", code);

        Page<SpecialtyDto> page = specialtyService.findByEducationForm(code, pageable);
        PageResponse<SpecialtyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get specialties by university and education type
     *
     * @param code     University code
     * @param typeCode Education type code
     * @param pageable Pagination parameters
     * @return Page of specialties
     */
    @GetMapping("/university/{code}/education-type/{typeCode}")
    @Operation(summary = "Get specialties by university and education type", description = "Returns specialties filtered by both university and education type")
    public ResponseEntity<ResponseWrapper<PageResponse<SpecialtyDto>>> getSpecialtiesByUniversityAndEducationType(
            @Parameter(description = "University code") @PathVariable String code,
            @Parameter(description = "Education type code") @PathVariable String typeCode,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/specialties/university/{}/education-type/{}", code, typeCode);

        Page<SpecialtyDto> page = specialtyService.findByUniversityAndEducationType(code, typeCode, pageable);
        PageResponse<SpecialtyDto> pageResponse = PageResponse.of(page);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Count specialties by university
     *
     * @param code University code
     * @return Count of specialties
     */
    @GetMapping("/university/{code}/count")
    @Operation(summary = "Count specialties by university", description = "Returns count of specialties for a university")
    public ResponseEntity<ResponseWrapper<Long>> countSpecialtiesByUniversity(
            @Parameter(description = "University code") @PathVariable String code
    ) {
        log.debug("GET /app/rest/v2/specialties/university/{}/count", code);

        long count = specialtyService.countByUniversity(code);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    /**
     * Count specialties by faculty
     *
     * @param id Faculty UUID
     * @return Count of specialties
     */
    @GetMapping("/faculty/{id}/count")
    @Operation(summary = "Count specialties by faculty", description = "Returns count of specialties for a faculty")
    public ResponseEntity<ResponseWrapper<Long>> countSpecialtiesByFaculty(
            @Parameter(description = "Faculty UUID") @PathVariable UUID id
    ) {
        log.debug("GET /app/rest/v2/specialties/faculty/{}/count", id);

        long count = specialtyService.countByFaculty(id);
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    // =====================================================
    // WRITE Operations (CREATE/UPDATE)
    // =====================================================

    /**
     * Create new specialty
     *
     * <p>Requires ADMIN or UNIVERSITY_ADMIN role</p>
     *
     * @param dto SpecialtyDto
     * @return Created specialty with generated ID
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Create specialty", description = "Creates new specialty (requires ADMIN or UNIVERSITY_ADMIN role)")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> createSpecialty(
            @Valid @RequestBody SpecialtyDto dto
    ) {
        log.debug("POST /app/rest/v2/specialties - dto: {}", dto);

        SpecialtyDto created = specialtyService.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(created));
    }

    /**
     * Update existing specialty
     *
     * <p>Partial update: Only non-null fields are updated</p>
     * <p>Requires ADMIN or UNIVERSITY_ADMIN role</p>
     *
     * @param id  Specialty UUID
     * @param dto SpecialtyDto with updated values
     * @return Updated specialty
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    @Operation(summary = "Update specialty", description = "Updates existing specialty (requires ADMIN or UNIVERSITY_ADMIN role)")
    public ResponseEntity<ResponseWrapper<SpecialtyDto>> updateSpecialty(
            @Parameter(description = "Specialty UUID") @PathVariable UUID id,
            @Valid @RequestBody SpecialtyDto dto
    ) {
        log.debug("PUT /app/rest/v2/specialties/{} - dto: {}", id, dto);

        SpecialtyDto updated = specialtyService.update(id, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // DELETE Operations (SOFT DELETE ONLY)
    // =====================================================

    /**
     * Soft delete specialty (deactivate)
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
     * @param id Specialty UUID
     * @return Success message
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete specialty", description = "Deactivates specialty (soft delete, requires ADMIN role)")
    public ResponseEntity<ResponseWrapper<Void>> deactivateSpecialty(
            @Parameter(description = "Specialty UUID") @PathVariable UUID id
    ) {
        log.debug("PATCH /app/rest/v2/specialties/{}/deactivate", id);

        specialtyService.softDelete(id);
        return ResponseEntity.ok(ResponseWrapper.success(null, "Specialty deactivated successfully"));
    }
}
