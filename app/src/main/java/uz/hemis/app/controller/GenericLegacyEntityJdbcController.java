package uz.hemis.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Minimal CUBA-compatible JDBC controllers for legacy entities that don't yet have JPA mappings.
 * Read-only: GET by id, GET list, optional GET /search (best-effort by code/name columns if exist).
 * Tables follow legacy naming convention (lowercase with underscores) and include soft-delete column delete_ts.
 */
@Tag(name = "Legacy Operations")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping
public class GenericLegacyEntityJdbcController {

    private final JdbcTemplate jdbcTemplate;

    private static final Map<String, String> PATH_TO_TABLE = Map.ofEntries(
            // Entities
            Map.entry("/app/rest/v2/entities/hemishe_EStudentGpa", "hemishe_e_student_gpa"),
            Map.entry("/app/rest/v2/entities/hemishe_EPublicationCriteria", "hemishe_e_publication_criteria"),
            Map.entry("/app/rest/v2/entities/hemishe_RAcademicGroup", "hemishe_r_academic_group"),
            Map.entry("/app/rest/v2/entities/hemishe_RExpel", "hemishe_r_expel"),
            Map.entry("/app/rest/v2/entities/hemishe_RAcademicAttendance", "hemishe_r_academic_attendance"),
            Map.entry("/app/rest/v2/entities/hemishe_RAcademicSubjects", "hemishe_r_academic_subjects"),
            Map.entry("/app/rest/v2/entities/hemishe_RAcademicScore", "hemishe_r_academic_score"),
            Map.entry("/app/rest/v2/entities/hemishe_REmployment", "hemishe_r_employment"),
            Map.entry("/app/rest/v2/entities/hemishe_EStudentDiploma", "hemishe_e_student_diploma"),
            // Classifiers (H-*)
            Map.entry("/app/rest/v2/entities/hemishe_HDoctoralStudentStatus", "hemishe_h_doctoral_student_status"),
            Map.entry("/app/rest/v2/entities/hemishe_HDoctoralStudentType", "hemishe_h_doctoral_student_type"),
            Map.entry("/app/rest/v2/entities/hemishe_HMethodicalPublicationType", "hemishe_h_methodical_publication_type"),
            Map.entry("/app/rest/v2/entities/hemishe_HPublicationLocality", "hemishe_h_publication_locality"),
            Map.entry("/app/rest/v2/entities/hemishe_HUniversityEmployeeForm", "hemishe_h_university_employee_form"),
            Map.entry("/app/rest/v2/entities/hemishe_HUniversityEmployeeRate", "hemishe_h_university_employee_rate"),
            Map.entry("/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType", "hemishe_h_university_employee_status_type")
    );

    private String resolveTable(String servletPathBase) {
        return PATH_TO_TABLE.get(servletPathBase);
    }

    private String resolveBasePath(String requestUri) {
        // Match the longest base path key that is a prefix of the request URI
        return PATH_TO_TABLE.keySet().stream()
                .filter(requestUri::startsWith)
                .max(Comparator.comparingInt(String::length))
                .orElse(null);
    }

    private String entityNameFromBase(String base) {
        if (base == null) return "";
        return base.substring("/app/rest/v2/entities/".length());
    }

    private Map<String, Object> toCubaMap(String entityName, Map<String, Object> row) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("_entityName", entityName);
        // _instanceName best-effort: code or name columns if present
        String instance = null;
        if (row.containsKey("code")) instance = Objects.toString(row.get("code"), null);
        if (instance == null && row.containsKey("name")) instance = Objects.toString(row.get("name"), null);
        if (instance == null && row.containsKey("_instance_name")) instance = Objects.toString(row.get("_instance_name"), null);
        if (instance != null) map.put("_instanceName", instance);
        map.putAll(row);
        return map;
    }

    // =============================
    // GET by ID
    // =============================
    @GetMapping({
            "/app/rest/v2/entities/hemishe_EStudentGpa/{id}",
            "/app/rest/v2/entities/hemishe_EPublicationCriteria/{id}",
            "/app/rest/v2/entities/hemishe_RAcademicGroup/{id}",
            "/app/rest/v2/entities/hemishe_RExpel/{id}",
            "/app/rest/v2/entities/hemishe_RAcademicAttendance/{id}",
            "/app/rest/v2/entities/hemishe_RAcademicSubjects/{id}",
            "/app/rest/v2/entities/hemishe_RAcademicScore/{id}",
            "/app/rest/v2/entities/hemishe_REmployment/{id}",
            "/app/rest/v2/entities/hemishe_EStudentDiploma/{id}",
            "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/{id}",
            "/app/rest/v2/entities/hemishe_HDoctoralStudentType/{id}",
            "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/{id}",
            "/app/rest/v2/entities/hemishe_HPublicationLocality/{id}",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeForm/{id}",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeRate/{id}",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/{id}"
    })
    @Operation(summary = "Get entity by ID (legacy CUBA format)")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable("id") String id, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String base = resolveBasePath(requestUri.replaceAll("/[^/]+$", ""));
        if (base == null) return ResponseEntity.notFound().build();

        String table = resolveTable(base);
        String sql = "select * from " + table + " where id = ? and (delete_ts is null)";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(sql, id);
            return ResponseEntity.ok(toCubaMap(entityNameFromBase(base), row));
        } catch (EmptyResultDataAccessException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // =============================
    // LIST (paginated)
    // =============================
    @GetMapping({
            "/app/rest/v2/entities/hemishe_EStudentGpa",
            "/app/rest/v2/entities/hemishe_EPublicationCriteria",
            "/app/rest/v2/entities/hemishe_RAcademicGroup",
            "/app/rest/v2/entities/hemishe_RExpel",
            "/app/rest/v2/entities/hemishe_RAcademicAttendance",
            "/app/rest/v2/entities/hemishe_RAcademicSubjects",
            "/app/rest/v2/entities/hemishe_RAcademicScore",
            "/app/rest/v2/entities/hemishe_REmployment",
            "/app/rest/v2/entities/hemishe_EStudentDiploma",
            "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus",
            "/app/rest/v2/entities/hemishe_HDoctoralStudentType",
            "/app/rest/v2/entities/hemishe_HMethodicalPublicationType",
            "/app/rest/v2/entities/hemishe_HPublicationLocality",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeForm",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeRate",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType"
    })
    @Operation(summary = "List entities (legacy CUBA list)")
    public ResponseEntity<List<Map<String, Object>>> list(
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            HttpServletRequest request
    ) {
        String requestUri = request.getRequestURI();
        String base = resolveBasePath(requestUri);
        if (base == null) return ResponseEntity.notFound().build();
        String table = resolveTable(base);

        String sql = "select * from " + table + " where (delete_ts is null) order by create_ts desc limit ? offset ?";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, limit, offset);
        String entityName = entityNameFromBase(base);
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            result.add(toCubaMap(entityName, row));
        }
        return ResponseEntity.ok(result);
    }

    // =============================
    // SEARCH (best-effort by code/name)
    // =============================
    @GetMapping({
            "/app/rest/v2/entities/hemishe_EStudentGpa/search",
            "/app/rest/v2/entities/hemishe_EPublicationCriteria/search",
            "/app/rest/v2/entities/hemishe_RAcademicGroup/search",
            "/app/rest/v2/entities/hemishe_RExpel/search",
            "/app/rest/v2/entities/hemishe_RAcademicAttendance/search",
            "/app/rest/v2/entities/hemishe_RAcademicSubjects/search",
            "/app/rest/v2/entities/hemishe_RAcademicScore/search",
            "/app/rest/v2/entities/hemishe_REmployment/search",
            "/app/rest/v2/entities/hemishe_EStudentDiploma/search",
            "/app/rest/v2/entities/hemishe_HDoctoralStudentStatus/search",
            "/app/rest/v2/entities/hemishe_HDoctoralStudentType/search",
            "/app/rest/v2/entities/hemishe_HMethodicalPublicationType/search",
            "/app/rest/v2/entities/hemishe_HPublicationLocality/search",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeForm/search",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeRate/search",
            "/app/rest/v2/entities/hemishe_HUniversityEmployeeStatusType/search"
    })
    @Operation(summary = "Search entities by code/name (best-effort)")
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestParam(value = "filter", required = false, defaultValue = "") String filter,
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
            HttpServletRequest request
    ) {
        String requestUri = request.getRequestURI();
        String base = resolveBasePath(requestUri.replace("/search", ""));
        if (base == null) return ResponseEntity.notFound().build();
        String table = resolveTable(base);

        String sql;
        Object[] args;
        if (StringUtils.hasText(filter)) {
            sql = "select * from " + table + " where (delete_ts is null) and (" +
                    "(code ilike ?) or (name ilike ?) ) order by create_ts desc limit ? offset ?";
            args = new Object[]{"%" + filter + "%", "%" + filter + "%", limit, offset};
        } else {
            sql = "select * from " + table + " where (delete_ts is null) order by create_ts desc limit ? offset ?";
            args = new Object[]{limit, offset};
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, args);
        String entityName = entityNameFromBase(base);
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            result.add(toCubaMap(entityName, row));
        }
        return ResponseEntity.ok(result);
    }
}


