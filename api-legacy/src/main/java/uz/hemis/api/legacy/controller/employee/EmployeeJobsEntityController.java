package uz.hemis.api.legacy.controller.employee;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.api.legacy.util.LegacySecurityHelper;
import uz.hemis.domain.entity.employee.Employee;
import uz.hemis.domain.entity.employee.EmployeeJobs;
import uz.hemis.service.legacy.EmployeeJobsLegacyService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

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

    private final EmployeeJobsLegacyService employeeJobsService;
    private final LegacySecurityHelper securityHelper;

    private static final String ENTITY_NAME = "hemishe_EEmployeeJobs";

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/{entityId}")
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

        Optional<EmployeeJobs> entity = employeeJobsService.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("EmployeeJob not found: id={}", entityId);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls, view));
    }

    /**
     * OLD-HEMIS Compatible: Bo'sh entityId bilan PUT so'rov
     */
    @PreAuthorize("hasAuthority('teachers.edit')")
    @PutMapping({"", "/"})
    @Operation(hidden = true)
    public ResponseEntity<Map<String, Object>> updateWithoutId(@RequestBody Map<String, Object> body) {
        log.warn("PUT without entityId - returning OLD-HEMIS compatible error");
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Server error");
        errorResponse.put("details", "");
        return ResponseEntity.status(500).body(errorResponse);
    }

    @PreAuthorize("hasAuthority('teachers.edit')")
    @PutMapping("/{entityId}")
    @Operation(
            summary = "Xodim lavozimini yangilash",
            description = """
                ID bo'yicha xodim lavozimi ma'lumotlarini yangilash.

                **OLD-HEMIS Compatible** - 100% backward compatibility
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

        Optional<EmployeeJobs> existingOpt = employeeJobsService.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("EmployeeJob not found: id={}", entityId);
            return ResponseEntity.notFound().build();
        }

        EmployeeJobs entity = existingOpt.get();
        updateFromMap(entity, body);

        EmployeeJobs saved = employeeJobsService.save(entity);
        log.info("EmployeeJob updated successfully: {}", entityId);
        return ResponseEntity.ok(toMap(saved, returnNulls, view));
    }

    /**
     * OLD-HEMIS Compatible: Bo'sh entityId bilan DELETE so'rov
     */
    @PreAuthorize("hasAuthority('teachers.delete')")
    @DeleteMapping({"", "/"})
    @Operation(hidden = true)
    public ResponseEntity<Map<String, Object>> deleteWithoutId() {
        log.warn("DELETE without entityId - returning OLD-HEMIS compatible error");
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("error", "Server error");
        errorResponse.put("details", "");
        return ResponseEntity.status(500).body(errorResponse);
    }

    @PreAuthorize("hasAuthority('teachers.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(
            summary = "Xodim lavozimini o'chirish",
            description = """
                ID bo'yicha xodim lavozimini o'chirish (soft delete).

                **OLD-HEMIS Compatible** - 100% backward compatibility
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

        Optional<EmployeeJobs> entity = employeeJobsService.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("EmployeeJob not found for deletion: id={}", entityId);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        employeeJobsService.delete(entity.get());
        log.info("EmployeeJob deleted successfully: {}", entityId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping("/search")
    @Operation(
        summary = "Lavozimlarni qidirish (GET)",
        description = """
            URL parametrlari orqali lavozimlarni qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            **Database:** Replica (read-only)
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        String universityCode = securityHelper.getUniversityCodeFromContext();
        log.debug("GET search EmployeeJobs - university: {}, filter: {}, view: {}", universityCode, filter, view);

        List<EmployeeJobs> entities = employeeJobsService.findByUniversity(universityCode);
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @PostMapping("/search")
    @Operation(
        summary = "Xodim kodi orqali ish joylarini olish",
        description = """
            Xodim kodi (employee.code) bo'yicha barcha ish joylarini qidirish.

            **NEW-HEMIS Enhanced** - OLD-HEMIS da tugallanmagan funksionallik
            **Database:** Replica (read-only)
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

        if (body == null || body.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        String employeeCode = extractEmployeeCodeFromFilter(body);
        if (employeeCode != null) {
            log.debug("Searching by employee.pinfl: {}", employeeCode);
            List<EmployeeJobs> entities = employeeJobsService.findByEmployeePinfl(employeeCode);
            return ResponseEntity.ok(entities.stream()
                .map(e -> toMap(e, returnNulls, view))
                .collect(Collectors.toList()));
        }

        UUID employeeId = extractEmployeeIdFromFilter(body);
        if (employeeId != null) {
            log.debug("Searching by employee UUID: {}", employeeId);
            List<EmployeeJobs> entities = employeeJobsService.findByEmployeeId(employeeId);
            return ResponseEntity.ok(entities.stream()
                .map(e -> toMap(e, returnNulls, view))
                .collect(Collectors.toList()));
        }

        log.warn("Unsupported filter format: {}", body);
        return ResponseEntity.ok(List.of());
    }

    @PreAuthorize("hasAuthority('teachers.view')")
    @GetMapping
    @Operation(
        summary = "Barcha xodim ish joylari",
        description = """
            Sahifalangan xodim ish joylari ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            **Database:** Replica (read-only)
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

        String universityCode = securityHelper.getUniversityCodeFromContext();
        log.debug("GET all EmployeeJobs - university: {}, offset: {}, limit: {}, view: {}", universityCode, offset, limit, view);

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
        Page<EmployeeJobs> entityPage = employeeJobsService.findAllByUniversity(universityCode, pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls, view))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('teachers.edit')")
    @PostMapping
    @Operation(
        summary = "Xodim ish joyini yaratish",
        description = """
            Yangi xodim ish joyini yaratish.

            **OLD-HEMIS Compatible** - 100% backward compatibility
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

        EmployeeJobs entity = new EmployeeJobs();
        updateFromMap(entity, body);

        if (entity.getUniversityCode() == null || entity.getUniversityCode().isEmpty()) {
            String universityCode = securityHelper.getUniversityCodeFromContext();
            entity.setUniversityCode(universityCode);
            log.debug("Auto-set university to: {}", universityCode);
        }

        try {
            EmployeeJobs saved = employeeJobsService.saveAndFlush(entity);
            log.info("EmployeeJob created successfully: id={}, university={}", saved.getId(), saved.getUniversityCode());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("_entityName", ENTITY_NAME);
            response.put("_instanceName", employeeJobsService.getEmployeeFullName(saved.getEmployee() != null ? saved.getEmployee().getId() : null));
            response.put("id", saved.getId().toString());
            return ResponseEntity.ok(response);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.error("Constraint violation on EmployeeJob create: {}", e.getMessage());
            String details = extractConstraintDetails(e);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Server Error");
            errorResponse.put("details", details);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // =====================================================
    // Private helpers
    // =====================================================

    private Map<String, Object> toMap(EmployeeJobs entity, Boolean returnNulls, String view) {
        return employeeJobsService.toMap(
                entity.getId(),
                entity.getEmployee() != null ? entity.getEmployee().getId() : null,
                entity.getUniversityCode(), entity.getDepartmentCode(),
                entity.getEmployeeType(), entity.getPositionCode(), entity.getEmployeeRate(),
                entity.getEmploymentForm(), null, // employee_status removed — derived from is_current+end_date
                entity.getVersion(), null,
                entity.getContractDate(), entity.getContractNumber(),
                entity.getStartDate(), entity.getEndDate(),
                entity.getDecreeDate(), entity.getDecreeNumber(),
                returnNulls, view);
    }

    private String extractConstraintDetails(org.springframework.dao.DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        if (message != null && message.contains("duplicate key")) {
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

    @SuppressWarnings("unchecked")
    private String extractEmployeeCodeFromFilter(Map<String, Object> body) {
        if (body == null || body.isEmpty()) return null;

        Object filterObj = body.get("filter");
        if (filterObj instanceof Map) {
            String code = extractCodeFromConditions((Map<String, Object>) filterObj);
            if (code != null) return code;
        }

        String code = extractCodeFromConditions(body);
        if (code != null) return code;

        Object simpleCode = body.get("employee.code");
        if (simpleCode != null) return simpleCode.toString();

        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractCodeFromConditions(Map<String, Object> map) {
        Object conditionsObj = map.get("conditions");
        if (conditionsObj instanceof List) {
            for (Object condObj : (List<Object>) conditionsObj) {
                if (condObj instanceof Map) {
                    Map<String, Object> condition = (Map<String, Object>) condObj;
                    if ("employee.code".equals(condition.get("property"))) {
                        Object value = condition.get("value");
                        return value != null ? value.toString() : null;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private UUID extractEmployeeIdFromFilter(Map<String, Object> body) {
        if (body == null || body.isEmpty()) return null;

        Object filterObj = body.get("filter");
        if (filterObj instanceof Map) {
            UUID id = extractIdFromConditions((Map<String, Object>) filterObj);
            if (id != null) return id;
        }

        UUID id = extractIdFromConditions(body);
        if (id != null) return id;

        Object simpleId = body.get("employee");
        if (simpleId != null) {
            try {
                return UUID.fromString(simpleId.toString());
            } catch (IllegalArgumentException e) {
                log.debug("Invalid employee UUID: {}", simpleId);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private UUID extractIdFromConditions(Map<String, Object> map) {
        Object conditionsObj = map.get("conditions");
        if (conditionsObj instanceof List) {
            for (Object condObj : (List<Object>) conditionsObj) {
                if (condObj instanceof Map) {
                    Map<String, Object> condition = (Map<String, Object>) condObj;
                    String property = (String) condition.get("property");
                    if ("employee".equals(property) || "employee.id".equals(property)) {
                        Object value = condition.get("value");
                        if (value != null) {
                            try {
                                return UUID.fromString(value.toString());
                            } catch (IllegalArgumentException e) {
                                log.debug("Invalid employee UUID in condition: {}", value);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void updateFromMap(EmployeeJobs entity, Map<String, Object> map) {
        if (map.containsKey("employee")) {
            UUID value = parseUuid(map.get("employee"));
            if (value != null) {
                Employee emp = new Employee();
                emp.setId(value);
                entity.setEmployee(emp);
            }
        }
        if (map.containsKey("university")) {
            String value = parseCode(map.get("university"));
            if (value != null) entity.setUniversityCode(value);
        }
        if (map.containsKey("department")) {
            String value = parseCode(map.get("department"));
            if (value != null) entity.setDepartmentCode(value);
        }
        if (map.containsKey("employeeType")) {
            String value = parseCode(map.get("employeeType"));
            if (value != null) entity.setEmployeeType(value);
        }
        if (map.containsKey("employeePosition")) {
            String value = parseCode(map.get("employeePosition"));
            if (value != null) entity.setPositionCode(value);
        }
        if (map.containsKey("teacherPositionType")) {
            String value = parseCode(map.get("teacherPositionType"));
            if (value != null) entity.setPositionCode(value);
        }
        if (map.containsKey("employeeRate")) {
            String value = parseCode(map.get("employeeRate"));
            if (value != null) entity.setEmployeeRate(value);
        }
        if (map.containsKey("employeeForm")) {
            String value = parseCode(map.get("employeeForm"));
            if (value != null) entity.setEmploymentForm(value);
        }
        if (map.containsKey("employmentForm")) {
            String value = parseCode(map.get("employmentForm"));
            if (value != null) entity.setEmploymentForm(value);
        }
        // employee_status / employmentStaff columns were removed from the schema —
        // legacy API still accepts these fields but they're ignored (active flag is
        // derived from is_current + end_date).
        if (map.containsKey("staffPosition") && !map.containsKey("employeePosition") && !map.containsKey("teacherPositionType")) {
            String value = parseCode(map.get("staffPosition"));
            if (value != null) entity.setPositionCode(value);
        }
        if (map.containsKey("jobStartDate")) entity.setStartDate(parseLocalDate(map.get("jobStartDate")));
        if (map.containsKey("jobEndDate")) entity.setEndDate(parseLocalDate(map.get("jobEndDate")));
        if (map.containsKey("contractDate")) entity.setContractDate(parseLocalDate(map.get("contractDate")));
        if (map.containsKey("decreeDate")) entity.setDecreeDate(parseLocalDate(map.get("decreeDate")));
        if (map.containsKey("contractNumber")) entity.setContractNumber(parseString(map.get("contractNumber")));
        if (map.containsKey("decreeNumber")) entity.setDecreeNumber(parseString(map.get("decreeNumber")));
    }

    @SuppressWarnings("unchecked")
    private UUID parseUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID u) return u;
        if (value instanceof String s) return s.isEmpty() ? null : UUID.fromString(s);
        if (value instanceof Map) {
            Object id = ((Map<String, Object>) value).get("id");
            if (id == null) return null;
            if (id instanceof UUID u) return u;
            if (id instanceof String s) return s.isEmpty() ? null : UUID.fromString(s);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String parseCode(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s.isEmpty() ? null : s;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object code = nested.get("code");
            if (code != null) {
                String codeStr = code.toString();
                return codeStr.isEmpty() ? null : codeStr;
            }
            Object id = nested.get("id");
            if (id != null) {
                String idStr = id.toString();
                return idStr.isEmpty() ? null : idStr;
            }
            return null;
        }
        String str = value.toString();
        return str.isEmpty() ? null : str;
    }

    private String parseString(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDate parseLocalDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate ld) return ld;
        String str = value.toString();
        return str.isEmpty() ? null : LocalDate.parse(str);
    }
}
