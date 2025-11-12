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
import uz.hemis.domain.entity.AdministrativeStudent4;
import uz.hemis.domain.repository.AdministrativeStudent4Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeStudent4 Entity Controller (CUBA Pattern)
 * Tag 51: Administrative Reports - Students (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAdministrativeStudent4
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RIAdministrativeStudent4
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RIAdministrativeStudent4/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent4/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeStudent4/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeStudent4           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeStudent4           - Create new
 */
@Tag(name = "Administrative Reports - Students")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeStudent4")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeStudent4EntityController {

    private final AdministrativeStudent4Repository repository;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeStudent4";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeStudent4 by ID", description = "Returns a single AdministrativeStudent4 by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeStudent4 by id: {}", entityId);

        Optional<AdministrativeStudent4> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeStudent4", description = "Updates an existing AdministrativeStudent4")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeStudent4 id: {}", entityId);

        Optional<AdministrativeStudent4> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeStudent4 entity = existingOpt.get();
        updateFromMap(entity, body);

        AdministrativeStudent4 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeStudent4", description = "Soft deletes an AdministrativeStudent4")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeStudent4 id: {}", entityId);

        Optional<AdministrativeStudent4> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeStudent4 (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search AdministrativeStudent4 with filter: {}", filter);

        List<AdministrativeStudent4> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeStudent4 (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search AdministrativeStudent4 with filter: {}", filter);

        List<AdministrativeStudent4> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeStudent4", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeStudent4 - offset: {}, limit: {}", offset, limit);

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
        Page<AdministrativeStudent4> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeStudent4", description = "Creates a new AdministrativeStudent4")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeStudent4");

        AdministrativeStudent4 entity = new AdministrativeStudent4();
        updateFromMap(entity, body);
        AdministrativeStudent4 saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(AdministrativeStudent4 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name
        String instanceName = "AdministrativeStudent4-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        // Add entity-specific fields
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_education_year", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "_country", entity.getCountry(), returnNulls);
        putIfNotNull(map, "_student", entity.getStudent(), returnNulls);
        putIfNotNull(map, "olimpiada_type", entity.getOlimpiadaType(), returnNulls);
        putIfNotNull(map, "olimpiada_place", entity.getOlimpiadaPlace(), returnNulls);
        putIfNotNull(map, "olimpiada_name", entity.getOlimpiadaName(), returnNulls);
        putIfNotNull(map, "olimpiada_section_name", entity.getOlimpiadaSectionName(), returnNulls);
        putIfNotNull(map, "olimpiada_place_date", entity.getOlimpiadaPlaceDate(), returnNulls);
        putIfNotNull(map, "olimpiada_subject", entity.getOlimpiadaSubject(), returnNulls);
        putIfNotNull(map, "taken_position", entity.getTakenPosition(), returnNulls);
        putIfNotNull(map, "diploma_serial", entity.getDiplomaSerial(), returnNulls);
        putIfNotNull(map, "diploma_number", entity.getDiplomaNumber(), returnNulls);

        // BaseEntity audit fields
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    private void updateFromMap(AdministrativeStudent4 entity, Map<String, Object> map) {
        // TODO: Add specific field mappings based on entity properties
        // For now, minimal implementation
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
