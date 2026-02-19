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
 * Speciality Service Controller - CUBA REST API Compatible
 *
 * <p>Uses raw SQL with JOINs to match OLD-HEMIS response format exactly.</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/speciality")
@Tag(name = "50.Mutaxassisliklar", description = "Mutaxassisliklar xizmatlari")
@RequiredArgsConstructor
@Slf4j
public class SpecialityServiceController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/get")
    @Operation(summary = "Get specialities by university")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getByUniversity(
            @Parameter(description = "University code", required = true)
            @RequestParam String university,
            @Parameter(description = "Speciality type")
            @RequestParam(required = false) String type) {

        log.info("[CUBA Service] speciality/get: university={}, type={}", university, type);

        String sql = "SELECT s.id, s.speciality_code, s.speciality_name, " +
                "s._university, u.name AS university_name, " +
                "s._faculty, f.name AS faculty_name, " +
                "s._education_type, ef.name AS education_form_name " +
                "FROM hemishe_e_university_speciality s " +
                "LEFT JOIN hemishe_e_university u ON s._university = u.code " +
                "LEFT JOIN hemishe_e_faculty f ON s._faculty = f.code AND f._university = s._university AND f.delete_ts IS NULL " +
                "LEFT JOIN hemishe_h_education_form ef ON s._education_type = ef.code AND ef.delete_ts IS NULL " +
                "WHERE s._university = ?";

        List<Object> params = new ArrayList<>();
        params.add(university);

        if (type != null && !type.isEmpty()) {
            sql += " AND s._education_type = ?";
            params.add(type);
        }

        sql += " ORDER BY s.speciality_name";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row.get("id") != null ? row.get("id").toString() : null);
            map.put("specialityCode", row.get("speciality_code"));
            map.put("specialityName", row.get("speciality_name"));
            map.put("universityCode", row.get("_university"));
            map.put("universityName", row.get("university_name"));
            map.put("facultyCode", row.get("_faculty"));
            map.put("facultyName", row.get("faculty_name"));
            map.put("educationFormCode", row.get("_education_type"));
            map.put("educationFormName", row.get("education_form_name"));
            data.add(map);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("count", data.size());
        if (type != null && !type.isEmpty()) {
            response.put("type", type);
        }
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}
