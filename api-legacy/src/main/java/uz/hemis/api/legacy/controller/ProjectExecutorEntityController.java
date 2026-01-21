package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.ProjectExecutor;
import uz.hemis.domain.repository.ProjectExecutorRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ProjectExecutor Entity Controller (CUBA Pattern)
 * Tag: 21.Loyiha ijrochilari
 * Entity: hemishe_EProjectExecutor
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - POST   /app/rest/v2/entities/hemishe_EProjectExecutor           - Create new
 * - GET    /app/rest/v2/entities/hemishe_EProjectExecutor/{id}      - Get by ID
 * - GET    /app/rest/v2/entities/hemishe_EProjectExecutor           - List all with pagination
 * - PUT    /app/rest/v2/entities/hemishe_EProjectExecutor/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EProjectExecutor/{id}      - Soft delete
 *
 * @since 2.0.0
 */
@Tag(name = "21.Loyiha ijrochilari", description = "Loyiha ijrochilari entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EProjectExecutor")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ProjectExecutorEntityController {

    private final ProjectExecutorRepository repository;
    private static final String ENTITY_NAME = "hemishe_EProjectExecutor";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    // TEST endpoint
    @GetMapping("/test")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> test() {
        log.info("TEST endpoint called");
        try {
            long count = repository.count();
            log.info("Count: {}", count);
            return ResponseEntity.ok("Test OK! Count: " + count);
        } catch (Exception e) {
            log.error("Error in test", e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getClass().getName());
            error.put("message", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(500).body(error);
        }
    }

    // =====================================================
    // POST - Yangi loyiha ijrochisi yaratish
    // =====================================================

    @PostMapping
    @Transactional
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Loyiha ijrochisi yaratish",
        description = """
            Yangi loyiha ijrochisi yozuvini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EProjectExecutor
            **Auth:** Bearer token (required)

            **Misol request body:**
            ```json
            {
                "project": {"id": "project-uuid"},
                "projectExecutorType": {"id": "executor-type-uuid"},
                "outsider": "Tashqi ijrochi ismi",
                "startDate": "2024-01-01",
                "endDate": "2024-12-31",
                "active": true
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EProjectExecutor\",\"_instanceName\":\"com.company.hemishe.entity.EProjectExecutor-uuid [detached]\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Loyiha ijrochisi ma'lumotlari",
                required = true)
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new ProjectExecutor");
        log.debug("Request body: {}", body);

        ProjectExecutor entity = new ProjectExecutor();

        // Agar id berilgan bo'lsa, ishlatish (OLD-HEMIS pattern)
        if (body.containsKey("id")) {
            try {
                entity.setId(UUID.fromString(body.get("id").toString()));
            } catch (Exception e) {
                log.warn("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        updateFromMap(entity, body);

        // Version va timestamps
        entity.setVersion(1);
        entity.setCreateTs(LocalDateTime.now());

        ProjectExecutor saved = repository.save(entity);
        log.info("ProjectExecutor created with id: {}", saved.getId());

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // GET /{entityId} - Bitta loyiha ijrochisini olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Bitta loyiha ijrochisini olish",
        description = """
            ID bo'yicha loyiha ijrochisi ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EProjectExecutor/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Loyiha ijrochisi UUID") @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET ProjectExecutor by id: {}, returnNulls: {}", entityId, returnNulls);

        Optional<ProjectExecutor> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EProjectExecutor/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // GET - Barcha loyiha ijrochilari ro'yxati (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Barcha loyiha ijrochilari ro'yxati")
    public ResponseEntity<?> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET all ProjectExecutor - offset: {}, limit: {}", offset, limit);

        try {
            if (limit == null) {
                List<ProjectExecutor> allEntities = repository.findAll();
                log.info("Found {} entities", allEntities.size());
                List<Map<String, Object>> result = allEntities.stream()
                    .map(e -> toMap(e, returnNulls))
                    .collect(Collectors.toList());

                if (Boolean.TRUE.equals(returnCount)) {
                    return ResponseEntity.ok()
                        .header("X-Total-Count", String.valueOf(result.size()))
                        .body(result);
                }
                return ResponseEntity.ok(result);
            }

            Sort sorting = Sort.unsorted();
            if (sort != null && !sort.isEmpty()) {
                String[] parts = sort.split(",");
                String field = parts[0];
                Sort.Direction direction = parts.length > 1 && "DESC".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
                sorting = Sort.by(direction, field);
            }

            int page = offset / limit;
            PageRequest pageRequest = PageRequest.of(page, limit, sorting);
            Page<ProjectExecutor> entityPage = repository.findAll(pageRequest);

            List<Map<String, Object>> result = entityPage.getContent().stream()
                .map(e -> toMap(e, returnNulls))
                .collect(Collectors.toList());

            if (Boolean.TRUE.equals(returnCount)) {
                return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                    .body(result);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching ProjectExecutor entities", e);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getClass().getName());
            error.put("message", e.getMessage());
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(500).body(error);
        }
    }

    // =====================================================
    // PUT /{entityId} - Loyiha ijrochisini yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Loyiha ijrochisini yangilash",
        description = "Mavjud loyiha ijrochisi ma'lumotlarini qisman yangilash"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT ProjectExecutor id: {}", entityId);

        Optional<ProjectExecutor> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("PUT /entities/hemishe_EProjectExecutor/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        ProjectExecutor entity = existingOpt.get();
        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        ProjectExecutor saved = repository.save(entity);

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE /{entityId} - Loyiha ijrochisini o'chirish (soft delete)
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
    @PreAuthorize("permitAll()")
    @Operation(
        summary = "Loyiha ijrochisini o'chirish",
        description = "Loyiha ijrochisini soft delete qilish (delete_ts belgilanadi)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE ProjectExecutor id: {}", entityId);

        Optional<ProjectExecutor> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("DELETE /entities/hemishe_EProjectExecutor/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        repository.delete(entity.get());
        log.info("DELETE /entities/hemishe_EProjectExecutor/{} - muvaffaqiyatli o'chirildi", entityId);

        // OLD-HEMIS: 200 OK
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private String buildInstanceName(ProjectExecutor entity) {
        // OLD-HEMIS format: com.company.hemishe.entity.EProjectExecutor-UUID [detached]
        return "com.company.hemishe.entity.EProjectExecutor-" + entity.getId() + " [detached]";
    }

    /**
     * Convert entity to Map with OLD-HEMIS exact field order.
     *
     * OLD-HEMIS field order (from actual response):
     * 1. _entityName, _instanceName, id
     * 2. endDate, active, idNumber, version
     * 3. deletedBy, deleteTs, translations, position
     * 4. outsider, startDate
     */
    private Map<String, Object> toMap(ProjectExecutor entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // 1. Entity metadata
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // 2. Date and status fields
        if (entity.getEndDate() != null) {
            map.put("endDate", entity.getEndDate().format(DATE_FORMAT));
        } else {
            map.put("endDate", null);
        }

        map.put("active", entity.getActive());
        map.put("idNumber", entity.getIdNumber());
        map.put("version", entity.getVersion());

        // 3. Audit fields
        map.put("deletedBy", entity.getDeletedBy());
        map.put("deleteTs", entity.getDeleteTs() != null ? entity.getDeleteTs().toString() : null);
        map.put("translations", entity.getTranslations());
        map.put("position", entity.getPosition());

        // 4. Main fields
        map.put("outsider", entity.getOutsider());

        if (entity.getStartDate() != null) {
            map.put("startDate", entity.getStartDate().format(DATE_FORMAT));
        } else {
            map.put("startDate", null);
        }

        return map;
    }

    private void updateFromMap(ProjectExecutor entity, Map<String, Object> map) {
        // project (nested object with "id" or plain UUID)
        if (map.containsKey("project")) {
            entity.setProject(extractUuid(map.get("project")));
        }

        // projectExecutorType (nested object with "id" or plain string)
        if (map.containsKey("projectExecutorType")) {
            entity.setProjectExecutorType(extractString(map.get("projectExecutorType")));
        }

        // idNumber
        if (map.containsKey("idNumber")) {
            entity.setIdNumber(getIntegerValue(map.get("idNumber")));
        }

        // outsider
        if (map.containsKey("outsider")) {
            entity.setOutsider(getStringValue(map.get("outsider")));
        }

        // startDate
        if (map.containsKey("startDate")) {
            entity.setStartDate(parseDate(map.get("startDate")));
        }

        // endDate
        if (map.containsKey("endDate")) {
            entity.setEndDate(parseDate(map.get("endDate")));
        }

        // position
        if (map.containsKey("position")) {
            entity.setPosition(getIntegerValue(map.get("position")));
        }

        // active
        if (map.containsKey("active")) {
            entity.setActive(getBooleanValue(map.get("active")));
        }

        // translations
        if (map.containsKey("translations")) {
            entity.setTranslations(getStringValue(map.get("translations")));
        }
    }

    // =====================================================
    // Utility Methods
    // =====================================================

    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) {
                try {
                    return UUID.fromString(id.toString());
                } catch (Exception e) {
                    log.warn("Invalid UUID: {}", id);
                    return null;
                }
            }
        }
        try {
            return UUID.fromString(value.toString());
        } catch (Exception e) {
            log.warn("Invalid UUID format: {}", value);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractString(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) {
                return id.toString();
            }
        }
        return value.toString();
    }

    private String getStringValue(Object value) {
        return value != null ? value.toString() : null;
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getBooleanValue(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private LocalDate parseDate(Object value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value.toString(), DATE_FORMAT);
        } catch (Exception e) {
            log.warn("Invalid date format: {}", value);
            return null;
        }
    }
}
