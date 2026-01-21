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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Project;
import uz.hemis.domain.repository.ProjectRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Project Entity Controller (CUBA Pattern)
 * Tag: 19.Loyihalar
 * Entity: hemishe_EProject
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EProject/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EProject/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EProject/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EProject/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EProject/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EProject           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EProject           - Create new
 *
 * @since 2.0.0
 */
@Tag(name = "19.Loyihalar", description = "Loyihalar entity API - CUBA Platform REST API compatible")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EProject")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ProjectEntityController {

    private final ProjectRepository repository;
    private static final String ENTITY_NAME = "hemishe_EProject";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    // =====================================================
    // GET /{entityId} - Bitta loyihani olish
    // =====================================================

    @GetMapping("/{entityId}")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Bitta loyihani olish",
        description = """
            ID bo'yicha loyiha ma'lumotlarini olish.

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** GET /app/rest/v2/entities/hemishe_EProject/{entityId}
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Loyiha UUID") @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.info("GET Project by id: {}, returnNulls: {}", entityId, returnNulls);

        Optional<Project> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("GET /entities/hemishe_EProject/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    // =====================================================
    // PUT /{entityId} - Loyihani yangilash
    // =====================================================

    @PutMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Loyihani yangilash",
        description = "Mavjud loyiha ma'lumotlarini qisman yangilash"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yangilandi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("PUT Project id: {}", entityId);

        Optional<Project> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            log.warn("PUT /entities/hemishe_EProject/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        Project entity = existingOpt.get();
        updateFromMap(entity, body);
        entity.setUpdateTs(LocalDateTime.now());

        Project saved = repository.save(entity);

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // DELETE /{entityId} - Loyihani o'chirish (soft delete)
    // =====================================================

    @DeleteMapping("/{entityId}")
    @Transactional
    @Operation(
        summary = "Loyihani o'chirish",
        description = "Loyihani soft delete qilish (delete_ts belgilanadi)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli o'chirildi"),
        @ApiResponse(responseCode = "404", description = "Topilmadi")
    })
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.info("DELETE Project id: {}", entityId);

        Optional<Project> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            log.warn("DELETE /entities/hemishe_EProject/{} - topilmadi", entityId);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Entity not found");
            error.put("details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found");
            return ResponseEntity.status(404).body(error);
        }

        repository.delete(entity.get());
        log.info("DELETE /entities/hemishe_EProject/{} - muvaffaqiyatli o'chirildi", entityId);

        // OLD-HEMIS: 200 OK
        return ResponseEntity.ok().build();
    }

    // =====================================================
    // GET /search - Qidirish (URL params)
    // =====================================================

    @GetMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Loyihalarni qidirish (GET)")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search Project with filter: {}", filter);

        List<Project> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // POST /search - Qidirish (JSON filter)
    // =====================================================

    @PostMapping("/search")
    @Transactional(readOnly = true)
    @Operation(summary = "Loyihalarni qidirish (POST)")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search Project with filter: {}", filter);

        List<Project> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    // =====================================================
    // GET - Barcha loyihalar ro'yxati (paginated)
    // =====================================================

    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    @Operation(summary = "Barcha loyihalar ro'yxati")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Umumiy sonni qaytarish") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Boshlang'ich pozitsiya") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Sahifadagi yozuvlar soni") @RequestParam(required = false) Integer limit,
            @Parameter(description = "Tartiblash") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all Project - offset: {}, limit: {}", offset, limit);

        if (limit == null) {
            List<Project> allEntities = repository.findAll();
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
        Page<Project> entityPage = repository.findAll(pageRequest);

        List<Map<String, Object>> result = entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList());

        if (Boolean.TRUE.equals(returnCount)) {
            return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(entityPage.getTotalElements()))
                .body(result);
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // POST - Yangi loyiha yaratish
    // =====================================================

    @PostMapping
    @Transactional
    @Operation(
        summary = "Loyiha yaratish",
        description = """
            Yangi loyiha yozuvini yaratish (CUBA Entity API).

            **OLD-HEMIS Compatible** - 100% backward compatibility

            **Endpoint:** POST /app/rest/v2/entities/hemishe_EProject
            **Auth:** Bearer token (required)

            **Misol request body:**
            ```json
            {
                "name": "Innovatsion loyiha",
                "projectNumber": "PRJ-2024-001",
                "contractNumber": "SH-001/2024",
                "contractDate": "2024-01-15",
                "startDate": "2024-02-01",
                "endDate": "2025-01-31",
                "active": true
            }
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Muvaffaqiyatli yaratildi",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"_entityName\":\"hemishe_EProject\",\"_instanceName\":\"Innovatsion loyiha\",\"id\":\"uuid\"}"))),
        @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov"),
        @ApiResponse(responseCode = "401", description = "Autentifikatsiya xatosi")
    })
    public ResponseEntity<Map<String, Object>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Loyiha ma'lumotlari",
                required = true)
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.info("POST create new Project");
        log.debug("Request body: {}", body);

        Project entity = new Project();

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

        Project saved = repository.save(entity);
        log.info("Project created with id: {}", saved.getId());

        // OLD-HEMIS: minimal response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("_entityName", ENTITY_NAME);
        response.put("_instanceName", buildInstanceName(saved));
        response.put("id", saved.getId().toString());

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // Helper Methods
    // =====================================================

    private String buildInstanceName(Project entity) {
        return entity.getName() != null ? entity.getName() : "Project-" + entity.getId();
    }

    /**
     * Convert entity to Map with OLD-HEMIS exact field order.
     *
     * OLD-HEMIS field order:
     * 1. _entityName, _instanceName, id
     * 2. contractDate, endDate, projectNumber, contractNumber
     * 3. active, version, deletedBy, deleteTs
     * 4. uId, translations, name, position, startDate
     */
    private Map<String, Object> toMap(Project entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // 1. Entity metadata
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        // 2. Contract and project fields
        if (entity.getContractDate() != null) {
            map.put("contractDate", entity.getContractDate().format(DATE_FORMAT));
        } else {
            map.put("contractDate", null);
        }

        if (entity.getEndDate() != null) {
            map.put("endDate", entity.getEndDate().format(DATE_FORMAT));
        } else {
            map.put("endDate", null);
        }

        map.put("projectNumber", entity.getProjectNumber());
        map.put("contractNumber", entity.getContractNumber());

        // 3. Status and audit fields
        map.put("active", entity.getActive());
        map.put("version", entity.getVersion());
        map.put("deletedBy", entity.getDeletedBy());
        map.put("deleteTs", entity.getDeleteTs() != null ? entity.getDeleteTs().toString() : null);

        // 4. Additional fields
        map.put("uId", entity.getUId());
        map.put("translations", entity.getTranslations());
        map.put("name", entity.getName());
        map.put("position", entity.getPosition());

        if (entity.getStartDate() != null) {
            map.put("startDate", entity.getStartDate().format(DATE_FORMAT));
        } else {
            map.put("startDate", null);
        }

        return map;
    }

    private void updateFromMap(Project entity, Map<String, Object> map) {
        // name
        if (map.containsKey("name")) {
            entity.setName(getStringValue(map.get("name")));
        }

        // projectNumber
        if (map.containsKey("projectNumber")) {
            entity.setProjectNumber(getStringValue(map.get("projectNumber")));
        }

        // university (nested object with "id" or plain string)
        if (map.containsKey("university")) {
            entity.setUniversity(extractStringId(map.get("university")));
        }

        // department (nested object with "id" or plain string)
        if (map.containsKey("department")) {
            entity.setDepartment(extractStringId(map.get("department")));
        }

        // projectType (nested object with "id" or plain string)
        if (map.containsKey("projectType")) {
            entity.setProjectType(extractStringId(map.get("projectType")));
        }

        // locality (nested object with "id" or plain string)
        if (map.containsKey("locality")) {
            entity.setLocality(extractStringId(map.get("locality")));
        }

        // projectCurrency (nested object with "id" or plain string)
        if (map.containsKey("projectCurrency")) {
            entity.setProjectCurrency(extractStringId(map.get("projectCurrency")));
        }

        // contractNumber
        if (map.containsKey("contractNumber")) {
            entity.setContractNumber(getStringValue(map.get("contractNumber")));
        }

        // contractDate
        if (map.containsKey("contractDate")) {
            entity.setContractDate(parseDate(map.get("contractDate")));
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

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null) {
            map.put(key, value);
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put(key, null);
        }
    }

    /**
     * CUBA format: {"id": "uuid-string"} - faqat Map qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id instanceof String str && !str.isEmpty()) {
                try {
                    return UUID.fromString(str);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * CUBA format: {"id": "string"} - faqat Map qabul qiladi
     */
    @SuppressWarnings("unchecked")
    private String extractStringId(Object value) {
        if (value == null) return null;
        if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            return id != null ? id.toString() : null;
        }
        return null;
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
