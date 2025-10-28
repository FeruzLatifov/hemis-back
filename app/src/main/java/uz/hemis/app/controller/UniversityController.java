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
import uz.hemis.app.service.UniversityService;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.UniversityDto;

import java.util.List;

/**
 * University REST Controller - API Layer
 *
 * <p><strong>CRITICAL - Legacy URL Preservation:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/universities (unchanged from CUBA)</li>
 *   <li>200+ universities depend on this API contract</li>
 *   <li>Response format must match legacy (ResponseWrapper + PageResponse)</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /app/rest/v2/universities - List all universities (paginated)</li>
 *   <li>GET /app/rest/v2/universities/{code} - Find by code (PK)</li>
 *   <li>GET /app/rest/v2/universities/tin/{tin} - Find by TIN</li>
 *   <li>GET /app/rest/v2/universities/search - Search by name</li>
 *   <li>GET /app/rest/v2/universities/active - List active universities</li>
 *   <li>POST /app/rest/v2/universities - Create new university</li>
 *   <li>PUT /app/rest/v2/universities/{code} - Update existing university</li>
 *   <li>PATCH /app/rest/v2/universities/{code} - Partial update</li>
 *   <li>❌ NO DELETE endpoint (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>All exceptions handled by {@link uz.hemis.app.exception.GlobalExceptionHandler}</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/universities")
@RequiredArgsConstructor
@Slf4j
public class UniversityController {

    private final UniversityService universityService;

    // =====================================================
    // Read Operations
    // =====================================================

    /**
     * Get all universities (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/universities</p>
     *
     * <p><strong>Query Parameters:</strong></p>
     * <ul>
     *   <li>page - page number (default: 0)</li>
     *   <li>size - page size (default: 20)</li>
     *   <li>sort - sorting (e.g., name,asc)</li>
     * </ul>
     *
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<UniversityDto>
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getAllUniversities(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<UniversityDto> universities = universityService.findAll(pageable);
        PageResponse<UniversityDto> pageResponse = PageResponse.of(universities);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get university by code (Primary Key)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/universities/{code}</p>
     *
     * @param code university code (PK - VARCHAR)
     * @return ResponseWrapper with UniversityDto
     */
    @GetMapping("/{code}")
    public ResponseEntity<ResponseWrapper<UniversityDto>> getUniversityByCode(@PathVariable String code) {
        log.info("GET /app/rest/v2/universities/{}", code);

        UniversityDto university = universityService.findByCode(code);
        return ResponseEntity.ok(ResponseWrapper.success(university));
    }

    /**
     * Get university by TIN
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/universities/tin/{tin}</p>
     *
     * @param tin Tax Identification Number
     * @return ResponseWrapper with UniversityDto
     */
    @GetMapping("/tin/{tin}")
    public ResponseEntity<ResponseWrapper<UniversityDto>> getUniversityByTin(@PathVariable String tin) {
        log.info("GET /app/rest/v2/universities/tin/{}", tin);

        UniversityDto university = universityService.findByTin(tin);
        return ResponseEntity.ok(ResponseWrapper.success(university));
    }

    /**
     * Search universities by name (partial match, case-insensitive)
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/universities/search?name={name}</p>
     *
     * @param name university name (partial)
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<UniversityDto>
     */
    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> searchUniversities(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/search?name={}", name);

        Page<UniversityDto> universities = universityService.findByName(name, pageable);
        PageResponse<UniversityDto> pageResponse = PageResponse.of(universities);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get active universities
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/universities/active</p>
     *
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<UniversityDto>
     */
    @GetMapping("/active")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getActiveUniversities(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/active");

        Page<UniversityDto> universities = universityService.findActiveUniversities(pageable);
        PageResponse<UniversityDto> pageResponse = PageResponse.of(universities);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get universities by type
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/universities/type/{typeCode}</p>
     *
     * @param typeCode university type code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<UniversityDto>
     */
    @GetMapping("/type/{typeCode}")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getUniversitiesByType(
            @PathVariable String typeCode,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/type/{}", typeCode);

        Page<UniversityDto> universities = universityService.findByType(typeCode, pageable);
        PageResponse<UniversityDto> pageResponse = PageResponse.of(universities);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get universities by ownership
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/universities/ownership/{ownershipCode}</p>
     *
     * @param ownershipCode ownership code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<UniversityDto>
     */
    @GetMapping("/ownership/{ownershipCode}")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getUniversitiesByOwnership(
            @PathVariable String ownershipCode,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/ownership/{}", ownershipCode);

        Page<UniversityDto> universities = universityService.findByOwnership(ownershipCode, pageable);
        PageResponse<UniversityDto> pageResponse = PageResponse.of(universities);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get universities by region
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/universities/region/{soatoRegion}</p>
     *
     * @param soatoRegion SOATO region code
     * @param pageable pagination parameters
     * @return ResponseWrapper with PageResponse<UniversityDto>
     */
    @GetMapping("/region/{soatoRegion}")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getUniversitiesByRegion(
            @PathVariable String soatoRegion,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/region/{}", soatoRegion);

        Page<UniversityDto> universities = universityService.findByRegion(soatoRegion, pageable);
        PageResponse<UniversityDto> pageResponse = PageResponse.of(universities);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get child universities by parent code
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/universities/parent/{parentCode}/children</p>
     *
     * @param parentCode parent university code
     * @return ResponseWrapper with List<UniversityDto>
     */
    @GetMapping("/parent/{parentCode}/children")
    public ResponseEntity<ResponseWrapper<List<UniversityDto>>> getChildUniversities(@PathVariable String parentCode) {
        log.info("GET /app/rest/v2/universities/parent/{}/children", parentCode);

        List<UniversityDto> children = universityService.findByParent(parentCode);
        return ResponseEntity.ok(ResponseWrapper.success(children));
    }

    /**
     * Count active universities
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/universities/count/active</p>
     *
     * @return ResponseWrapper with count
     */
    @GetMapping("/count/active")
    public ResponseEntity<ResponseWrapper<Long>> countActiveUniversities() {
        log.info("GET /app/rest/v2/universities/count/active");

        long count = universityService.countActive();
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    // =====================================================
    // Write Operations (ADMIN/UNIVERSITY_ADMIN only)
    // =====================================================

    /**
     * Create new university
     *
     * <p><strong>Legacy URL:</strong> POST /app/rest/v2/universities</p>
     *
     * <p><strong>Authorization:</strong> ADMIN only</p>
     *
     * @param dto university DTO
     * @return ResponseWrapper with created UniversityDto
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<UniversityDto>> createUniversity(@Valid @RequestBody UniversityDto dto) {
        log.info("POST /app/rest/v2/universities - code: {}", dto.getCode());

        UniversityDto created = universityService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseWrapper.success(created));
    }

    /**
     * Update existing university
     *
     * <p><strong>Legacy URL:</strong> PUT /app/rest/v2/universities/{code}</p>
     *
     * <p><strong>Authorization:</strong> ADMIN only</p>
     *
     * @param code university code (PK - cannot be changed)
     * @param dto university DTO
     * @return ResponseWrapper with updated UniversityDto
     */
    @PutMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<UniversityDto>> updateUniversity(
            @PathVariable String code,
            @Valid @RequestBody UniversityDto dto
    ) {
        log.info("PUT /app/rest/v2/universities/{}", code);

        UniversityDto updated = universityService.update(code, dto);
        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    /**
     * Partial update (PATCH) - only update non-null fields
     *
     * <p><strong>Legacy URL:</strong> PATCH /app/rest/v2/universities/{code}</p>
     *
     * <p><strong>Authorization:</strong> ADMIN only</p>
     *
     * @param code university code (PK)
     * @param dto university DTO with fields to update
     * @return ResponseWrapper with updated UniversityDto
     */
    @PatchMapping("/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<UniversityDto>> partialUpdateUniversity(
            @PathVariable String code,
            @RequestBody UniversityDto dto
    ) {
        log.info("PATCH /app/rest/v2/universities/{}", code);

        UniversityDto updated = universityService.partialUpdate(code, dto);
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
