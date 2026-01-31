package uz.hemis.api.legacy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.PublicationMethodical;
import uz.hemis.domain.repository.PublicationMethodicalRepository;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PublicationMethodical Entity Controller (CUBA Pattern)
 * Tag 55: Publications - Methodical (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EPublicationMethodical
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_EPublicationMethodical
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EPublicationMethodical/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EPublicationMethodical/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EPublicationMethodical/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EPublicationMethodical/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EPublicationMethodical/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EPublicationMethodical           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EPublicationMethodical           - Create new
 */
@Tag(name = "24.Ilmiy uslubiy nashlar")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationMethodical")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationMethodicalEntityController {

    private final PublicationMethodicalRepository repository;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EPublicationMethodical";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get PublicationMethodical by ID", description = "Returns a single PublicationMethodical by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET PublicationMethodical by id: {}", entityId);

        Optional<PublicationMethodical> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update PublicationMethodical", description = "Updates an existing PublicationMethodical")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT PublicationMethodical id: {}", entityId);

        Optional<PublicationMethodical> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationMethodical entity = existingOpt.get();
        updateFromMap(entity, body);

        PublicationMethodical saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete PublicationMethodical", description = "Soft deletes an PublicationMethodical")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE PublicationMethodical id: {}", entityId);

        Optional<PublicationMethodical> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Soft delete: delete_ts belgilash (hard delete emas — FK constraint buzilmasligi uchun)
        PublicationMethodical pub = entity.get();
        pub.setDeleteTs(java.time.LocalDateTime.now());
        repository.save(pub);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search PublicationMethodical (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<PublicationMethodical> allEntities = repository.findAll();
        List<PublicationMethodical> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search PublicationMethodical (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @Parameter(description = "Offset") @RequestParam(required = false) Integer offset,
            @Parameter(description = "Limit") @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        int effectiveOffset = filterHelper.extractInt(body, "offset", offset, 0);
        int effectiveLimit = filterHelper.extractInt(body, "limit", limit, 50);
        String filterJson = filterHelper.extractFilterFromBody(body);

        log.debug("POST search - offset: {}, limit: {}, filter: {}", effectiveOffset, effectiveLimit, filterJson);

        List<PublicationMethodical> allEntities = repository.findAll();
        List<PublicationMethodical> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all PublicationMethodical", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all PublicationMethodical - offset: {}, limit: {}", offset, limit);

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
        Page<PublicationMethodical> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create PublicationMethodical", description = "Creates a new PublicationMethodical")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create/upsert PublicationMethodical");

        // CUBA UPSERT: if body contains 'id' and entity exists, update instead of create
        if (body.containsKey("id")) {
            try {
                UUID existingId = UUID.fromString(body.get("id").toString());
                Optional<PublicationMethodical> existingOpt = repository.findById(existingId);
                if (existingOpt.isPresent()) {
                    log.info("POST with existing id={} — performing UPSERT (update)", existingId);
                    PublicationMethodical existing = existingOpt.get();
                    updateFromMap(existing, body);
                    existing.setUpdateTs(java.time.LocalDateTime.now());
                    PublicationMethodical saved = repository.save(existing);
                    return ResponseEntity.ok(toMap(saved, returnNulls));
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID format for id: {}", body.get("id"));
            }
        }

        PublicationMethodical entity = new PublicationMethodical();
        updateFromMap(entity, body);
        PublicationMethodical saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(PublicationMethodical entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name - use entity name if available
        String instanceName = entity.getName() != null ? entity.getName() : "PublicationMethodical-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());
        map.put("version", entity.getVersion());

        // Add entity-specific fields (camelCase like Old Hemis CUBA format)
        putIfNotNull(map, "authorCounts", entity.getAuthorCounts(), returnNulls);
        putIfNotNull(map, "parameter", entity.getParameter(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "isChecked", entity.getIsChecked(), returnNulls);
        putIfNotNull(map, "issueYear", entity.getIssueYear(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "publisher", entity.getPublisher(), returnNulls);
        putIfNotNull(map, "authors", entity.getAuthors(), returnNulls);
        // Additional fields (not in Old Hemis default view but needed for full data)
        putIfNotNull(map, "uId", entity.getUId(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "sourceName", entity.getSourceName(), returnNulls);
        putIfNotNull(map, "_methodical_publication_type", entity.getMethodicalPublicationType(), returnNulls);
        putIfNotNull(map, "_publication_database", entity.getPublicationDatabase(), returnNulls);
        putIfNotNull(map, "_employee", entity.getEmployee(), returnNulls);
        putIfNotNull(map, "position", entity.getPosition(), returnNulls);
        putIfNotNull(map, "_translations", entity.getTranslations(), returnNulls);
        putIfNotNull(map, "isCheckedDate", entity.getIsCheckedDate(), returnNulls);
        putIfNotNull(map, "_education_year", entity.getEducationYear(), returnNulls);

        // BaseEntity audit fields
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    private void updateFromMap(PublicationMethodical entity, Map<String, Object> map) {
        // uId - support both formats
        if (map.containsKey("u_id") || map.containsKey("uId")) {
            entity.setUId(extractInteger(map.getOrDefault("uId", map.get("u_id"))));
        }
        if (map.containsKey("_university") || map.containsKey("university")) {
            entity.setUniversity(extractString(map.getOrDefault("university", map.get("_university"))));
        }
        if (map.containsKey("name")) {
            entity.setName(extractString(map.get("name")));
        }
        if (map.containsKey("authors")) {
            entity.setAuthors(extractString(map.get("authors")));
        }
        // authorCounts - support both camelCase and snake_case
        if (map.containsKey("author_counts") || map.containsKey("authorCounts")) {
            entity.setAuthorCounts(extractInteger(map.getOrDefault("authorCounts", map.get("author_counts"))));
        }
        if (map.containsKey("publisher")) {
            entity.setPublisher(extractString(map.get("publisher")));
        }
        // issueYear - support both formats
        if (map.containsKey("issue_year") || map.containsKey("issueYear")) {
            entity.setIssueYear(extractInteger(map.getOrDefault("issueYear", map.get("issue_year"))));
        }
        // sourceName - support both formats
        if (map.containsKey("source_name") || map.containsKey("sourceName")) {
            entity.setSourceName(extractString(map.getOrDefault("sourceName", map.get("source_name"))));
        }
        if (map.containsKey("parameter")) {
            entity.setParameter(extractString(map.get("parameter")));
        }
        if (map.containsKey("_methodical_publication_type") || map.containsKey("methodicalPublicationType")) {
            entity.setMethodicalPublicationType(extractString(map.getOrDefault("methodicalPublicationType", map.get("_methodical_publication_type"))));
        }
        if (map.containsKey("_publication_database") || map.containsKey("publicationDatabase")) {
            entity.setPublicationDatabase(extractString(map.getOrDefault("publicationDatabase", map.get("_publication_database"))));
        }
        if (map.containsKey("_employee") || map.containsKey("employee")) {
            entity.setEmployee(extractUuid(map.getOrDefault("employee", map.get("_employee"))));
        }
        if (map.containsKey("filename")) {
            entity.setFilename(extractString(map.get("filename")));
        }
        if (map.containsKey("position")) {
            entity.setPosition(extractInteger(map.get("position")));
        }
        if (map.containsKey("active")) {
            entity.setActive(extractBoolean(map.get("active")));
        }
        if (map.containsKey("_translations")) {
            entity.setTranslations(extractString(map.get("_translations")));
        }
        // isChecked - support both formats
        if (map.containsKey("is_checked") || map.containsKey("isChecked")) {
            entity.setIsChecked(extractBoolean(map.getOrDefault("isChecked", map.get("is_checked"))));
        }
        // isCheckedDate - support both formats
        if (map.containsKey("is_checked_date") || map.containsKey("isCheckedDate")) {
            entity.setIsCheckedDate(extractLocalDateTime(map.getOrDefault("isCheckedDate", map.get("is_checked_date"))));
        }
        if (map.containsKey("_education_year") || map.containsKey("educationYear")) {
            entity.setEducationYear(extractString(map.getOrDefault("educationYear", map.get("_education_year"))));
        }
    }

    /**
     * Extracts string value from various formats:
     * - Simple string: "401" -> "401"
     * - Nested object (CUBA format): {"_entityName": "...", "id": "401"} -> "401"
     * - Nested object with code: {"code": "401"} -> "401"
     */
    private String extractString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) return extractString(id);
            Object code = nested.get("code");
            if (code != null) return extractString(code);
        }
        return value.toString();
    }

    private Integer extractInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean extractBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private UUID extractUuid(Object value) {
        if (value == null) return null;
        if (value instanceof UUID) return (UUID) value;
        // CUBA format: {"id": "uuid-string"}
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
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

    private java.time.LocalDateTime extractLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) value;
        if (value instanceof String) {
            String str = (String) value;
            if (str.isEmpty()) return null;
            try {
                return java.time.LocalDateTime.parse(str);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
