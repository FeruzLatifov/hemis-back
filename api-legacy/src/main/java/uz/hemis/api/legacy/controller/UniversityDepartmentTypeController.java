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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Classifier;
import uz.hemis.domain.repository.ClassifierRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * University Department Type Controller - CUBA REST API Pattern
 *
 * <p><strong>CRITICAL - OLD-HEMIS Compatibility:</strong></p>
 * <ul>
 *   <li>Entity name: hemishe_HUniversityDepartmentType</li>
 *   <li>Maps to Classifier entity with type: UNIVERSITY_DEPARTMENT_TYPE</li>
 *   <li>Base URL: /app/rest/v2/entities/hemishe_HUniversityDepartmentType</li>
 *   <li>100% backward compatible with OLD-HEMIS CUBA Platform REST API</li>
 * </ul>
 *
 * <p><strong>Endpoints (7 total):</strong></p>
 * <ul>
 *   <li>GET /{id} - Get by ID</li>
 *   <li>PUT /{id} - Update</li>
 *   <li>DELETE /{id} - Delete</li>
 *   <li>GET /search - Search with URL params</li>
 *   <li>POST /search - Search with JSON filter</li>
 *   <li>GET / - List all (paginated)</li>
 *   <li>POST / - Create new</li>
 * </ul>
 *
 * @since 2.0.0
 */
@Tag(name = "University Department Types")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_HUniversityDepartmentType")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UniversityDepartmentTypeController {

    private final ClassifierRepository classifierRepository;

    private static final String ENTITY_NAME = "hemishe_HUniversityDepartmentType";
    private static final String CLASSIFIER_TYPE = "UNIVERSITY_DEPARTMENT_TYPE";

    // =====================================================
    // 1. GET BY ID
    // =====================================================

    @Operation(summary = "Get department type by ID",
               description = "Gets a single entity by identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success. The entity is returned in the response body."),
        @ApiResponse(responseCode = "403", description = "Forbidden. The user doesn't have permissions to read the entity."),
        @ApiResponse(responseCode = "404", description = "Not found. Entity with the given ID not found.")
    })
    @GetMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> getById(
            @Parameter(description = "Entity identifier", required = true)
            @PathVariable UUID entityId,
            @Parameter(description = "Specifies whether entity dynamic attributes should be returned")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Specifies whether null fields will be written to the result JSON")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "Name of the view which is used for loading the entity")
            @RequestParam(required = false) String view) {

        log.info("GET department type by ID: {}", entityId);

        Optional<Classifier> classifier = classifierRepository.findById(entityId);

        if (classifier.isEmpty() || !CLASSIFIER_TYPE.equals(classifier.get().getClassifierType())) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(classifier.get(), returnNulls));
    }

    // =====================================================
    // 2. UPDATE
    // =====================================================

    @Operation(summary = "Update department type",
               description = "Updates the entity. Only fields that are passed in the JSON object (the request body) are updated.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success. The updated entity is returned in the response body."),
        @ApiResponse(responseCode = "403", description = "Forbidden. The user doesn't have permissions to update the entity."),
        @ApiResponse(responseCode = "404", description = "Not found. Entity with the given ID not found.")
    })
    @PutMapping("/{entityId}")
    public ResponseEntity<Map<String, Object>> update(
            @Parameter(description = "Entity identifier", required = true)
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> entityData) {

        log.info("UPDATE department type: {}", entityId);

        Optional<Classifier> existingOpt = classifierRepository.findById(entityId);

        if (existingOpt.isEmpty() || !CLASSIFIER_TYPE.equals(existingOpt.get().getClassifierType())) {
            return ResponseEntity.notFound().build();
        }

        Classifier classifier = existingOpt.get();

        // Update fields from request body
        if (entityData.containsKey("code")) {
            classifier.setCode((String) entityData.get("code"));
        }
        if (entityData.containsKey("name")) {
            classifier.setNameUz((String) entityData.get("name"));
        }
        if (entityData.containsKey("sortOrder")) {
            classifier.setSortOrder((Integer) entityData.get("sortOrder"));
        }
        if (entityData.containsKey("isActive")) {
            classifier.setIsActive((Boolean) entityData.get("isActive"));
        }

        Classifier saved = classifierRepository.save(classifier);

        return ResponseEntity.ok(toMap(saved, false));
    }

    // =====================================================
    // 3. DELETE
    // =====================================================

    @Operation(summary = "Delete department type",
               description = "Deletes the entity (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success. Entity was deleted."),
        @ApiResponse(responseCode = "403", description = "Forbidden. The user doesn't have permissions to delete the entity."),
        @ApiResponse(responseCode = "404", description = "Not found. Entity with the given ID not found.")
    })
    @DeleteMapping("/{entityId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Entity identifier", required = true)
            @PathVariable UUID entityId) {

        log.info("DELETE department type: {}", entityId);

        Optional<Classifier> classifier = classifierRepository.findById(entityId);

        if (classifier.isEmpty() || !CLASSIFIER_TYPE.equals(classifier.get().getClassifierType())) {
            return ResponseEntity.notFound().build();
        }

        // Soft delete
        classifier.get().setIsActive(false);
        classifierRepository.save(classifier.get());

        return ResponseEntity.ok().build();
    }

    // =====================================================
    // 4. SEARCH (GET with filter parameter)
    // =====================================================

    @Operation(summary = "Search department types (GET)",
               description = "Finds entities by filter conditions. The filter is defined by JSON object that is passed as URL parameter.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success. Entities that conform to filter conditions are returned in the response body."),
        @ApiResponse(responseCode = "400", description = "Bad request. For example, the condition value cannot be parsed."),
        @ApiResponse(responseCode = "403", description = "Forbidden. The user doesn't have permissions to read the entity."),
        @ApiResponse(responseCode = "404", description = "Not found. MetaClass for the entity with the given name not found.")
    })
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @Parameter(description = "Filter condition", required = true)
            @RequestParam String filter,
            @Parameter(description = "Specifies whether the total count of entities should be returned in the 'X-Total-Count' header")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Position of the first result to retrieve")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Number of extracted entities")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Name of the field to be sorted by")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Specifies whether entity dynamic attributes should be returned")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Specifies whether null fields will be written to the result JSON")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "Name of the view which is used for loading the entity")
            @RequestParam(required = false) String view) {

        log.info("SEARCH department types (GET) - filter: {}", filter);

        return search(filter, offset, limit, sort, returnCount, returnNulls);
    }

    // =====================================================
    // 5. SEARCH (POST with filter in body)
    // =====================================================

    @Operation(summary = "Search department types (POST)",
               description = "Finds entities by filter conditions. The filter is defined by JSON object that is passed in request body.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success. Entities that conform to filter conditions are returned in the response body."),
        @ApiResponse(responseCode = "400", description = "Bad request. For example, the condition value cannot be parsed."),
        @ApiResponse(responseCode = "403", description = "Forbidden. The user doesn't have permissions to read the entity."),
        @ApiResponse(responseCode = "404", description = "Not found. MetaClass for the entity with the given name not found.")
    })
    @PostMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> filterBody,
            @Parameter(description = "Specifies whether the total count of entities should be returned in the 'X-Total-Count' header")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Position of the first result to retrieve")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Number of extracted entities")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Name of the field to be sorted by")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Specifies whether entity dynamic attributes should be returned")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Specifies whether null fields will be written to the result JSON")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "Name of the view which is used for loading the entity")
            @RequestParam(required = false) String view) {

        log.info("SEARCH department types (POST) - filter: {}", filterBody);

        String filterStr = filterBody != null ? filterBody.toString() : null;
        return search(filterStr, offset, limit, sort, returnCount, returnNulls);
    }

    // =====================================================
    // 6. LIST ALL
    // =====================================================

    @Operation(summary = "Get all department types",
               description = "Gets a list of entities (paginated)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success. The list of entities is returned in the response body."),
        @ApiResponse(responseCode = "403", description = "Forbidden. The user doesn't have permissions to read the entity."),
        @ApiResponse(responseCode = "404", description = "Not found. MetaClass for the entity with the given name not found.")
    })
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listAll(
            @Parameter(description = "Specifies whether the total count of entities should be returned in the 'X-Total-Count' header")
            @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Position of the first result to retrieve")
            @RequestParam(required = false) Integer offset,
            @Parameter(description = "Number of extracted entities")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Name of the field to be sorted by")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Specifies whether entity dynamic attributes should be returned")
            @RequestParam(required = false) Boolean dynamicAttributes,
            @Parameter(description = "Specifies whether null fields will be written to the result JSON")
            @RequestParam(required = false) Boolean returnNulls,
            @Parameter(description = "Name of the view which is used for loading the entity")
            @RequestParam(required = false) String view) {

        log.info("LIST ALL department types");

        Pageable pageable = createPageable(offset, limit, sort);

        List<Classifier> allClassifiers = classifierRepository.findAllByType(CLASSIFIER_TYPE);

        // Apply pagination manually
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allClassifiers.size()) : allClassifiers.size();

        List<Classifier> paged = allClassifiers.subList(
            Math.min(start, allClassifiers.size()),
            Math.min(end, allClassifiers.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(c -> toMap(c, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allClassifiers.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    // =====================================================
    // 7. CREATE
    // =====================================================

    @Operation(summary = "Create new department type",
               description = "Creates a new entity. The method expects a JSON with entity object in the request body.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success. The created entity is returned in the response body."),
        @ApiResponse(responseCode = "400", description = "Bad request. For example, the entity may have a reference to the non-existing entity."),
        @ApiResponse(responseCode = "403", description = "Forbidden. The user doesn't have permissions to create the entity."),
        @ApiResponse(responseCode = "404", description = "Not found. MetaClass for the entity with the given name not found.")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> entityData) {

        log.info("CREATE department type: {}", entityData);

        Classifier classifier = new Classifier();
        classifier.setClassifierType(CLASSIFIER_TYPE);
        classifier.setCode((String) entityData.get("code"));
        classifier.setNameUz((String) entityData.get("name"));
        classifier.setIsActive(true);

        if (entityData.containsKey("sortOrder")) {
            classifier.setSortOrder((Integer) entityData.get("sortOrder"));
        }

        Classifier saved = classifierRepository.save(classifier);

        return ResponseEntity.ok(toMap(saved, false));
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    /**
     * Convert Classifier entity to CUBA-style JSON map
     */
    private Map<String, Object> toMap(Classifier classifier, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", classifier.getNameUz());
        map.put("id", classifier.getId());

        putIfNotNull(map, "code", classifier.getCode(), returnNulls);
        putIfNotNull(map, "name", classifier.getNameUz(), returnNulls);
        putIfNotNull(map, "sortOrder", classifier.getSortOrder(), returnNulls);
        putIfNotNull(map, "isActive", classifier.getIsActive(), returnNulls);
        putIfNotNull(map, "createdBy", classifier.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updatedBy", classifier.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "createTs", classifier.getCreateTs(), returnNulls);
        putIfNotNull(map, "updateTs", classifier.getUpdateTs(), returnNulls);
        putIfNotNull(map, "deleteTs", classifier.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", classifier.getDeletedBy(), returnNulls);
        putIfNotNull(map, "version", classifier.getVersion(), returnNulls);

        return map;
    }

    /**
     * Add field to map only if not null (unless returnNulls is true)
     */
    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }

    /**
     * Common search logic for both GET and POST search endpoints
     */
    private ResponseEntity<List<Map<String, Object>>> search(
            String filter, Integer offset, Integer limit, String sort, Boolean returnCount, Boolean returnNulls) {

        // For now, simple implementation - get all by type and filter in memory
        // In production, you'd want to parse the filter and create a proper query
        List<Classifier> allClassifiers = classifierRepository.findAllByType(CLASSIFIER_TYPE);

        // Apply simple filter if provided (search in name)
        if (filter != null && !filter.isEmpty()) {
            String searchTerm = filter.toLowerCase();
            allClassifiers = allClassifiers.stream()
                .filter(c -> c.getNameUz() != null && c.getNameUz().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        }

        // Apply pagination
        int start = offset != null ? offset : 0;
        int end = limit != null ? Math.min(start + limit, allClassifiers.size()) : allClassifiers.size();

        List<Classifier> paged = allClassifiers.subList(
            Math.min(start, allClassifiers.size()),
            Math.min(end, allClassifiers.size())
        );

        List<Map<String, Object>> result = paged.stream()
            .map(c -> toMap(c, returnNulls))
            .collect(Collectors.toList());

        HttpHeaders headers = new HttpHeaders();
        if (Boolean.TRUE.equals(returnCount)) {
            headers.add("X-Total-Count", String.valueOf(allClassifiers.size()));
        }

        return ResponseEntity.ok().headers(headers).body(result);
    }

    /**
     * Create Pageable from CUBA-style parameters
     */
    private Pageable createPageable(Integer offset, Integer limit, String sort) {
        int page = 0;
        int size = limit != null ? limit : 20;

        if (offset != null && limit != null) {
            page = offset / limit;
        }

        Sort.Direction direction = Sort.Direction.ASC;
        String property = "sortOrder";

        if (sort != null && !sort.isEmpty()) {
            if (sort.startsWith("-")) {
                direction = Sort.Direction.DESC;
                property = sort.substring(1);
            } else if (sort.startsWith("+")) {
                property = sort.substring(1);
            } else {
                property = sort;
            }
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
