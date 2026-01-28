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
import uz.hemis.domain.entity.*;
import uz.hemis.domain.repository.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EmployeeJobs Entity Controller (CUBA Pattern)
 * Tag 53: Employee Jobs (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EEmployeeJob
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_EEmployeeJobs
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 * - VIEW SUPPORT: When view=eEmployeeJob-view, returns nested objects (OLD-HEMIS compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EEmployeeJobs/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EEmployeeJobs/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EEmployeeJobs/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EEmployeeJobs/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EEmployeeJobs/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EEmployeeJobs           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EEmployeeJobs           - Create new
 */
@Tag(name = "06.Xodim lavozimlari")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EEmployeeJobs")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class EmployeeJobsEntityController {

    private final EmployeeJobsRepository repository;
    private final UserRepository userRepository;

    // Related entity repositories for view support
    private final UniversityRepository universityRepository;
    private final UniversityDepartmentRepository universityDepartmentRepository;
    private final UniversityEmployeeStatusTypeRepository employeeStatusRepository;
    private final UniversityEmployeeTypeRepository employeeTypeRepository;
    private final UniversityEmployeeRateRepository employeeRateRepository;
    private final TeacherPositionTypeRepository positionTypeRepository;
    private final TeacherRepository teacherRepository;

    private static final String ENTITY_NAME = "hemishe_EEmployeeJobs";

    /**
     * OLD-HEMIS Compatible: Constraint xatolikda HTTP 500 qaytarish
     * Format: {"error":"Server error","details":""}
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

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Xodim ish joyini olish",
        description = """
            ID bo'yicha xodim ish joyi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EEmployeeJob/{entityId}
            **Auth:** Bearer token (required)
            **Database:** Replica (read-only)

            **Note:** OLD-HEMIS da universitet tekshiruvi yo'q - har qanday entity ko'rilishi mumkin.
            Bu xatti-harakat saqlab qolingan.

            **View Support:**
            - view=eEmployeeJob-view: Returns nested objects (university, department, etc.)
            - No view: Returns flat IDs only
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Xodim ish joyi UUID", example = "00000000-0000-0000-0000-000000000000")
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET EmployeeJobs by id: {}, view: {}", entityId, view);

        // OLD-HEMIS Compatible: Universitet tekshiruvi yo'q - ID bo'yicha topish
        Optional<EmployeeJobs> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("EmployeeJob not found: id={}", entityId);
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id=" + entityId + " is not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls, view));
    }

    /**
     * OLD-HEMIS Compatible: Bo'sh entityId bilan PUT so'rov
     * CUBA platformasi bunday so'rovda 500 Server error qaytaradi
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
     * Xodim lavozimini yangilash
     *
     * <p><strong>URL:</strong> {@code PUT /app/rest/v2/entities/hemishe_EEmployeeJob/{entityId}}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * @param entityId Lavozim UUID identifikatori
     * @param body Yangilanadigan maydonlar (partial update)
     * @param returnNulls Null qiymatlarni qaytarish
     * @return Yangilangan lavozim ma'lumotlari (CUBA format)
     */
    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
            summary = "Xodim lavozimini yangilash",
            description = """
                ID bo'yicha xodim lavozimi ma'lumotlarini yangilash.

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** PUT /app/rest/v2/entities/hemishe_EEmployeeJob/{entityId}
                **Auth:** Bearer token (required)

                **Note:** OLD-HEMIS da universitet tekshiruvi yo'q - har qanday entity yangilanishi mumkin.
                Bu xatti-harakat saqlab qolingan.

                **Qo'llab-quvvatlanadigan maydonlar:**
                - `_employee`: Xodim UUID
                - `_university`: OTM kodi
                - `_department`: Bo'lim kodi
                - `_employeeType`: Xodim turi kodi
                - `_employeePosition`: Lavozim kodi
                - `_employeeRate`: Stavka kodi
                - `_employeeForm`: Ish shakli kodi
                - `_employeeStatus`: Holat kodi
                - `jobStartDate`: Ish boshlash sanasi (YYYY-MM-DD)
                - `jobEndDate`: Ish tugash sanasi (YYYY-MM-DD)
                - `contractDate`: Shartnoma sanasi
                - `contractNumber`: Shartnoma raqami
                - `decreeDate`: Buyruq sanasi
                - `decreeNumber`: Buyruq raqami
                - `tag`: Yorliq/belgi
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
            @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov parametrlari"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "Lavozim topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Lavozim UUID identifikatori", example = "00000000-0000-0000-0000-000000000000")
            @PathVariable UUID entityId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Yangilanadigan maydonlar (partial update)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Lavozim yangilash",
                                    value = """
                                        {
                                          "_employeeStatus": "11",
                                          "jobEndDate": "2025-12-31",
                                          "contractNumber": "123/2025"
                                        }
                                        """
                            )
                    )
            )
            @RequestBody Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("PUT /app/rest/v2/entities/hemishe_EEmployeeJob/{}", entityId);

        // OLD-HEMIS Compatible: Universitet tekshiruvi yo'q - ID bo'yicha topish
        Optional<EmployeeJobs> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("EmployeeJob not found: id={}", entityId);
            return ResponseEntity.notFound().build();
        }

        EmployeeJobs entity = existingOpt.get();
        updateFromMap(entity, body);

        EmployeeJobs saved = repository.save(entity);
        log.info("EmployeeJob updated successfully: {}", entityId);
        return ResponseEntity.ok(toMap(saved, returnNulls, view));
    }

    /**
     * OLD-HEMIS Compatible: Bo'sh entityId bilan DELETE so'rov
     * CUBA platformasi bunday so'rovda 500 Server error qaytaradi
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
     * Xodim lavozimini o'chirish
     *
     * <p><strong>URL:</strong> {@code DELETE /app/rest/v2/entities/hemishe_EEmployeeJob/{entityId}}</p>
     *
     * <p><strong>OLD-HEMIS Compatible</strong> - 100% backward compatibility</p>
     *
     * <p>Soft delete: ma'lumot bazadan o'chirilmaydi, faqat delete_ts belgilanadi</p>
     *
     * @param entityId Lavozim UUID identifikatori
     * @return 204 No Content (muvaffaqiyatli) yoki 404 Not Found
     */
    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
            summary = "Xodim lavozimini o'chirish",
            description = """
                ID bo'yicha xodim lavozimini o'chirish (soft delete).

                **OLD-HEMIS Compatible** - 100% backward compatibility

                **Endpoint:** DELETE /app/rest/v2/entities/hemishe_EEmployeeJob/{entityId}
                **Auth:** Bearer token (required)

                **Note:** OLD-HEMIS da universitet tekshiruvi yo'q - har qanday entity o'chirilishi mumkin.
                Bu xatti-harakat saqlab qolingan.

                **Soft Delete:** Ma'lumot bazadan o'chirilmaydi, faqat `delete_ts` va `deleted_by`
                maydonlari belgilanadi. Keyinchalik `@Where(clause = "delete_ts IS NULL")` orqali
                o'chirilgan yozuvlar avtomatik filtrlanadi.

                **Response:**
                - `200 OK` - Muvaffaqiyatli o'chirildi (OLD-HEMIS compatible)
                - `404 Not Found` - Lavozim topilmadi (OLD-HEMIS format: {"error": "...", "details": "..."})
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
            @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
            @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
            @ApiResponse(responseCode = "404", description = "Lavozim topilmadi")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "Lavozim UUID identifikatori", example = "00000000-0000-0000-0000-000000000000")
            @PathVariable UUID entityId) {

        log.info("DELETE /app/rest/v2/entities/hemishe_EEmployeeJob/{}", entityId);

        // OLD-HEMIS Compatible: Universitet tekshiruvi yo'q - ID bo'yicha topish
        Optional<EmployeeJobs> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("EmployeeJob not found for deletion: id={}", entityId);
            // OLD-HEMIS format: {"error": "Entity not found", "details": "..."}
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id=" + entityId + " is not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        repository.delete(entity.get());
        log.info("EmployeeJob deleted successfully: {}", entityId);
        // OLD-HEMIS returns 200 OK with empty body (not 204)
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Lavozimlarni qidirish (GET)",
        description = """
            URL parametrlari orqali lavozimlarni qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            **Database:** Replica (read-only)

            **View Support:**
            - view=eEmployeeJob-view: Returns nested objects
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        String universityCode = getUniversityCodeFromContext();
        log.debug("GET search EmployeeJobs - university: {}, filter: {}, view: {}", universityCode, filter, view);

        // Security: Faqat o'z universitetining lavozimlarini qidirish
        List<EmployeeJobs> entities = repository.findByUniversity(universityCode);
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Xodim kodi orqali ish joylarini olish",
        description = """
            Xodim kodi (employee.code) bo'yicha barcha ish joylarini qidirish.

            **NEW-HEMIS Enhanced** - OLD-HEMIS da tugallanmagan funksionallik
            **Database:** Replica (read-only)

            **MUHIM:** OLD-HEMIS CUBA platformasida nested property filterlar
            (employee.code, employee.id) qo'llab-quvvatlanmagan va filterni e'tiborsiz qoldirgan.
            Bu endpoint to'liq filter qo'llab-quvvatlashni amalga oshiradi.

            **Qo'llab-quvvatlanadigan filterlar:**
            - employee.code: Xodim kodi bo'yicha qidirish (masalan: "3011911003")
            - employee (UUID): Xodim ID bo'yicha qidirish

            **Filter formatlari:**
            1. CUBA format: {"filter": {"conditions": [{"property": "employee.code", "operator": "=", "value": "3011911003"}]}}
            2. Oddiy format: {"employee.code": "3011911003"}

            **View Support:**
            - view=eEmployeeJob-view: Returns nested objects (_employee, _department, etc.)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli - ish joylari ro'yxati"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri filter formati")
    })
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "CUBA filter format (employee.code yoki employee ID)",
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Xodim kodi bo'yicha",
                            value = """
                                {
                                  "filter": {
                                    "conditions": [
                                      {
                                        "property": "employee.code",
                                        "operator": "=",
                                        "value": "3011911003"
                                      }
                                    ]
                                  }
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Oddiy format",
                            value = """
                                {
                                  "employee.code": "3011911003"
                                }
                                """
                        )
                    }
                )
            )
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Null qiymatlarni qaytarish")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "View nomi (eEmployeeJob-view)")
            @RequestParam(required = false) String view) {

        log.debug("POST search EmployeeJobs - body: {}, view: {}", body, view);

        // Filter yo'q bo'lsa bo'sh array
        if (body == null || body.isEmpty()) {
            log.debug("Empty body - returning empty array");
            return ResponseEntity.ok(List.of());
        }

        // employee.code filter ni ajratib olish
        String employeeCode = extractEmployeeCodeFromFilter(body);
        if (employeeCode != null) {
            log.debug("Searching by employee.code: {}", employeeCode);
            List<EmployeeJobs> entities = repository.findByEmployeeCode(employeeCode);
            log.debug("Found {} jobs for employee code: {}", entities.size(), employeeCode);
            return ResponseEntity.ok(entities.stream()
                .map(e -> toMap(e, returnNulls, view))
                .collect(Collectors.toList()));
        }

        // employee (UUID) filter ni tekshirish
        UUID employeeId = extractEmployeeIdFromFilter(body);
        if (employeeId != null) {
            log.debug("Searching by employee UUID: {}", employeeId);
            List<EmployeeJobs> entities = repository.findByEmployeeId(employeeId);
            log.debug("Found {} jobs for employee UUID: {}", entities.size(), employeeId);
            return ResponseEntity.ok(entities.stream()
                .map(e -> toMap(e, returnNulls, view))
                .collect(Collectors.toList()));
        }

        // Qo'llab-quvvatlanmagan filter
        log.warn("Unsupported filter format: {}", body);
        return ResponseEntity.ok(List.of());
    }

    /**
     * CUBA filter formatidan employee.code ni ajratib olish
     *
     * <p>Qo'llab-quvvatlanadigan formatlar:</p>
     * <ul>
     *   <li>{"filter": {"conditions": [{"property": "employee.code", "operator": "=", "value": "3011911003"}]}}</li>
     *   <li>{"conditions": [{"property": "employee.code", "operator": "=", "value": "3011911003"}]}</li>
     *   <li>{"employee.code": "3011911003"}</li>
     * </ul>
     *
     * @param body request body
     * @return employee code yoki null
     */
    @SuppressWarnings("unchecked")
    private String extractEmployeeCodeFromFilter(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        // Format 1: {"filter": {"conditions": [...]}}
        Object filterObj = body.get("filter");
        if (filterObj instanceof Map) {
            Map<String, Object> filter = (Map<String, Object>) filterObj;
            String code = extractCodeFromConditions(filter);
            if (code != null) return code;
        }

        // Format 2: {"conditions": [...]}
        String code = extractCodeFromConditions(body);
        if (code != null) return code;

        // Format 3: {"employee.code": "..."} - oddiy format
        Object simpleCode = body.get("employee.code");
        if (simpleCode != null) {
            return simpleCode.toString();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractCodeFromConditions(Map<String, Object> map) {
        Object conditionsObj = map.get("conditions");
        if (conditionsObj instanceof List) {
            List<Object> conditions = (List<Object>) conditionsObj;
            for (Object condObj : conditions) {
                if (condObj instanceof Map) {
                    Map<String, Object> condition = (Map<String, Object>) condObj;
                    String property = (String) condition.get("property");
                    if ("employee.code".equals(property)) {
                        Object value = condition.get("value");
                        return value != null ? value.toString() : null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * CUBA filter formatidan employee.id (UUID) ni ajratib olish
     *
     * @param body request body
     * @return employee UUID yoki null
     */
    @SuppressWarnings("unchecked")
    private UUID extractEmployeeIdFromFilter(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        // Format 1: {"filter": {"conditions": [{"property": "employee", ...}]}}
        Object filterObj = body.get("filter");
        if (filterObj instanceof Map) {
            Map<String, Object> filter = (Map<String, Object>) filterObj;
            UUID id = extractIdFromConditions(filter);
            if (id != null) return id;
        }

        // Format 2: {"conditions": [{"property": "employee", ...}]}
        UUID id = extractIdFromConditions(body);
        if (id != null) return id;

        // Format 3: {"employee": "uuid-string"} - oddiy format
        Object simpleId = body.get("employee");
        if (simpleId != null) {
            try {
                return UUID.fromString(simpleId.toString());
            } catch (IllegalArgumentException e) {
                // UUID emas, e'tiborsiz qoldirish
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private UUID extractIdFromConditions(Map<String, Object> map) {
        Object conditionsObj = map.get("conditions");
        if (conditionsObj instanceof List) {
            List<Object> conditions = (List<Object>) conditionsObj;
            for (Object condObj : conditions) {
                if (condObj instanceof Map) {
                    Map<String, Object> condition = (Map<String, Object>) condObj;
                    String property = (String) condition.get("property");
                    if ("employee".equals(property) || "employee.id".equals(property)) {
                        Object value = condition.get("value");
                        if (value != null) {
                            try {
                                return UUID.fromString(value.toString());
                            } catch (IllegalArgumentException e) {
                                // UUID emas
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(
        summary = "Barcha xodim ish joylari",
        description = """
            Sahifalangan xodim ish joylari ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            **Database:** Replica (read-only)

            **View Support:**
            - view=eEmployeeJob-view: Returns nested objects (university, department, etc.)
            - No view: Returns flat IDs only
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        String universityCode = getUniversityCodeFromContext();
        log.debug("GET all EmployeeJobs - university: {}, offset: {}, limit: {}, view: {}", universityCode, offset, limit, view);

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
        // Security: Faqat o'z universitetining lavozimlarini ko'rish
        Page<EmployeeJobs> entityPage = repository.findAllByUniversity(universityCode, pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Transactional
    @Operation(
        summary = "Xodim ish joyini yaratish",
        description = """
            Yangi xodim ish joyini yaratish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EEmployeeJob
            **Auth:** Bearer token (required)

            **Note:** OLD-HEMIS da universitet tekshiruvi yo'q - har qanday OTM uchun
            yozuv yaratish mumkin. Bu xatti-harakat saqlab qolingan.

            **MUHIM:** OLD-HEMIS da POST = oddiy INSERT. Agar constraint buzilsa,
            xatolik qaytaradi. Pre-validation yoki UPSERT logikasi YO'Q.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "500", description = "Constraint xatoligi (duplicate key)")
    })
    public ResponseEntity<?> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST create EmployeeJobs - body: {}", body);

        // OLD-HEMIS Compatible: Oddiy INSERT qilish (UPSERT emas)
        // Agar constraint buzilsa, xatolik qaytariladi (OLD-HEMIS singari)
        EmployeeJobs entity = new EmployeeJobs();
        updateFromMap(entity, body);

        // OLD-HEMIS Compatible: Universitet tekshiruvi yo'q
        // Foydalanuvchi har qanday OTM uchun yozuv yarata oladi
        // Agar universitet ko'rsatilmagan bo'lsa, joriy foydalanuvchining OTM sini belgilash
        if (entity.getUniversity() == null || entity.getUniversity().isEmpty()) {
            String universityCode = getUniversityCodeFromContext();
            entity.setUniversity(universityCode);
            log.debug("Auto-set university to: {}", universityCode);
        }

        try {
            // saveAndFlush() - darhol bazaga yozish (try-catch ishlashi uchun)
            EmployeeJobs saved = repository.saveAndFlush(entity);
            log.info("EmployeeJob created successfully: id={}, university={}", saved.getId(), saved.getUniversity());

            // OLD-HEMIS Compatible: HTTP 201 + minimal response
            // OLD-HEMIS qaytaradi: {"_entityName":"...","_instanceName":"...","id":"..."}
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("_entityName", ENTITY_NAME);
            response.put("_instanceName", getEmployeeFullName(saved.getEmployee()));
            response.put("id", saved.getId().toString());
            return ResponseEntity.status(201).body(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // OLD-HEMIS Compatible: Constraint xatolikda HTTP 500 qaytarish
            log.error("Constraint violation on EmployeeJob create: {}", e.getMessage());
            String details = extractConstraintDetails(e);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Server Error");
            errorResponse.put("details", details);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * DataIntegrityViolationException dan constraint details ni ajratib olish
     */
    private String extractConstraintDetails(org.springframework.dao.DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        if (message != null && message.contains("duplicate key")) {
            // PostgreSQL duplicate key xabari
            if (message.contains("employee_jobs_main_form_unique_constraint")) {
                return "Xodim allaqachon asosiy shtat birligida ishlamoqda";
            }
            if (message.contains("hemishe_e_employee_jobs__employee__university__department___key")) {
                return "Xodimda bunday lavozim allaqachon mavjud";
            }
            return "Duplicate key: " + message.substring(message.indexOf("Detail:"));
        }
        return message != null ? message : "Database constraint violation";
    }

    /**
     * Convert entity to CUBA-compatible Map
     *
     * OLD-HEMIS Compatible: CUBA platformasi doimo nested employee qaytaradi.
     * Boshqa related entitylar (university, department, etc.) faqat view=eEmployeeJob-view
     * bilan qaytariladi.
     *
     * Response format OLD-HEMIS ga mos:
     * - employee: nested object (doimo)
     * - version, tag, dates: flat fields
     * - university, department, etc.: faqat view bilan
     */
    private Map<String, Object> toMap(EmployeeJobs entity, Boolean returnNulls, String view) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name - xodim to'liq ismi (OLD-HEMIS compatible)
        String instanceName = getEmployeeFullName(entity.getEmployee());
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        // OLD-HEMIS Compatible: Date va string fields avval keladi
        putIfNotNull(map, "contractDate", entity.getContractDate(), returnNulls);
        putIfNotNull(map, "jobStartDate", entity.getJobStartDate(), returnNulls);
        putIfNotNull(map, "contractNumber", entity.getContractNumber(), returnNulls);

        // OLD-HEMIS Compatible: employee DOIMO nested object qaytaradi
        putNestedEmployee(map, entity.getEmployee(), returnNulls);

        // OLD-HEMIS Compatible: version field
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        // Date and string fields
        putIfNotNull(map, "decreeDate", entity.getDecreeDate(), returnNulls);
        putIfNotNull(map, "decreeNumber", entity.getDecreeNumber(), returnNulls);
        putIfNotNull(map, "tag", entity.getTag(), returnNulls);
        putIfNotNull(map, "jobEndDate", entity.getJobEndDate(), returnNulls);

        // Check if view is specified for additional nested objects
        boolean useFullView = view != null && !view.isEmpty();

        if (useFullView) {
            // Full view: Return all nested objects
            putNestedUniversity(map, entity.getUniversity(), returnNulls);
            putNestedDepartment(map, entity.getDepartment(), returnNulls);
            putNestedEmployeeType(map, entity.getEmployeeType(), returnNulls);
            putNestedPosition(map, entity.getEmployeePosition(), returnNulls);
            putNestedRate(map, entity.getEmployeeRate(), returnNulls);
            putNestedEmployeeForm(map, entity.getEmployeeForm(), returnNulls);
            putNestedEmployeeStatus(map, entity.getEmployeeStatus(), returnNulls);
        }
        // OLD-HEMIS Compatible: Default view da university, department, etc. qaytarmaydi

        return map;
    }

    // =====================================================
    // Nested Object Builders (OLD-HEMIS Compatible)
    // =====================================================

    /**
     * Build nested employee object (hemishe_ETeacher)
     *
     * OLD-HEMIS Compatible format:
     * {
     *   "_entityName": "hemishe_ETeacher",
     *   "_instanceName": "XAMZAYEV RAFIK AZIMOVICH",
     *   "id": "uuid",
     *   "firstname": "RAFIK",
     *   "version": 9,
     *   "lastname": "XAMZAYEV",
     *   "fathername": "AZIMOVICH",
     *   "fullname": "XAMZAYEV RAFIK AZIMOVICH"
     * }
     */
    private void putNestedEmployee(Map<String, Object> map, UUID employeeId, Boolean returnNulls) {
        if (employeeId == null) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employee", null);
            }
            return;
        }

        try {
            Optional<Teacher> teacherOpt = teacherRepository.findById(employeeId);
            if (teacherOpt.isPresent()) {
                Teacher teacher = teacherOpt.get();
                Map<String, Object> nested = new LinkedHashMap<>();

                // OLD-HEMIS: hemishe_ETeacher entity name
                nested.put("_entityName", "hemishe_ETeacher");

                // Build full name: FAMILIYA ISM OTASINING_ISMI
                String fullName = buildFullName(teacher.getFirstName(), teacher.getSecondName(), teacher.getThirdName());
                nested.put("_instanceName", fullName);

                nested.put("id", teacher.getId());

                // OLD-HEMIS field names: lowercase, no camelCase
                nested.put("firstname", teacher.getFirstName());
                nested.put("version", teacher.getVersion());
                nested.put("lastname", teacher.getSecondName());  // secondName = familiya
                nested.put("fathername", teacher.getThirdName()); // thirdName = otasining ismi
                nested.put("fullname", fullName);

                map.put("employee", nested);
                return;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch employee: {}", e.getMessage());
        }

        // Employee not found or error, return ID only
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_ETeacher");
        nested.put("id", employeeId);
        map.put("employee", nested);
    }

    /**
     * Build nested university object (hemishe_EUniversity)
     */
    private void putNestedUniversity(Map<String, Object> map, String universityCode, Boolean returnNulls) {
        if (universityCode == null || universityCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("university", null);
            }
            return;
        }

        try {
            Optional<University> uniOpt = universityRepository.findById(universityCode);
            if (uniOpt.isPresent()) {
                University university = uniOpt.get();
                Map<String, Object> nested = new LinkedHashMap<>();
                nested.put("_entityName", "hemishe_EUniversity");
                nested.put("_instanceName", university.getCode() + "-" + university.getName());
                nested.put("id", university.getCode());
                nested.put("name", university.getName());

                map.put("university", nested);
                return;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch university: {}", e.getMessage());
        }

        // University not found or error, return code only
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_EUniversity");
        nested.put("id", universityCode);
        map.put("university", nested);
    }

    /**
     * Build nested department object (hemishe_EDepartment)
     * Uses hemishe_e_university_department table
     */
    private void putNestedDepartment(Map<String, Object> map, String departmentCode, Boolean returnNulls) {
        if (departmentCode == null || departmentCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("department", null);
            }
            return;
        }

        try {
            Optional<UniversityDepartment> deptOpt = universityDepartmentRepository.findByCode(departmentCode);
            if (deptOpt.isPresent()) {
                UniversityDepartment department = deptOpt.get();
                Map<String, Object> nested = new LinkedHashMap<>();
                nested.put("_entityName", "hemishe_EDepartment");
                nested.put("_instanceName", department.getName());
                nested.put("id", department.getCode());
                nested.put("code", department.getCode());
                nested.put("name", department.getName());

                map.put("department", nested);
                return;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch department: {}", e.getMessage());
        }

        // Department not found or error, return code only
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_EDepartment");
        nested.put("code", departmentCode);
        map.put("department", nested);
    }

    /**
     * Build nested employeeType object (hemishe_HUniversityEmployeeType)
     */
    private void putNestedEmployeeType(Map<String, Object> map, String typeCode, Boolean returnNulls) {
        if (typeCode == null || typeCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employeeType", null);
            }
            return;
        }

        try {
            Optional<UniversityEmployeeType> typeOpt = employeeTypeRepository.findById(typeCode);
            if (typeOpt.isPresent()) {
                UniversityEmployeeType type = typeOpt.get();
                Map<String, Object> nested = new LinkedHashMap<>();
                nested.put("_entityName", "hemishe_HUniversityEmployeeType");
                nested.put("_instanceName", type.getName());
                nested.put("id", type.getCode());
                nested.put("code", type.getCode());
                nested.put("name", type.getName());

                map.put("employeeType", nested);
                return;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch employeeType: {}", e.getMessage());
        }

        // Type not found or error, return code only
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_HUniversityEmployeeType");
        nested.put("id", typeCode);
        map.put("employeeType", nested);
    }

    /**
     * Build nested position object (hemishe_HTeacherPositionType)
     */
    private void putNestedPosition(Map<String, Object> map, String positionCode, Boolean returnNulls) {
        if (positionCode == null || positionCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employeePosition", null);
            }
            return;
        }

        try {
            Optional<TeacherPositionType> posOpt = positionTypeRepository.findById(positionCode);
            if (posOpt.isPresent()) {
                TeacherPositionType position = posOpt.get();
                Map<String, Object> nested = new LinkedHashMap<>();
                nested.put("_entityName", "hemishe_HTeacherPositionType");
                nested.put("_instanceName", position.getName());
                nested.put("id", position.getCode());
                nested.put("code", position.getCode());
                nested.put("name", position.getName());

                map.put("employeePosition", nested);
                return;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch position: {}", e.getMessage());
        }

        // Position not found or error, return code only
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_HTeacherPositionType");
        nested.put("id", positionCode);
        map.put("employeePosition", nested);
    }

    /**
     * Build nested rate object (hemishe_HUniversityEmployeeRate)
     */
    private void putNestedRate(Map<String, Object> map, String rateCode, Boolean returnNulls) {
        if (rateCode == null || rateCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employeeRate", null);
            }
            return;
        }

        try {
            Optional<UniversityEmployeeRate> rateOpt = employeeRateRepository.findById(rateCode);
            if (rateOpt.isPresent()) {
                UniversityEmployeeRate rate = rateOpt.get();
                Map<String, Object> nested = new LinkedHashMap<>();
                nested.put("_entityName", "hemishe_HUniversityEmployeeRate");
                nested.put("_instanceName", rate.getName());
                nested.put("id", rate.getCode());
                nested.put("code", rate.getCode());
                nested.put("name", rate.getName());

                map.put("employeeRate", nested);
                return;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch rate: {}", e.getMessage());
        }

        // Rate not found or error, return code only
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_HUniversityEmployeeRate");
        nested.put("id", rateCode);
        map.put("employeeRate", nested);
    }

    /**
     * Build nested employeeForm object (hemishe_HEmployeeForm)
     * Note: This classifier may not exist in the system yet
     */
    private void putNestedEmployeeForm(Map<String, Object> map, String formCode, Boolean returnNulls) {
        if (formCode == null || formCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employeeForm", null);
            }
            return;
        }

        // EmployeeForm classifier - return basic structure
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_HEmployeeForm");
        nested.put("id", formCode);
        nested.put("code", formCode);

        map.put("employeeForm", nested);
    }

    /**
     * Build nested employeeStatus object (hemishe_HUniversityEmployeeStatusType)
     */
    private void putNestedEmployeeStatus(Map<String, Object> map, String statusCode, Boolean returnNulls) {
        if (statusCode == null || statusCode.isEmpty()) {
            if (Boolean.TRUE.equals(returnNulls)) {
                map.put("employeeStatus", null);
            }
            return;
        }

        try {
            Optional<UniversityEmployeeStatusType> statusOpt = employeeStatusRepository.findById(statusCode);
            if (statusOpt.isPresent()) {
                UniversityEmployeeStatusType status = statusOpt.get();
                Map<String, Object> nested = new LinkedHashMap<>();
                nested.put("_entityName", "hemishe_HUniversityEmployeeStatusType");
                nested.put("_instanceName", status.getName());
                nested.put("id", status.getCode());
                nested.put("code", status.getCode());
                nested.put("name", status.getName());

                map.put("employeeStatus", nested);
                return;
            }
        } catch (Exception e) {
            log.debug("Failed to fetch employeeStatus: {}", e.getMessage());
        }

        // Status not found or error, return code only
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("_entityName", "hemishe_HUniversityEmployeeStatusType");
        nested.put("id", statusCode);
        map.put("employeeStatus", nested);
    }

    /**
     * Get employee full name by UUID (for _instanceName)
     * Returns "FAMILIYA ISM OTASI" format (uppercase)
     */
    private String getEmployeeFullName(UUID employeeId) {
        if (employeeId == null) {
            return "Unknown Employee";
        }
        try {
            Optional<Teacher> teacherOpt = teacherRepository.findById(employeeId);
            if (teacherOpt.isPresent()) {
                Teacher teacher = teacherOpt.get();
                String fullName = buildFullName(teacher.getFirstName(), teacher.getSecondName(), teacher.getThirdName());
                return fullName.toUpperCase(); // OLD-HEMIS returns uppercase
            }
        } catch (Exception e) {
            log.debug("Failed to fetch employee name: {}", e.getMessage());
        }
        return "Employee-" + employeeId;
    }

    /**
     * Build full name from parts
     */
    private String buildFullName(String firstName, String secondName, String thirdName) {
        StringBuilder sb = new StringBuilder();
        if (secondName != null && !secondName.isEmpty()) {
            sb.append(secondName);
        }
        if (firstName != null && !firstName.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(firstName);
        }
        if (thirdName != null && !thirdName.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(thirdName);
        }
        return sb.length() > 0 ? sb.toString() : "Employee";
    }

    /**
     * Map dan entity maydonlarini yangilash (partial update)
     *
     * <p>CUBA format qo'llab-quvvatlaydi (ikkala format):</p>
     * <ul>
     *   <li>_employee - Xodim UUID: "uuid-string" YOKI {"id": "uuid-string"}</li>
     *   <li>_university - OTM kodi: "305" YOKI {"code": "305"}</li>
     *   <li>_department - Bo'lim kodi: "305-212" YOKI {"code": "305-212"}</li>
     *   <li>_employeeType - Xodim turi: "12" YOKI {"code": "12"}</li>
     *   <li>_teacherPositionType - Lavozim: "11" YOKI {"code": "11"}</li>
     *   <li>_employeeRate - Stavka: "11" YOKI {"code": "11"}</li>
     *   <li>_employmentForm - Ish shakli: "11" YOKI {"code": "11"}</li>
     *   <li>_employeeStatus - Holat: "11" YOKI {"code": "11"}</li>
     *   <li>jobStartDate, jobEndDate - Sana maydonlari (YYYY-MM-DD)</li>
     *   <li>contractDate, contractNumber - Shartnoma ma'lumotlari</li>
     *   <li>decreeDate, decreeNumber - Buyruq ma'lumotlari</li>
     *   <li>tag - Yorliq</li>
     * </ul>
     *
     * @param entity Yangilanadigan entity
     * @param map So'rov body (CUBA format)
     */
    private void updateFromMap(EmployeeJobs entity, Map<String, Object> map) {
        // OLD-HEMIS Compatible: CUBA format - underscore prefix EMAS
        // CUBA platformasi "employee", "university" kutadi, "_employee", "_university" ni e'tiborsiz qoldiradi

        // UUID field: employee (CUBA format - nested object {"id": "uuid"})
        if (map.containsKey("employee")) {
            UUID value = parseUuid(map.get("employee"));
            if (value != null) {
                entity.setEmployee(value);
            }
        }

        // String fields (reference codes) - CUBA format: {"code": "..."} yoki {"id": "..."}
        if (map.containsKey("university")) {
            String value = parseCode(map.get("university"));
            if (value != null) {
                entity.setUniversity(value);
            }
        }
        if (map.containsKey("department")) {
            String value = parseCode(map.get("department"));
            if (value != null) {
                entity.setDepartment(value);
            }
        }
        if (map.containsKey("employeeType")) {
            String value = parseCode(map.get("employeeType"));
            if (value != null) {
                entity.setEmployeeType(value);
            }
        }
        // Support both employeePosition and teacherPositionType
        if (map.containsKey("employeePosition")) {
            String value = parseCode(map.get("employeePosition"));
            if (value != null) {
                entity.setEmployeePosition(value);
            }
        }
        if (map.containsKey("teacherPositionType")) {
            String value = parseCode(map.get("teacherPositionType"));
            if (value != null) {
                entity.setEmployeePosition(value);
            }
        }
        if (map.containsKey("employeeRate")) {
            String value = parseCode(map.get("employeeRate"));
            if (value != null) {
                entity.setEmployeeRate(value);
            }
        }
        // Support both employeeForm and employmentForm
        if (map.containsKey("employeeForm")) {
            String value = parseCode(map.get("employeeForm"));
            if (value != null) {
                entity.setEmployeeForm(value);
            }
        }
        if (map.containsKey("employmentForm")) {
            String value = parseCode(map.get("employmentForm"));
            if (value != null) {
                entity.setEmployeeForm(value);
            }
        }
        if (map.containsKey("employeeStatus")) {
            String value = parseCode(map.get("employeeStatus"));
            if (value != null) {
                entity.setEmployeeStatus(value);
            }
        }

        // Date fields - allow null to clear the date
        if (map.containsKey("jobStartDate")) {
            entity.setJobStartDate(parseLocalDate(map.get("jobStartDate")));
        }
        if (map.containsKey("jobEndDate")) {
            entity.setJobEndDate(parseLocalDate(map.get("jobEndDate")));
        }
        if (map.containsKey("contractDate")) {
            entity.setContractDate(parseLocalDate(map.get("contractDate")));
        }
        if (map.containsKey("decreeDate")) {
            entity.setDecreeDate(parseLocalDate(map.get("decreeDate")));
        }

        // String fields - allow empty to clear
        if (map.containsKey("contractNumber")) {
            entity.setContractNumber(parseString(map.get("contractNumber")));
        }
        if (map.containsKey("decreeNumber")) {
            entity.setDecreeNumber(parseString(map.get("decreeNumber")));
        }
        if (map.containsKey("tag")) {
            entity.setTag(parseString(map.get("tag")));
        }
    }

    /**
     * Parse UUID from flat string or nested object {"id": "uuid"}
     */
    @SuppressWarnings("unchecked")
    private UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID u) {
            return u;
        }
        if (value instanceof String s) {
            return s.isEmpty() ? null : UUID.fromString(s);
        }
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id == null) {
                return null;
            }
            if (id instanceof UUID u) {
                return u;
            }
            if (id instanceof String s) {
                return s.isEmpty() ? null : UUID.fromString(s);
            }
        }
        return null;
    }

    /**
     * Parse code from flat string or nested object {"code": "..."} or {"id": "..."}
     * Returns null for empty strings or empty objects to avoid FK constraint violations
     */
    @SuppressWarnings("unchecked")
    private String parseCode(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s.isEmpty() ? null : s;
        }
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            // Try "code" first, then "id"
            Object code = nested.get("code");
            if (code != null) {
                String codeStr = code.toString();
                // Return null for empty strings
                return codeStr.isEmpty() ? null : codeStr;
            }
            Object id = nested.get("id");
            if (id != null) {
                String idStr = id.toString();
                // Return null for empty strings
                return idStr.isEmpty() ? null : idStr;
            }
            // Empty map - return null
            return null;
        }
        // Fallback to toString, but check for empty
        String str = value.toString();
        return str.isEmpty() ? null : str;
    }

    /**
     * Object dan String ga parse qilish
     */
    private String parseString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * Object dan LocalDate ga parse qilish (YYYY-MM-DD format)
     */
    private LocalDate parseLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate ld) {
            return ld;
        }
        String str = value.toString();
        if (str.isEmpty()) {
            return null;
        }
        // YYYY-MM-DD format
        return LocalDate.parse(str);
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    // =====================================================
    // Security: University Code from Context
    // =====================================================

    /**
     * Security context dan joriy foydalanuvchining universitet kodini olish
     *
     * <p><strong>OLD-HEMIS Compatible:</strong> Har bir OTM faqat o'z ma'lumotlarini ko'rishi kerak</p>
     *
     * @return universitet kodi yoki null (development uchun fallback "520")
     */
    private String getUniversityCodeFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.warn("No authentication found in security context");
            return getDefaultUniversityCode();
        }

        // JWT token dan username olish
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            String username = jwt.getClaimAsString("username");
            if (username == null) {
                username = jwt.getSubject();
            }

            // User dan university olish
            if (username != null) {
                try {
                    UUID userId = UUID.fromString(jwt.getSubject());
                    var userOpt = userRepository.findById(userId);
                    if (userOpt.isPresent() && userOpt.get().getUniversity() != null) {
                        String code = userOpt.get().getUniversity().getCode();
                        log.debug("University code from user: {}", code);
                        return code;
                    }
                } catch (Exception e) {
                    log.debug("Could not get university from user: {}", e.getMessage());
                }
            }
        }

        return getDefaultUniversityCode();
    }

    /**
     * Development uchun default universitet kodi
     */
    private String getDefaultUniversityCode() {
        log.warn("Using default university code for development");
        return "520";
    }
}
