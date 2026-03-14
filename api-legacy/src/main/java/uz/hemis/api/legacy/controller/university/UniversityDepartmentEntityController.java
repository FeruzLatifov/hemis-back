package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.CubaFilterHelper;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.domain.entity.UniversityDepartment;
import uz.hemis.service.legacy.UniversityDepartmentLegacyService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * University Department Entity Controller - CUBA REST API Compatible
 *
 * <p>Tag 07: OTM bo'linmalari (Entity API)</p>
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_EUniversityDepartment</p>
 *
 * <p><strong>CRITICAL - 100% Backward Compatible:</strong></p>
 * <ul>
 *   <li>Preserves exact CUBA entity API pattern</li>
 *   <li>URL: /app/rest/v2/entities/hemishe_EUniversityDepartment</li>
 *   <li>Response format: CUBA Map structure with _entityName, _instanceName</li>
 *   <li>Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)</li>
 *   <li>Primary key: code (String), NOT UUID!</li>
 * </ul>
 *
 * <p><strong>Endpoints:</strong></p>
 * <ul>
 *   <li>GET    /app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}      - Get by ID (code)</li>
 *   <li>PUT    /app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}      - Update</li>
 *   <li>DELETE /app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}      - Soft delete</li>
 *   <li>GET    /app/rest/v2/entities/hemishe_EUniversityDepartment/search          - Search (URL params)</li>
 *   <li>POST   /app/rest/v2/entities/hemishe_EUniversityDepartment/search          - Search (JSON filter)</li>
 *   <li>GET    /app/rest/v2/entities/hemishe_EUniversityDepartment                 - List all with pagination</li>
 *   <li>POST   /app/rest/v2/entities/hemishe_EUniversityDepartment                 - Create new</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "07.OTM bo'linmalari", description = "OTM bo'linmalari (fakultet, kafedra, bo'lim) bilan ishlash API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EUniversityDepartment")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityDepartmentEntityController {

    private final UniversityDepartmentLegacyService legacyService;
    private final CubaFilterHelper filterHelper;
    private final LegacySecurityHelper securityHelper;

    /**
     * OLD-HEMIS Compatible: Constraint xatolikda HTTP 500 qaytarish
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException e) {
        log.error("Constraint violation: {}", e.getMessage());
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Server error");
        errorResponse.put("details", "");
        return ResponseEntity.status(500).body(errorResponse);
    }

    // ==================== GET BY ID ====================

    /**
     * Bo'linmani ID (code) bo'yicha olish
     *
     * <p><strong>URL:</strong> GET /app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * @param entityId Bo'linma kodi (masalan: "305-10", "305-10-01")
     * @param dynamicAttributes Dynamic attributes qaytarish (CUBA legacy)
     * @param returnNulls null qiymatlarni ham qaytarish
     * @param view View nomi (masalan: eUniversityDepartment-view)
     * @return Bo'linma ma'lumotlari
     */
    @GetMapping("/{entityId}")
    @Operation(
            summary = "Bo'linmani ID bo'yicha olish",
            description = """
                    Bo'linma (fakultet, kafedra, bo'lim) ma'lumotlarini kod bo'yicha olish.

                    **OLD-HEMIS Compatible** - 100% backward compatibility

                    **Endpoint:** GET /app/rest/v2/entities/hemishe_EUniversityDepartment/{entityId}
                    **Auth:** Bearer token (required)

                    **ID formati:** {universityCode}-{sequence} yoki {universityCode}-{parent}-{sequence}
                    **Misollar:** "305-10" (fakultet), "305-10-01" (kafedra)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bo'linma topildi",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                        "_entityName": "hemishe_EUniversityDepartment",
                                        "_instanceName": "Kompyuter injiniringi fakulteti",
                                        "id": "305-10",
                                        "code": "305-10",
                                        "nameUz": "Kompyuter injiniringi fakulteti",
                                        "nameRu": "Факультет компьютерной инженерии",
                                        "university": {
                                            "_entityName": "hemishe_EUniversity",
                                            "code": "305"
                                        },
                                        "deparmentType": {
                                            "_entityName": "hemishe_HUniversityDepartmentType",
                                            "code": "10"
                                        },
                                        "status": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "404", description = "Bo'linma topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Bo'linma kodi", example = "305-10")
            @PathVariable String entityId,
            @Parameter(description = "Dynamic attributes (CUBA legacy)", hidden = true)
            @RequestParam(required = false) String dynamicAttributes,
            @Parameter(description = "null qiymatlarni ham qaytarish")
            @RequestParam(required = false) String returnNulls,
            @Parameter(description = "View nomi", example = "eUniversityDepartment-view")
            @RequestParam(required = false) String view) {

        String universityCode = securityHelper.getUniversityCodeFromContext();
        log.debug("GET UniversityDepartment by id: {}, university: {}, view: {}", entityId, universityCode, view);

        Optional<UniversityDepartment> entity = legacyService.findByCode(entityId);
        if (entity.isEmpty()) {
            log.warn("UniversityDepartment not found: id={}", entityId);
            return ResponseEntity.status(404).body(legacyService.cubaNotFoundError(entityId));
        }

        UniversityDepartment dept = entity.get();
        if (universityCode != null && !dept.getUniversityCode().equals(universityCode)) {
            log.warn("Access denied: department {} belongs to university {}, user university: {}",
                    entityId, dept.getUniversityCode(), universityCode);
            return ResponseEntity.status(404).body(legacyService.cubaNotFoundError(entityId));
        }

        return ResponseEntity.ok(legacyService.toDepartmentMap(dept, legacyService.parseBoolean(returnNulls), view));
    }

    // ==================== UPDATE ====================

    /**
     * OLD-HEMIS Compatible: Bo'sh entityId bilan PUT so'rov
     */
    @PutMapping({"", "/"})
    @Operation(hidden = true)
    public ResponseEntity<Map<String, Object>> updateWithoutId(@RequestBody Map<String, Object> body) {
        log.warn("PUT without entityId - returning OLD-HEMIS compatible error");
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Server error");
        errorResponse.put("details", "");
        return ResponseEntity.status(500).body(errorResponse);
    }

    /**
     * Bo'linmani yangilash
     *
     * @param entityId Bo'linma kodi
     * @param body Yangilash ma'lumotlari
     * @param returnNulls null qiymatlarni ham qaytarish
     * @return Yangilangan bo'linma
     */
    @PutMapping("/{entityId}")
    @Operation(
            summary = "Bo'linmani yangilash",
            description = """
                    Mavjud bo'linma ma'lumotlarini yangilash.

                    **OLD-HEMIS Compatible** - 100% backward compatibility
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bo'linma yangilandi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "404", description = "Bo'linma topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Bo'linma kodi", example = "305-10")
            @PathVariable String entityId,
            @RequestBody Map<String, Object> body,
            @Parameter(description = "null qiymatlarni ham qaytarish")
            @RequestParam(required = false) String returnNulls) {

        String universityCode = securityHelper.getUniversityCodeFromContext();
        log.info("PUT /app/rest/v2/entities/hemishe_EUniversityDepartment/{} - university: {}", entityId, universityCode);

        Optional<UniversityDepartment> existingOpt = legacyService.findByCode(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(legacyService.cubaNotFoundError(entityId));
        }

        UniversityDepartment entity = existingOpt.get();

        if (universityCode != null && !entity.getUniversityCode().equals(universityCode)) {
            log.warn("Access denied for update: department {} belongs to university {}, user university: {}",
                    entityId, entity.getUniversityCode(), universityCode);
            return ResponseEntity.status(404).body(legacyService.cubaNotFoundError(entityId));
        }

        legacyService.updateDepartmentFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());
        entity.setUpdatedBy(securityHelper.getCurrentUsername());

        UniversityDepartment saved = legacyService.save(entity);
        log.info("UniversityDepartment updated: {}", entityId);
        return ResponseEntity.ok(legacyService.toDepartmentMap(saved, legacyService.parseBoolean(returnNulls), null));
    }

    // ==================== DELETE ====================

    /**
     * OLD-HEMIS Compatible: Bo'sh entityId bilan DELETE so'rov
     */
    @DeleteMapping({"", "/"})
    @Operation(hidden = true)
    public ResponseEntity<Map<String, Object>> deleteWithoutId() {
        log.warn("DELETE without entityId - returning OLD-HEMIS compatible error");
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Server error");
        errorResponse.put("details", "");
        return ResponseEntity.status(500).body(errorResponse);
    }

    /**
     * Bo'linmani o'chirish (soft delete)
     *
     * @param entityId Bo'linma kodi
     * @return O'chirilgan bo'linma
     */
    @DeleteMapping("/{entityId}")
    @Operation(
            summary = "Bo'linmani o'chirish",
            description = "Bo'linmani soft delete qilish (delete_ts belgilanadi)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bo'linma o'chirildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "404", description = "Bo'linma topilmadi")
    })
    public ResponseEntity<Map<String, Object>> delete(
            @Parameter(description = "Bo'linma kodi", example = "305-10")
            @PathVariable String entityId) {

        String universityCode = securityHelper.getUniversityCodeFromContext();
        log.info("DELETE /app/rest/v2/entities/hemishe_EUniversityDepartment/{} - university: {}", entityId, universityCode);

        Optional<UniversityDepartment> entity = legacyService.findByCode(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(legacyService.cubaNotFoundError(entityId));
        }

        UniversityDepartment dept = entity.get();

        if (universityCode != null && !dept.getUniversityCode().equals(universityCode)) {
            log.warn("Access denied for delete: department {} belongs to university {}, user university: {}",
                    entityId, dept.getUniversityCode(), universityCode);
            return ResponseEntity.status(404).body(legacyService.cubaNotFoundError(entityId));
        }

        legacyService.softDelete(dept, securityHelper.getCurrentUsername());

        log.info("UniversityDepartment deleted: {}", entityId);
        return ResponseEntity.ok().build();
    }

    // ==================== SEARCH (GET) ====================

    /**
     * Bo'linmalarni qidirish (GET)
     *
     * <p>CUBA filter format:</p>
     * <pre>
     * {"filter":{"conditions":[{"property":"university.code","operator":"=","value":"305"}]}}
     * </pre>
     *
     * <p>Qo'llab-quvvatlanadigan operatorlar: =, <>, >, <, >=, <=, like, startsWith, endsWith, in, isNull, notNull</p>
     */
    @GetMapping("/search")
    @Operation(
            summary = "Bo'linmalarni qidirish (GET)",
            description = """
                    URL parametrlari orqali qidirish.

                    **Filter formati (CUBA compatible):**
                    ```json
                    {"filter":{"conditions":[{"property":"university.code","operator":"=","value":"305"}]}}
                    ```

                    **Qo'llab-quvvatlanadigan operatorlar:** =, <>, like, startsWith, endsWith, in, isNull, notNull

                    **Filtrlash mumkin bo'lgan maydonlar:**
                    - code - Bo'linma kodi
                    - nameUz - O'zbekcha nomi
                    - nameRu - Ruscha nomi
                    - university.code - Universitet kodi
                    - deparmentType.code - Bo'linma turi kodi
                    - status - Holati (true/false)
                    """
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @Parameter(description = "Filter (CUBA format JSON)")
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "null qiymatlarni ham qaytarish")
            @RequestParam(required = false) String returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        Map<String, Object> filterMap = legacyService.parseFilterFromString(filter);

        List<UniversityDepartment> allEntities = legacyService.findAll();
        log.debug("OLD-HEMIS compatible: returning all departments, filter will be applied");

        List<UniversityDepartment> filtered = legacyService.applyFilter(allEntities, filterMap);
        List<UniversityDepartment> result = filterHelper.applyPagination(filtered, offset, limit);

        Boolean returnNullsBool = legacyService.parseBoolean(returnNulls);
        return ResponseEntity.ok(result.stream()
                .map(e -> legacyService.toDepartmentMap(e, returnNullsBool, view))
                .collect(Collectors.toList()));
    }

    // ==================== SEARCH (POST) ====================

    /**
     * Bo'linmalarni qidirish (POST)
     *
     * <p>CUBA filter format:</p>
     * <pre>
     * {
     *   "filter": {
     *     "conditions": [
     *       {"property": "university.code", "operator": "=", "value": "305"},
     *       {"property": "status", "operator": "=", "value": true}
     *     ]
     *   },
     *   "view": "eUniversityDepartment-view"
     * }
     * </pre>
     */
    @PostMapping("/search")
    @Operation(
            summary = "Bo'linmalarni qidirish (POST)",
            description = """
                    JSON filter orqali qidirish.

                    **Request body formati (CUBA compatible):**
                    ```json
                    {
                      "filter": {
                        "conditions": [
                          {"property": "university.code", "operator": "=", "value": "305"},
                          {"property": "status", "operator": "=", "value": true}
                        ]
                      },
                      "view": "eUniversityDepartment-view"
                    }
                    ```

                    **Qo'llab-quvvatlanadigan operatorlar:** =, <>, like, startsWith, endsWith, in, isNull, notNull

                    **Filtrlash mumkin bo'lgan maydonlar:**
                    - code - Bo'linma kodi
                    - nameUz - O'zbekcha nomi
                    - nameRu - Ruscha nomi
                    - university.code - Universitet kodi
                    - deparmentType.code - Bo'linma turi kodi
                    - status - Holati (true/false)
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "CUBA filter",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "filter": {
                                "conditions": [
                                  {"property": "university.code", "operator": "=", "value": "305"}
                                ]
                              }
                            }
                            """)
            )
    )
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Offset") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Limit") @RequestParam(required = false) Integer limit,
            @Parameter(description = "null qiymatlarni ham qaytarish")
            @RequestParam(required = false) String returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        int effectiveOffset = filterHelper.extractInt(body, "offset", offset, 0);
        int effectiveLimit = filterHelper.extractInt(body, "limit", limit, 50);

        log.debug("POST search - offset: {}, limit: {}, body: {}", effectiveOffset, effectiveLimit, body);

        List<UniversityDepartment> allEntities = legacyService.findAll();
        log.debug("OLD-HEMIS compatible: returning all departments, filter will be applied");

        List<UniversityDepartment> filtered = legacyService.applyFilter(allEntities, body);
        List<UniversityDepartment> result = filterHelper.applyPagination(filtered, effectiveOffset, effectiveLimit);

        final String effectiveView;
        if (view != null) {
            effectiveView = view;
        } else if (body != null && body.containsKey("view")) {
            effectiveView = (String) body.get("view");
        } else {
            effectiveView = null;
        }

        Boolean returnNullsBool = legacyService.parseBoolean(returnNulls);
        return ResponseEntity.ok(result.stream()
                .map(e -> legacyService.toDepartmentMap(e, returnNullsBool, effectiveView))
                .collect(Collectors.toList()));
    }

    // ==================== GET ALL ====================

    /**
     * Barcha bo'linmalarni olish (pagination bilan)
     */
    @GetMapping
    @Operation(
            summary = "Barcha bo'linmalarni olish",
            description = """
                    Pagination bilan barcha bo'linmalarni olish.

                    **OLD-HEMIS Compatible** - 100% backward compatibility
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bo'linmalar ro'yxati"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count")
            @RequestParam(required = false) String returnCount,
            @Parameter(description = "Offset for pagination")
            @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page")
            @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort field")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Dynamic attributes", hidden = true)
            @RequestParam(required = false) String dynamicAttributes,
            @Parameter(description = "null qiymatlarni ham qaytarish")
            @RequestParam(required = false) String returnNulls,
            @Parameter(description = "View nomi")
            @RequestParam(required = false) String view) {

        String universityCode = securityHelper.getUniversityCodeFromContext();
        log.debug("GET all UniversityDepartment - university: {}, offset: {}, limit: {}, view: {}",
                universityCode, offset, limit, view);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int safeLimit = Math.max(limit, 1);
        int page = offset / safeLimit;
        PageRequest pageRequest = PageRequest.of(page, safeLimit, sorting);

        Boolean returnNullsBool = legacyService.parseBoolean(returnNulls);
        var entityPage = universityCode != null
                ? legacyService.findByUniversityCode(universityCode, pageRequest)
                : legacyService.findAll(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
                .map(e -> legacyService.toDepartmentMap(e, returnNullsBool, view))
                .collect(Collectors.toList());

        if (Boolean.TRUE.equals(legacyService.parseBoolean(returnCount))) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    // ==================== CREATE ====================

    /**
     * Yangi bo'linma yaratish
     */
    @PostMapping
    @Operation(
            summary = "Yangi bo'linma yaratish",
            description = """
                    Yangi bo'linma (fakultet, kafedra, bo'lim) yaratish.

                    **OLD-HEMIS Compatible** - 100% backward compatibility
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bo'linma yaratildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov")
    })
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @Parameter(description = "null qiymatlarni ham qaytarish")
            @RequestParam(required = false) String returnNulls) {

        String universityCode = securityHelper.getUniversityCodeFromContext();
        log.info("POST /app/rest/v2/entities/hemishe_EUniversityDepartment - university: {}, body: {}", universityCode, body);

        try {
            UniversityDepartment entity = new UniversityDepartment();
            legacyService.updateDepartmentFromMap(entity, body);

            if (universityCode == null) {
                log.error("University code not found for user");
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Forbidden");
                error.put("details", "University code could not be determined for user");
                return ResponseEntity.status(403).body(error);
            }

            String requestUniversityCode = entity.getUniversityCode();
            if (requestUniversityCode != null && !requestUniversityCode.equals(universityCode)) {
                log.warn("Security violation: user {} (university {}) tried to create department for university {}",
                        securityHelper.getCurrentUsername(), universityCode, requestUniversityCode);
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Forbidden");
                error.put("details", "You can only create departments for your own university (" + universityCode + ")");
                return ResponseEntity.status(403).body(error);
            }

            entity.setUniversityCode(universityCode);
            log.debug("Using university code from user context: {}", universityCode);

            if (entity.getCode() == null || entity.getCode().isEmpty()) {
                log.error("id is required but was null");
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Bad Request");
                error.put("details", "id is required");
                return ResponseEntity.status(400).body(error);
            }

            if (entity.getDepartmentType() == null) {
                log.error("deparmentType is required but was null");
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Bad Request");
                error.put("details", "deparmentType is required");
                return ResponseEntity.status(400).body(error);
            }

            // CRITICAL: createOrRestore runs find + save in SINGLE transaction
            // This prevents @SQLRestriction from breaking merge() on soft-deleted entities
            UniversityDepartment saved = legacyService.createOrRestore(entity, universityCode, securityHelper.getCurrentUsername());
            log.info("UniversityDepartment created/restored: {}", saved.getCode());
            return ResponseEntity.ok(legacyService.minimalDepartmentResponse(saved));

        } catch (SecurityException e) {
            log.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(404).body(legacyService.cubaNotFoundError(
                    body.containsKey("id") ? String.valueOf(body.get("id")) : "unknown"));
        } catch (Exception e) {
            log.error("Failed to create UniversityDepartment: {}", e.getMessage(), e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Server error");
            error.put("details", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
