package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
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
import uz.hemis.service.university.UniversityService;
import uz.hemis.common.dto.PageResponse;
import uz.hemis.service.util.PageResponses;
import uz.hemis.common.dto.ResponseWrapper;
import uz.hemis.common.dto.university.UniversityDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
@Tag(name = "15.OTM", description = "Oliy ta'lim muassasalari")
@SecurityRequirement(name = "apiKeyAuth")
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
    @Operation(summary = "Barcha OTM ro'yxati", description = "Old-hemis CUBA klient uchun: barcha aktiv 200+ universitetlar (paginatsiyasiz, JSON `data` array).")
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUniversities() {
        log.info("GET /app/rest/v2/universities - barcha universitetlar (old-hemis format)");

        List<UniversityDto> universities = universityService.findAllList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("count", universities.size());
        result.put("data", universities);

        return ResponseEntity.ok(result);
    }

    /**
     * Get university by code (Primary Key)
     *
     * <p><strong>Legacy URL:</strong> GET /app/rest/v2/universities/{code}</p>
     *
     * @param code university code (PK - VARCHAR)
     * @return ResponseWrapper with UniversityDto
     */
    @Operation(summary = "Kod bo'yicha OTM topish", description = "PK - VARCHAR. CUBA klient va OTM B2B integratsiyasi uchun.")
    @PreAuthorize("isAuthenticated()")
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
    @Operation(summary = "STIR bo'yicha OTM topish", description = "Soliq Identifikatsion Raqami orqali — moliyaviy hisobotlar uchun.")
    @PreAuthorize("isAuthenticated()")
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
    @Operation(summary = "OTM nomi bo'yicha qidirish", description = "Partial match, case-insensitive. CUBA admin paneli filter uchun.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> searchUniversities(
            @RequestParam String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/search?name={}", name);

        Page<UniversityDto> universities = universityService.findByName(name, pageable);
        PageResponse<UniversityDto> pageResponse = PageResponses.from(universities);

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
    @Operation(summary = "Faqat aktiv OTM lar", description = "active=true bo'lgan universitetlar (yopilgan/sus filterlash uchun).")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/active")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getActiveUniversities(
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/active");

        Page<UniversityDto> universities = universityService.findActiveUniversities(pageable);
        PageResponse<UniversityDto> pageResponse = PageResponses.from(universities);

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
    @Operation(summary = "Tur bo'yicha OTM lar", description = "Universitet, Institut, Akademiya, Filial — h_university_type kodi.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/type/{typeCode}")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getUniversitiesByType(
            @PathVariable String typeCode,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/type/{}", typeCode);

        Page<UniversityDto> universities = universityService.findByType(typeCode, pageable);
        PageResponse<UniversityDto> pageResponse = PageResponses.from(universities);

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
    @Operation(summary = "Mulkchilik turi bo'yicha OTM lar", description = "Davlat, Xususiy, Aralash — h_ownership kodi.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/ownership/{ownershipCode}")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getUniversitiesByOwnership(
            @PathVariable String ownershipCode,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/ownership/{}", ownershipCode);

        Page<UniversityDto> universities = universityService.findByOwnership(ownershipCode, pageable);
        PageResponse<UniversityDto> pageResponse = PageResponses.from(universities);

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
    @Operation(summary = "Hudud bo'yicha OTM lar", description = "SOATO viloyat kodi (Toshkent, Samarqand, Farg'ona, ...).")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/region/{soatoRegion}")
    public ResponseEntity<ResponseWrapper<PageResponse<UniversityDto>>> getUniversitiesByRegion(
            @PathVariable String soatoRegion,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        log.info("GET /app/rest/v2/universities/region/{}", soatoRegion);

        Page<UniversityDto> universities = universityService.findByRegion(soatoRegion, pageable);
        PageResponse<UniversityDto> pageResponse = PageResponses.from(universities);

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
    @Operation(summary = "Bosh OTM ning filiallari", description = "Hierarchical structure — parent OTM kodi orqali farzand OTMlar.")
    @PreAuthorize("isAuthenticated()")
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
    @Operation(summary = "Aktiv OTMlar soni", description = "Dashboard statistika uchun count(*) WHERE active=true.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/count/active")
    public ResponseEntity<ResponseWrapper<Long>> countActiveUniversities() {
        log.info("GET /app/rest/v2/universities/count/active");

        long count = universityService.countActive();
        return ResponseEntity.ok(ResponseWrapper.success(count));
    }

    // =====================================================
    // Write Operations (ADMIN/OTM_API only)
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
    @Operation(summary = "Yangi OTM yaratish", description = "ADMIN roli kerak. Code (PK) takrorlana olmaydi.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<UniversityDto>> createUniversity(@Valid @RequestBody UniversityDto dto) {
        log.info("POST /app/rest/v2/universities - code: {}", dto.getCode());

        UniversityDto created = universityService.create(dto);
        return ResponseEntity.ok(ResponseWrapper.success(created));
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
    @Operation(summary = "OTM ni to'liq yangilash (PUT)", description = "ADMIN roli kerak. Code (PK) o'zgartirib bo'lmaydi — boshqa fieldlar to'liq replace.")
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
    @Operation(summary = "OTM ni qisman yangilash (PATCH)", description = "ADMIN roli kerak. Faqat null bo'lmagan fieldlar yangilanadi.")
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
