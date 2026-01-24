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
import uz.hemis.domain.entity.AcademicEducationalWork;
import uz.hemis.domain.repository.AcademicEducationalWorkRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AcademicEducationalWork Entity Controller (CUBA Pattern)
 * Tag 47: Academic Reports - Educational Work (Entity API)
 *
 * CUBA Platform REST API compatible controller
 * Entity: hemishe_RIAcademicEducationalWork
 *
 * O'quv ishlari haqida ma'lumot
 */
@Tag(name = "Academic Reports - Educational Work")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAcademicEducationalWork")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AcademicEducationalWorkEntityController {

    private final AcademicEducationalWorkRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private static final String ENTITY_NAME = "hemishe_RIAcademicEducationalWork";

    // Cache for reference data (simple in-memory cache)
    private final Map<String, Map<String, String>> universityCache = new HashMap<>();
    private final Map<String, Map<String, String>> educationYearCache = new HashMap<>();
    private final Map<String, Map<String, String>> courseCache = new HashMap<>();

    @GetMapping("/{entityId}")
    @Operation(summary = "Get AcademicEducationalWork by ID", description = "Returns a single AcademicEducationalWork by UUID")
    public ResponseEntity<?> getById(
            @PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET AcademicEducationalWork by id: {}", entityId);

        Optional<AcademicEducationalWork> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAcademicEducationalWork with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update AcademicEducationalWork", description = "Updates an existing AcademicEducationalWork")
    public ResponseEntity<?> update(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("PUT AcademicEducationalWork id: {}", entityId);

        Optional<AcademicEducationalWork> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAcademicEducationalWork with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        AcademicEducationalWork entity = existingOpt.get();
        updateFromMap(entity, body);

        AcademicEducationalWork saved = repository.save(entity);
        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete AcademicEducationalWork", description = "Soft deletes an AcademicEducationalWork")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        log.debug("DELETE AcademicEducationalWork id: {}", entityId);

        Optional<AcademicEducationalWork> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            Map<String, String> errorResponse = new LinkedHashMap<>();
            errorResponse.put("error", "Entity not found");
            errorResponse.put("details", "Entity hemishe_RIAcademicEducationalWork with id " + entityId + " not found");
            return ResponseEntity.status(404).body(errorResponse);
        }

        repository.delete(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search AcademicEducationalWork (GET)", description = "Search using URL parameters")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort) {

        log.debug("GET search AcademicEducationalWork with filter: {}, offset: {}, limit: {}", filter, offset, limit);

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
        Page<AcademicEducationalWork> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search AcademicEducationalWork (POST)", description = "Search using JSON filter")
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

        log.debug("POST search AcademicEducationalWork with body: {}, offset: {}, limit: {}", body, effectiveOffset, effectiveLimit);

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
        Page<AcademicEducationalWork> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all AcademicEducationalWork", description = "Returns paginated list")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @Parameter(description = "Return total count") @RequestParam(required = false) Boolean returnCount,
            @Parameter(description = "Offset for pagination") @RequestParam(defaultValue = "0") Integer offset,
            @Parameter(description = "Limit per page") @RequestParam(defaultValue = "50") Integer limit,
            @Parameter(description = "Sort") @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {

        log.debug("GET all AcademicEducationalWork - offset: {}, limit: {}", offset, limit);

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
        Page<AcademicEducationalWork> entityPage = repository.findAll(pageRequest);

        return ResponseEntity.ok(entityPage.getContent().stream()
            .map(e -> toMap(e, returnNulls))
            .collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create AcademicEducationalWork", description = "Creates a new AcademicEducationalWork")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {

        log.debug("POST create new AcademicEducationalWork");

        AcademicEducationalWork entity = new AcademicEducationalWork();
        updateFromMap(entity, body);
        AcademicEducationalWork saved = repository.save(entity);

        return ResponseEntity.ok(toMap(saved, returnNulls));
    }

    /**
     * OLD-HEMIS response formatiga 100% mos
     * Reference jadvallardan haqiqiy nomlarni oladi
     * Maydonlar tartibi: studentCount, university, document, subjects, educationYear, languageName, specialityCode, specialityName, course
     */
    private Map<String, Object> toMap(AcademicEducationalWork entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();

        // OLD-HEMIS CUBA format
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", buildInstanceName(entity));
        map.put("id", entity.getId());

        // studentCount (OLD-HEMIS tartibida - birinchi)
        putIfNotNull(map, "studentCount", entity.getStudentCount(), returnNulls);

        // university - nested object with real data from DB
        if (entity.getUniversity() != null) {
            map.put("university", buildUniversityObject(entity.getUniversity()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("university", null);
        }

        // document
        putIfNotNull(map, "document", entity.getDocument(), returnNulls);

        // subjects
        putIfNotNull(map, "subjects", entity.getSubjects(), returnNulls);

        // educationYear - nested object with real data from DB
        if (entity.getEducationYear() != null) {
            map.put("educationYear", buildEducationYearObject(entity.getEducationYear()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("educationYear", null);
        }

        // languageName
        putIfNotNull(map, "languageName", entity.getLanguageName(), returnNulls);

        // specialityCode
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);

        // specialityName
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);

        // course - nested object with real data from DB
        if (entity.getCourse() != null) {
            map.put("course", buildCourseObject(entity.getCourse()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("course", null);
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
     * Build course nested object - OLD-HEMIS format
     * Fetches real name from hemishe_h_course table
     * _instanceName format: "11 1-kurs" (code + space + name)
     */
    private Map<String, Object> buildCourseObject(String code) {
        Map<String, String> courseData = getCourseData(code);

        Map<String, Object> courseMap = new LinkedHashMap<>();
        courseMap.put("_entityName", "hemishe_HCourse");
        courseMap.put("_instanceName", code + " " + courseData.get("name"));
        courseMap.put("id", code);
        courseMap.put("code", code);
        courseMap.put("name", courseData.get("name"));
        return courseMap;
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

    /**
     * Get course data from DB with caching
     */
    private Map<String, String> getCourseData(String code) {
        if (courseCache.containsKey(code)) {
            return courseCache.get(code);
        }

        try {
            String sql = "SELECT code, name FROM hemishe_h_course WHERE code = ?";
            Map<String, String> data = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, String> result = new HashMap<>();
                result.put("code", rs.getString("code"));
                result.put("name", rs.getString("name"));
                return result;
            }, code);

            if (data != null) {
                courseCache.put(code, data);
                return data;
            }
        } catch (Exception e) {
            log.warn("Course not found for code: {}", code);
        }

        // Fallback
        Map<String, String> fallback = new HashMap<>();
        fallback.put("code", code);
        fallback.put("name", code + "-kurs");
        return fallback;
    }

    private String buildInstanceName(AcademicEducationalWork entity) {
        return "com.company.hemishe.entity.RIAcademicEducationalWork-" + entity.getId() + " [detached]";
    }

    private void updateFromMap(AcademicEducationalWork entity, Map<String, Object> body) {
        // university
        if (body.containsKey("university")) {
            entity.setUniversity(extractCode(body.get("university")));
        }

        // educationYear
        if (body.containsKey("educationYear")) {
            entity.setEducationYear(extractCode(body.get("educationYear")));
        }

        // specialityCode
        if (body.containsKey("specialityCode")) {
            Object val = body.get("specialityCode");
            entity.setSpecialityCode(val != null ? val.toString() : null);
        }

        // specialityName
        if (body.containsKey("specialityName")) {
            Object val = body.get("specialityName");
            entity.setSpecialityName(val != null ? val.toString() : null);
        }

        // document
        if (body.containsKey("document")) {
            Object val = body.get("document");
            entity.setDocument(val != null ? val.toString() : null);
        }

        // subjects
        if (body.containsKey("subjects")) {
            Object val = body.get("subjects");
            entity.setSubjects(val != null ? val.toString() : null);
        }

        // languageName
        if (body.containsKey("languageName")) {
            Object val = body.get("languageName");
            entity.setLanguageName(val != null ? val.toString() : null);
        }

        // course
        if (body.containsKey("course")) {
            entity.setCourse(extractCode(body.get("course")));
        }

        // studentCount
        if (body.containsKey("studentCount")) {
            Object val = body.get("studentCount");
            if (val != null) {
                entity.setStudentCount(Integer.parseInt(val.toString()));
            } else {
                entity.setStudentCount(null);
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
