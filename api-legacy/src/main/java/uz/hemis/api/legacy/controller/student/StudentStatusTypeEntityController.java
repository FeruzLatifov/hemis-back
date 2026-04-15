package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import uz.hemis.domain.entity.StudentStatusType;
import uz.hemis.service.legacy.student.StudentEntityLegacyService;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Student Status Type Entity Controller (CUBA Pattern)
 * Tag: 10.Talaba holati
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_HStudentStatusType</p>
 *
 * <p><strong>Talaba holatlari:</strong></p>
 * <ul>
 *   <li>10 - Boshqa</li>
 *   <li>11 - O'qimoqda</li>
 *   <li>12 - Chetlashgan</li>
 *   <li>13 - Akademik ta'til</li>
 *   <li>14 - Bitirgan</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "10.Talaba holati", description = "Talaba holatlari klassifikatori - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HStudentStatusType")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class StudentStatusTypeEntityController {

    private final StudentEntityLegacyService studentService;
    private final LegacySecurityHelper securityHelper;
    private static final String ENTITY_NAME = "hemishe_HStudentStatusType";

    // =====================================================
    // GET BY ID
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.view')")
    @GetMapping("/{entityId}")
    @Operation(
        summary = "Bitta talaba holatini olish",
        description = """
            Kod bo'yicha talaba holati ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}
            **Auth:** Bearer token (required)

            **Holatlar:** 10=Boshqa, 11=O'qimoqda, 12=Chetlashgan, 13=Akademik ta'til, 14=Bitirgan
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Holat kodi", example = "11")
            @PathVariable String entityId,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET StudentStatusType by code: {}", entityId);

        Optional<StudentStatusType> entity = studentService.findStudentStatusTypeById(entityId);
        if (entity.isEmpty()) {
            // OLD-HEMIS COMPATIBLE: Error response format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(studentService.toStudentStatusTypeMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT - UPDATE ENTITY
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.edit')")
    @PutMapping("/{entityId}")
    @Operation(
        summary = "Talaba holatini yangilash",
        description = """
            Talaba holati ma'lumotlarini yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}
            **Auth:** Bearer token (required)

            Faqat yuborilgan maydonlar yangilanadi.

            **Mavjud fieldlar:**
            - name - O'zbekcha nomi
            - nameEn - Inglizcha nomi
            - nameRu - Ruscha nomi
            - active - Faol holati (true/false)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> update(
            @Parameter(description = "Holat kodi", example = "11")
            @PathVariable String entityId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Yangilanadigan maydonlar",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        example = """
                            {
                                "name": "O'qimoqda",
                                "nameEn": "Studying",
                                "nameRu": "Учится",
                                "active": true
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> entityData) {

        log.info("PUT StudentStatusType - entityId: {}, data: {}", entityId, entityData);

        Optional<StudentStatusType> existingOpt = studentService.findStudentStatusTypeById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("StudentStatusType not found: {}", entityId);
            // OLD-HEMIS COMPATIBLE: Error response format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        StudentStatusType entity = existingOpt.get();
        studentService.updateStudentStatusTypeFromMap(entity, entityData);

        // Update timestamp va user
        entity.setUpdateTs(java.time.LocalDateTime.now());
        entity.setUpdatedBy(securityHelper.getCurrentUsername());

        StudentStatusType saved = studentService.saveStudentStatusType(entity);
        log.info("StudentStatusType updated successfully: {}", entityId);

        // OLD-HEMIS COMPATIBLE: Minimal response qaytarish
        return ResponseEntity.ok(studentService.toStudentStatusTypeMinimalMap(saved));
    }

    // =====================================================
    // DELETE - SOFT DELETE ENTITY
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.delete')")
    @DeleteMapping("/{entityId}")
    @Operation(
        summary = "Talaba holatini o'chirish",
        description = """
            Talaba holatini o'chirish (soft delete).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** DELETE /app/rest/v2/entities/hemishe_HStudentStatusType/{entityId}
            **Auth:** Bearer token (required)

            Diqqat: Bu soft delete - ma'lumot bazadan o'chirilmaydi,
            faqat deleteTs va deletedBy fieldlari o'rnatiladi.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(
            @Parameter(description = "Holat kodi", example = "11")
            @PathVariable String entityId) {

        log.info("DELETE StudentStatusType - entityId: {}", entityId);

        Optional<StudentStatusType> existingOpt = studentService.findStudentStatusTypeById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("StudentStatusType not found for delete: {}", entityId);
            // OLD-HEMIS COMPATIBLE: Error response format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        StudentStatusType entity = existingOpt.get();
        entity.setDeletedBy(securityHelper.getCurrentUsername());

        studentService.deleteStudentStatusType(entity);
        log.info("StudentStatusType soft deleted successfully: {}", entityId);

        return ResponseEntity.ok().build();
    }

    // =====================================================
    // POST - CREATE NEW ENTITY
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.edit')")
    @PostMapping
    @Operation(
        summary = "Yangi talaba holatini yaratish",
        description = """
            Yangi talaba holati yaratish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_HStudentStatusType
            **Auth:** Bearer token (required)

            **Majburiy fieldlar:**
            - code - Holat kodi (unique)
            - name - O'zbekcha nomi

            **Ixtiyoriy fieldlar:**
            - nameEn - Inglizcha nomi
            - nameRu - Ruscha nomi
            - active - Faol holati (default: true)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov yoki code allaqachon mavjud"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Yangi talaba holati ma'lumotlari",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        example = """
                            {
                                "code": "15",
                                "name": "Yangi holat",
                                "nameEn": "New status",
                                "nameRu": "Новый статус",
                                "active": true
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> entityData) {

        log.info("POST StudentStatusType - data: {}", entityData);

        // Validate required fields
        String code = (String) entityData.get("code");
        String name = (String) entityData.get("name");

        if (code == null || code.isBlank()) {
            log.warn("POST StudentStatusType - code is required");
            return ResponseEntity.badRequest().body(Map.of(
                "error", "code is required",
                "details", "code maydoni majburiy"
            ));
        }

        if (name == null || name.isBlank()) {
            log.warn("POST StudentStatusType - name is required");
            return ResponseEntity.badRequest().body(Map.of(
                "error", "name is required",
                "details", "name maydoni majburiy"
            ));
        }

        // OLD-HEMIS COMPATIBLE: CUBA Platform does upsert
        // Step 1: Try to find existing entity (non-deleted) via JPA
        Optional<StudentStatusType> existingOpt = studentService.findStudentStatusTypeById(code);

        StudentStatusType entity;
        boolean isNew = false;
        boolean wasRestored = false;

        if (existingOpt.isPresent()) {
            // Entity exists and is not soft-deleted - just update
            entity = existingOpt.get();
            entity.setUpdateTs(java.time.LocalDateTime.now());
            entity.setUpdatedBy(securityHelper.getCurrentUsername());
            log.debug("Found existing entity with code: {}", code);
        } else {
            // Step 2: Check if soft-deleted (bypass @SQLRestriction)
            if (studentService.hasSoftDeletedStudentStatusType(code)) {
                // Step 3: Restore soft-deleted entity via service
                studentService.restoreSoftDeletedStudentStatusType(code);

                // Now fetch the restored entity via JPA
                entity = studentService.findStudentStatusTypeById(code)
                    .orElseThrow(() -> new IllegalStateException("Failed to restore entity with code: " + code));
                wasRestored = true;
                entity.setUpdatedBy(securityHelper.getCurrentUsername());
                log.info("Restored soft-deleted entity with code: {}", code);
            } else {
                // Step 4: Truly new entity
                entity = new StudentStatusType();
                entity.setCode(code);
                entity.setCreateTs(java.time.LocalDateTime.now());
                entity.setCreatedBy(securityHelper.getCurrentUsername());
                isNew = true;
                log.debug("Creating new entity with code: {}", code);
            }
        }

        // Set common fields
        entity.setName(name);
        studentService.updateStudentStatusTypeFromMap(entity, entityData);

        if (isNew && entity.getActive() == null) {
            entity.setActive(true); // Default for new entities only
        }

        // Save entity
        StudentStatusType saved = studentService.saveStudentStatusType(entity);
        log.info("StudentStatusType {} successfully: {}", isNew ? "created" : (wasRestored ? "restored" : "updated"), code);

        // OLD-HEMIS COMPATIBLE: Return 201 CREATED with minimal response like CUBA Platform
        return ResponseEntity.ok(studentService.toStudentStatusTypeMinimalMap(saved));
    }

    // =====================================================
    // GET ALL - LIST ALL ENTITIES
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.view')")
    @GetMapping
    @Operation(
        summary = "Barcha talaba holatlari",
        description = """
            Sahifalangan talaba holatlari ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HStudentStatusType
            **Auth:** Bearer token (required)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all StudentStatusType - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.by(Sort.Direction.ASC, "code");
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length >= 2) {
                Sort.Direction direction = sortParts[1].equalsIgnoreCase("DESC")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
                sorting = Sort.by(direction, sortParts[0]);
            }
        }

        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), limit, sorting);
        Page<StudentStatusType> page = studentService.findAllStudentStatusType(pageRequest);

        List<Map<String, Object>> result = page.getContent().stream()
            .map(e -> studentService.toStudentStatusTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // GET SEARCH - SEARCH WITH URL PARAMS
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.view')")
    @GetMapping("/search")
    @Operation(
        summary = "Talaba holatlarini qidirish (GET)",
        description = """
            URL parametrlari orqali talaba holatlarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **CUBA Filter Format (URL encoded JSON):**
            ```
            filter={"conditions":[{"property":"active","operator":"=","value":true}]}
            ```

            **Qo'llab-quvvatlanadigan operatorlar:** =, <>, like, in
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        log.debug("GET search StudentStatusType with filter: {}", filter);

        List<StudentStatusType> entities = studentService.findAllStudentStatusType();
        entities.sort(Comparator.comparing(StudentStatusType::getCode));

        // Apply CUBA filter if present (URL encoded JSON)
        if (filter != null && !filter.isBlank()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> filterMap = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(filter, Map.class);
                entities = applyCubaFilter(entities, filterMap);
            } catch (Exception e) {
                log.warn("Failed to parse CUBA filter: {}", e.getMessage());
            }
        }

        // Apply pagination
        int fromIndex = Math.min(offset, entities.size());
        int toIndex = Math.min(offset + limit, entities.size());
        List<StudentStatusType> paginatedEntities = entities.subList(fromIndex, toIndex);

        List<Map<String, Object>> result = paginatedEntities.stream()
            .map(e -> studentService.toStudentStatusTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST SEARCH - SEARCH WITH JSON BODY
    // =====================================================

    @PreAuthorize("hasAuthority('classifiers.view')")
    @PostMapping("/search")
    @Operation(
        summary = "Talaba holatlarini qidirish (POST)",
        description = """
            JSON filter orqali talaba holatlarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **CUBA Filter Format:**
            ```json
            {
              "filter": {
                "conditions": [
                  {"property": "code", "operator": "=", "value": "11"}
                ]
              },
              "limit": 50,
              "offset": 0
            }
            ```

            **Qo'llab-quvvatlanadigan operatorlar:** =, <>, like, in
            """
    )
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search StudentStatusType with body: {}", requestBody);

        List<StudentStatusType> entities = studentService.findAllStudentStatusType();
        entities.sort(Comparator.comparing(StudentStatusType::getCode));

        // Apply CUBA filter if present
        // OLD-HEMIS CUBA format: {"filter": {"conditions": [...]}, "limit": 50, "offset": 0}
        if (requestBody != null && requestBody.containsKey("filter")) {
            entities = applyCubaFilter(entities, requestBody.get("filter"));
        }

        // Apply limit and offset from request body (OLD-HEMIS CUBA format)
        int limit = 50; // default
        int offset = 0; // default
        if (requestBody != null) {
            if (requestBody.containsKey("limit")) {
                Object limitObj = requestBody.get("limit");
                if (limitObj instanceof Number) {
                    limit = ((Number) limitObj).intValue();
                }
            }
            if (requestBody.containsKey("offset")) {
                Object offsetObj = requestBody.get("offset");
                if (offsetObj instanceof Number) {
                    offset = ((Number) offsetObj).intValue();
                }
            }
        }

        // Apply pagination
        int fromIndex = Math.min(offset, entities.size());
        int toIndex = Math.min(offset + limit, entities.size());
        List<StudentStatusType> paginatedEntities = entities.subList(fromIndex, toIndex);

        List<Map<String, Object>> result = paginatedEntities.stream()
            .map(e -> studentService.toStudentStatusTypeMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Apply CUBA-style filter to entity list
     */
    @SuppressWarnings("unchecked")
    private List<StudentStatusType> applyCubaFilter(List<StudentStatusType> entities, Object filterObj) {
        if (!(filterObj instanceof Map)) {
            return entities;
        }

        Map<String, Object> filter = (Map<String, Object>) filterObj;
        Object conditionsObj = filter.get("conditions");

        if (!(conditionsObj instanceof List)) {
            return entities;
        }

        List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;

        return entities.stream()
            .filter(entity -> matchesAllConditions(entity, conditions))
            .collect(Collectors.toList());
    }

    /**
     * Check if entity matches all filter conditions
     */
    private boolean matchesAllConditions(StudentStatusType entity, List<Map<String, Object>> conditions) {
        for (Map<String, Object> condition : conditions) {
            String property = (String) condition.get("property");
            String operator = (String) condition.get("operator");
            Object value = condition.get("value");

            if (!matchesCondition(entity, property, operator, value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if entity matches a single condition
     */
    private boolean matchesCondition(StudentStatusType entity, String property, String operator, Object value) {
        if (property == null || operator == null) {
            return true;
        }

        Object entityValue = getEntityValue(entity, property);
        String strValue = value != null ? value.toString() : null;
        String strEntityValue = entityValue != null ? entityValue.toString() : null;

        return switch (operator.toLowerCase()) {
            case "=" -> strValue != null && strValue.equals(strEntityValue);
            case "<>", "!=" -> strValue == null || !strValue.equals(strEntityValue);
            case "like" -> strEntityValue != null && strValue != null &&
                strEntityValue.toLowerCase().contains(strValue.toLowerCase().replace("%", ""));
            case "in" -> {
                if (value instanceof List) {
                    yield ((List<?>) value).stream()
                        .anyMatch(v -> v != null && v.toString().equals(strEntityValue));
                }
                yield false;
            }
            case "isnull", "isNull" -> entityValue == null;
            case "notnull", "notNull" -> entityValue != null;
            default -> true;
        };
    }

    /**
     * Get entity property value by name
     */
    private Object getEntityValue(StudentStatusType entity, String property) {
        return switch (property.toLowerCase()) {
            case "code", "id" -> entity.getCode();
            case "name" -> entity.getName();
            case "nameen", "name_en" -> entity.getNameEn();
            case "nameru", "name_ru" -> entity.getNameRu();
            case "active" -> entity.getActive();
            case "version" -> entity.getVersion();
            default -> null;
        };
    }
}
