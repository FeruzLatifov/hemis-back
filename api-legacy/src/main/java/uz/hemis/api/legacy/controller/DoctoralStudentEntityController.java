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
import uz.hemis.domain.entity.DoctoralStudent;
import uz.hemis.domain.repository.DoctoralStudentRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Doctoral Student Entity Controller (CUBA Pattern)
 * Entity: hemishe_EDoctorateStudent
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EDoctorateStudent/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EDoctorateStudent/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EDoctorateStudent/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EDoctorateStudent/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EDoctorateStudent/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EDoctorateStudent           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EDoctorateStudent           - Create new
 */
@Tag(name = "Doctoral Students")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EDoctorateStudent")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class DoctoralStudentEntityController {

    private final DoctoralStudentRepository repository;
    private static final String ENTITY_NAME = "hemishe_EDoctorateStudent";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get doctoral student by ID", description = "Returns a single doctoral student by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET doctoral student by id: {}", entityId);

        Optional<DoctoralStudent> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update doctoral student", description = "Updates an existing doctoral student")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT doctoral student id: {}", entityId);

        Optional<DoctoralStudent> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DoctoralStudent entity = existingOpt.get();
        updateFromMap(entity, body);

        DoctoralStudent saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete doctoral student", description = "Soft deletes a doctoral student")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE doctoral student id: {}", entityId);

        Optional<DoctoralStudent> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search doctoral students (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search doctoral students with filter: {}", filter);

        List<DoctoralStudent> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search doctoral students (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("POST search doctoral students with filter: {}", filter);

        List<DoctoralStudent> entities = repository.findAll();
        return ResponseEntity.ok(entities.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all doctoral students", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all doctoral students - offset: {}, limit: {}", offset, limit);

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
        Page<DoctoralStudent> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create doctoral student", description = "Creates a new doctoral student")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new doctoral student");

        DoctoralStudent entity = new DoctoralStudent();
        updateFromMap(entity, body);
        DoctoralStudent saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    private Map<String, Object> toMap(DoctoralStudent entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        String instanceName = entity.getDoctoralCode() != null ?
            entity.getDoctoralCode() : "DoctoralStudent-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        putIfNotNull(map, "doctoralCode", entity.getDoctoralCode(), returnNulls);
        putIfNotNull(map, "_student", entity.getStudent(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_department", entity.getDepartment(), returnNulls);
        putIfNotNull(map, "_scientificAdvisor", entity.getScientificAdvisor(), returnNulls);
        putIfNotNull(map, "_doctoralStudentType", entity.getDoctoralStudentType(), returnNulls);
        putIfNotNull(map, "dissertationTopic", entity.getDissertationTopic(), returnNulls);
        putIfNotNull(map, "dissertationTopicUz", entity.getDissertationTopicUz(), returnNulls);
        putIfNotNull(map, "dissertationTopicRu", entity.getDissertationTopicRu(), returnNulls);
        putIfNotNull(map, "dissertationTopicEn", entity.getDissertationTopicEn(), returnNulls);
        putIfNotNull(map, "_specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "admissionDate", entity.getAdmissionDate(), returnNulls);
        putIfNotNull(map, "expectedDefenseDate", entity.getExpectedDefenseDate(), returnNulls);
        putIfNotNull(map, "actualDefenseDate", entity.getActualDefenseDate(), returnNulls);
        putIfNotNull(map, "_defenseStatus", entity.getDefenseStatus(), returnNulls);
        putIfNotNull(map, "orderNumber", entity.getOrderNumber(), returnNulls);
        putIfNotNull(map, "orderDate", entity.getOrderDate(), returnNulls);
        putIfNotNull(map, "researchDirection", entity.getResearchDirection(), returnNulls);
        putIfNotNull(map, "notes", entity.getNotes(), returnNulls);
        putIfNotNull(map, "isActive", entity.getIsActive(), returnNulls);

        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    private void updateFromMap(DoctoralStudent entity, Map<String, Object> map) {
        // TODO: Add specific field mappings based on entity properties
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
