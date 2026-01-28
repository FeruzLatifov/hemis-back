package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.AcademicGroup;
import uz.hemis.domain.repository.AcademicGroupRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Akademik Guruhlar Controller - CUBA REST API Pattern
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_RAcademicGroup</li>
 *   <li>Table: hemishe_r_academic_group</li>
 *   <li>Primary key: id (UUID)</li>
 *   <li>Base URL: /app/rest/v2/entities/hemishe_RAcademicGroup</li>
 *   <li>100% backward compatible with OLD-HEMIS CUBA Platform REST API</li>
 * </ul>
 *
 * <p><strong>Endpoints (7 ta):</strong></p>
 * <ul>
 *   <li>GET /{entityId} - ID bo'yicha olish</li>
 *   <li>PUT /{entityId} - Yangilash</li>
 *   <li>DELETE /{entityId} - O'chirish (soft delete)</li>
 *   <li>GET /search - URL parametrlari bilan qidirish</li>
 *   <li>POST /search - JSON filter bilan qidirish</li>
 *   <li>GET / - Barcha ro'yxat (sahifalangan)</li>
 *   <li>POST / - Yangi yaratish</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "32.Akademik hisobotlar akademik guruhlar", description = "Akademik guruhlar hisobotlarini boshqarish API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RAcademicGroup")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AcademicGroupEntityController {

    private final AcademicGroupRepository repository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private static final String ENTITY_NAME = "hemishe_RAcademicGroup";

    // =====================================================
    // 1. GET BY ID
    // =====================================================

    @Operation(
        summary = "Akademik guruh yozuvini ID bo'yicha olish",
        description = """
            Berilgan UUID bo'yicha bitta yozuvni qaytaradi.

            **OLD-HEMIS Compatible** - 100% backward compatibility
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli."),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getById(
            @Parameter(description = "Entity ID (UUID)", required = true)
            @PathVariable String entityId,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("GET hemishe_RAcademicGroup: {}", entityId);

        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicGroup> entity = repository.findById(id);

            if (entity.isEmpty()) {
                Map<String, String> errorResponse = new LinkedHashMap<>();
                errorResponse.put("error", "Entity not found");
                errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            return ResponseEntity.ok(toMap(entity.get(), returnNulls));
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Invalid UUID");
            errorResponse.put("details", "Invalid UUID format: " + entityId);
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // =====================================================
    // 2. UPDATE
    // =====================================================

    @Operation(
        summary = "Akademik guruh yozuvini yangilash",
        description = "Mavjud yozuvni yangilaydi."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @PutMapping("/{entityId}")
    public ResponseEntity<?> update(
            @PathVariable String entityId,
            @RequestBody Map<String, Object> entityData) {

        log.info("UPDATE hemishe_RAcademicGroup: {}", entityId);

        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicGroup> existingOpt = repository.findById(id);

            if (existingOpt.isEmpty()) {
                Map<String, String> errorResponse = new LinkedHashMap<>();
                errorResponse.put("error", "Entity not found");
                errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            AcademicGroup entity = existingOpt.get();
            updateEntityFromMap(entity, entityData);
            entity.setUpdateTs(LocalDateTime.now());

            AcademicGroup saved = repository.save(entity);
            return ResponseEntity.ok(toMap(saved, false));
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Invalid UUID");
            errorResponse.put("details", "Invalid UUID format: " + entityId);
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // =====================================================
    // 3. DELETE (Soft Delete)
    // =====================================================

    @Operation(
        summary = "Akademik guruh yozuvini o'chirish",
        description = "Soft delete - delete_ts qo'yiladi."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli."),
        @ApiResponse(responseCode = "404", description = "Topilmadi.")
    })
    @DeleteMapping("/{entityId}")
    public ResponseEntity<?> delete(@PathVariable String entityId) {

        log.info("DELETE hemishe_RAcademicGroup: {}", entityId);

        try {
            UUID id = UUID.fromString(entityId);
            Optional<AcademicGroup> entity = repository.findById(id);

            if (entity.isEmpty()) {
                Map<String, String> errorResponse = new LinkedHashMap<>();
                errorResponse.put("error", "Entity not found");
                errorResponse.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
                return ResponseEntity.status(404).body(errorResponse);
            }

            entity.get().setDeleteTs(LocalDateTime.now());
            repository.save(entity.get());

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Invalid UUID");
            errorResponse.put("details", "Invalid UUID format: " + entityId);
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    // =====================================================
    // 4. SEARCH (GET)
    // =====================================================

    @Operation(
        summary = "Akademik guruhlarni qidirish (GET)",
        description = "Filter shartlari bo'yicha qidiradi."
    )
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam String filter,
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_RAcademicGroup (GET) - filter: {}", filter);
        return search(filter, offset, limit, returnCount, returnNulls);
    }

    // =====================================================
    // 5. SEARCH (POST)
    // =====================================================

    @Operation(
        summary = "Akademik guruhlarni qidirish (POST)",
        description = "Filter shartlari bo'yicha qidiradi."
    )
    @PostMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("SEARCH hemishe_RAcademicGroup (POST) - filter: {}", filterBody);

        String filterStr = null;
        Integer bodyOffset = offset;
        Integer bodyLimit = limit;

        if (filterBody != null) {
            // Filter ni olish
            Object filterObj = filterBody.get("filter");
            if (filterObj != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    filterStr = mapper.writeValueAsString(filterObj);
                } catch (Exception e) {
                    filterStr = filterObj.toString();
                }
            }

            // Body dan offset va limit ni olish (agar query param da bo'lmasa)
            if (bodyOffset == null && filterBody.containsKey("offset")) {
                Object offsetObj = filterBody.get("offset");
                if (offsetObj instanceof Number) {
                    bodyOffset = ((Number) offsetObj).intValue();
                } else if (offsetObj != null) {
                    try {
                        bodyOffset = Integer.parseInt(offsetObj.toString());
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (bodyLimit == null && filterBody.containsKey("limit")) {
                Object limitObj = filterBody.get("limit");
                if (limitObj instanceof Number) {
                    bodyLimit = ((Number) limitObj).intValue();
                } else if (limitObj != null) {
                    try {
                        bodyLimit = Integer.parseInt(limitObj.toString());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return search(filterStr, bodyOffset, bodyLimit, returnCount, returnNulls);
    }

    // =====================================================
    // 6. LIST ALL
    // =====================================================

    @Operation(
        summary = "Barcha akademik guruhlar yozuvlarini olish",
        description = "Sahifalangan ro'yxat."
    )
    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> listAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("LIST ALL hemishe_RAcademicGroup");

        List<AcademicGroup> allEntities = repository.findAll();

        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<AcademicGroup> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    // =====================================================
    // 7. CREATE
    // =====================================================

    @Operation(
        summary = "Yangi akademik guruh yozuvi yaratish",
        description = "Yangi yozuv yaratadi."
    )
    @PostMapping({"", "/"})
    public ResponseEntity<?> create(@RequestBody Map<String, Object> entityData) {

        log.info("CREATE hemishe_RAcademicGroup: {}", entityData);

        try {
            AcademicGroup entity = new AcademicGroup();

            // ID - agar berilgan bo'lsa ishlatamiz, aks holda @PrePersist da generatsiya qilinadi
            if (entityData.containsKey("id") && entityData.get("id") != null) {
                entity.setId(UUID.fromString(entityData.get("id").toString()));
            }

            updateEntityFromMap(entity, entityData);
            // version va createTs @PrePersist da avtomatik set qilinadi

            AcademicGroup saved = repository.save(entity);
            return ResponseEntity.ok(toMap(saved, false));

        } catch (Exception e) {
            log.error("CREATE xatosi: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Server error");
            errorResponse.put("details", e.getClass().getSimpleName() + ": " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private void updateEntityFromMap(AcademicGroup entity, Map<String, Object> data) {
        if (data.containsKey("universityCode")) {
            entity.setUniversityCode((String) data.get("universityCode"));
        }
        if (data.containsKey("universityName")) {
            entity.setUniversityName((String) data.get("universityName"));
        }
        if (data.containsKey("educationTypeCode")) {
            entity.setEducationTypeCode((String) data.get("educationTypeCode"));
        }
        if (data.containsKey("educationTypeName")) {
            entity.setEducationTypeName((String) data.get("educationTypeName"));
        }
        if (data.containsKey("educationFormCode")) {
            entity.setEducationFormCode((String) data.get("educationFormCode"));
        }
        if (data.containsKey("educationFormName")) {
            entity.setEducationFormName((String) data.get("educationFormName"));
        }
        if (data.containsKey("educationYearCode")) {
            entity.setEducationYearCode((String) data.get("educationYearCode"));
        }
        if (data.containsKey("educationYearName")) {
            entity.setEducationYearName((String) data.get("educationYearName"));
        }
        if (data.containsKey("groupCount")) {
            Object countObj = data.get("groupCount");
            if (countObj != null) {
                if (countObj instanceof Number) {
                    entity.setGroupCount(((Number) countObj).intValue());
                } else {
                    entity.setGroupCount(Integer.parseInt(countObj.toString()));
                }
            }
        }
        if (data.containsKey("updateDate")) {
            Object dateObj = data.get("updateDate");
            if (dateObj != null) {
                if (dateObj instanceof String) {
                    entity.setUpdateDate(LocalDate.parse((String) dateObj));
                }
            }
        }
    }

    private Map<String, Object> toMap(AcademicGroup entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", entity.getUniversityName() != null ? entity.getUniversityName() : entity.getId().toString());
        map.put("id", entity.getId().toString());

        putIfNotNull(map, "universityCode", entity.getUniversityCode(), returnNulls);
        putIfNotNull(map, "universityName", entity.getUniversityName(), returnNulls);
        putIfNotNull(map, "educationTypeCode", entity.getEducationTypeCode(), returnNulls);
        putIfNotNull(map, "educationTypeName", entity.getEducationTypeName(), returnNulls);
        putIfNotNull(map, "educationFormCode", entity.getEducationFormCode(), returnNulls);
        putIfNotNull(map, "educationFormName", entity.getEducationFormName(), returnNulls);
        putIfNotNull(map, "educationYearCode", entity.getEducationYearCode(), returnNulls);
        putIfNotNull(map, "educationYearName", entity.getEducationYearName(), returnNulls);
        putIfNotNull(map, "groupCount", entity.getGroupCount(), returnNulls);
        putIfNotNull(map, "updateDate", entity.getUpdateDate(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    private ResponseEntity<List<Map<String, Object>>> search(
            String filter, Integer offset, Integer limit, Boolean returnCount, Boolean returnNulls) {

        List<AcademicGroup> allEntities = repository.findAll();

        if (filter != null && !filter.isEmpty()) {
            allEntities = applyFilter(allEntities, filter);
        }

        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allEntities.size()) : allEntities.size();

        List<AcademicGroup> paged = allEntities.subList(
            Math.min(start, allEntities.size()),
            Math.min(end, allEntities.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allEntities.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    @SuppressWarnings("unchecked")
    private List<AcademicGroup> applyFilter(List<AcademicGroup> entities, String filter) {
        if (filter.trim().startsWith("{")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                Map<String, Object> filterJson = mapper.readValue(filter, Map.class);

                Object conditionsObj = filterJson.get("conditions");
                if (conditionsObj instanceof List) {
                    List<Map<String, Object>> conditions = (List<Map<String, Object>>) conditionsObj;

                    for (Map<String, Object> condition : conditions) {
                        String property = (String) condition.get("property");
                        String operator = (String) condition.get("operator");
                        Object value = condition.get("value");

                        entities = filterByCondition(entities, property, operator, value);
                    }
                }
                return entities;
            } catch (Exception e) {
                log.warn("CUBA filter parse xatosi: {}", e.getMessage());
            }
        }

        // Simple text search
        String searchTerm = filter.toLowerCase();
        return entities.stream()
            .filter(e -> (e.getUniversityName() != null && e.getUniversityName().toLowerCase().contains(searchTerm)) ||
                        (e.getEducationTypeName() != null && e.getEducationTypeName().toLowerCase().contains(searchTerm)) ||
                        (e.getEducationFormName() != null && e.getEducationFormName().toLowerCase().contains(searchTerm)))
            .collect(Collectors.toList());
    }

    private List<AcademicGroup> filterByCondition(List<AcademicGroup> entities, String property, String operator, Object value) {
        return entities.stream()
            .filter(e -> matchesCondition(e, property, operator, value))
            .collect(Collectors.toList());
    }

    private boolean matchesCondition(AcademicGroup entity, String property, String operator, Object value) {
        Object fieldValue = getFieldValue(entity, property);

        if (operator == null) operator = "=";

        switch (operator) {
            case "=":
                return fieldValue != null && fieldValue.toString().equals(value != null ? value.toString() : null);
            case "contains":
                return fieldValue != null && value != null &&
                       fieldValue.toString().toLowerCase().contains(value.toString().toLowerCase());
            case "notEmpty":
                return fieldValue != null && !fieldValue.toString().isEmpty();
            case "isNull":
                return fieldValue == null;
            default:
                return true;
        }
    }

    private Object getFieldValue(AcademicGroup entity, String property) {
        if (property == null) return null;

        switch (property) {
            case "universityCode": return entity.getUniversityCode();
            case "universityName": return entity.getUniversityName();
            case "educationTypeCode": return entity.getEducationTypeCode();
            case "educationTypeName": return entity.getEducationTypeName();
            case "educationFormCode": return entity.getEducationFormCode();
            case "educationFormName": return entity.getEducationFormName();
            case "educationYearCode": return entity.getEducationYearCode();
            case "educationYearName": return entity.getEducationYearName();
            case "groupCount": return entity.getGroupCount();
            default: return null;
        }
    }
}
