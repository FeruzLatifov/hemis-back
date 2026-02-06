package uz.hemis.api.legacy.controller.student;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Hokimiyat Service Controller - CUBA REST API Compatible
 *
 * <p>OLD-HEMIS: HokimiyatServiceBean.students()</p>
 * <p>Returns students updated yesterday with faculty name join</p>
 *
 * @since 2.0.0
 */
@RestController
@RequestMapping("/app/rest/v2/services/hokimiyat")
@Tag(name = "62.Hokimiyat", description = "Hokimiyat uchun ma'lumotlar")
@RequiredArgsConstructor
@Slf4j
public class HokimiyatServiceController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Get students updated yesterday
     *
     * <p>OLD-HEMIS: HokimiyatServiceBean.students()</p>
     * <p>Native SQL: joins hemishe_e_student with hemishe_e_university and hemishe_e_university_department</p>
     * <p>Filter: update_ts between yesterday and today</p>
     */
    @GetMapping("/students")
    @Operation(summary = "Kechagi yangilangan talabalar (hokimiyat)", description = "Kecha yangilangan talabalar - tuman hokimiyatlari uchun")
    public ResponseEntity<?> students() {
        log.info("[CUBA Service] hokimiyat/students");

        String sql = """
            SELECT
                s.id,
                s.create_ts,
                s.update_ts,
                s."_university",
                s.pinfl,
                s.serial_number,
                s."_country",
                s."_citizenship",
                s."_nationality",
                s."_gender",
                s.birthday,
                s."_soato",
                s."_current_soato",
                s."_student_type",
                s."_payment_form",
                s."_education_year",
                s."_education_form",
                s."_education_type",
                s."_faculty",
                d.name_uz as faculty_name,
                s."_language",
                s."_accomodation",
                s."_course",
                s."_social_category",
                s."_stipend_rate",
                s."_roommate_type",
                s."_living_status",
                s."_student_status"
            FROM hemishe_e_student s
            INNER JOIN hemishe_e_university u ON u.code = s."_university"
            LEFT JOIN hemishe_e_university_department d ON d.code = s."_faculty"
            WHERE
                s.delete_ts IS NULL
                AND s.update_ts >= CURRENT_DATE - 1
                AND s.update_ts < CURRENT_DATE
            """;

        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> items = jdbcTemplate.queryForList(sql);

            result.put("status", "OK");
            result.put("count", items.size());
            result.put("items", items);
        } catch (Exception e) {
            log.error("Error fetching hokimiyat students", e);
            result.put("status", "ERROR");
            result.put("title", "Chetlashgan talabalar");
            result.put("message", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
