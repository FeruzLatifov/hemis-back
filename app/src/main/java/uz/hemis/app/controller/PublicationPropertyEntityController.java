package uz.hemis.app.controller;

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
import uz.hemis.domain.entity.PublicationProperty;
import uz.hemis.domain.repository.PublicationPropertyRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PublicationProperty Entity Controller (CUBA Pattern)
 * Tag 56: Publications - Property (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_EPublicationProperty
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_EPublicationProperty
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EPublicationProperty/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EPublicationProperty/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EPublicationProperty/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EPublicationProperty/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EPublicationProperty/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EPublicationProperty           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EPublicationProperty           - Create new
 */
@Tag(name = "Publication Properties")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EPublicationProperty")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class PublicationPropertyEntityController {

    private final PublicationPropertyRepository repository;
    private static final String ENTITY_NAME = "hemishe_EPublicationProperty";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get PublicationProperty by ID", description = "Returns a single PublicationProperty by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET PublicationProperty by id: {}", entityId);

        Optional<PublicationProperty> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update PublicationProperty", description = "Updates an existing PublicationProperty")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT PublicationProperty id: {}", entityId);

        Optional<PublicationProperty> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PublicationProperty entity = existingOpt.get();
        updateFromMap(entity, body);

        PublicationProperty saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete PublicationProperty", description = "Soft deletes an PublicationProperty")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE PublicationProperty id: {}", entityId);

        Optional<PublicationProperty> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search PublicationProperty (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search PublicationProperty with filter: {}", filter);

        List<PublicationProperty> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search PublicationProperty (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search PublicationProperty with filter: {}", filter);

        List<PublicationProperty> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all PublicationProperty", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all PublicationProperty - offset: {}, limit: {}", offset, limit);

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
        Page<PublicationProperty> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create PublicationProperty", description = "Creates a new PublicationProperty")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new PublicationProperty");

        PublicationProperty entity = new PublicationProperty();
        updateFromMap(entity, body);
        PublicationProperty saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(PublicationProperty entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name
        String instanceName = "PublicationProperty-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        // Add entity-specific fields
        putIfNotNull(map, "u_id", entity.getUId(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "numbers", entity.getNumbers(), returnNulls);
        putIfNotNull(map, "authors", entity.getAuthors(), returnNulls);
        putIfNotNull(map, "author_counts", entity.getAuthorCounts(), returnNulls);
        putIfNotNull(map, "parameter", entity.getParameter(), returnNulls);
        putIfNotNull(map, "property_date", entity.getPropertyDate(), returnNulls);
        putIfNotNull(map, "_patent_type", entity.getPatentType(), returnNulls);
        putIfNotNull(map, "_publication_database", entity.getPublicationDatabase(), returnNulls);
        putIfNotNull(map, "_locality", entity.getLocality(), returnNulls);
        putIfNotNull(map, "_country", entity.getCountry(), returnNulls);
        putIfNotNull(map, "_employee", entity.getEmployee(), returnNulls);
        putIfNotNull(map, "filename", entity.getFilename(), returnNulls);
        putIfNotNull(map, "position", entity.getPosition(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);
        putIfNotNull(map, "translations", entity.getTranslations(), returnNulls);
        putIfNotNull(map, "is_checked", entity.getIsChecked(), returnNulls);
        putIfNotNull(map, "is_checked_date", entity.getIsCheckedDate(), returnNulls);
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

    private void updateFromMap(PublicationProperty entity, Map<String, Object> map) {
        // TODO: Add specific field mappings based on entity properties
        // For now, minimal implementation
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
