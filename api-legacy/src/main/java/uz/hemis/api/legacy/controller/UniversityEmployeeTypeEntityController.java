package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.UniversityEmployeeType;
import uz.hemis.domain.repository.UniversityEmployeeTypeRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * University Employee Type Entity Controller (CUBA Pattern)
 * Tag: 09.OTM xodimlari kategoriyasi
 *
 * <p>CUBA Platform REST API compatible controller</p>
 * <p>Entity: hemishe_HUniversityEmployeeType</p>
 *
 * <p><strong>OTM xodimlari kategoriyalari:</strong></p>
 * <ul>
 *   <li>10 - Boshqa</li>
 *   <li>11 - Administrativ-boshqaruv xodim</li>
 *   <li>12 - Professor-o'qituvchi xodim</li>
 *   <li>13 - O'quv-yordamchi va texnik xodim</li>
 *   <li>14 - Xizmat ko'rsatuvchi xodim</li>
 * </ul>
 *
 * @since 1.0.0
 */
@Tag(name = "09.OTM xodimlari kategoriyasi", description = "OTM xodimlari turlari klassifikatori - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HUniversityEmployeeType")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityEmployeeTypeEntityController {

    private final UniversityEmployeeTypeRepository repository;
    private final EntityManager entityManager;
    private static final String ENTITY_NAME = "hemishe_HUniversityEmployeeType";

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Bitta xodim turini olish",
        description = """
            Kod bo'yicha xodim turi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}
            **Auth:** Bearer token (required)

            **Turlar:** 10=Boshqa, 11=Administrativ, 12=Professor, 13=Texnik, 14=Xizmat
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> getById(
            @Parameter(description = "Tur kodi", example = "12")
            @PathVariable String entityId,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET UniversityEmployeeType by code: {}", entityId);

        Optional<UniversityEmployeeType> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            // OLD-HEMIS COMPATIBLE: Error response format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT - UPDATE ENTITY
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Xodim turini yangilash",
        description = """
            Xodim turi ma'lumotlarini yangilash.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** PUT /app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}
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
            @Parameter(description = "Tur kodi", example = "12")
            @PathVariable String entityId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Yangilanadigan maydonlar",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        example = """
                            {
                                "name": "Professor-o'qituvchi xodim",
                                "nameEn": "Teaching staff",
                                "nameRu": "Профессорско-преподавательский состав",
                                "active": true
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> entityData) {

        log.info("PUT UniversityEmployeeType - entityId: {}, data: {}", entityId, entityData);

        Optional<UniversityEmployeeType> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("UniversityEmployeeType not found: {}", entityId);
            // OLD-HEMIS COMPATIBLE: Error response format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        UniversityEmployeeType entity = existingOpt.get();

        // Update fields from request body (faqat yuborilgan fieldlar)
        if (entityData.containsKey("name")) {
            entity.setName((String) entityData.get("name"));
        }
        if (entityData.containsKey("nameEn")) {
            entity.setNameEn((String) entityData.get("nameEn"));
        }
        if (entityData.containsKey("nameRu")) {
            entity.setNameRu((String) entityData.get("nameRu"));
        }
        if (entityData.containsKey("active")) {
            Object activeValue = entityData.get("active");
            if (activeValue instanceof Boolean) {
                entity.setActive((Boolean) activeValue);
            } else if (activeValue instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeValue));
            }
        }

        // Update timestamp va user
        entity.setUpdateTs(java.time.LocalDateTime.now());
        // TODO: updatedBy ni SecurityContext dan olish kerak

        UniversityEmployeeType saved = repository.save(entity);
        log.info("UniversityEmployeeType updated successfully: {}", entityId);

        // OLD-HEMIS COMPATIBLE: Minimal response qaytarish
        // Old-hemis faqat _entityName, _instanceName, id qaytaradi
        return ResponseEntity.ok(toMinimalMap(saved));
    }

    // =====================================================
    // DELETE - SOFT DELETE ENTITY
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Xodim turini o'chirish",
        description = """
            Xodim turini o'chirish (soft delete).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** DELETE /app/rest/v2/entities/hemishe_HUniversityEmployeeType/{entityId}
            **Auth:** Bearer token (required)

            ⚠️ **Diqqat:** Bu soft delete - ma'lumot bazadan o'chirilmaydi,
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
            @Parameter(description = "Tur kodi", example = "12")
            @PathVariable String entityId) {

        log.info("DELETE UniversityEmployeeType - entityId: {}", entityId);

        Optional<UniversityEmployeeType> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("UniversityEmployeeType not found for delete: {}", entityId);
            // OLD-HEMIS COMPATIBLE: Error response format
            Map<String, String> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        UniversityEmployeeType entity = existingOpt.get();

        // Soft delete - deleteTs va deletedBy o'rnatish
        entity.setDeleteTs(java.time.LocalDateTime.now());
        // TODO: deletedBy ni SecurityContext dan olish kerak

        repository.save(entity);
        log.info("UniversityEmployeeType soft deleted successfully: {}", entityId);

        return ResponseEntity.ok().build();
    }

    // =====================================================
    // POST - CREATE NEW ENTITY
    // =====================================================

    @PostMapping
    @Transactional
    @Operation(
        summary = "Yangi xodim turini yaratish",
        description = """
            Yangi xodim turi yaratish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_HUniversityEmployeeType
            **Auth:** Bearer token (required)

            **Majburiy fieldlar:**
            - code - Tur kodi (unique)
            - name - O'zbekcha nomi

            **Ixtiyoriy fieldlar:**
            - nameEn - Inglizcha nomi
            - nameRu - Ruscha nomi
            - active - Faol holati (default: true)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi"),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov yoki code allaqachon mavjud"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi"),
        @ApiResponse(responseCode = "403", description = "Ruxsat yo'q")
    })
    public ResponseEntity<?> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Yangi xodim turi ma'lumotlari",
                content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                        example = """
                            {
                                "code": "15",
                                "name": "Yangi xodim turi",
                                "nameEn": "New employee type",
                                "nameRu": "Новый тип сотрудника",
                                "active": true
                            }
                            """
                    )
                )
            )
            @RequestBody Map<String, Object> entityData) {

        log.info("POST UniversityEmployeeType - data: {}", entityData);

        // Validate required fields
        // CUBA compatibility: accept both "code" and "id" fields
        Object codeObj = entityData.get("code");
        if (codeObj == null) {
            codeObj = entityData.get("id"); // CUBA uses "id" field
        }
        String code = codeObj != null ? String.valueOf(codeObj) : null;
        String name = (String) entityData.get("name");

        if (code == null || code.isBlank()) {
            log.warn("POST UniversityEmployeeType - code is required");
            return ResponseEntity.badRequest().body(Map.of(
                "error", "code is required",
                "details", "code maydoni majburiy"
            ));
        }

        if (name == null || name.isBlank()) {
            log.warn("POST UniversityEmployeeType - name is required");
            return ResponseEntity.badRequest().body(Map.of(
                "error", "name is required",
                "details", "name maydoni majburiy"
            ));
        }

        // OLD-HEMIS COMPATIBLE: CUBA Platform does upsert
        // Step 1: Try to find existing entity (non-deleted) via JPA
        Optional<UniversityEmployeeType> existingOpt = repository.findById(code);

        UniversityEmployeeType entity;
        boolean isNew = false;
        boolean wasRestored = false;

        if (existingOpt.isPresent()) {
            // Entity exists and is not soft-deleted - just update
            entity = existingOpt.get();
            entity.setUpdateTs(java.time.LocalDateTime.now());
            log.debug("Found existing entity with code: {}", code);
        } else {
            // Step 2: Check if soft-deleted (bypass @SQLRestriction)
            Long softDeletedCount = (Long) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM hemishe_h_university_employee_type WHERE code = :code AND delete_ts IS NOT NULL"
            ).setParameter("code", code).getSingleResult();

            if (softDeletedCount > 0) {
                // Step 3: Restore soft-deleted entity via native UPDATE
                entityManager.createNativeQuery(
                    "UPDATE hemishe_h_university_employee_type SET delete_ts = NULL, deleted_by = NULL, update_ts = NOW() WHERE code = :code"
                ).setParameter("code", code).executeUpdate();

                // Flush and clear to ensure we get fresh data
                entityManager.flush();
                entityManager.clear();

                // Now fetch the restored entity via JPA
                entity = repository.findById(code)
                    .orElseThrow(() -> new IllegalStateException("Failed to restore entity with code: " + code));
                wasRestored = true;
                log.info("Restored soft-deleted entity with code: {}", code);
            } else {
                // Step 4: Truly new entity
                entity = new UniversityEmployeeType();
                entity.setCode(code);
                entity.setCreateTs(java.time.LocalDateTime.now());
                isNew = true;
                log.debug("Creating new entity with code: {}", code);
            }
        }

        // Set common fields
        entity.setName(name);

        // Optional fields
        if (entityData.containsKey("nameEn")) {
            entity.setNameEn((String) entityData.get("nameEn"));
        }
        if (entityData.containsKey("nameRu")) {
            entity.setNameRu((String) entityData.get("nameRu"));
        }
        if (entityData.containsKey("active")) {
            Object activeValue = entityData.get("active");
            if (activeValue instanceof Boolean) {
                entity.setActive((Boolean) activeValue);
            } else if (activeValue instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeValue));
            }
        } else if (isNew) {
            entity.setActive(true); // Default for new entities only
        }

        // TODO: createdBy/updatedBy ni SecurityContext dan olish kerak

        // Save entity
        UniversityEmployeeType saved;
        if (isNew) {
            entityManager.persist(entity);
            entityManager.flush();
            saved = entity;
        } else {
            saved = repository.save(entity);
        }
        log.info("UniversityEmployeeType {} successfully: {}", isNew ? "created" : (wasRestored ? "restored" : "updated"), code);

        // OLD-HEMIS COMPATIBLE: Return 201 CREATED with minimal response like CUBA Platform
        return ResponseEntity.status(201).body(toMinimalMap(saved));
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(
        summary = "Barcha xodim turlari",
        description = """
            Sahifalangan xodim turlari ro'yxatini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_HUniversityEmployeeType
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

        log.debug("GET all UniversityEmployeeType - offset: {}, limit: {}", offset, limit);

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
        Page<UniversityEmployeeType> page = repository.findAll(pageRequest);

        List<Map<String, Object>> result = page.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(page.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Xodim turlarini qidirish (GET)",
        description = """
            URL parametrlari orqali xodim turlarini qidirish.

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

        log.debug("GET search UniversityEmployeeType with filter: {}", filter);

        List<UniversityEmployeeType> entities = repository.findAll(Sort.by(Sort.Direction.ASC, "code"));

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
        List<UniversityEmployeeType> paginatedEntities = entities.subList(fromIndex, toIndex);

        List<Map<String, Object>> result = paginatedEntities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Xodim turlarini qidirish (POST)",
        description = """
            JSON filter orqali xodim turlarini qidirish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **CUBA Filter Format:**
            ```json
            {
              "filter": {
                "conditions": [
                  {"property": "code", "operator": "=", "value": "12"}
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

        log.debug("POST search UniversityEmployeeType with body: {}", requestBody);

        List<UniversityEmployeeType> entities = repository.findAll(Sort.by(Sort.Direction.ASC, "code"));

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
        List<UniversityEmployeeType> paginatedEntities = entities.subList(fromIndex, toIndex);

        List<Map<String, Object>> result = paginatedEntities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Apply CUBA-style filter to entity list
     *
     * <p>Supported filter format:</p>
     * <pre>
     * {
     *   "conditions": [
     *     {"property": "code", "operator": "=", "value": "12"},
     *     {"property": "name", "operator": "like", "value": "Professor"}
     *   ]
     * }
     * </pre>
     *
     * @param entities List of entities to filter
     * @param filterObj CUBA filter object
     * @return Filtered list
     */
    @SuppressWarnings("unchecked")
    private List<UniversityEmployeeType> applyCubaFilter(List<UniversityEmployeeType> entities, Object filterObj) {
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
    private boolean matchesAllConditions(UniversityEmployeeType entity, List<Map<String, Object>> conditions) {
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
    private boolean matchesCondition(UniversityEmployeeType entity, String property, String operator, Object value) {
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
    private Object getEntityValue(UniversityEmployeeType entity, String property) {
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

    /**
     * Convert entity to CUBA-compatible Map
     *
     * <p>OLD-HEMIS response format:</p>
     * <pre>
     * {
     *   "_entityName": "hemishe_HUniversityEmployeeType",
     *   "_instanceName": "Professor-o'qituvchi xodim",
     *   "id": "12",
     *   "code": "12",
     *   "name": "Professor-o'qituvchi xodim",
     *   "nameEn": "Teaching staff",
     *   "nameRu": "Профессорско-преподавательский состав",
     *   "active": true,
     *   "version": 1
     * }
     * </pre>
     */
    private Map<String, Object> toMap(UniversityEmployeeType entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());
        map.put("code", entity.getCode());
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "nameEn", entity.getNameEn(), returnNulls);
        putIfNotNull(map, "nameRu", entity.getNameRu(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        return map;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    /**
     * Convert entity to minimal CUBA-compatible Map (for PUT/DELETE responses)
     *
     * <p>OLD-HEMIS PUT/DELETE response format:</p>
     * <pre>
     * {
     *   "_entityName": "hemishe_HUniversityEmployeeType",
     *   "_instanceName": "Professor-o'qituvchi xodim",
     *   "id": "12"
     * }
     * </pre>
     */
    private Map<String, Object> toMinimalMap(UniversityEmployeeType entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getName() != null ? entity.getName() : entity.getCode());
        map.put("id", entity.getCode());
        return map;
    }
}
