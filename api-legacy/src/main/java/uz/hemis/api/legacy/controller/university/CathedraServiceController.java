package uz.hemis.api.legacy.controller.university;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Cathedra (Department) Service Controller - CUBA REST API Compatible
 *
 * <p>Uses raw SQL because Department entity mapping doesn't match legacy DB schema.</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/cathedra")
@Tag(name = "07.OTM bo'linmalari", description = "CUBA compatible cathedra (department) service endpoints")
@RequiredArgsConstructor
@Slf4j
public class CathedraServiceController {

    private final JdbcTemplate jdbcTemplate;

    private static final String ENTITY_NAME = "hemishe_EUniversityDepartment";

    @GetMapping("/get")
    @Operation(summary = "Get cathedras by university", description = "Returns list of cathedras (departments) for given university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getByUniversity(
            @Parameter(description = "University code", required = true, example = "00001")
            @RequestParam String university) {
        log.info("[CUBA Service] cathedra/get: university={}", university);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT code, name_uz, name_ru, _deparment_type, status, university_code " +
                "FROM hemishe_e_university_department " +
                "WHERE university_code = ? AND delete_ts IS NULL " +
                "ORDER BY name_uz",
                university
        );

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("_entityName", ENTITY_NAME);
            map.put("id", row.get("code"));
            map.put("code", row.get("code"));
            // deparmentType as nested object (OLD-HEMIS format)
            String deptTypeCode = row.get("_deparment_type") != null ? row.get("_deparment_type").toString() : null;
            if (deptTypeCode != null) {
                Map<String, Object> deptType = buildDepartmentType(deptTypeCode);
                map.put("deparmentType", deptType);
            }
            map.put("version", 6);
            map.put("nameUz", row.get("name_uz"));
            map.put("nameRu", row.get("name_ru"));
            map.put("status", row.get("status"));
            data.add(map);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("count", data.size());
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildDepartmentType(String code) {
        Map<String, Object> dt = new LinkedHashMap<>();
        dt.put("_entityName", "hemishe_HUniversityDepartmentType");
        dt.put("id", code);
        dt.put("code", code);
        // Lookup name from DB
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT name, active FROM hemishe_h_university_department_type WHERE code = ? AND delete_ts IS NULL",
                    code);
            dt.put("name", row.get("name"));
            dt.put("active", row.get("active"));
        } catch (Exception e) {
            dt.put("name", code);
            dt.put("active", true);
        }
        dt.put("version", 1);
        return dt;
    }
}
