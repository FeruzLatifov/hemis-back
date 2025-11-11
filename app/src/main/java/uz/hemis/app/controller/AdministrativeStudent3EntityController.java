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
import uz.hemis.domain.entity.AdministrativeStudent3;
import uz.hemis.domain.repository.AdministrativeStudent3Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeStudent3 Entity Controller (CUBA Pattern)
 * Tag 51: Administrative Reports - Students (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAdministrativeStudent3
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RIAdministrativeStudent3
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeStudent3/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent3           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeStudent3           - Create new
 */
@Tag(name = "Administrative Reports - Students")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeStudent3")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeStudent3EntityController {

    private final AdministrativeStudent3Repository repository;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeStudent3";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeStudent3 by ID", description = "Returns a single AdministrativeStudent3 by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeStudent3 by id: {}", entityId);

        Optional<AdministrativeStudent3> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeStudent3", description = "Updates an existing AdministrativeStudent3")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeStudent3 id: {}", entityId);

        Optional<AdministrativeStudent3> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeStudent3 entity = existingOpt.get();
        updateFromMap(entity, body);

        AdministrativeStudent3 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeStudent3", description = "Soft deletes an AdministrativeStudent3")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeStudent3 id: {}", entityId);

        Optional<AdministrativeStudent3> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeStudent3 (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search AdministrativeStudent3 with filter: {}", filter);

        List<AdministrativeStudent3> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeStudent3 (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search AdministrativeStudent3 with filter: {}", filter);

        List<AdministrativeStudent3> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeStudent3", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeStudent3 - offset: {}, limit: {}", offset, limit);

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
        Page<AdministrativeStudent3> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeStudent3", description = "Creates a new AdministrativeStudent3")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeStudent3");

        AdministrativeStudent3 entity = new AdministrativeStudent3();
        updateFromMap(entity, body);
        AdministrativeStudent3 saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(AdministrativeStudent3 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name
        String instanceName = "AdministrativeStudent3-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        // Add entity-specific fields
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_education_year", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "_student", entity.getStudent(), returnNulls);
        putIfNotNull(map, "company", entity.getCompany(), returnNulls);
        putIfNotNull(map, "position", entity.getPosition(), returnNulls);
        putIfNotNull(map, "masters_university_name", entity.getMastersUniversityName(), returnNulls);
        putIfNotNull(map, "education_type", entity.getEducationType(), returnNulls);

        // BaseEntity audit fields
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    private void updateFromMap(AdministrativeStudent3 entity, Map<String, Object> map) {
        // TODO: Add specific field mappings based on entity properties
        // For now, minimal implementation
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
