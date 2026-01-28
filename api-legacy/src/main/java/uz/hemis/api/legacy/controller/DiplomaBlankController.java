package uz.hemis.api.legacy.controller;

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
import uz.hemis.service.DiplomaBlankService;
import uz.hemis.common.dto.DiplomaBlankDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

/**
 * Diploma Blank REST Controller - API Layer
 *
 * <p><strong>CRITICAL - Legacy URL Preservation:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/diploma-blanks (new standardized endpoint)</li>
 *   <li>Legacy URLs: /app/rest/diplom-blank/get, /app/rest/diplom-blank/setStatus (preserved)</li>
 *   <li>200+ universities depend on this API contract</li>
 *   <li>Response format must match legacy (ResponseWrapper + PageResponse)</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /app/rest/v2/diploma-blanks - List all diploma blanks (paginated)</li>
 *   <li>GET /app/rest/v2/diploma-blanks/{id} - Find by ID</li>
 *   <li>GET /app/rest/v2/diploma-blanks/code/{code} - Find by blank code</li>
 *   <li>GET /app/rest/v2/diploma-blanks?university={code} - Filter by university</li>
 *   <li>GET /app/rest/v2/diploma-blanks/available?university={code}&type={type} - Get available blanks</li>
 *   <li>POST /app/rest/v2/diploma-blanks - Create new diploma blank</li>
 *   <li>PUT /app/rest/v2/diploma-blanks/{id} - Update existing diploma blank</li>
 *   <li>PUT /app/rest/v2/diploma-blanks/{id}/status - Update blank status</li>
 *   <li>❌ NO DELETE endpoint (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>All exceptions handled by {@link uz.hemis.app.exception.GlobalExceptionHandler}</p>
 *
 * @since 1.0.0
 */
@Tag(name = "Modern Web APIs - Diploma Blanks", description = "RESTful endpoints for diploma blank management with advanced filtering and pagination")
@RestController
@RequestMapping("/app/rest/v2/diploma-blanks")
@RequiredArgsConstructor
@Slf4j
public class DiplomaBlankController {

    private final DiplomaBlankService diplomaBlankService;

    // =====================================================
    // Read Operations
    // =====================================================

    /**
     * Get all diploma blanks (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/diploma-blanks</p>
     *
     * @param pageable pagination parameters
     * @return paginated list of diploma blanks
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaBlankDto>>> getAllDiplomaBlanks(
            @PageableDefault(size = 20, sort = "series,number", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/diploma-blanks - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<DiplomaBlankDto> diplomas = diplomaBlankService.findAll(pageable);
        PageResponse<DiplomaBlankDto> pageResponse = PageResponse.of(diplomas);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get diploma blank by ID
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/diplom-blank/get (preserved for compatibility)</p>
     * <p><strong>New URL:</strong> GET /app/rest/v2/diploma-blanks/{id}</p>
     *
     * @param id diploma blank ID (UUID)
     * @return diploma blank details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<DiplomaBlankDto>> getDiplomaBlankById(@PathVariable UUID id) {
        log.debug("GET /app/rest/v2/diploma-blanks/{}", id);

        DiplomaBlankDto diplomaBlank = diplomaBlankService.findById(id);

        return ResponseEntity.ok(ResponseWrapper.success(diplomaBlank));
    }

    /**
     * Get diploma blank by code (series + number)
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diploma-blanks/code/{code}</p>
     *
     * @param blankCode blank code (e.g., "AB 1234567")
     * @return diploma blank details
     */
    @GetMapping("/code/{blankCode}")
    public ResponseEntity<ResponseWrapper<DiplomaBlankDto>> getDiplomaBlankByCode(@PathVariable String blankCode) {
        log.debug("GET /app/rest/v2/diploma-blanks/code/{}", blankCode);

        DiplomaBlankDto diplomaBlank = diplomaBlankService.findByCode(blankCode);

        return ResponseEntity.ok(ResponseWrapper.success(diplomaBlank));
    }

    /**
     * Get diploma blank by series and number
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diploma-blanks/series/{series}/number/{number}</p>
     *
     * @param series series (e.g., "AB")
     * @param number number (e.g., "1234567")
     * @return diploma blank details
     */
    @GetMapping("/series/{series}/number/{number}")
    public ResponseEntity<ResponseWrapper<DiplomaBlankDto>> getDiplomaBlankBySeriesAndNumber(
            @PathVariable String series,
            @PathVariable String number
    ) {
        log.debug("GET /app/rest/v2/diploma-blanks/series/{}/number/{}", series, number);

        DiplomaBlankDto diplomaBlank = diplomaBlankService.findBySeriesAndNumber(series, number);

        return ResponseEntity.ok(ResponseWrapper.success(diplomaBlank));
    }

    /**
     * Get diploma blanks by university code
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diploma-blanks?university={code}</p>
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return paginated list of diploma blanks for the university
     */
    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaBlankDto>>> getDiplomaBlanksByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "series,number", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/diploma-blanks?university={}", universityCode);

        Page<DiplomaBlankDto> diplomas = diplomaBlankService.findByUniversity(universityCode, pageable);
        PageResponse<DiplomaBlankDto> pageResponse = PageResponse.of(diplomas);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get diploma blanks by university and status
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diploma-blanks?university={code}&status={status}</p>
     *
     * @param universityCode university code
     * @param status blank status
     * @param pageable pagination parameters
     * @return paginated list of diploma blanks
     */
    @GetMapping(params = {"university", "status"})
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaBlankDto>>> getDiplomaBlanksByUniversityAndStatus(
            @RequestParam("university") String universityCode,
            @RequestParam("status") String status,
            @PageableDefault(size = 20, sort = "series,number", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/diploma-blanks?university={}&status={}", universityCode, status);

        Page<DiplomaBlankDto> diplomas = diplomaBlankService.findByUniversityAndStatus(universityCode, status, pageable);
        PageResponse<DiplomaBlankDto> pageResponse = PageResponse.of(diplomas);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get diploma blanks by university and academic year
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diploma-blanks?university={code}&year={year}</p>
     *
     * @param universityCode university code
     * @param academicYear academic year
     * @param pageable pagination parameters
     * @return paginated list of diploma blanks
     */
    @GetMapping(params = {"university", "year"})
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaBlankDto>>> getDiplomaBlanksByUniversityAndYear(
            @RequestParam("university") String universityCode,
            @RequestParam("year") Integer academicYear,
            @PageableDefault(size = 20, sort = "series,number", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/diploma-blanks?university={}&year={}", universityCode, academicYear);

        Page<DiplomaBlankDto> diplomas = diplomaBlankService.findByUniversityAndYear(universityCode, academicYear, pageable);
        PageResponse<DiplomaBlankDto> pageResponse = PageResponse.of(diplomas);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get available diploma blanks by university and type
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diploma-blanks/available?university={code}&type={type}</p>
     *
     * <p>Used for diploma issuance - finds blanks with AVAILABLE status</p>
     *
     * @param universityCode university code
     * @param blankType blank type (BACHELOR, MASTER, DOCTORATE, etc.)
     * @return list of available diploma blanks
     */
    @GetMapping(value = "/available", params = {"university", "type"})
    public ResponseEntity<ResponseWrapper<List<DiplomaBlankDto>>> getAvailableDiplomaBlanks(
            @RequestParam("university") String universityCode,
            @RequestParam("type") String blankType
    ) {
        log.debug("GET /app/rest/v2/diploma-blanks/available?university={}&type={}", universityCode, blankType);

        List<DiplomaBlankDto> diplomas = diplomaBlankService.findAvailableByUniversityAndType(universityCode, blankType);

        return ResponseEntity.ok(ResponseWrapper.success(diplomas));
    }

    /**
     * Get diploma blanks by series
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diploma-blanks/series/{series}</p>
     *
     * @param series series (e.g., "AB")
     * @return list of diploma blanks (ordered by number)
     */
    @GetMapping("/series/{series}")
    public ResponseEntity<ResponseWrapper<List<DiplomaBlankDto>>> getDiplomaBlanksBySeries(
            @PathVariable String series
    ) {
        log.debug("GET /app/rest/v2/diploma-blanks/series/{}", series);

        List<DiplomaBlankDto> diplomas = diplomaBlankService.findBySeries(series);

        return ResponseEntity.ok(ResponseWrapper.success(diplomas));
    }

    // =====================================================
    // Write Operations
    // =====================================================

    /**
     * Create new diploma blank
     *
     * <p><strong>URL:</strong> POST /app/rest/v2/diploma-blanks</p>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN</p>
     *
     * @param diplomaBlankDto diploma blank data
     * @return created diploma blank (HTTP 201 CREATED)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<DiplomaBlankDto>> createDiplomaBlank(
            @Valid @RequestBody DiplomaBlankDto diplomaBlankDto
    ) {
        log.info("POST /app/rest/v2/diploma-blanks - creating diploma blank: {}", diplomaBlankDto.getBlankCode());

        DiplomaBlankDto created = diplomaBlankService.create(diplomaBlankDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(created));
    }

    /**
     * Update existing diploma blank (full update)
     *
     * <p><strong>URL:</strong> PUT /app/rest/v2/diploma-blanks/{id}</p>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN</p>
     *
     * @param id diploma blank ID (UUID)
     * @param diplomaBlankDto diploma blank data
     * @return updated diploma blank
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<DiplomaBlankDto>> updateDiplomaBlank(
            @PathVariable UUID id,
            @Valid @RequestBody DiplomaBlankDto diplomaBlankDto
    ) {
        log.info("PUT /app/rest/v2/diploma-blanks/{} - updating diploma blank", id);

        DiplomaBlankDto updated = diplomaBlankService.update(id, diplomaBlankDto);

        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    /**
     * Update diploma blank status
     *
     * <p><strong>Legacy URL:</strong> PUT /app/rest/diplom-blank/setStatus (preserved for compatibility)</p>
     * <p><strong>New URL:</strong> PUT /app/rest/v2/diploma-blanks/{id}/status</p>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN or ROLE_UNIVERSITY_ADMIN</p>
     *
     * @param id diploma blank ID (UUID)
     * @param status new status (AVAILABLE, ASSIGNED, ISSUED, DAMAGED, LOST, ANNULLED)
     * @return updated diploma blank
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<DiplomaBlankDto>> updateDiplomaBlankStatus(
            @PathVariable UUID id,
            @RequestParam("status") String status
    ) {
        log.info("PUT /app/rest/v2/diploma-blanks/{}/status - updating status to: {}", id, status);

        DiplomaBlankDto updated = diplomaBlankService.updateStatus(id, status);

        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // NOTE: NO DELETE ENDPOINT
    // =====================================================
    // Physical DELETE is prohibited (NDG - Non-Deletion Guarantee)
    // Soft delete is handled internally by admin-api module
    // =====================================================
}
