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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import uz.hemis.domain.entity.AdministrativeSportFacilities;
import uz.hemis.domain.repository.AdministrativeSportFacilitiesRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AdministrativeSportFacilities Entity Controller (CUBA Pattern)
 * Tag 45: Administrative Reports - Sport Facilities (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAdministrativeSportFacilities
 *
 * Sport inshootlari maydoni haqida ma'lumot
 */
@Tag(name = "Administrative Reports - Sport Facilities")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAdministrativeSportFacilities")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AdministrativeSportFacilitiesEntityController {

    private final AdministrativeSportFacilitiesRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private static final String ENTITY_NAME = "hemishe_RIAdministrativeSportFacilities";

    // Cache for reference data (simple in-memory cache)
    private final Map<String, Map<String, String>> universityCache = new HashMap<>();
    private final Map<String, Map<String, String>> educationYearCache = new HashMap<>();

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AdministrativeSportFacilities by ID", description = "Returns a single AdministrativeSportFacilities by UUID")
    public ResponseEntity<?> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AdministrativeSportFacilities by id: {}", entityId);

        Optional<AdministrativeSportFacilities> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeSportFacilities with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AdministrativeSportFacilities", description = "Updates an existing AdministrativeSportFacilities")
    public ResponseEntity<?> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AdministrativeSportFacilities id: {}", entityId);

        Optional<AdministrativeSportFacilities> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeSportFacilities with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        AdministrativeSportFacilities entity = existingOpt.get();
        updateFromMap(entity, body);

        AdministrativeSportFacilities saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AdministrativeSportFacilities", description = "Soft deletes an AdministrativeSportFacilities")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AdministrativeSportFacilities id: {}", entityId);

        Optional<AdministrativeSportFacilities> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAdministrativeSportFacilities with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        repository.delete(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AdministrativeSportFacilities (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort) {

        log.debug("GET search AdministrativeSportFacilities with filter: {}, offset: {}, limit: {}", filter, offset, limit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<AdministrativeSportFacilities> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AdministrativeSportFacilities (POST)", description = "Search using JSON filter")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort) {

        int effectiveLimit = limit != null ? limit :
            (body != null && body.get("limit") != null ? ((Number) body.get("limit")).intValue() : 50);
        int effectiveOffset = offset != null ? offset :
            (body != null && body.get("offset") != null ? ((Number) body.get("offset")).intValue() : 0);

        log.debug("POST search AdministrativeSportFacilities with body: {}, offset: {}, limit: {}", body, effectiveOffset, effectiveLimit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = effectiveOffset / Math.max(effectiveLimit, 1);
        PageRequest pageRequest = PageRequest.of(page, effectiveLimit, sorting);
        Page<AdministrativeSportFacilities> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AdministrativeSportFacilities", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AdministrativeSportFacilities - offset: {}, limit: {}", offset, limit);

        Sort sorting = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] parts = sort.split("-");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sorting = Sort.by(direction, field);
        }

        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, sorting);
        Page<AdministrativeSportFacilities> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AdministrativeSportFacilities", description = "Creates a new AdministrativeSportFacilities")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AdministrativeSportFacilities");

        AdministrativeSportFacilities entity = new AdministrativeSportFacilities();
        updateFromMap(entity, body);
        AdministrativeSportFacilities saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    /**
     * OLD-HEMIS response formatiga 100% mos
     * Reference jadvallardan haqiqiy nomlarni oladi
     */
    private Map<String, Object> toMap(AdministrativeSportFacilities entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS CUBA format
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId());

        // square field (OLD-HEMIS tartibida)
        putIfNotNull(map, "square", entity.getSquare(), returnNulls);

        // university - nested object with real data from DB
        if (entity.getUniversity() != null) {
            map.put("university", buildUniversityObject(entity.getUniversity()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("university", null);
        }

        // educationYear - nested object with real data from DB
        if (entity.getEducationYear() != null) {
            map.put("educationYear", buildEducationYearObject(entity.getEducationYear()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("educationYear", null);
        }

        // version
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        return map;
    }

    /**
     * Build university nested object - OLD-HEMIS format
     * Fetches real name from hemishe_e_university table
     */
    private Map<String, Object> buildUniversityObject(String code) {
        Map<String, String> universityData = getUniversityData(code);

        Map<String, Object> universityMap = new LinkedHashMap<>();
        universityMap.put("_entityName", "hemishe_EUniversity");
        universityMap.put("_instanceName", code + "-" + universityData.get("name"));
        universityMap.put("id", code);
        universityMap.put("code", code);
        universityMap.put("name", universityData.get("name"));
        return universityMap;
    }

    /**
     * Build educationYear nested object - OLD-HEMIS format
     * Fetches real name from hemishe_h_education_year table
     */
    private Map<String, Object> buildEducationYearObject(String code) {
        Map<String, String> yearData = getEducationYearData(code);

        Map<String, Object> yearMap = new LinkedHashMap<>();
        yearMap.put("_entityName", "hemishe_HEducationYear");
        yearMap.put("_instanceName", yearData.get("name"));
        yearMap.put("id", code);
        yearMap.put("name", yearData.get("name"));
        return yearMap;
    }

    /**
     * Get university data from DB with caching
     */
    private Map<String, String> getUniversityData(String code) {
        if (universityCache.containsKey(code)) {
            return universityCache.get(code);
        }

        try {
            String sql = "SELECT code, name FROM hemishe_e_university WHERE code = ?";
            Map<String, String> data = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, String> result = new HashMap<>();
                result.put("code", rs.getString("code"));
                result.put("name", rs.getString("name"));
                return result;
            }, code);

            if (data != null) {
                universityCache.put(code, data);
                return data;
            }
        } catch (Exception e) {
            log.warn("University not found for code: {}", code);
        }

        // Fallback
        Map<String, String> fallback = new HashMap<>();
        fallback.put("code", code);
        fallback.put("name", "University " + code);
        return fallback;
    }

    /**
     * Get education year data from DB with caching
     */
    private Map<String, String> getEducationYearData(String code) {
        if (educationYearCache.containsKey(code)) {
            return educationYearCache.get(code);
        }

        try {
            String sql = "SELECT code, name FROM hemishe_h_education_year WHERE code = ?";
            Map<String, String> data = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, String> result = new HashMap<>();
                result.put("code", rs.getString("code"));
                result.put("name", rs.getString("name"));
                return result;
            }, code);

            if (data != null) {
                educationYearCache.put(code, data);
                return data;
            }
        } catch (Exception e) {
            log.warn("Education year not found for code: {}", code);
        }

        // Fallback
        Map<String, String> fallback = new HashMap<>();
        fallback.put("code", code);
        fallback.put("name", code);
        return fallback;
    }

    private String buildInstanceName(AdministrativeSportFacilities entity) {
        return "com.company.hemishe.entity.RIAdministrativeSportFacilities-" + entity.getId() + " [detached]";
    }

    private void updateFromMap(AdministrativeSportFacilities entity, Map<String, Object> body) {
        // university
        if (body.containsKey("university")) {
            entity.setUniversity(extractCode(body.get("university")));
        }

        // educationYear
        if (body.containsKey("educationYear")) {
            entity.setEducationYear(extractCode(body.get("educationYear")));
        }

        // square
        if (body.containsKey("square")) {
            Object val = body.get("square");
            if (val != null) {
                entity.setSquare(Double.parseDouble(val.toString()));
            } else {
                entity.setSquare(null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof String str) {
            return str.isEmpty() ? null : str;
        } else if (value instanceof Map) {
            Map<String, Object> nested = (Map<String, Object>) value;
            Object id = nested.get("id");
            if (id != null) return id.toString();
            Object code = nested.get("code");
            if (code != null) return code.toString();
        }
        return value.toString();
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) {
            map.put(key, value);
        }
    }
}
