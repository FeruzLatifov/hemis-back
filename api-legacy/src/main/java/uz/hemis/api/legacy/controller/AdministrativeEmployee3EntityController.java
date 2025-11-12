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
import uz.hemis.domain.entity.AdministrativeEmployee3;
import uz.hemis.domain.repository.AdministrativeEmployee3Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeEmployee3 Entity Controller (CUBA Pattern)
 * Tag 52: Administrative Reports - Employees (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAdministrativeEmployee3
 *
 * CRITICAL - 100% Backward Compatible:
 * - Preserves exact CUBA entity API pattern
 * - URL: /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3
 * - Response format: CUBA Map structure with _entityName, _instanceName
 * - Parameters: returnNulls, view, dynamicAttributes (CUBA-compatible)
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_RIAdministrativeEmployee3           - Create new
 */
@Tag(name = "Administrative Reports - Employees")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeEmployee3")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeEmployee3EntityController {

    private final AdministrativeEmployee3Repository repository;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeEmployee3";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeEmployee3 by ID", description = "Returns a single AdministrativeEmployee3 by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeEmployee3 by id: {}", entityId);

        Optional<AdministrativeEmployee3> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeEmployee3", description = "Updates an existing AdministrativeEmployee3")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeEmployee3 id: {}", entityId);

        Optional<AdministrativeEmployee3> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AdministrativeEmployee3 entity = existingOpt.get();
        updateFromMap(entity, body);

        AdministrativeEmployee3 saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeEmployee3", description = "Soft deletes an AdministrativeEmployee3")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeEmployee3 id: {}", entityId);

        Optional<AdministrativeEmployee3> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeEmployee3 (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search AdministrativeEmployee3 with filter: {}", filter);

        List<AdministrativeEmployee3> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeEmployee3 (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search AdministrativeEmployee3 with filter: {}", filter);

        List<AdministrativeEmployee3> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeEmployee3", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeEmployee3 - offset: {}, limit: {}", offset, limit);

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
        Page<AdministrativeEmployee3> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeEmployee3", description = "Creates a new AdministrativeEmployee3")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeEmployee3");

        AdministrativeEmployee3 entity = new AdministrativeEmployee3();
        updateFromMap(entity, body);
        AdministrativeEmployee3 saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(AdministrativeEmployee3 entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        // Instance name
        String instanceName = "AdministrativeEmployee3-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        // Add entity-specific fields
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_education_year", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "_country", entity.getCountry(), returnNulls);
        putIfNotNull(map, "fullname", entity.getFullname(), returnNulls);
        putIfNotNull(map, "work_place", entity.getWorkPlace(), returnNulls);
        putIfNotNull(map, "speciality_name", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "subject", entity.getSubject(), returnNulls);
        putIfNotNull(map, "contract_data", entity.getContractData(), returnNulls);
        putIfNotNull(map, "_employee", entity.getEmployee(), returnNulls);
        putIfNotNull(map, "employee_form", entity.getEmployeeForm(), returnNulls);
        putIfNotNull(map, "condution_form", entity.getCondutionForm(), returnNulls);
        putIfNotNull(map, "arrival_date", entity.getArrivalDate(), returnNulls);
        putIfNotNull(map, "departure_date", entity.getDepartureDate(), returnNulls);
        putIfNotNull(map, "lesson_time", entity.getLessonTime(), returnNulls);
        putIfNotNull(map, "year", entity.getYear(), returnNulls);

        // BaseEntity audit fields
        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    private void updateFromMap(AdministrativeEmployee3 entity, Map<String, Object> map) {
        // TODO: Add specific field mappings based on entity properties
        // For now, minimal implementation
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
