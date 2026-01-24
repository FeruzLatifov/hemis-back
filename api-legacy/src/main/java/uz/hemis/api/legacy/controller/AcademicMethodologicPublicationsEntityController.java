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
import uz.hemis.domain.entity.AcademicMethodologicPublications;
import uz.hemis.domain.repository.AcademicMethodologicPublicationsRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AcademicMethodologicPublications Entity Controller (CUBA Pattern)
 * Entity: hemishe_RIAcademicMethodologicPublications
 * Uslubiy nashrlar haqida ma'lumot
 */
@Tag(name = "Academic Reports - Methodologic Publications")
@RestController
@RequestMapping("/app/rest/v2/entities/hemishe_RIAcademicMethodologicPublications")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class AcademicMethodologicPublicationsEntityController {

    private final AcademicMethodologicPublicationsRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private static final String ENTITY_NAME = "hemishe_RIAcademicMethodologicPublications";

    private final Map<String, Map<String, String>> universityCache = new HashMap<>();
    private final Map<String, Map<String, String>> educationYearCache = new HashMap<>();

    @GetMapping("/{entityId}")
    @Operation(summary = "Get by ID")
    public ResponseEntity<?> getById(@PathVariable UUID entityId,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        Optional<AcademicMethodologicPublications> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found",
                "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
        }
        return ResponseEntity.ok(toMap(entity.get(), returnNulls));
    }

    @PutMapping("/{entityId}")
    @Operation(summary = "Update")
    public ResponseEntity<?> update(@PathVariable UUID entityId, @RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        Optional<AcademicMethodologicPublications> existingOpt = repository.findById(entityId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found",
                "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
        }
        AcademicMethodologicPublications entity = existingOpt.get();
        updateFromMap(entity, body);
        return ResponseEntity.ok(toMap(repository.save(entity), returnNulls));
    }

    @DeleteMapping("/{entityId}")
    @Operation(summary = "Delete")
    public ResponseEntity<?> delete(@PathVariable UUID entityId) {
        Optional<AcademicMethodologicPublications> entity = repository.findById(entityId);
        if (entity.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Entity not found",
                "details", "Entity " + ENTITY_NAME + " with id " + entityId + " not found"));
        }
        repository.delete(entity.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search GET")
    public ResponseEntity<List<Map<String, Object>>> searchGet(
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort) {
        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), limit, buildSort(sort));
        return ResponseEntity.ok(repository.findAll(pageRequest).getContent().stream()
            .map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping("/search")
    @Operation(summary = "Search POST")
    public ResponseEntity<List<Map<String, Object>>> searchPost(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort) {
        int effectiveLimit = limit != null ? limit : (body != null && body.get("limit") != null ? ((Number) body.get("limit")).intValue() : 50);
        int effectiveOffset = offset != null ? offset : (body != null && body.get("offset") != null ? ((Number) body.get("offset")).intValue() : 0);
        PageRequest pageRequest = PageRequest.of(effectiveOffset / Math.max(effectiveLimit, 1), effectiveLimit, buildSort(sort));
        return ResponseEntity.ok(repository.findAll(pageRequest).getContent().stream()
            .map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @GetMapping
    @Operation(summary = "Get all")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Boolean returnCount,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Boolean dynamicAttributes,
            @RequestParam(required = false) Boolean returnNulls,
            @RequestParam(required = false) String view) {
        PageRequest pageRequest = PageRequest.of(offset / Math.max(limit, 1), limit, buildSort(sort));
        return ResponseEntity.ok(repository.findAll(pageRequest).getContent().stream()
            .map(e -> toMap(e, returnNulls)).collect(Collectors.toList()));
    }

    @PostMapping
    @Operation(summary = "Create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
            @RequestParam(required = false) Boolean returnNulls) {
        AcademicMethodologicPublications entity = new AcademicMethodologicPublications();
        updateFromMap(entity, body);
        return ResponseEntity.ok(toMap(repository.save(entity), returnNulls));
    }

    private Sort buildSort(String sort) {
        if (sort == null || sort.isEmpty()) return Sort.unsorted();
        String[] parts = sort.split("-");
        return Sort.by(parts.length > 1 && "desc".equalsIgnoreCase(parts[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
    }

    /**
     * OLD-HEMIS response formatiga 100% mos
     */
    private Map<String, Object> toMap(AcademicMethodologicPublications entity, Boolean returnNulls) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", ENTITY_NAME);
        map.put("_instanceName", "com.company.hemishe.entity.RIAcademicMethodologicPublications-" + entity.getId() + " [detached]");
        map.put("id", entity.getId());

        // OLD-HEMIS tartibida
        putIfNotNull(map, "certificateDate", entity.getCertificateDate(), returnNulls);
        putIfNotNull(map, "authorFullname", entity.getAuthorFullname(), returnNulls);

        if (entity.getUniversity() != null) {
            map.put("university", buildUniversityObject(entity.getUniversity()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("university", null);
        }

        if (entity.getEducationYear() != null) {
            map.put("educationYear", buildEducationYearObject(entity.getEducationYear()));
        } else if (Boolean.TRUE.equals(returnNulls)) {
            map.put("educationYear", null);
        }

        putIfNotNull(map, "bookName", entity.getBookName(), returnNulls);
        putIfNotNull(map, "specialityCode", entity.getSpecialityCode(), returnNulls);
        putIfNotNull(map, "certificateNumber", entity.getCertificateNumber(), returnNulls);
        putIfNotNull(map, "specialityName", entity.getSpecialityName(), returnNulls);
        putIfNotNull(map, "bookType", entity.getBookType(), returnNulls);
        putIfNotNull(map, "version", entity.getVersion(), returnNulls);

        return map;
    }

    private Map<String, Object> buildUniversityObject(String code) {
        Map<String, String> data = getUniversityData(code);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_entityName", "hemishe_EUniversity");
        m.put("_instanceName", code + "-" + data.get("name"));
        m.put("id", code);
        m.put("code", code);
        m.put("name", data.get("name"));
        return m;
    }

    private Map<String, Object> buildEducationYearObject(String code) {
        Map<String, String> data = getEducationYearData(code);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("_entityName", "hemishe_HEducationYear");
        m.put("_instanceName", data.get("name"));
        m.put("id", code);
        m.put("name", data.get("name"));
        return m;
    }

    private Map<String, String> getUniversityData(String code) {
        if (universityCache.containsKey(code)) return universityCache.get(code);
        try {
            Map<String, String> data = jdbcTemplate.queryForObject(
                "SELECT code, name FROM hemishe_e_university WHERE code = ?",
                (rs, rowNum) -> Map.of("code", rs.getString("code"), "name", rs.getString("name")), code);
            if (data != null) { universityCache.put(code, data); return data; }
        } catch (Exception e) { log.warn("University not found: {}", code); }
        return Map.of("code", code, "name", "University " + code);
    }

    private Map<String, String> getEducationYearData(String code) {
        if (educationYearCache.containsKey(code)) return educationYearCache.get(code);
        try {
            Map<String, String> data = jdbcTemplate.queryForObject(
                "SELECT code, name FROM hemishe_h_education_year WHERE code = ?",
                (rs, rowNum) -> Map.of("code", rs.getString("code"), "name", rs.getString("name")), code);
            if (data != null) { educationYearCache.put(code, data); return data; }
        } catch (Exception e) { log.warn("Education year not found: {}", code); }
        return Map.of("code", code, "name", code);
    }

    private void updateFromMap(AcademicMethodologicPublications entity, Map<String, Object> body) {
        if (body.containsKey("university")) entity.setUniversity(extractCode(body.get("university")));
        if (body.containsKey("educationYear")) entity.setEducationYear(extractCode(body.get("educationYear")));
        if (body.containsKey("authorFullname")) entity.setAuthorFullname(str(body.get("authorFullname")));
        if (body.containsKey("specialityCode")) entity.setSpecialityCode(str(body.get("specialityCode")));
        if (body.containsKey("specialityName")) entity.setSpecialityName(str(body.get("specialityName")));
        if (body.containsKey("bookType")) entity.setBookType(str(body.get("bookType")));
        if (body.containsKey("bookName")) entity.setBookName(str(body.get("bookName")));
        if (body.containsKey("certificateDate")) {
            Object val = body.get("certificateDate");
            entity.setCertificateDate(val != null ? LocalDate.parse(val.toString()) : null);
        }
        if (body.containsKey("certificateNumber")) entity.setCertificateNumber(str(body.get("certificateNumber")));
    }

    @SuppressWarnings("unchecked")
    private String extractCode(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s.isEmpty() ? null : s;
        if (value instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) value;
            if (m.get("id") != null) return m.get("id").toString();
            if (m.get("code") != null) return m.get("code").toString();
        }
        return value.toString();
    }

    private String str(Object val) { return val != null ? val.toString() : null; }

    private void putIfNotNull(Map<String, Object> map, String key, Object value, Boolean returnNulls) {
        if (value != null || Boolean.TRUE.equals(returnNulls)) map.put(key, value);
    }
}
