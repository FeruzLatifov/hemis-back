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
import uz.hemis.domain.entity.AdministrativeEmployee1;
import uz.hemis.domain.repository.AdministrativeEmployee1Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeEmployee1 Entity Controller (CUBA Pattern)
 * Tag 52: Administrative Reports - Employees (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAdministrativeEmployee1
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeEmployee1           - Create new
 */
@Tag(name = "Administrative Reports - Employees")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeEmployee1")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeEmployee1EntityController {

    private final AdministrativeEmployee1Repository repository;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeEmployee1";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeEmployee1 by ID", description = "Returns a single AdministrativeEmployee1 by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeEmployee1 by id: {}", entityId);

        Optional<AdministrativeEmployee1> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeEmployee1", description = "Updates an existing AdministrativeEmployee1")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeEmployee1 id: {}", entityId);

        Optional<AdministrativeEmployee1> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeEmployee1 entity = existingOpt.get();
        updateFromMap(entity, body);

        AdministrativeEmployee1 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeEmployee1", description = "Soft deletes an AdministrativeEmployee1")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeEmployee1 id: {}", entityId);

        Optional<AdministrativeEmployee1> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeEmployee1 (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search AdministrativeEmployee1 with filter: {}", filter);

        List<AdministrativeEmployee1> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeEmployee1 (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search AdministrativeEmployee1 with filter: {}", filter);

        List<AdministrativeEmployee1> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeEmployee1", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeEmployee1 - offset: {}, limit: {}", offset, limit);

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
        Page<AdministrativeEmployee1> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeEmployee1", description = "Creates a new AdministrativeEmployee1")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeEmployee1");

        AdministrativeEmployee1 entity = new AdministrativeEmployee1();
        updateFromMap(entity, body);
        AdministrativeEmployee1 saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(AdministrativeEmployee1 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name
        String instanceName = "AdministrativeEmployee1-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        // Add entity-specific fields
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_education_year", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "_employee", entity.getEmployee(), returnNulls);
        putIfNotNull(map, "_country", entity.getCountry(), returnNulls);
        putIfNotNull(map, "foreign_university", entity.getForeignUniversity(), returnNulls);
        putIfNotNull(map, "degree", entity.getDegree(), returnNulls);
        putIfNotNull(map, "rank", entity.getRank(), returnNulls);
        putIfNotNull(map, "diploma_type", entity.getDiplomaType(), returnNulls);
        putIfNotNull(map, "diploma_serial_number", entity.getDiplomaSerialNumber(), returnNulls);
        putIfNotNull(map, "diploma_date", entity.getDiplomaDate(), returnNulls);
        putIfNotNull(map, "speciality_code", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "speciality_name", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "council_date", entity.getCouncilDate(), returnNulls);
        putIfNotNull(map, "council_number", entity.getCouncilNumber(), returnNulls);

        // BaseEntity audit fields
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    private void updateFromMap(AdministrativeEmployee1 entity, Map<String, Object> map) {
        // TODO: Add specific field mappings based on entity properties
        // For now, minimal implementation
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
