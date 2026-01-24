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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.Group;
import uz.hemis.domain.repository.GroupRepository;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Group Entity Controller (CUBA Pattern)
 * Entity: hemishe_EUniversityGroup
 *
 * CUBA Platform REST API compatible controller
 *
 * Endpoints:
 * - GET    /app/rest/v2/entities/hemishe_EUniversityGroup/{id}      - Get by ID
 * - PUT    /app/rest/v2/entities/hemishe_EUniversityGroup/{id}      - Update
 * - DELETE /app/rest/v2/entities/hemishe_EUniversityGroup/{id}      - Soft delete
 * - GET    /app/rest/v2/entities/hemishe_EUniversityGroup/search    - Search (URL params)
 * - POST   /app/rest/v2/entities/hemishe_EUniversityGroup/search    - Search (JSON filter)
 * - GET    /app/rest/v2/entities/hemishe_EUniversityGroup           - List all with pagination
 * - POST   /app/rest/v2/entities/hemishe_EUniversityGroup           - Create new
 */
@Tag(name = "51.Guruhlar", description = "Guruhlar entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EUniversityGroup")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class GroupEntityController {

    private final GroupRepository repository;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EUniversityGroup";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get group by ID", description = "Returns a single group by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET group by id: {}", entityId);

        Optional<Group> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update group", description = "Updates an existing group")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT group id: {}", entityId);

        Optional<Group> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Group entity = existingOpt.get();
        updateFromMap(entity, body);

        Group saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete group", description = "Soft deletes a group")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE group id: {}", entityId);

        Optional<Group> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search groups (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Group> allEntities = repository.findAll();
        List<Group> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search groups (POST)", description = "Search using JSON filter")
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

        List<Group> allEntities = repository.findAll();
        List<Group> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all groups", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all groups - offset: {}, limit: {}", offset, limit);

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
        Page<Group> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    /**
     * Create group(s) - OLD-HEMIS Compatible
     *
     * <p>OLD-HEMIS sends an ARRAY of groups:</p>
     * <pre>
     * POST /entities/hemishe_EUniversityGroup
     * [
     *     {
     *         "university": {"code": "999"},
     *         "educationType": {"code": "11"},
     *         "educationYear": {"code": "2021"},
     *         "groupId": "1",
     *         "groupName": "200-21"
     *     }
     * ]
     * </pre>
     *
     * <p>Returns 201 Created with array response:</p>
     * <pre>
     * [
     *     {
     *         "_entityName": "hemishe_EUniversityGroup",
     *         "_instanceName": "200-21",
     *         "id": "dca5bd9f-3ff5-7957-bfaa-ed61fb62b2ce"
     *     }
     * ]
     * </pre>
     */
    @PostMapping
    @Operation(summary = "Create group(s)", description = "Creates new group(s) - accepts array (OLD-HEMIS format)")
    public ResponseEntity<List<Map<String, Object>>> create(
            @RequestBody List<Map<String, Object>> bodyList,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new group(s) - count: {}", bodyList.size());

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> body : bodyList) {
            Group entity = new Group();
            updateFromMap(entity, body);
            Group saved = repository.save(entity);

            // Build minimal response (OLD-HEMIS format)
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_entityName", ENTITY_NAME);
            result.put("_instanceName", saved.getName() != null ? saved.getName() : "Group-" + saved.getId());
            result.put("id", saved.getId().toString());
            results.add(result);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(results);
    }

    private Map<String, Object> toMap(Group entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        String instanceName = entity.getName() != null ?
            entity.getName() : "Group-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());

        // OLD-HEMIS compatible fields
        putIfNotNull(map, "groupId", extractGroupId(entity), returnNulls);
        putIfNotNull(map, "groupName", entity.getName(), returnNulls);

        putIfNotNull(map, "name", entity.getName(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_specialty", entity.getSpecialty(), returnNulls);
        putIfNotNull(map, "_faculty", entity.getFaculty(), returnNulls);
        putIfNotNull(map, "_curriculum", entity.getCurriculum(), returnNulls);
        putIfNotNull(map, "academicYear", entity.getAcademicYear(), returnNulls);
        putIfNotNull(map, "course", entity.getCourse(), returnNulls);
        putIfNotNull(map, "capacity", entity.getCapacity(), returnNulls);
        putIfNotNull(map, "studentCount", entity.getStudentCount(), returnNulls);
        putIfNotNull(map, "_educationType", entity.getEducationType(), returnNulls);
        putIfNotNull(map, "_educationForm", entity.getEducationForm(), returnNulls);
        putIfNotNull(map, "_educationLang", entity.getEducationLang(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);

        putIfNotNull(map, "createTs", entity.getCreateTs(), returnNulls);
        putIfNotNull(map, "createdBy", entity.getCreatedBy(), returnNulls);
        putIfNotNull(map, "updateTs", entity.getUpdateTs(), returnNulls);
        putIfNotNull(map, "updatedBy", entity.getUpdatedBy(), returnNulls);
        putIfNotNull(map, "deleteTs", entity.getDeleteTs(), returnNulls);
        putIfNotNull(map, "deletedBy", entity.getDeletedBy(), returnNulls);

        return map;
    }

    /**
     * Extract groupId from entity.
     * Uses first 8 characters of UUID hash as a stable identifier.
     */
    private String extractGroupId(Group entity) {
        if (entity.getId() != null) {
            // Generate a stable numeric-like ID from UUID
            return String.valueOf(Math.abs(entity.getId().hashCode() % 1000000));
        }
        return null;
    }

    /**
     * Update entity from OLD-HEMIS format map.
     *
     * <p>OLD-HEMIS format:</p>
     * <pre>
     * {
     *     "university": {"code": "999"},
     *     "educationType": {"code": "11"},
     *     "educationYear": {"code": "2021"},
     *     "groupId": "1",
     *     "groupName": "200-21"
     * }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    private void updateFromMap(Group entity, Map<String, Object> map) {
        // Parse university.code -> university field
        if (map.containsKey("university")) {
            Object universityObj = map.get("university");
            if (universityObj instanceof Map) {
                Map<String, Object> universityMap = (Map<String, Object>) universityObj;
                if (universityMap.containsKey("code")) {
                    entity.setUniversity(String.valueOf(universityMap.get("code")));
                }
            } else if (universityObj instanceof String) {
                entity.setUniversity((String) universityObj);
            }
        }

        // Also check for _university (internal format)
        if (map.containsKey("_university")) {
            entity.setUniversity(String.valueOf(map.get("_university")));
        }

        // Parse educationType.code -> educationType field
        if (map.containsKey("educationType")) {
            Object eduTypeObj = map.get("educationType");
            if (eduTypeObj instanceof Map) {
                Map<String, Object> eduTypeMap = (Map<String, Object>) eduTypeObj;
                if (eduTypeMap.containsKey("code")) {
                    entity.setEducationType(String.valueOf(eduTypeMap.get("code")));
                }
            } else if (eduTypeObj instanceof String) {
                entity.setEducationType((String) eduTypeObj);
            }
        }

        // Also check for _educationType (internal format)
        if (map.containsKey("_educationType")) {
            entity.setEducationType(String.valueOf(map.get("_educationType")));
        }

        // Parse educationYear.code -> academicYear field
        if (map.containsKey("educationYear")) {
            Object eduYearObj = map.get("educationYear");
            if (eduYearObj instanceof Map) {
                Map<String, Object> eduYearMap = (Map<String, Object>) eduYearObj;
                if (eduYearMap.containsKey("code")) {
                    entity.setAcademicYear(String.valueOf(eduYearMap.get("code")));
                }
            } else if (eduYearObj instanceof String) {
                entity.setAcademicYear((String) eduYearObj);
            } else if (eduYearObj instanceof Number) {
                entity.setAcademicYear(String.valueOf(((Number) eduYearObj).intValue()));
            }
        }

        // Also check for academicYear (internal format)
        if (map.containsKey("academicYear")) {
            entity.setAcademicYear(String.valueOf(map.get("academicYear")));
        }

        // Parse groupName -> name field
        if (map.containsKey("groupName")) {
            entity.setName(String.valueOf(map.get("groupName")));
        }

        // Also check for name (internal format)
        if (map.containsKey("name")) {
            entity.setName(String.valueOf(map.get("name")));
        }

        // Parse educationForm.code -> educationForm field
        if (map.containsKey("educationForm")) {
            Object eduFormObj = map.get("educationForm");
            if (eduFormObj instanceof Map) {
                Map<String, Object> eduFormMap = (Map<String, Object>) eduFormObj;
                if (eduFormMap.containsKey("code")) {
                    entity.setEducationForm(String.valueOf(eduFormMap.get("code")));
                }
            } else if (eduFormObj instanceof String) {
                entity.setEducationForm((String) eduFormObj);
            }
        }

        if (map.containsKey("_educationForm")) {
            entity.setEducationForm(String.valueOf(map.get("_educationForm")));
        }

        // Parse educationLang.code -> educationLang field
        if (map.containsKey("educationLang")) {
            Object eduLangObj = map.get("educationLang");
            if (eduLangObj instanceof Map) {
                Map<String, Object> eduLangMap = (Map<String, Object>) eduLangObj;
                if (eduLangMap.containsKey("code")) {
                    entity.setEducationLang(String.valueOf(eduLangMap.get("code")));
                }
            } else if (eduLangObj instanceof String) {
                entity.setEducationLang((String) eduLangObj);
            }
        }

        if (map.containsKey("_educationLang")) {
            entity.setEducationLang(String.valueOf(map.get("_educationLang")));
        }

        // Parse specialty -> specialty field (UUID)
        if (map.containsKey("specialty")) {
            Object specialtyObj = map.get("specialty");
            if (specialtyObj instanceof Map) {
                Map<String, Object> specialtyMap = (Map<String, Object>) specialtyObj;
                if (specialtyMap.containsKey("id")) {
                    entity.setSpecialty(UUID.fromString(String.valueOf(specialtyMap.get("id"))));
                }
            } else if (specialtyObj instanceof String) {
                entity.setSpecialty(UUID.fromString((String) specialtyObj));
            }
        }

        if (map.containsKey("_specialty")) {
            Object val = map.get("_specialty");
            if (val instanceof String) {
                entity.setSpecialty(UUID.fromString((String) val));
            } else if (val instanceof UUID) {
                entity.setSpecialty((UUID) val);
            }
        }

        // Parse faculty -> faculty field (UUID)
        if (map.containsKey("faculty")) {
            Object facultyObj = map.get("faculty");
            if (facultyObj instanceof Map) {
                Map<String, Object> facultyMap = (Map<String, Object>) facultyObj;
                if (facultyMap.containsKey("id")) {
                    entity.setFaculty(UUID.fromString(String.valueOf(facultyMap.get("id"))));
                }
            } else if (facultyObj instanceof String) {
                entity.setFaculty(UUID.fromString((String) facultyObj));
            }
        }

        if (map.containsKey("_faculty")) {
            Object val = map.get("_faculty");
            if (val instanceof String) {
                entity.setFaculty(UUID.fromString((String) val));
            } else if (val instanceof UUID) {
                entity.setFaculty((UUID) val);
            }
        }

        // Parse curriculum -> curriculum field (UUID)
        if (map.containsKey("curriculum")) {
            Object curriculumObj = map.get("curriculum");
            if (curriculumObj instanceof Map) {
                Map<String, Object> curriculumMap = (Map<String, Object>) curriculumObj;
                if (curriculumMap.containsKey("id")) {
                    entity.setCurriculum(UUID.fromString(String.valueOf(curriculumMap.get("id"))));
                }
            } else if (curriculumObj instanceof String) {
                entity.setCurriculum(UUID.fromString((String) curriculumObj));
            }
        }

        if (map.containsKey("_curriculum")) {
            Object val = map.get("_curriculum");
            if (val instanceof String) {
                entity.setCurriculum(UUID.fromString((String) val));
            } else if (val instanceof UUID) {
                entity.setCurriculum((UUID) val);
            }
        }

        // Parse course field
        if (map.containsKey("course")) {
            Object courseObj = map.get("course");
            if (courseObj instanceof Number) {
                entity.setCourse(((Number) courseObj).intValue());
            } else if (courseObj instanceof String) {
                try {
                    entity.setCourse(Integer.parseInt((String) courseObj));
                } catch (NumberFormatException ignored) {
                    // Ignore invalid course values
                }
            }
        }

        // Parse capacity field
        if (map.containsKey("capacity")) {
            Object capacityObj = map.get("capacity");
            if (capacityObj instanceof Number) {
                entity.setCapacity(((Number) capacityObj).intValue());
            } else if (capacityObj instanceof String) {
                try {
                    entity.setCapacity(Integer.parseInt((String) capacityObj));
                } catch (NumberFormatException ignored) {
                    // Ignore invalid capacity values
                }
            }
        }

        // Parse studentCount field
        if (map.containsKey("studentCount")) {
            Object studentCountObj = map.get("studentCount");
            if (studentCountObj instanceof Number) {
                entity.setStudentCount(((Number) studentCountObj).intValue());
            } else if (studentCountObj instanceof String) {
                try {
                    entity.setStudentCount(Integer.parseInt((String) studentCountObj));
                } catch (NumberFormatException ignored) {
                    // Ignore invalid studentCount values
                }
            }
        }

        // Parse active field
        if (map.containsKey("active")) {
            Object activeObj = map.get("active");
            if (activeObj instanceof Boolean) {
                entity.setActive((Boolean) activeObj);
            } else if (activeObj instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeObj));
            }
        }

        // Set default active to true if not specified (for new entities)
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
