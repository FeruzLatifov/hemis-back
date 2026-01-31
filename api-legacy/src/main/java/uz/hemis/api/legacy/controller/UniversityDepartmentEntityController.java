package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.UniversityDepartment;
import uz.hemis.domain.entity.User;
import uz.hemis.domain.repository.UniversityDepartmentRepository;
import uz.hemis.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import uz.hemis.api.legacy.util.CubaFilterHelper;

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

    private final UniversityDepartmentRepository repository;
    private final UserRepository userRepository;
    private final CubaFilterHelper filterHelper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String ENTITY_NAME = "hemishe_EUniversityDepartment";

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
    @Transactional(readOnly = true)
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

        String universityCode = getUniversityCodeFromContext();
        log.debug("GET UniversityDepartment by id: {}, university: {}, view: {}", entityId, universityCode, view);

        Optional<UniversityDepartment> entity = repository.findByCode(entityId);
        if (entity.isEmpty()) {
            log.warn("UniversityDepartment not found: id={}", entityId);
            return ResponseEntity.status(404).body(cubaNotFoundError(entityId));
        }

        // Security check - faqat o'z universitetining bo'linmalarini ko'rish
        UniversityDepartment dept = entity.get();
        if (universityCode != null && !dept.getUniversityCode().equals(universityCode)) {
            log.warn("Access denied: department {} belongs to university {}, user university: {}",
                    entityId, dept.getUniversityCode(), universityCode);
            return ResponseEntity.status(404).body(cubaNotFoundError(entityId));
        }

        return ResponseEntity.ok(toMap(dept, parseBoolean(returnNulls), view));
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
    @Transactional
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

        String universityCode = getUniversityCodeFromContext();
        log.info("PUT /app/rest/v2/entities/hemishe_EUniversityDepartment/{} - university: {}", entityId, universityCode);

        Optional<UniversityDepartment> existingOpt = repository.findByCode(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(cubaNotFoundError(entityId));
        }

        UniversityDepartment entity = existingOpt.get();

        // Security check
        if (universityCode != null && !entity.getUniversityCode().equals(universityCode)) {
            log.warn("Access denied for update: department {} belongs to university {}, user university: {}",
                    entityId, entity.getUniversityCode(), universityCode);
            return ResponseEntity.status(404).body(cubaNotFoundError(entityId));
        }

        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUsername());

        UniversityDepartment saved = repository.save(entity);
        log.info("UniversityDepartment updated: {}", entityId);
        return ResponseEntity.ok(toMap(saved, parseBoolean(returnNulls), null));
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
    @Transactional
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

        String universityCode = getUniversityCodeFromContext();
        log.info("DELETE /app/rest/v2/entities/hemishe_EUniversityDepartment/{} - university: {}", entityId, universityCode);

        Optional<UniversityDepartment> entity = repository.findByCode(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(cubaNotFoundError(entityId));
        }

        UniversityDepartment dept = entity.get();

        // Security check
        if (universityCode != null && !dept.getUniversityCode().equals(universityCode)) {
            log.warn("Access denied for delete: department {} belongs to university {}, user university: {}",
                    entityId, dept.getUniversityCode(), universityCode);
            return ResponseEntity.status(404).body(cubaNotFoundError(entityId));
        }

        // Soft delete
        dept.setDeleteTs(LocalDateTime.now());
        dept.setDeletedBy(getCurrentUsername());
        repository.save(dept);

        log.info("UniversityDepartment deleted: {}", entityId);
        // OLD-HEMIS: DELETE 200 OK qaytaradi (bo'sh body)
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
    @Transactional(readOnly = true)
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

        // Parse filter from URL-encoded JSON string
        Map<String, Object> filterMap = parseFilterFromString(filter);

        // ✅ OLD-HEMIS COMPATIBLE: university.code filter'ni E'TIBORSIZ QOLDIRISH
        // OLD-HEMIS da /search endpoint BARCHA universitetlardan natija qaytaradi
        // Faqat status va boshqa filterlar qo'llaniladi
        List<UniversityDepartment> allEntities = repository.findAll();
        log.debug("OLD-HEMIS compatible: returning all departments, filter will be applied");

        // Apply CUBA filter conditions (status va boshqa filterlar)
        // university.code filter ham qo'llaniladi agar kerak bo'lsa
        List<UniversityDepartment> filtered = applyFilter(allEntities, filterMap);

        // Apply pagination
        List<UniversityDepartment> result = filterHelper.applyPagination(filtered, offset, limit);

        Boolean returnNullsBool = parseBoolean(returnNulls);
        return ResponseEntity.ok(result.stream()
                .map(e -> toMap(e, returnNullsBool, view))
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
    @Transactional(readOnly = true)
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

        // ✅ OLD-HEMIS COMPATIBLE: university.code filter'ni E'TIBORSIZ QOLDIRISH
        // OLD-HEMIS da /search endpoint BARCHA universitetlardan natija qaytaradi
        List<UniversityDepartment> allEntities = repository.findAll();
        log.debug("OLD-HEMIS compatible: returning all departments, filter will be applied");

        // Apply CUBA filter conditions from request body (university.code e'tiborsiz qoldiriladi)
        List<UniversityDepartment> filtered = applyFilter(allEntities, body);

        // Apply pagination
        List<UniversityDepartment> result = filterHelper.applyPagination(filtered, effectiveOffset, effectiveLimit);

        // Extract view from body if not in query param
        final String effectiveView;
        if (view != null) {
            effectiveView = view;
        } else if (body != null && body.containsKey("view")) {
            effectiveView = (String) body.get("view");
        } else {
            effectiveView = null;
        }

        Boolean returnNullsBool = parseBoolean(returnNulls);
        return ResponseEntity.ok(result.stream()
                .map(e -> toMap(e, returnNullsBool, effectiveView))
                .collect(Collectors.toList()));
    }

    // ==================== GET ALL ====================

    /**
     * Barcha bo'linmalarni olish (pagination bilan)
     */
    @GetMapping
    @Transactional(readOnly = true)
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

        String universityCode = getUniversityCodeFromContext();
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

        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);

        Boolean returnNullsBool = parseBoolean(returnNulls);
        Page<UniversityDepartment> entityPage;
        if (universityCode != null) {
            // ✅ TO'G'RI: Repository da pagination bilan filter
            entityPage = repository.findByUniversityCode(universityCode, pageRequest);
        } else {
            entityPage = repository.findAll(pageRequest);
        }

        return ResponseEntity.ok(entityPage.getContent().stream()
                .map(e -> toMap(e, returnNullsBool, view))
                .collect(Collectors.toList()));
    }

    // ==================== CREATE ====================

    /**
     * Yangi bo'linma yaratish
     */
    @PostMapping
    @Transactional
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

        String universityCode = getUniversityCodeFromContext();
        log.info("POST /app/rest/v2/entities/hemishe_EUniversityDepartment - university: {}, body: {}", universityCode, body);

        try {
            UniversityDepartment entity = new UniversityDepartment();
            updateFromMap(entity, body);

            // ⭐ SECURITY: Foydalanuvchi FAQAT o'z universitetiga tegishli bo'limlarni yaratishi mumkin
            if (universityCode == null) {
                log.error("University code not found for user");
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Forbidden");
                error.put("details", "University code could not be determined for user");
                return ResponseEntity.status(403).body(error);
            }

            // Request body dagi university.code ni tekshirish
            String requestUniversityCode = entity.getUniversityCode();
            if (requestUniversityCode != null && !requestUniversityCode.equals(universityCode)) {
                log.warn("Security violation: user {} (university {}) tried to create department for university {}",
                        getCurrentUsername(), universityCode, requestUniversityCode);
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Forbidden");
                error.put("details", "You can only create departments for your own university (" + universityCode + ")");
                return ResponseEntity.status(403).body(error);
            }

            // User kontekstidagi university code ni majburiy qo'llash
            entity.setUniversityCode(universityCode);
            log.debug("Using university code from user context: {}", universityCode);

            // id/code required tekshiruv
            if (entity.getCode() == null || entity.getCode().isEmpty()) {
                log.error("id is required but was null");
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Bad Request");
                error.put("details", "id is required");
                return ResponseEntity.status(400).body(error);
            }

            // deparmentType required tekshiruv
            if (entity.getDepartmentType() == null) {
                log.error("deparmentType is required but was null");
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("error", "Bad Request");
                error.put("details", "deparmentType is required");
                return ResponseEntity.status(400).body(error);
            }

            // ⭐ CUBA UPSERT BEHAVIOR - mavjud bo'lsa yangilash, bo'lmasa yaratish
            // IMPORTANT: Soft-deleted entitylarni ham topish kerak (aks holda duplicate key error)
            Optional<UniversityDepartment> existingOpt = repository.findByCodeIncludingDeleted(entity.getCode());

            if (existingOpt.isPresent()) {
                // UPDATE mavjud entity (yoki UNDELETE agar soft-deleted bo'lsa)
                UniversityDepartment existing = existingOpt.get();

                // ⭐ SECURITY: Agar user kontekstida university code bo'lsa, faqat o'z universitetini yangilashi mumkin
                if (universityCode != null && !universityCode.equals(existing.getUniversityCode())) {
                    log.warn("Access denied: department {} belongs to university {}, user university: {}",
                            entity.getCode(), existing.getUniversityCode(), universityCode);
                    return ResponseEntity.status(404).body(cubaNotFoundError(entity.getCode()));
                }
                // Agar universityCode == null bo'lsa → old-hemis behavior (ruxsat beriladi)

                // Agar soft-deleted bo'lsa → UNDELETE
                if (existing.getDeleteTs() != null) {
                    log.info("Entity was soft-deleted, restoring: {}", entity.getCode());
                    existing.setDeleteTs(null);
                    existing.setDeletedBy(null);
                } else {
                    log.info("Entity exists, updating: {}", entity.getCode());
                }

                // Faqat kelgan fieldlarni yangilash (universityCode o'zgartirilmaydi!)
                if (entity.getNameUz() != null) existing.setNameUz(entity.getNameUz());
                if (entity.getNameRu() != null) existing.setNameRu(entity.getNameRu());
                if (entity.getStatus() != null) existing.setStatus(entity.getStatus());
                if (entity.getDepartmentType() != null) existing.setDepartmentType(entity.getDepartmentType());
                if (entity.getParentCode() != null) existing.setParentCode(entity.getParentCode());
                if (entity.getPath() != null) existing.setPath(entity.getPath());
                // NOTE: universityCode O'ZGARTIRILMAYDI - xavfsizlik uchun

                existing.setUpdateTs(LocalDateTime.now());
                existing.setUpdatedBy(getCurrentUsername());

                UniversityDepartment saved = repository.save(existing);
                log.info("UniversityDepartment updated via POST: {}", saved.getCode());
                // OLD-HEMIS: POST response faqat minimal ma'lumot qaytaradi
                return ResponseEntity.ok(toMinimalMap(saved));

            } else {
                // CREATE yangi entity
                // Note: version and createTs are set by @PrePersist callback in entity
                entity.setCreatedBy(getCurrentUsername());

                log.info("Creating new entity: code={}, nameUz={}, universityCode={}, departmentType={}",
                        entity.getCode(), entity.getNameUz(), entity.getUniversityCode(), entity.getDepartmentType());

                // Use repository.saveAndFlush() - entity implements Persistable<String>
                // so Spring Data JPA will correctly use persist() for new entities
                UniversityDepartment saved = repository.saveAndFlush(entity);

                log.info("UniversityDepartment created: {}", saved.getCode());
                // OLD-HEMIS: POST response faqat minimal ma'lumot qaytaradi
                return ResponseEntity.ok(toMinimalMap(saved));
            }

        } catch (Exception e) {
            log.error("Failed to create UniversityDepartment: {}", e.getMessage(), e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Server error");
            error.put("details", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * POST/PUT response uchun minimal CUBA format
     * OLD-HEMIS formatiga 100% mos: faqat _entityName, _instanceName, id
     */
    private Map<String, Object> toMinimalMap(UniversityDepartment entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getNameUz() != null ? entity.getNameUz() : entity.getCode());
        map.put("id", entity.getCode());
        return map;
    }

    /**
     * Entity ni CUBA-compatible Map ga o'girish
     *
     * <p><strong>CRITICAL - _instanceName formati:</strong></p>
     * <ul>
     *   <li>Old-hemis: faqat nameUz ("Test bo'lim")</li>
     *   <li>New-hemis: ham shunday bo'lishi kerak!</li>
     *   <li>XATO format: nameUz + code ("Test bo'lim [305-99-TEST]")</li>
     * </ul>
     */
    private Map<String, Object> toMap(UniversityDepartment entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();

        // ✅ OLD-HEMIS 100% COMPATIBLE FORMAT
        // Old-hemis response formati (faqat shu maydonlar!):
        // {
        //   "_entityName": "hemishe_EUniversityDepartment",
        //   "_instanceName": "O'zbek tili va adabiyoti",
        //   "id": "351-118",
        //   "code": "351-118",
        //   "version": 5,
        //   "nameUz": "O'zbek tili va adabiyoti",
        //   "nameRu": "O'zbek tili va adabiyoti",
        //   "status": false
        // }

        // CUBA meta fields
        map.put("_entityName", ENTITY_NAME);
        String instanceName = entity.getNameUz() != null
                ? entity.getNameUz()
                : entity.getCode();
        map.put("_instanceName", instanceName);

        // Primary key - code (not UUID!)
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());

        // Version - OLD-HEMIS qaytaradi
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        // Main fields - OLD-HEMIS qaytaradi
        putIfNotNull(map, "nameUz", entity.getNameUz(), returnNulls);
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
        putIfNotNull(map, "status", entity.getStatus(), returnNulls);

        // View bilan so'rov bo'lsa - expanded reference objectlarni qo'shish
        if (view != null && !"_local".equals(view)) {
            // parent - nested department object yoki null
            if (entity.getParentCode() != null) {
                Map<String, Object> parentObj = new LinkedHashMap<>();
                parentObj.put("_entityName", ENTITY_NAME);
                parentObj.put("id", entity.getParentCode());
                parentObj.put("code", entity.getParentCode());
                map.put("parent", parentObj);
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("parent", null);
            }

            // university - full expanded object
            Map<String, Object> uniObj = loadUniversityObject(entity.getUniversityCode());
            if (uniObj != null) {
                map.put("university", uniObj);
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", null);
            }

            // deparmentType - classifier object (note: typo in old-hemis)
            if (entity.getDepartmentType() != null) {
                map.put("deparmentType", loadClassifierObject(
                    "hemishe_h_university_department_type", "HUniversityDepartmentType", entity.getDepartmentType()));
            } else if (Boolean.TRUE.equals(returnNulls)) {
                map.put("deparmentType", null);
            }

            // path
            putIfNotNull(map, "path", entity.getPath(), returnNulls);
        }

        return map;
    }

    /**
     * Map dan entity ga qiymatlarni o'tkazish
     */
    @SuppressWarnings("unchecked")
    private void updateFromMap(UniversityDepartment entity, Map<String, Object> map) {
        // Code (id)
        if (map.containsKey("id")) {
            entity.setCode(String.valueOf(map.get("id")));
        }

        // Names
        if (map.containsKey("nameUz")) {
            entity.setNameUz((String) map.get("nameUz"));
        }
        if (map.containsKey("nameRu")) {
            entity.setNameRu((String) map.get("nameRu"));
        }

        // Status
        if (map.containsKey("status")) {
            Object status = map.get("status");
            if (status instanceof Boolean) {
                entity.setStatus((Boolean) status);
            } else if (status instanceof String) {
                entity.setStatus(Boolean.parseBoolean((String) status));
            }
        }

        // Path
        if (map.containsKey("path")) {
            entity.setPath((String) map.get("path"));
        }

        // Parent (nested object)
        if (map.containsKey("parent")) {
            Object parent = map.get("parent");
            if (parent instanceof Map) {
                Map<String, Object> parentMap = (Map<String, Object>) parent;
                if (parentMap.containsKey("code")) {
                    entity.setParentCode((String) parentMap.get("code"));
                } else if (parentMap.containsKey("id")) {
                    entity.setParentCode(String.valueOf(parentMap.get("id")));
                }
            } else if (parent instanceof String) {
                entity.setParentCode((String) parent);
            }
        }

        // University (nested object)
        if (map.containsKey("university")) {
            Object university = map.get("university");
            if (university instanceof Map) {
                Map<String, Object> uniMap = (Map<String, Object>) university;
                if (uniMap.containsKey("code")) {
                    entity.setUniversityCode((String) uniMap.get("code"));
                }
            } else if (university instanceof String) {
                entity.setUniversityCode((String) university);
            }
        }

        // DeparmentType (nested object)
        if (map.containsKey("deparmentType")) {
            Object deptType = map.get("deparmentType");
            if (deptType instanceof Map) {
                Map<String, Object> typeMap = (Map<String, Object>) deptType;
                if (typeMap.containsKey("code")) {
                    entity.setDepartmentType((String) typeMap.get("code"));
                }
            } else if (deptType instanceof String) {
                entity.setDepartmentType((String) deptType);
            }
        }
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    /**
     * Security context dan universitet kodini olish
     *
     * Ustuvorlik tartibi:
     * 1. User jadvalidagi entity_code
     * 2. Username dan extract qilish (otm401 -> 401)
     */
    private String getUniversityCodeFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("No authentication found in security context");
            return null;
        }

        // JWT dan username olish
        String username = null;
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            username = jwt.getClaimAsString("username");
            if (username == null) {
                username = jwt.getSubject();
            }
        } else {
            username = auth.getName();
        }

        if (username == null) {
            return null;
        }

        // 1. Users jadvalidan university code olish
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String entityCode = user.getEntityCode();
                if (entityCode != null && !entityCode.isEmpty()) {
                    log.debug("University code from user.entity_code: {}", entityCode);
                    return entityCode;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get university code from database for user: {}", username, e);
        }

        // 2. Username dan extract qilish (otm401 -> 401, otm351 -> 351)
        String universityCode = extractUniversityCodeFromUsername(username);
        if (universityCode != null) {
            log.debug("University code extracted from username {}: {}", username, universityCode);
            return universityCode;
        }

        log.warn("Could not determine university code for user: {}", username);
        return null;
    }

    /**
     * Username dan universitet kodini extract qilish
     * Format: otmXXX -> XXX (masalan: otm401 -> 401, otm351 -> 351)
     */
    private String extractUniversityCodeFromUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        // otmXXX format (masalan: otm401, otm351)
        if (username.toLowerCase().startsWith("otm")) {
            String code = username.substring(3);
            // Faqat raqamlardan iborat bo'lsa
            if (code.matches("\\d+")) {
                return code;
            }
        }

        // Boshqa formatlar uchun null
        return null;
    }

    /**
     * Joriy foydalanuvchi username ni olish
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "system";
        }

        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaimAsString("username");
            return username != null ? username : jwt.getSubject();
        }

        return auth.getName();
    }

    /**
     * String qiymatni Boolean ga parse qilish
     * "undefined", "", null -> null
     * "true" -> true
     * "false" -> false
     */
    private Boolean parseBoolean(String value) {
        if (value == null || value.isEmpty() || "undefined".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)) {
            return null;
        }
        return "true".equalsIgnoreCase(value);
    }

    /**
     * CUBA-compatible 404 Not Found xato response
     * Old-hemis formatiga 100% mos
     */
    private Map<String, Object> cubaNotFoundError(String entityId) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Entity not found");
        error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
        return error;
    }

    // ==================== CUBA FILTER SUPPORT ====================

    /**
     * URL-encoded JSON string dan filter Map ga parse qilish
     * GET /search endpoint uchun
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseFilterFromString(String filterStr) {
        if (filterStr == null || filterStr.isEmpty()) {
            return null;
        }

        try {
            // URL decode qilish (agar kerak bo'lsa)
            String decoded = java.net.URLDecoder.decode(filterStr, java.nio.charset.StandardCharsets.UTF_8);
            // JSON parse qilish
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(decoded, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse filter string: {}", filterStr, e);
            return null;
        }
    }

    /**
     * CUBA filter conditions ni entity listga qo'llash
     *
     * Filter format:
     * {
     *   "filter": {
     *     "conditions": [
     *       {"property": "university.code", "operator": "=", "value": "305"}
     *     ]
     *   }
     * }
     */
    @SuppressWarnings("unchecked")
    private List<UniversityDepartment> applyFilter(List<UniversityDepartment> entities, Map<String, Object> filterBody) {
        if (filterBody == null || filterBody.isEmpty()) {
            return entities;
        }

        // Filter object ni olish
        Object filterObj = filterBody.get("filter");
        if (filterObj == null) {
            return entities;
        }

        Map<String, Object> filter;
        if (filterObj instanceof Map) {
            filter = (Map<String, Object>) filterObj;
        } else {
            return entities;
        }

        // Conditions ni olish
        Object conditionsObj = filter.get("conditions");
        if (conditionsObj == null || !(conditionsObj instanceof List)) {
            return entities;
        }

        List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;
        if (conditions.isEmpty()) {
            return entities;
        }

        // Har bir condition ni qo'llash (AND logic)
        return entities.stream()
                .filter(entity -> matchesAllConditions(entity, conditions))
                .collect(Collectors.toList());
    }

    /**
     * Entity barcha conditionsga mos kelishini tekshirish
     */
    private boolean matchesAllConditions(UniversityDepartment entity, List<Map<String, Object>> conditions) {
        for (Map<String, Object> condition : conditions) {
            if (!matchesCondition(entity, condition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Entity bitta conditionga mos kelishini tekshirish
     */
    private boolean matchesCondition(UniversityDepartment entity, Map<String, Object> condition) {
        String property = (String) condition.get("property");
        String operator = (String) condition.get("operator");
        Object value = condition.get("value");

        if (property == null || operator == null) {
            return true; // Skip invalid conditions
        }

        // ✅ OLD-HEMIS COMPATIBLE: university.code filter'ni E'TIBORSIZ QOLDIRISH
        // OLD-HEMIS da /search endpoint university.code bo'yicha filtrlash qilmaydi
        if ("university.code".equals(property) || "universityCode".equals(property)) {
            log.trace("Skipping university.code filter (OLD-HEMIS compatible)");
            return true; // Skip university filter - OLD-HEMIS behavior
        }

        // Entity dan property qiymatini olish
        Object entityValue = getPropertyValue(entity, property);

        // Operator bo'yicha solishtirish
        return evaluateCondition(entityValue, operator, value);
    }

    /**
     * Entity dan property qiymatini olish (nested propertylarni ham qo'llab-quvvatlash)
     */
    private Object getPropertyValue(UniversityDepartment entity, String property) {
        // Nested property: university.code -> universityCode
        // deparmentType.code -> departmentType
        return switch (property) {
            case "code", "id" -> entity.getCode();
            case "nameUz" -> entity.getNameUz();
            case "nameRu" -> entity.getNameRu();
            case "name" -> entity.getNameUz(); // Default name
            case "status" -> entity.getStatus();
            case "path" -> entity.getPath();
            case "university.code", "universityCode" -> entity.getUniversityCode();
            case "deparmentType.code", "departmentType.code", "departmentType" -> entity.getDepartmentType();
            case "parent.code", "parentCode" -> entity.getParentCode();
            default -> {
                log.warn("Unknown property: {}", property);
                yield null;
            }
        };
    }

    /**
     * Condition ni baholash
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateCondition(Object entityValue, String operator, Object filterValue) {
        return switch (operator.toLowerCase()) {
            case "=" -> {
                if (entityValue == null && filterValue == null) yield true;
                if (entityValue == null || filterValue == null) yield false;
                yield entityValue.toString().equals(filterValue.toString());
            }
            case "<>", "!=" -> {
                if (entityValue == null && filterValue == null) yield false;
                if (entityValue == null || filterValue == null) yield true;
                yield !entityValue.toString().equals(filterValue.toString());
            }
            case "like", "contains" -> {
                if (entityValue == null || filterValue == null) yield false;
                yield entityValue.toString().toLowerCase().contains(filterValue.toString().toLowerCase());
            }
            case "startswith" -> {
                if (entityValue == null || filterValue == null) yield false;
                yield entityValue.toString().toLowerCase().startsWith(filterValue.toString().toLowerCase());
            }
            case "endswith" -> {
                if (entityValue == null || filterValue == null) yield false;
                yield entityValue.toString().toLowerCase().endsWith(filterValue.toString().toLowerCase());
            }
            case "in" -> {
                if (entityValue == null || filterValue == null) yield false;
                if (filterValue instanceof List) {
                    List<Object> values = (List<Object>) filterValue;
                    yield values.stream().anyMatch(v -> entityValue.toString().equals(v.toString()));
                }
                yield false;
            }
            case "isnull" -> entityValue == null;
            case "notnull" -> entityValue != null;
            case ">" -> compareValues(entityValue, filterValue) > 0;
            case "<" -> compareValues(entityValue, filterValue) < 0;
            case ">=" -> compareValues(entityValue, filterValue) >= 0;
            case "<=" -> compareValues(entityValue, filterValue) <= 0;
            default -> {
                log.warn("Unknown operator: {}", operator);
                yield true; // Skip unknown operators
            }
        };
    }

    /**
     * Qiymatlarni solishtirish (number, string, boolean)
     */
    private int compareValues(Object entityValue, Object filterValue) {
        if (entityValue == null || filterValue == null) {
            return 0;
        }

        // Try numeric comparison
        try {
            double d1 = Double.parseDouble(entityValue.toString());
            double d2 = Double.parseDouble(filterValue.toString());
            return Double.compare(d1, d2);
        } catch (NumberFormatException e) {
            // Fall back to string comparison
            return entityValue.toString().compareTo(filterValue.toString());
        }
    }

    // ==================== REFERENCE OBJECT LOADERS ====================

    /**
     * University object ni JDBC bilan yuklash (OLD-HEMIS CUBA format)
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> loadUniversityObject(String universityCode) {
        if (universityCode == null) return null;
        try {
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT code, name, tin, address, student_url, teacher_url, active, version, " +
                    "add_student, accreditation_edit, allow_grouping, allow_transfer_outside, one_id, gpa_edit, " +
                    "_university_type, _ownership, _university_contract_category, _soato, _soato_region, " +
                    "_university_activity_status, _university_belongs_to " +
                    "FROM hemishe_e_university WHERE code = :code AND delete_ts IS NULL")
                    .setParameter("code", universityCode)
                    .getResultList();
            if (results.isEmpty()) return null;

            Object[] row = results.get(0);
            Map<String, Object> uni = new LinkedHashMap<>();
            uni.put("_entityName", "hemishe_EUniversity");
            uni.put("_instanceName", row[0] + "-" + row[1]);
            uni.put("id", str(row[0]));
            if (row[4] != null) uni.put("studentUrl", str(row[4]));
            uni.put("code", str(row[0]));

            // soato nested
            if (row[17] != null) {
                Map<String, Object> soatoObj = loadSoatoObject(str(row[17]));
                if (soatoObj != null) uni.put("soato", soatoObj);
            }
            // universityActivityStatus
            if (row[19] != null) uni.put("universityActivityStatus", loadClassifierObject("hemishe_h_university_activity_status", "HUniversityActivityStatus", str(row[19])));
            // universityType
            if (row[14] != null) uni.put("universityType", loadClassifierObject("hemishe_h_university_type", "HUniversityType", str(row[14])));
            if (row[2] != null) uni.put("tin", str(row[2]));
            // soatoRegion nested
            if (row[18] != null) {
                Map<String, Object> soatoRegionObj = loadSoatoObject(str(row[18]));
                if (soatoRegionObj != null) uni.put("soatoRegion", soatoRegionObj);
            }
            // versionType
            String versionTypeCode = getVersionType(str(row[0]));
            if (versionTypeCode != null) uni.put("versionType", loadClassifierObjectWithNames("hemishe_h_hemis_version_type", "HHemisVersionType", versionTypeCode));

            uni.put("addStudent", row[8] != null ? row[8] : true);
            if (row[3] != null) uni.put("address", str(row[3]));
            uni.put("accreditationEdit", row[9] != null ? row[9] : false);
            uni.put("active", row[6] != null ? row[6] : false);
            // universityContractCategory
            if (row[16] != null) uni.put("universityContractCategory", loadClassifierObjectWithNames("hemishe_h_university_contract_category", "HUniversityContractCategory", str(row[16])));
            uni.put("version", row[7] != null ? ((Number) row[7]).intValue() : 1);
            uni.put("oneId", row[12] != null ? row[12] : true);
            uni.put("allowGrouping", row[10] != null ? row[10] : true);
            if (row[5] != null) uni.put("teacherUrl", str(row[5]));
            uni.put("allowTransferOutside", row[11] != null ? row[11] : true);
            // ownership
            if (row[15] != null) uni.put("ownership", loadClassifierObject("hemishe_h_ownership", "HOwnership", str(row[15])));
            uni.put("name", str(row[1]));
            uni.put("gpaEdit", row[13] != null ? row[13] : false);
            // belongsTo
            if (row[20] != null) uni.put("belongsTo", loadClassifierObject("hemishe_h_university_belongs_to", "HUniversityBelongsTo", str(row[20])));

            return uni;
        } catch (Exception e) {
            log.debug("University yuklashda xatolik (code={}): {}", universityCode, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadClassifierObject(String tableName, String entityName, String code) {
        try {
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT code, name, active, version FROM " + tableName + " WHERE code = :code AND delete_ts IS NULL")
                    .setParameter("code", code)
                    .getResultList();
            if (results.isEmpty()) return null;

            Object[] row = results.get(0);
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            obj.put("id", str(row[0]));
            obj.put("code", str(row[0]));
            obj.put("name", row[1] != null ? str(row[1]) : "");
            obj.put("active", row[2] != null ? row[2] : true);
            obj.put("version", row[3] != null ? ((Number) row[3]).intValue() : 1);
            return obj;
        } catch (Exception e) {
            log.debug("Klassifikator yuklashda xatolik ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadClassifierObjectWithNames(String tableName, String entityName, String code) {
        try {
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT code, name, active, version, name_ru, name_en FROM " + tableName + " WHERE code = :code AND delete_ts IS NULL")
                    .setParameter("code", code)
                    .getResultList();
            if (results.isEmpty()) return null;

            Object[] row = results.get(0);
            Map<String, Object> obj = new LinkedHashMap<>();
            obj.put("_entityName", "hemishe_" + entityName);
            obj.put("id", str(row[0]));
            obj.put("code", str(row[0]));
            obj.put("name", row[1] != null ? str(row[1]) : "");
            obj.put("active", row[2] != null ? row[2] : true);
            obj.put("version", row[3] != null ? ((Number) row[3]).intValue() : 1);
            if (row[4] != null) obj.put("nameRu", str(row[4]));
            if (row[5] != null) obj.put("nameEn", str(row[5]));
            return obj;
        } catch (Exception e) {
            log.debug("Klassifikator yuklashda xatolik ({}, code={}): {}", tableName, code, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadSoatoObject(String soatoCode) {
        try {
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT code, name_uz, name_ru, active, version FROM hemishe_h_soato WHERE code = :code AND delete_ts IS NULL")
                    .setParameter("code", soatoCode)
                    .getResultList();
            if (results.isEmpty()) return null;

            Object[] row = results.get(0);
            Map<String, Object> soato = new LinkedHashMap<>();
            soato.put("_entityName", "hemishe_HSoato");
            soato.put("id", row[0]);
            soato.put("code", row[0]);
            soato.put("active", row[3] != null ? row[3] : true);
            soato.put("version", row[4] != null ? ((Number) row[4]).intValue() : 1);
            soato.put("name_ru", row[2] != null ? str(row[2]) : "");
            soato.put("name_uz", row[1] != null ? str(row[1]) : "");
            return soato;
        } catch (Exception e) {
            log.debug("SOATO yuklashda xatolik (code={}): {}", soatoCode, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String getVersionType(String universityCode) {
        try {
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT _university_version FROM hemishe_e_university WHERE code = :code AND delete_ts IS NULL")
                    .setParameter("code", universityCode)
                    .getResultList();
            if (!results.isEmpty() && results.get(0) != null) {
                return String.valueOf(results.get(0));
            }
        } catch (Exception e) {
            log.debug("versionType olishda xatolik (code={}): {}", universityCode, e.getMessage());
        }
        return null;
    }

    private String str(Object obj) {
        return obj != null ? String.valueOf(obj) : null;
    }

    /**
     * Filter dan university.code qiymatini olish
     *
     * <p>OLD-HEMIS COMPATIBLE: CUBA filter ichidan university.code ni topish</p>
     *
     * <p>Filter formati:</p>
     * <pre>
     * {
     *   "filter": {
     *     "conditions": [
     *       {"property": "university.code", "operator": "=", "value": "401"}
     *     ]
     *   }
     * }
     * </pre>
     *
     * @param filterBody Filter map (request body yoki parsed JSON)
     * @return university.code qiymati yoki null
     */
    @SuppressWarnings("unchecked")
    private String extractUniversityCodeFromFilter(Map<String, Object> filterBody) {
        if (filterBody == null || filterBody.isEmpty()) {
            return null;
        }

        // Filter object ni olish
        Object filterObj = filterBody.get("filter");
        if (filterObj == null || !(filterObj instanceof Map)) {
            return null;
        }

        Map<String, Object> filter = (Map<String, Object>) filterObj;

        // Conditions ni olish
        Object conditionsObj = filter.get("conditions");
        if (conditionsObj == null || !(conditionsObj instanceof List)) {
            return null;
        }

        List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;

        // university.code conditionni topish
        for (Map<String, Object> condition : conditions) {
            String property = (String) condition.get("property");
            String operator = (String) condition.get("operator");
            Object value = condition.get("value");

            // university.code yoki universityCode property ni tekshirish
            if (property != null && value != null &&
                    (property.equals("university.code") || property.equals("universityCode")) &&
                    "=".equals(operator)) {
                String universityCode = value.toString();
                log.debug("Extracted university.code from filter: {}", universityCode);
                return universityCode;
            }
        }

        return null;
    }
}
