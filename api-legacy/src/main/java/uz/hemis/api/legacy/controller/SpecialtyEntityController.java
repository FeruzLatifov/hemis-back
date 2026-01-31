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
import uz.hemis.domain.entity.Specialty;
import uz.hemis.domain.repository.SpecialtyRepository;

import uz.hemis.api.legacy.util.CubaFilterHelper;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

/**
 * Specialty Entity Controller (CUBA Pattern)
 * Entity: hemishe_EUniversitySpeciality
 *
 * CUBA Platform REST API compatible controller
 */
@Tag(name = "50.Mutaxassisliklar", description = "Mutaxassisliklar entity API")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_EUniversitySpeciality")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class SpecialtyEntityController {

    private final SpecialtyRepository repository;
    private final CubaFilterHelper filterHelper;
    private static final String ENTITY_NAME = "hemishe_EUniversitySpeciality";

    @GetMapping("/{entityId}")
    @Operation(summary = "Get specialty by ID", description = "Returns a single specialty by UUID")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET specialty by id: {}", entityId);
        Optional<Specialty> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update specialty", description = "Updates an existing specialty")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT specialty id: {}", entityId);
        Optional<Specialty> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Specialty entity = existingOpt.get();
        updateFromMap(entity, body);
        Specialty saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete specialty", description = "Soft deletes a specialty")
    public ResponseEntity<Void> delete(@PathVariable UUID entityId) {
        log.debug("DELETE specialty id: {}", entityId);
        Optional<Specialty> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.delete(entity.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search specialties (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @Parameter(description = "Offset") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit") @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET search with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        List<Specialty> allEntities = repository.findAll();
        List<Specialty> result = filterHelper.applyFilterAndPagination(
            allEntities, filter, offset, limit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search specialties (POST)", description = "Search using JSON filter")
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

        List<Specialty> allEntities = repository.findAll();
        List<Specialty> result = filterHelper.applyFilterAndPagination(
            allEntities, filterJson, effectiveOffset, effectiveLimit,
            req -> filterHelper.getPropertyByReflection(req.entity(), req.property())
        );

        return ResponseEntity.ok(result.stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all specialties", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all specialties - offset: {}, limit: {}", offset, limit);

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
        Page<Specialty> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    /**
     * Create specialty (single object)
     *
     * OLD-HEMIS: Bitta object qabul qiladi
     */
    @PostMapping
    @Operation(summary = "Create specialty", description = "Creates a new specialty (OLD-HEMIS format)")
    public ResponseEntity<?> create(
            @RequestBody Object body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new specialty");

        // OLD-HEMIS: Array yoki single object qabul qilishi mumkin
        if (body instanceof List) {
            // Array format - OLD-HEMIS batch create
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body;
            List<Map<String, Object>> results = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Specialty entity = findExistingOrNew(item);
                updateFromMap(entity, item);
                Specialty saved = repository.save(entity);
                results.add(toCreateResponse(saved));
            }

            // OLD-HEMIS: 201 Created + Array response
            return ResponseEntity.ok(results);
        } else {
            // Single object
            @SuppressWarnings("unchecked")
            Map<String, Object> item = (Map<String, Object>) body;
            Specialty entity = findExistingOrNew(item);
            updateFromMap(entity, item);
            Specialty saved = repository.save(entity);
            return ResponseEntity.ok(toCreateResponse(saved));
        }
    }

    /**
     * OLD-HEMIS POST response formati
     *
     * {
     *     "_entityName": "hemishe_EUniversitySpeciality",
     *     "_instanceName": "2223 test2223",
     *     "id": "7f797721-93bf-0518-49eb-49ddf26dd124"
     * }
     */
    private Map<String, Object> toCreateResponse(Specialty entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        String instanceName = (entity.getCode() != null ? entity.getCode() : "") + " " +
                              (entity.getName() != null ? entity.getName() : "");
        map.put("_instanceName", instanceName.trim());
        map.put("id", entity.getId() != null ? entity.getId().toString() : null);

        return map;
    }

    private Map<String, Object> toMap(Specialty entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);

        String instanceName = entity.getCode() != null ?
            entity.getCode() + " - " + entity.getName() : "Specialty-" + entity.getId();
        map.put("_instanceName", instanceName);

        map.put("id", entity.getId());
        putIfNotNull(map, "specialityCode", entity.getCode(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getName(), returnNulls);
        putIfNotNull(map, "_university", entity.getUniversity(), returnNulls);
        putIfNotNull(map, "_faculty", entity.getFaculty(), returnNulls);
        putIfNotNull(map, "_educationType", entity.getEducationType(), returnNulls);
        putIfNotNull(map, "_educationYear", entity.getEducationYear(), returnNulls);
        putIfNotNull(map, "active", entity.getActive(), returnNulls);

        return map;
    }

    /**
     * OLD-HEMIS request formatidan entity yaratish
     *
     * OLD-HEMIS request:
     * {
     *     "university": {"code": "999"},
     *     "faculty": {"code": "999-102"},
     *     "educationType": {"code": "11"},
     *     "educationYear": {"code": "2021"},
     *     "specialityCode": "2223",
     *     "specialityName": "test2223",
     *     "active": true
     * }
     */
    @SuppressWarnings("unchecked")
    private void updateFromMap(Specialty entity, Map<String, Object> map) {
        // specialityCode -> code
        if (map.containsKey("specialityCode")) {
            entity.setCode((String) map.get("specialityCode"));
        } else if (map.containsKey("code")) {
            entity.setCode((String) map.get("code"));
        }

        // specialityName -> name
        if (map.containsKey("specialityName")) {
            entity.setName((String) map.get("specialityName"));
        } else if (map.containsKey("name")) {
            entity.setName((String) map.get("name"));
        }

        // university.code -> university
        if (map.containsKey("university") && map.get("university") instanceof Map) {
            Map<String, Object> univ = (Map<String, Object>) map.get("university");
            if (univ.containsKey("code")) {
                entity.setUniversity((String) univ.get("code"));
            }
        } else if (map.containsKey("_university")) {
            entity.setUniversity((String) map.get("_university"));
        }

        // faculty.code -> faculty (String)
        if (map.containsKey("faculty") && map.get("faculty") instanceof Map) {
            Map<String, Object> fac = (Map<String, Object>) map.get("faculty");
            if (fac.containsKey("code")) {
                entity.setFaculty((String) fac.get("code"));
            }
        } else if (map.containsKey("_faculty")) {
            entity.setFaculty((String) map.get("_faculty"));
        }

        // educationYear.code -> educationYear
        if (map.containsKey("educationYear") && map.get("educationYear") instanceof Map) {
            Map<String, Object> edYear = (Map<String, Object>) map.get("educationYear");
            if (edYear.containsKey("code")) {
                entity.setEducationYear((String) edYear.get("code"));
            }
        } else if (map.containsKey("_education_year")) {
            entity.setEducationYear((String) map.get("_education_year"));
        }

        // educationType.code -> educationType
        if (map.containsKey("educationType") && map.get("educationType") instanceof Map) {
            Map<String, Object> edType = (Map<String, Object>) map.get("educationType");
            if (edType.containsKey("code")) {
                entity.setEducationType((String) edType.get("code"));
            }
        } else if (map.containsKey("_educationType")) {
            entity.setEducationType((String) map.get("_educationType"));
        }

        // active
        if (map.containsKey("active")) {
            Object activeVal = map.get("active");
            if (activeVal instanceof Boolean) {
                entity.setActive((Boolean) activeVal);
            } else if (activeVal instanceof String) {
                entity.setActive(Boolean.parseBoolean((String) activeVal));
            }
        }

    }

    /**
     * UPSERT: Find existing specialty by unique constraint or create new.
     * Unique key: (_university, _education_type, _education_year, speciality_code, speciality_name)
     */
    @SuppressWarnings("unchecked")
    private Specialty findExistingOrNew(Map<String, Object> item) {
        String code = (String) item.getOrDefault("specialityCode", item.get("code"));
        String name = (String) item.getOrDefault("specialityName", item.get("name"));
        String university = null;
        String educationType = null;
        String educationYear = null;

        if (item.get("university") instanceof Map) {
            university = (String) ((Map<String, Object>) item.get("university")).get("code");
        }
        if (item.get("educationType") instanceof Map) {
            educationType = (String) ((Map<String, Object>) item.get("educationType")).get("code");
        }
        if (item.get("educationYear") instanceof Map) {
            educationYear = (String) ((Map<String, Object>) item.get("educationYear")).get("code");
        }

        if (code != null && university != null && educationType != null && educationYear != null && name != null) {
            return repository.findByUniqueKey(university, educationType, educationYear, code, name)
                    .orElseGet(Specialty::new);
        }
        return new Specialty();
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
