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
import uz.hemis.app.service.DiplomaService;
import uz.hemis.common.dto.DiplomaDto;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.common.dto.ResponseWrapper;

import java.util.List;
import java.util.UUID;

/**
 * Diploma REST Controller - API Layer
 *
 * <p><strong>CRITICAL - Legacy URL Preservation:</strong></p>
 * <ul>
 *   <li>Base URL: /app/rest/v2/diplomas (new standardized endpoint)</li>
 *   <li>Legacy URLs: /app/rest/diploma/info, /app/rest/diploma/byhash (preserved)</li>
 *   <li>200+ universities depend on this API contract</li>
 *   <li>Response format must match legacy (ResponseWrapper + PageResponse)</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET /app/rest/v2/diplomas - List all diplomas (paginated)</li>
 *   <li>GET /app/rest/v2/diplomas/{id} - Find by ID</li>
 *   <li>GET /app/rest/v2/diplomas/number/{number} - Find by diploma number</li>
 *   <li>GET /app/rest/v2/diplomas/hash/{hash} - Find by hash (verification)</li>
 *   <li>GET /app/rest/v2/diplomas?university={code} - Filter by university</li>
 *   <li>GET /app/rest/v2/diplomas?student={id} - Filter by student</li>
 *   <li>POST /app/rest/v2/diplomas - Create new diploma</li>
 *   <li>PUT /app/rest/v2/diplomas/{id} - Update existing diploma</li>
 *   <li>❌ NO DELETE endpoint (NDG - Non-Deletion Guarantee)</li>
 * </ul>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>All exceptions handled by {@link uz.hemis.app.exception.GlobalExceptionHandler}</p>
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/diplomas")
@RequiredArgsConstructor
@Slf4j
public class DiplomaController {

    private final DiplomaService diplomaService;

    // =====================================================
    // Read Operations
    // =====================================================

    /**
     * Get all diplomas (paginated)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/diplomas</p>
     *
     * @param pageable pagination parameters
     * @return paginated list of diplomas
     */
    @GetMapping
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaDto>>> getAllDiplomas(
            @PageableDefault(size = 20, sort = "issueDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/diplomas - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<DiplomaDto> diplomas = diplomaService.findAll(pageable);
        PageResponse<DiplomaDto> pageResponse = PageResponse.of(diplomas);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get diploma by ID
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/diplomas/{id}</p>
     *
     * @param id diploma ID (UUID)
     * @return diploma details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<DiplomaDto>> getDiplomaById(@PathVariable UUID id) {
        log.debug("GET /app/rest/v2/diplomas/{}", id);

        DiplomaDto diploma = diplomaService.findById(id);

        return ResponseEntity.ok(ResponseWrapper.success(diploma));
    }

    /**
     * Get diploma by number
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/diploma/info (preserved for compatibility)</p>
     * <p><strong>New URL:</strong> GET /app/rest/v2/diplomas/number/{number}</p>
     *
     * @param diplomaNumber diploma registration number
     * @return diploma details
     */
    @GetMapping("/number/{diplomaNumber}")
    public ResponseEntity<ResponseWrapper<DiplomaDto>> getDiplomaByNumber(@PathVariable String diplomaNumber) {
        log.debug("GET /app/rest/v2/diplomas/number/{}", diplomaNumber);

        DiplomaDto diploma = diplomaService.findByDiplomaNumber(diplomaNumber);

        return ResponseEntity.ok(ResponseWrapper.success(diploma));
    }

    /**
     * Get diploma by hash (for verification)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/diploma/byhash (preserved for compatibility)</p>
     * <p><strong>New URL:</strong> GET /app/rest/v2/diplomas/hash/{hash}</p>
     *
     * <p>Used by external verification systems</p>
     *
     * @param diplomaHash SHA-256 hash of diploma data
     * @return diploma details
     */
    @GetMapping("/hash/{diplomaHash}")
    public ResponseEntity<ResponseWrapper<DiplomaDto>> getDiplomaByHash(@PathVariable String diplomaHash) {
        log.debug("GET /app/rest/v2/diplomas/hash/{}", diplomaHash);

        DiplomaDto diploma = diplomaService.findByHash(diplomaHash);

        return ResponseEntity.ok(ResponseWrapper.success(diploma));
    }

    /**
     * Get diplomas by university code
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/diplomas?university={code}</p>
     *
     * @param universityCode university code
     * @param pageable pagination parameters
     * @return paginated list of diplomas for the university
     */
    @GetMapping(params = "university")
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaDto>>> getDiplomasByUniversity(
            @RequestParam("university") String universityCode,
            @PageableDefault(size = 20, sort = "issueDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/diplomas?university={}", universityCode);

        Page<DiplomaDto> diplomas = diplomaService.findByUniversity(universityCode, pageable);
        PageResponse<DiplomaDto> pageResponse = PageResponse.of(diplomas);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get diplomas by student
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/diplomas?student={id}</p>
     *
     * @param studentId student UUID
     * @return list of diplomas for the student
     */
    @GetMapping(params = "student")
    public ResponseEntity<ResponseWrapper<List<DiplomaDto>>> getDiplomasByStudent(
            @RequestParam("student") UUID studentId
    ) {
        log.debug("GET /app/rest/v2/diplomas?student={}", studentId);

        List<DiplomaDto> diplomas = diplomaService.findByStudent(studentId);

        return ResponseEntity.ok(ResponseWrapper.success(diplomas));
    }

    /**
     * Get diplomas by university and status
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diplomas?university={code}&status={status}</p>
     *
     * @param universityCode university code
     * @param status diploma status
     * @param pageable pagination parameters
     * @return paginated list of diplomas
     */
    @GetMapping(params = {"university", "status"})
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaDto>>> getDiplomasByUniversityAndStatus(
            @RequestParam("university") String universityCode,
            @RequestParam("status") String status,
            @PageableDefault(size = 20, sort = "issueDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/diplomas?university={}&status={}", universityCode, status);

        Page<DiplomaDto> diplomas = diplomaService.findByUniversityAndStatus(universityCode, status, pageable);
        PageResponse<DiplomaDto> pageResponse = PageResponse.of(diplomas);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    /**
     * Get diplomas by university and graduation year
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/diplomas?university={code}&year={year}</p>
     *
     * @param universityCode university code
     * @param graduationYear graduation year
     * @param pageable pagination parameters
     * @return paginated list of diplomas
     */
    @GetMapping(params = {"university", "year"})
    public ResponseEntity<ResponseWrapper<PageResponse<DiplomaDto>>> getDiplomasByUniversityAndYear(
            @RequestParam("university") String universityCode,
            @RequestParam("year") Integer graduationYear,
            @PageableDefault(size = 20, sort = "issueDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.debug("GET /app/rest/v2/diplomas?university={}&year={}", universityCode, graduationYear);

        Page<DiplomaDto> diplomas = diplomaService.findByUniversityAndYear(universityCode, graduationYear, pageable);
        PageResponse<DiplomaDto> pageResponse = PageResponse.of(diplomas);

        return ResponseEntity.ok(ResponseWrapper.success(pageResponse));
    }

    // =====================================================
    // Write Operations
    // =====================================================

    /**
     * Create new diploma
     *
     * <p><strong>Legacy URL:</strong> POST /app/rest/v2/diplomas</p>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN or ROLE_UNIVERSITY_ADMIN</p>
     *
     * @param diplomaDto diploma data
     * @return created diploma (HTTP 201 CREATED)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<DiplomaDto>> createDiploma(
            @Valid @RequestBody DiplomaDto diplomaDto
    ) {
        log.info("POST /app/rest/v2/diplomas - creating diploma: {}", diplomaDto.getDiplomaNumber());

        DiplomaDto created = diplomaService.create(diplomaDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseWrapper.success(created));
    }

    /**
     * Update existing diploma (full update)
     *
     * <p><strong>Legacy URL:</strong> PUT /app/rest/v2/diplomas/{id}</p>
     *
     * <p><strong>Security:</strong> Requires ROLE_ADMIN or ROLE_UNIVERSITY_ADMIN</p>
     *
     * @param id diploma ID (UUID)
     * @param diplomaDto diploma data
     * @return updated diploma
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<ResponseWrapper<DiplomaDto>> updateDiploma(
            @PathVariable UUID id,
            @Valid @RequestBody DiplomaDto diplomaDto
    ) {
        log.info("PUT /app/rest/v2/diplomas/{} - updating diploma", id);

        DiplomaDto updated = diplomaService.update(id, diplomaDto);

        return ResponseEntity.ok(ResponseWrapper.success(updated));
    }

    // =====================================================
    // NOTE: NO DELETE ENDPOINT
    // =====================================================
    // Physical DELETE is prohibited (NDG - Non-Deletion Guarantee)
    // Soft delete is handled internally by admin-api module
    // =====================================================
}
